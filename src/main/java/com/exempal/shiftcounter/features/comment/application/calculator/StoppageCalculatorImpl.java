package com.exempal.shiftcounter.features.comment.application.calculator;

import com.exempal.shiftcounter.features.comment.application.ReconcileDiagnostic;
import com.exempal.shiftcounter.features.comment.application.ReconcileDiagnosticCode;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class StoppageCalculatorImpl implements StoppageCalculator {
    private final StoppageFixedLossCalculator fixedLossCalculator;
    private final StoppageTempoLossCalculator tempoLossCalculator;

    public StoppageCalculatorImpl(StoppageFixedLossCalculator fixedLossCalculator,
                                  StoppageTempoLossCalculator tempoLossCalculator) {
        this.fixedLossCalculator = fixedLossCalculator;
        this.tempoLossCalculator = tempoLossCalculator;
    }

    @Override
    public StoppageCalculation calculate(StoppageCalculationContext context) {
        List<ReconcileDiagnostic> diagnostics = new ArrayList<>();
        if (!context.intervalEnd().isAfter(context.intervalStart()) || context.plan() < 0
                || context.actual() < 0) {
            return new StoppageCalculation(List.of(), List.of(ReconcileDiagnostic.fatal(
                    ReconcileDiagnosticCode.INVALID_INTERVAL, "invalid interval, plan or actual")));
        }
        int totalLoss = Math.max(0, context.plan() - context.actual());
        if (totalLoss > 0 && context.cansPerMinute() <= 0) {
            return new StoppageCalculation(List.of(), List.of(ReconcileDiagnostic.fatal(
                    ReconcileDiagnosticCode.INVALID_PRODUCTIVITY, "positive loss requires positive productivity")));
        }
        List<StoppageCandidate> fixed = fixedLossCalculator.calculateFixed(context).stream()
                .filter(value -> value.lostCans() > 0).toList();
        int rawFixed = fixed.stream().mapToInt(StoppageCandidate::lostCans).sum();
        if (rawFixed > totalLoss) {
            diagnostics.add(ReconcileDiagnostic.warning(ReconcileDiagnosticCode.FIXED_EXCEEDS_TOTAL_LOSS,
                    "raw FIXED " + rawFixed + " exceeds total loss " + totalLoss));
            fixed = scaleFixed(fixed, totalLoss, rawFixed);
        }
        int effectiveFixed = fixed.stream().mapToInt(StoppageCandidate::lostCans).sum();
        int tempo = totalLoss - effectiveFixed;
        List<StoppageCandidate> result = new ArrayList<>(fixed);
        tempoLossCalculator.calculateTempo(context, tempo).ifPresent(result::add);
        int balance = result.stream().mapToInt(StoppageCandidate::lostCans).sum();
        if (balance != totalLoss) {
            diagnostics.add(ReconcileDiagnostic.fatal(ReconcileDiagnosticCode.BALANCE_MISMATCH,
                    "FIXED + TEMPO " + balance + " differs from total loss " + totalLoss));
        }
        List<StoppageCandidate> persistent = result.stream()
                .filter(value -> value.lostCans() > 0)
                .filter(value -> Math.round(value.exactDuration().toNanos() / 60_000_000_000.0) >= 1)
                .toList();
        return new StoppageCalculation(persistent, diagnostics);
    }

    private List<StoppageCandidate> scaleFixed(List<StoppageCandidate> source, int target, int rawTotal) {
        if (target == 0) return List.of();
        record Share(int index, int floor, double remainder) {}
        List<Share> shares = new ArrayList<>();
        int floorSum = 0;
        for (int index = 0; index < source.size(); index++) {
            double exact = (double) target * source.get(index).lostCans() / rawTotal;
            int floor = (int) Math.floor(exact);
            floorSum += floor;
            shares.add(new Share(index, floor, exact - floor));
        }
        int[] values = shares.stream().mapToInt(Share::floor).toArray();
        shares.stream().sorted(Comparator.comparingDouble(Share::remainder).reversed()
                        .thenComparingInt(Share::index))
                .limit(target - floorSum).forEach(value -> values[value.index()]++);
        List<StoppageCandidate> result = new ArrayList<>();
        for (int index = 0; index < source.size(); index++) {
            if (values[index] > 0) result.add(source.get(index).withLostCans(values[index]));
        }
        return List.copyOf(result);
    }
}
