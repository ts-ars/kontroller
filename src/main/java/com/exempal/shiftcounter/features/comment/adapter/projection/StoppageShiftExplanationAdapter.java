package com.exempal.shiftcounter.features.comment.adapter.projection;

import com.exempal.shiftcounter.features.comment.application.StoppageRepository;
import com.exempal.shiftcounter.features.comment.domain.StoppageState;
import com.exempal.shiftcounter.features.sensor.domain.SensorCatalog;
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

    public StoppageShiftExplanationAdapter(StoppageRepository stoppages) {
        this.stoppages = stoppages;
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
                    .filter(stoppage -> stoppage.state() == StoppageState.ACTIVE)
                    .forEach(stoppage -> stoppage.explanations().forEach(explanation ->
                            result.computeIfAbsent(stoppage.intervalIndex(), ignored -> new ArrayList<>())
                                    .add(new IntervalExplanationView(source, explanation.comment(),
                                            explanation.allocatedMinutes()))));
        }
        return result.entrySet().stream().collect(java.util.stream.Collectors.toMap(
                Map.Entry::getKey, entry -> List.copyOf(entry.getValue()),
                (left, right) -> left, LinkedHashMap::new));
    }
}
