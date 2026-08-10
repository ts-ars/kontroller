package com.exempal.shiftcounter.features.comment.application;

import com.exempal.shiftcounter.features.comment.calculator.StoppageCalculationContext;
import com.exempal.shiftcounter.features.comment.calculator.StoppageCandidate;
import com.exempal.shiftcounter.features.comment.domain.DetectionType;
import com.exempal.shiftcounter.features.comment.domain.ExplanationStatus;
import com.exempal.shiftcounter.features.comment.domain.Stoppage;
import com.exempal.shiftcounter.features.comment.domain.StoppageState;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;

@Component
public class StoppageMatcher {
    public MatchPlan match(StoppageCalculationContext context, List<Stoppage> existingRange,
                           List<StoppageCandidate> candidates) {
        List<ReconcileDiagnostic> diagnostics = new ArrayList<>();
        List<Stoppage> target = existingRange.stream()
                .filter(value -> value.intervalIndex() == context.intervalIndex()
                        && value.state() == StoppageState.ACTIVE).toList();
        List<Stoppage> existingFixed = target.stream()
                .filter(value -> value.detectionType() == DetectionType.FIXED).toList();
        List<Stoppage> existingTempo = target.stream()
                .filter(value -> value.detectionType() == DetectionType.TEMPO).toList();
        List<StoppageCandidate> fixed = candidates.stream()
                .filter(value -> value.detectionType() == DetectionType.FIXED).toList();
        List<StoppageCandidate> tempo = candidates.stream()
                .filter(value -> value.detectionType() == DetectionType.TEMPO).toList();

        if (existingTempo.size() > 1 || tempo.size() > 1) {
            diagnostics.add(ReconcileDiagnostic.fatal(ReconcileDiagnosticCode.AMBIGUOUS_TEMPO_MATCH,
                    "expected at most one active and one desired TEMPO"));
            return MatchPlan.failed(diagnostics);
        }

        Map<Integer, Stoppage> fixedMatches = matchFixed(fixed, existingFixed, diagnostics);
        if (diagnostics.stream().anyMatch(ReconcileDiagnostic::fatal)) return MatchPlan.failed(diagnostics);

        List<Stoppage> active = new ArrayList<>();
        List<Stoppage> toSave = new ArrayList<>();
        Set<Long> matchedIds = new HashSet<>();
        for (int index = 0; index < fixed.size(); index++) {
            StoppageCandidate candidate = fixed.get(index);
            Stoppage existing = fixedMatches.get(index);
            Stoppage desired;
            if (existing != null) {
                matchedIds.add(existing.id());
                desired = existing.withSystemMeasurement(candidate.startedAt(), candidate.exactDuration(),
                        candidate.lostCans());
            } else {
                UUID detectionKey = deterministicKey(context, candidate, index);
                UUID incidentKey = incidentKey(context, candidate, existingRange, diagnostics, detectionKey);
                desired = Stoppage.detected(detectionKey, incidentKey, context.shiftId(), context.sensorKey(),
                        context.intervalIndex(), candidate.startedAt(), candidate.exactDuration(),
                        candidate.lostCans(), DetectionType.FIXED);
            }
            active.add(desired);
            if (existing == null || systemChanged(existing, desired)) toSave.add(desired);
            addAllocationDiagnostic(desired, diagnostics);
        }

        if (!tempo.isEmpty()) {
            StoppageCandidate candidate = tempo.getFirst();
            Stoppage existing = existingTempo.isEmpty() ? null : existingTempo.getFirst();
            Stoppage desired;
            if (existing == null) {
                UUID key = deterministicKey(context, candidate, 0);
                desired = Stoppage.detected(key, key, context.shiftId(), context.sensorKey(),
                        context.intervalIndex(), candidate.startedAt(), candidate.exactDuration(),
                        candidate.lostCans(), DetectionType.TEMPO);
            } else {
                matchedIds.add(existing.id());
                desired = existing.withSystemMeasurement(candidate.startedAt(), candidate.exactDuration(),
                        candidate.lostCans());
            }
            active.add(desired);
            if (existing == null || systemChanged(existing, desired)) toSave.add(desired);
            addAllocationDiagnostic(desired, diagnostics);
        }

        for (Stoppage old : target) {
            if (old.id() != null && !matchedIds.contains(old.id())) toSave.add(old.resolve());
        }
        if (diagnostics.stream().anyMatch(ReconcileDiagnostic::fatal)) return MatchPlan.failed(diagnostics);
        return new MatchPlan(active, toSave, diagnostics, true);
    }

    private Map<Integer, Stoppage> matchFixed(List<StoppageCandidate> candidates,
                                               List<Stoppage> existing,
                                               List<ReconcileDiagnostic> diagnostics) {
        Map<Integer, Stoppage> matches = new HashMap<>();
        Set<Long> used = new HashSet<>();
        for (int candidateIndex = 0; candidateIndex < candidates.size(); candidateIndex++) {
            StoppageCandidate candidate = candidates.get(candidateIndex);
            long best = 0;
            List<Stoppage> bestRows = new ArrayList<>();
            for (Stoppage row : existing) {
                long overlap = overlapNanos(candidate.startedAt(), candidate.endedAt(),
                        row.startedAt(), row.endedAt());
                if (overlap > best) {
                    best = overlap;
                    bestRows.clear();
                    bestRows.add(row);
                } else if (overlap > 0 && overlap == best) {
                    bestRows.add(row);
                }
            }
            if (bestRows.size() > 1) {
                diagnostics.add(ReconcileDiagnostic.fatal(ReconcileDiagnosticCode.AMBIGUOUS_FIXED_MATCH,
                        "candidate " + candidate.startedAt() + " has equal best overlap"));
                continue;
            }
            if (bestRows.size() == 1) {
                Stoppage selected = bestRows.getFirst();
                if (!used.add(selected.id())) {
                    diagnostics.add(ReconcileDiagnostic.fatal(ReconcileDiagnosticCode.AMBIGUOUS_FIXED_MATCH,
                            "several candidates select stoppage " + selected.detectionKey()));
                } else {
                    matches.put(candidateIndex, selected);
                }
            }
        }
        return matches;
    }

    private UUID incidentKey(StoppageCalculationContext context, StoppageCandidate candidate,
                             List<Stoppage> range, List<ReconcileDiagnostic> diagnostics,
                             UUID fallback) {
        Set<UUID> adjacent = new LinkedHashSet<>();
        if (candidate.startedAt().equals(context.intervalStart())) {
            range.stream().filter(value -> value.detectionType() == DetectionType.FIXED
                            && value.intervalIndex() == context.intervalIndex() - 1
                            && value.state() == StoppageState.ACTIVE
                            && value.endedAt().equals(context.intervalStart()))
                    .map(Stoppage::incidentKey).forEach(adjacent::add);
        }
        if (candidate.endedAt().equals(context.intervalEnd())) {
            range.stream().filter(value -> value.detectionType() == DetectionType.FIXED
                            && value.intervalIndex() == context.intervalIndex() + 1
                            && value.state() == StoppageState.ACTIVE
                            && value.startedAt().equals(context.intervalEnd()))
                    .map(Stoppage::incidentKey).forEach(adjacent::add);
        }
        if (adjacent.size() > 1) {
            diagnostics.add(ReconcileDiagnostic.fatal(ReconcileDiagnosticCode.AMBIGUOUS_INCIDENT_LINK,
                    "adjacent FIXED parts have different incident identities"));
            return fallback;
        }
        return adjacent.isEmpty() ? fallback : adjacent.iterator().next();
    }

    private UUID deterministicKey(StoppageCalculationContext context, StoppageCandidate candidate,
                                  int ordinal) {
        String value = context.shiftId() + "|" + context.sensorKey() + "|" + context.intervalIndex()
                + "|" + candidate.detectionType() + "|" + candidate.startedAt() + "|"
                + candidate.exactDuration().toNanos() + "|" + ordinal;
        return UUID.nameUUIDFromBytes(value.getBytes(StandardCharsets.UTF_8));
    }

    private long overlapNanos(LocalDateTime leftStart, LocalDateTime leftEnd,
                              LocalDateTime rightStart, LocalDateTime rightEnd) {
        LocalDateTime start = leftStart.isAfter(rightStart) ? leftStart : rightStart;
        LocalDateTime end = leftEnd.isBefore(rightEnd) ? leftEnd : rightEnd;
        return end.isAfter(start) ? Duration.between(start, end).toNanos() : 0;
    }

    private boolean systemChanged(Stoppage before, Stoppage after) {
        return !before.startedAt().equals(after.startedAt())
                || !before.exactDuration().equals(after.exactDuration())
                || before.lostCans() != after.lostCans()
                || !before.explanations().equals(after.explanations());
    }

    private void addAllocationDiagnostic(Stoppage value, List<ReconcileDiagnostic> diagnostics) {
        if (value.explanationStatus() == ExplanationStatus.ALLOCATION_CONFLICT) {
            diagnostics.add(ReconcileDiagnostic.warning(ReconcileDiagnosticCode.ALLOCATION_CONFLICT,
                    "stoppage " + value.detectionKey() + " has operator minutes above system duration"));
        }
    }

    public record MatchPlan(List<Stoppage> active, List<Stoppage> toSave,
                            List<ReconcileDiagnostic> diagnostics, boolean valid) {
        public MatchPlan {
            active = List.copyOf(active);
            toSave = List.copyOf(toSave);
            diagnostics = List.copyOf(diagnostics);
        }

        static MatchPlan failed(List<ReconcileDiagnostic> diagnostics) {
            return new MatchPlan(List.of(), List.of(), diagnostics, false);
        }
    }
}
