package com.exempal.shiftcounter.features.comment.adapter.projection;

import com.exempal.shiftcounter.features.comment.application.StoppageRepository;
import com.exempal.shiftcounter.features.sensor.domain.SensorCatalog;
import com.exempal.shiftcounter.features.shift.application.ProductionDayService;
import com.exempal.shiftcounter.features.shift.application.projection.IntervalExplanationView;
import com.exempal.shiftcounter.features.shift.application.projection.ShiftExplanationPort;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Component
public class StoppageShiftExplanationAdapter implements ShiftExplanationPort {
    private final StoppageRepository stoppages;
    private final ProductionDayService productionDays;

    public StoppageShiftExplanationAdapter(StoppageRepository stoppages, ProductionDayService productionDays) {
        this.stoppages = stoppages;
        this.productionDays = productionDays;
    }

    @Override
    @Transactional(readOnly = true)
    public Map<Integer, List<IntervalExplanationView>> findByInterval(LocalDate date, String sensorId) {
        List<String> sources = SensorCatalog.SENSOR_5.equals(sensorId)
                ? List.of("sensor-1", "sensor-2", "sensor-3", "sensor-4")
                : List.of(sensorId);
        Map<Integer, List<IntervalExplanationView>> result = new LinkedHashMap<>();
        for (String source : sources) {
            stoppages.findByShiftDateAndSensorId(date, source).stream()
                    .forEach(stoppage -> {
                        var interval = result.computeIfAbsent(stoppage.intervalIndex(), ignored -> new ArrayList<>());
                        stoppage.explanations().forEach(explanation -> interval.add(
                                new IntervalExplanationView(source, explanation.comment(),
                                        explanation.allocatedMinutes(), explanation.allocatedCans(), "EXPLAINED")));
                        if (!stoppage.endedAt().isAfter(productionDays.now())) {
                            int remainingMinutes = Math.max(0,
                                    stoppage.roundedMinutes() - Math.toIntExact(stoppage.allocatedMinutes()));
                            int allocatedCans = stoppage.explanations().stream()
                                    .mapToInt(value -> value.allocatedCans()).sum();
                            int remainingCans = Math.max(0, stoppage.lostCans() - allocatedCans);
                            if (stoppage.explanationStatus().name().equals("ALLOCATION_CONFLICT")) {
                                interval.add(new IntervalExplanationView(source,
                                        "Allocation conflict", 0, 0, "ALLOCATION_CONFLICT"));
                            } else if (remainingMinutes > 0 || remainingCans > 0) {
                                interval.add(new IntervalExplanationView(source,
                                        "Unexplained stoppage", remainingMinutes, remainingCans, "UNEXPLAINED"));
                            }
                        }
                    });
        }
        return result.entrySet().stream().collect(java.util.stream.Collectors.toMap(
                Map.Entry::getKey, entry -> List.copyOf(entry.getValue()),
                (left, right) -> left, LinkedHashMap::new));
    }
}
