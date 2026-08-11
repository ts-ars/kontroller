package com.exempal.shiftcounter.features.comment.application;

import com.exempal.shiftcounter.features.comment.domain.*;
import com.exempal.shiftcounter.features.shift.application.ActualDataPort;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class CommentsReadServiceTest {
    @Test
    void sensorFiveReturnsFourReadOnlySourceBucketsAndNoOwnLosses() {
        StoppageRepository repository = mock(StoppageRepository.class);
        ActualDataPort shifts = mock(ActualDataPort.class);
        LocalDate date = LocalDate.of(2026, 8, 7);
        when(shifts.findByDateAndSensorId(date, "sensor-5")).thenReturn(Optional.empty());
        when(repository.findByShiftDateAndSensorId(date, "sensor-1"))
                .thenReturn(List.of(stoppage("sensor-1", "One", "Two")));
        when(repository.findByShiftDateAndSensorId(date, "sensor-2")).thenReturn(List.of());
        when(repository.findByShiftDateAndSensorId(date, "sensor-3")).thenReturn(List.of());
        when(repository.findByShiftDateAndSensorId(date, "sensor-4")).thenReturn(List.of());

        var data = new CommentsReadService(repository, shifts).read(date, "sensor-5");

        assertThat(data.rows()).isEmpty();
        assertThat(data.sourceComments()).extracting(CommentsReadUseCase.SourceComments::sensorId)
                .containsExactly("sensor-1", "sensor-2", "sensor-3", "sensor-4");
        assertThat(data.sourceComments().getFirst().rows())
                .extracting(CommentsReadUseCase.ExplanationRow::comment).containsExactly("One", "Two");
        verify(repository, never()).findByShiftDateAndSensorId(date, "sensor-5");
    }

    @Test
    void sensorSixNeverReadsOtherSensorComments() {
        StoppageRepository repository = mock(StoppageRepository.class);
        ActualDataPort shifts = mock(ActualDataPort.class);
        LocalDate date = LocalDate.of(2026, 8, 7);
        when(shifts.findByDateAndSensorId(date, "sensor-6")).thenReturn(Optional.empty());

        var data = new CommentsReadService(repository, shifts).read(date, "sensor-6");

        assertThat(data.sourceComments()).isEmpty();
        verify(repository, never()).findByShiftDateAndSensorId(any(), anyString());
    }

    private Stoppage stoppage(String sensorId, String... comments) {
        long id = 101;
        List<LossExplanation> rows = java.util.stream.IntStream.range(0, comments.length)
                .mapToObj(index -> new LossExplanation((long) index + 1, id, LossCategory.MATERIAL,
                        comments[index], 2, 4)).toList();
        return new Stoppage(id, UUID.randomUUID(), 10L, sensorId, 1,
                LocalDateTime.of(2026, 8, 7, 8, 15), Duration.ofMinutes(10), 10, 20,
                DetectionType.FIXED, StoppageState.ACTIVE, rows, 0);
    }
}
