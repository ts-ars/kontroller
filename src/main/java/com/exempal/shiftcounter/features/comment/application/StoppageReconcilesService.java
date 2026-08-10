package com.exempal.shiftcounter.features.comment.application;

import com.exempal.shiftcounter.features.comment.calculator.*;
import com.exempal.shiftcounter.features.shift.application.ShiftTimeHelper;
import com.exempal.shiftcounter.features.shift.domain.Shift;
import com.exempal.shiftcounter.features.signal.application.SignalService;
import com.exempal.shiftcounter.features.signal.domain.Signal;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class StoppageReconcilesService implements ReconcileStoppagesUseCase {
    private final ShiftTimeHelper timeHelper;
    private final SignalService signalService;
    private final StoppageCalculator calculator;
    private final StoppageMatcher matcher;
    private final ReconcileShiftRepository shifts;
    private final StoppageRepository stoppages;

    @Override
    @Transactional
    public ReconcileResult reconcile(ReconcileStoppagesCommand command) {
        Shift shift = shifts.findForUpdateByDate(command.shiftDate()).orElse(null);
        if (shift == null) {
            return invalid(-1, "shift not found for " + command.shiftDate());
        }
        int intervalIndex = command.intervalIndex() == null
                ? timeHelper.resolveHourIndex(shift.getDate(), shift.getHourlyLabels(), command.calculationTime())
                : command.intervalIndex();
        if (intervalIndex < 0 || intervalIndex >= shift.getHourlyLabels().size()
                || intervalIndex >= shift.getHourlyPlanValues().size()
                || intervalIndex >= shift.getHourlyActualValues().size()) {
            return invalid(intervalIndex, "interval is outside persisted shift structure");
        }

        LocalDateTime start = timeHelper.resolveStartTime(shift.getHourlyLabels().get(intervalIndex), shift.getDate());
        LocalDateTime end = timeHelper.resolveEndTime(shift.getHourlyLabels(), intervalIndex, shift.getDate());
        if (!end.isAfter(start)) return invalid(intervalIndex, "interval end must be after start");
        List<Signal> signals = signalService.getSignalsBetween(start, end.minusNanos(1));
        int plan = shift.getHourlyPlanValues().get(intervalIndex);
        int actual = shift.getHourlyActualValues().get(intervalIndex);
        double minutes = Duration.between(start, end).toNanos() / 60_000_000_000.0;
        double cansPerMinute = minutes <= 0 ? 0 : plan / minutes;
        StoppageCalculationContext context = new StoppageCalculationContext(shift.getId(),
                command.sensorKey(), intervalIndex, start, end, plan, actual, cansPerMinute,
                signals.stream().map(Signal::timestamp).toList(), command.calculationTime());

        StoppageCalculation calculation = calculator.calculate(context);
        if (calculation.diagnostics().stream().anyMatch(ReconcileDiagnostic::fatal)) {
            return new ReconcileResult(intervalIndex, List.of(), calculation.diagnostics(), 0, false);
        }
        List<com.exempal.shiftcounter.features.comment.domain.Stoppage> existing =
                stoppages.findActiveByShiftSensorAndIntervalRange(shift.getId(), command.sensorKey(),
                        Math.max(0, intervalIndex - 1), intervalIndex + 1);
        StoppageMatcher.MatchPlan planResult = matcher.match(context, existing, calculation.candidates());
        List<ReconcileDiagnostic> diagnostics = new ArrayList<>(calculation.diagnostics());
        diagnostics.addAll(planResult.diagnostics());
        if (!planResult.valid()) {
            return new ReconcileResult(intervalIndex, List.of(), diagnostics, 0, false);
        }
        List<com.exempal.shiftcounter.features.comment.domain.Stoppage> saved = planResult.toSave().isEmpty()
                ? List.of() : stoppages.saveAll(planResult.toSave());
        Map<java.util.UUID, com.exempal.shiftcounter.features.comment.domain.Stoppage> savedByKey = saved.stream()
                .collect(Collectors.toMap(com.exempal.shiftcounter.features.comment.domain.Stoppage::detectionKey,
                        Function.identity()));
        List<com.exempal.shiftcounter.features.comment.domain.Stoppage> active = planResult.active().stream()
                .map(value -> savedByKey.getOrDefault(value.detectionKey(), value)).toList();
        log.info("Reconciled date={}, sensor={}, interval={} changed={} diagnostics={}",
                command.shiftDate(), command.sensorKey(), intervalIndex, planResult.toSave().size(),
                diagnostics.size());
        return new ReconcileResult(intervalIndex, active, diagnostics,
                planResult.toSave().size(), true);
    }

    private ReconcileResult invalid(int intervalIndex, String detail) {
        return new ReconcileResult(intervalIndex, List.of(), List.of(ReconcileDiagnostic.fatal(
                ReconcileDiagnosticCode.INVALID_INTERVAL, detail)), 0, false);
    }
}
