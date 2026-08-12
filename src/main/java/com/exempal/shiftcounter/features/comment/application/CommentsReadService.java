package com.exempal.shiftcounter.features.comment.application;

import com.exempal.shiftcounter.features.comment.domain.ExplanationStatus;
import com.exempal.shiftcounter.features.comment.domain.Stoppage;
import com.exempal.shiftcounter.features.comment.domain.StoppageState;
import com.exempal.shiftcounter.features.shift.application.ActualDataPort;
import com.exempal.shiftcounter.features.shift.domain.Shift;
import com.exempal.shiftcounter.features.sensor.domain.SensorCatalog;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

import static com.exempal.shiftcounter.features.comment.domain.StoppageComparators.chronological;

@Service
public class CommentsReadService implements CommentsReadUseCase {
    private final StoppageRepository repository;
    private final ActualDataPort actualDataPort;

    public CommentsReadService(StoppageRepository repository, ActualDataPort actualDataPort) {
        this.repository = repository;
        this.actualDataPort = actualDataPort;
    }

    @Override
    @Transactional(readOnly = true)
    public Data read(LocalDate date, String sensorId) {
        Shift shift = actualDataPort.findByDateAndSensorId(date, sensorId).orElse(null);
        if (SensorCatalog.SENSOR_5.equals(sensorId)) {
            List<SourceComments> sources = List.of("sensor-1", "sensor-2", "sensor-3", "sensor-4").stream()
                    .map(source -> new SourceComments(source, explanationRows(date, source)))
                    .toList();
            return new Data(shift, List.of(), List.of(), sources);
        }
        if (shift == null) return new Data(null, List.of(), List.of());
        List<Stoppage> rows = repository.findByShiftDateAndSensorId(date, sensorId).stream()
                .filter(value -> value.state() == StoppageState.ACTIVE)
                .sorted(chronological()).toList();
        List<Stoppage> missing = rows.stream()
                .filter(value -> value.explanationStatus() == ExplanationStatus.UNEXPLAINED)
                .toList();
        return new Data(shift, rows, missing);
    }

    private List<ExplanationRow> explanationRows(LocalDate date, String sensorId) {
        return repository.findByShiftDateAndSensorId(date, sensorId).stream()
                .filter(value -> value.state() == StoppageState.ACTIVE)
                .sorted(chronological())
                .flatMap(stoppage -> stoppage.explanations().stream().map(explanation ->
                        new ExplanationRow(sensorId, stoppage.startedAt(), explanation.category(),
                                explanation.comment(), explanation.allocatedMinutes())))
                .toList();
    }
}
