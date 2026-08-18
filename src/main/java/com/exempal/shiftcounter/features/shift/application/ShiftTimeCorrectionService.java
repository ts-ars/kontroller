package com.exempal.shiftcounter.features.shift.application;

import com.exempal.shiftcounter.features.shift.application.ActualDataPort;
import com.exempal.shiftcounter.features.sensor.domain.SensorCatalog;
import com.exempal.shiftcounter.features.shift.domain.ProductionDay;
import com.exempal.shiftcounter.features.shift.domain.Shift;
import com.exempal.shiftcounter.features.shift.domain.ShiftInterval;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@Transactional
public class ShiftTimeCorrectionService {
    private final ShiftIntervalService intervals;
    private final ShiftSignalHistoryPort signals;
    private final ActualDataPort shifts;
    private final ShiftReconcilePort reconcile;

    public ShiftTimeCorrectionService(ShiftIntervalService intervals, ShiftSignalHistoryPort signals,
                                      ActualDataPort shifts, ShiftReconcilePort reconcile) {
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
            List<LocalDateTime> savedSignals = signals.findTimestamps(current.getSensorId(), day.start(), day.end());
            LocalDateTime latest = savedSignals.stream().max(LocalDateTime::compareTo)
                    .orElse(null);
            if (latest != null) {
                labels = new ArrayList<>(intervals.extendUntil(current.getDate(), labels,
                        configuredPlans.size(), latest));
            }
            List<ShiftInterval> timeline = intervals.resolve(current.getDate(), labels, configuredPlans.size());
            actuals = new ArrayList<>();
            for (int ignored = 0; ignored < timeline.size(); ignored++) actuals.add(0);
            for (LocalDateTime timestamp : savedSignals) {
                timeline.stream().filter(interval -> interval.contains(timestamp)).findFirst()
                        .ifPresent(interval -> actuals.set(interval.index(), actuals.get(interval.index()) + 1));
            }
        } else {
            labels = preserveExtension(current, labels);
            actuals = normalize(current.getHourlyActualValues(), labels.size());
        }

        Shift updated = current.withUpdatedStructure(labels, configuredPlans, actuals);
        Shift saved = shifts.save(updated);
        if (SensorCatalog.SENSOR_5.equals(saved.getSensorId())) return saved;
        List<ShiftInterval> savedTimeline = intervals.resolve(saved.getDate(), saved.getHourlyLabels(),
                saved.getHourlyPlanValues().size());
        for (int index = 0; index < configuredPlans.size() && index < savedTimeline.size(); index++) {
            if (!savedTimeline.get(index).end().isAfter(calculationTime)) {
                reconcile.reconcile(saved.getDate(), saved.getSensorId(), index, calculationTime);
            }
        }
        for (int index = configuredPlans.size(); index < current.getHourlyPlanValues().size(); index++) {
            reconcile.resolveRemovedInterval(saved.getDate(), saved.getSensorId(), index, calculationTime);
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
