package com.exempal.shiftcounter.features.shift.application;

import com.exempal.shiftcounter.features.comment.application.ReconcileStoppagesCommand;
import com.exempal.shiftcounter.features.comment.application.ReconcileStoppagesUseCase;
import com.exempal.shiftcounter.features.shift.domain.ActualDataPort;
import com.exempal.shiftcounter.features.shift.domain.ProductionDay;
import com.exempal.shiftcounter.features.shift.domain.Shift;
import com.exempal.shiftcounter.features.shift.domain.ShiftInterval;
import com.exempal.shiftcounter.features.signal.domain.Signal;
import com.exempal.shiftcounter.features.signal.domain.SignalStoragePort;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class ShiftTimeCorrectionService {
    private final ShiftIntervalService intervals;
    private final SignalStoragePort signals;
    private final ActualDataPort shifts;
    private final ReconcileStoppagesUseCase reconcile;

    public ShiftTimeCorrectionService(ShiftIntervalService intervals, SignalStoragePort signals,
                                      ActualDataPort shifts, ReconcileStoppagesUseCase reconcile) {
        this.intervals = intervals;
        this.signals = signals;
        this.shifts = shifts;
        this.reconcile = reconcile;
    }

    public Shift apply(Shift current, List<String> configuredLabels, List<Integer> configuredPlans,
                       boolean timeChanged, LocalDateTime calculationTime) {
        List<String> labels = new ArrayList<>(configuredLabels);
        List<Integer> actuals;
        if (timeChanged) {
            ProductionDay day = ProductionDay.of(current.getDate());
            List<Signal> savedSignals = signals.findBySensorAndRange(current.getSensorId(), day.start(), day.end());
            LocalDateTime latest = savedSignals.stream().map(Signal::timestamp).max(LocalDateTime::compareTo)
                    .orElse(null);
            if (latest != null) {
                labels = new ArrayList<>(intervals.extendUntil(current.getDate(), labels,
                        configuredPlans.size(), latest));
            }
            List<ShiftInterval> timeline = intervals.resolve(current.getDate(), labels, configuredPlans.size());
            actuals = new ArrayList<>();
            for (int ignored = 0; ignored < timeline.size(); ignored++) actuals.add(0);
            for (Signal signal : savedSignals) {
                timeline.stream().filter(interval -> interval.contains(signal.timestamp())).findFirst()
                        .ifPresent(interval -> actuals.set(interval.index(), actuals.get(interval.index()) + 1));
            }
        } else {
            labels = preserveExtension(current, labels);
            actuals = normalize(current.getHourlyActualValues(), labels.size());
        }

        Shift updated = current.withUpdatedStructure(labels, configuredPlans, actuals);
        Shift saved = shifts.save(updated);
        for (int index = 0; index < configuredPlans.size(); index++) {
            reconcile.reconcile(new ReconcileStoppagesCommand(saved.getDate(), saved.getSensorId(),
                    index, calculationTime));
        }
        for (int index = configuredPlans.size(); index < current.getHourlyPlanValues().size(); index++) {
            reconcile.reconcile(ReconcileStoppagesCommand.resolveRemovedInterval(saved.getDate(),
                    saved.getSensorId(), index, calculationTime));
        }
        return saved;
    }

    private List<String> preserveExtension(Shift current, List<String> configured) {
        List<String> labels = new ArrayList<>(configured);
        int oldPlanCount = current.getHourlyPlanValues().size();
        if (current.getHourlyLabels().size() > oldPlanCount) {
            labels.addAll(current.getHourlyLabels().subList(oldPlanCount, current.getHourlyLabels().size()));
        }
        return labels;
    }

    private List<Integer> normalize(List<Integer> source, int size) {
        List<Integer> result = new ArrayList<>(size);
        for (int index = 0; index < size; index++) result.add(index < source.size() ? source.get(index) : 0);
        return result;
    }
}
