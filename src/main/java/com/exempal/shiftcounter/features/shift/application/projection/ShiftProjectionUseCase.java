package com.exempal.shiftcounter.features.shift.application.projection;

import com.exempal.shiftcounter.features.shift.application.ShiftSettingsPort;
import com.exempal.shiftcounter.features.shift.application.ActualDataPort;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Component
@Transactional(readOnly = true)
public class ShiftProjectionUseCase {

    private final ShiftSettingsPort settings;
    private final ActualDataPort actual;
    private final ShiftExplanationPort explanations;

    public ShiftProjectionUseCase(ShiftSettingsPort settings, ActualDataPort actual,
                                  ShiftExplanationPort explanations) {
        this.settings = settings;
        this.actual = actual;
        this.explanations = explanations;
    }

    public ShiftView buildView(LocalDate date) {
        return buildView(date, com.exempal.shiftcounter.features.sensor.domain.SensorCatalog.SENSOR_1);
    }

    public ShiftView buildView(LocalDate date, String sensorId) {
        var loaded = settings.getForSensor(sensorId);
        var comments = explanations.findByInterval(date, sensorId);

        return actual.findByDateAndSensorId(date, sensorId)
                .map(shift -> {
                    // Р В§Р В°РЎРѓРЎвЂ№ РІР‚вЂќ Р С‘Р В· Р Т‘Р С•Р СР ВµР Р…Р В° (РЎвЂћР В°Р С”РЎвЂљР С‘РЎвЂЎР ВµРЎРѓР С”Р С•Р в„– РЎРѓР СР ВµР Р…РЎвЂ№), Р С—Р С•РЎР‚РЎРЏР Т‘Р С•Р С” Р Р…Р Вµ РЎвЂљРЎР‚Р С•Р С–Р В°Р ВµР С
                    List<String> hours = shift.getHourlyLabels();
                    int expectedSize = hours.size();
                    int suppliedPlans = shift.getHourlyPlanValues().size();
                    List<Integer> plan = ensureSize(shift.getHourlyPlanValues(), expectedSize);
                    List<Integer> actualValues = ensureSize(shift.getHourlyActualValues(), expectedSize);
                    List<Boolean> planSupplied = java.util.stream.IntStream.range(0, expectedSize)
                            .mapToObj(index -> index < suppliedPlans).toList();
                    return new ShiftView(date, sensorId, actualValues, plan, hours, planSupplied,
                            explanationRows(comments, expectedSize));
                })
                .orElseGet(() -> {
                    // Р вЂўРЎРѓР В»Р С‘ РЎРѓР СР ВµР Р…РЎвЂ№ Р ВµРЎвЂ°РЎвЂ Р Р…Р ВµРЎвЂљ РІР‚вЂќ Р В±Р ВµРЎР‚РЎвЂР С Р Т‘Р ВµРЎвЂћР С•Р В»РЎвЂљР Р…РЎвЂ№Р Вµ РЎвЂЎР В°РЎРѓРЎвЂ№ Р С‘Р В· Р Р…Р В°РЎРѓРЎвЂљРЎР‚Р С•Р ВµР С”
                    List<String> hours = loaded.labels();
                    int expectedSize = hours.size();
                    List<Integer> plan = ensureSize(loaded.plans(), expectedSize);
                    List<Integer> actualValues = Collections.nCopies(expectedSize, 0);
                    return new ShiftView(date, sensorId, actualValues, plan, hours,
                            Collections.nCopies(expectedSize, true), explanationRows(comments, expectedSize));
                });
    }

    public ShiftView buildView(LocalDate date, String sensorId, ShiftSlice slice) {
        ShiftView full = buildView(date, sensorId);
        List<Integer> indexes = java.util.stream.IntStream.range(0, full.hours().size())
                .filter(index -> slice.contains(date, intervalStart(date, full.hours().get(index))))
                .boxed().toList();
        return new ShiftView(date, sensorId,
                indexes.stream().map(full.actual()::get).toList(),
                indexes.stream().map(full.plan()::get).toList(),
                indexes.stream().map(full.hours()::get).toList(),
                indexes.stream().map(full.planSupplied()::get).toList(),
                indexes.stream().map(full.explanations()::get).toList());
    }

    private java.time.LocalDateTime intervalStart(LocalDate date, String label) {
        java.time.LocalTime time = java.time.LocalTime.parse(label);
        return (time.isBefore(java.time.LocalTime.of(7, 0)) ? date.plusDays(1) : date).atTime(time);
    }

    private List<List<IntervalExplanationView>> explanationRows(
            java.util.Map<Integer, List<IntervalExplanationView>> comments, int size) {
        return java.util.stream.IntStream.range(0, size)
                .mapToObj(index -> comments.getOrDefault(index, List.of())).toList();
    }

    private List<Integer> ensureSize(List<Integer> source, int size) {
        List<Integer> result = new ArrayList<>(source != null ? source : List.of());
        while (result.size() < size) result.add(0);
        return result.subList(0, size);
    }
}
