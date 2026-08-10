package com.exempal.shiftcounter.features.comment.application;

import com.exempal.shiftcounter.features.comment.domain.ExplanationStatus;
import com.exempal.shiftcounter.features.comment.domain.Stoppage;
import com.exempal.shiftcounter.features.comment.domain.StoppageState;
import com.exempal.shiftcounter.features.shift.domain.ActualDataPort;
import com.exempal.shiftcounter.features.shift.domain.Shift;
import org.springframework.stereotype.Service;

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
    public Data read(LocalDate date, String sensorId) {
        Shift shift = actualDataPort.findByDateAndSensorId(date, sensorId).orElse(null);
        if (shift == null) return new Data(null, List.of(), List.of());
        List<Stoppage> rows = repository.findByShiftDateAndSensorId(date, sensorId).stream()
                .filter(value -> value.state() == StoppageState.ACTIVE)
                .sorted(chronological()).toList();
        List<Stoppage> missing = rows.stream()
                .filter(value -> value.explanationStatus() == ExplanationStatus.UNEXPLAINED)
                .toList();
        return new Data(shift, rows, missing);
    }
}
