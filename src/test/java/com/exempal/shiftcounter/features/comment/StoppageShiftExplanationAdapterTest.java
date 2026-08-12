package com.exempal.shiftcounter.features.comment.adapter.projection;

import com.exempal.shiftcounter.features.comment.application.StoppageRepository;
import com.exempal.shiftcounter.features.comment.domain.*;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class StoppageShiftExplanationAdapterTest {
    @Test
    void sensorFiveAggregatesMultipleRowsFromSensorsOneToFourWithSources() {
        StoppageRepository repository = mock(StoppageRepository.class);
        LocalDate date = LocalDate.of(2026, 8, 7);
        when(repository.findByShiftDateAndSensorId(date, "sensor-1"))
                .thenReturn(List.of(stoppage("sensor-1", 2, "First", "Second")));
        when(repository.findByShiftDateAndSensorId(date, "sensor-2"))
                .thenReturn(List.of(stoppage("sensor-2", 2, "Third")));
        when(repository.findByShiftDateAndSensorId(date, "sensor-3")).thenReturn(List.of());
        when(repository.findByShiftDateAndSensorId(date, "sensor-4")).thenReturn(List.of());

        var result = new StoppageShiftExplanationAdapter(repository).findByInterval(date, "sensor-5");

        assertThat(result.get(2)).extracting("sourceSensorId", "comment", "minutes")
                .containsExactly(
                        org.assertj.core.groups.Tuple.tuple("sensor-1", "First", 2),
                        org.assertj.core.groups.Tuple.tuple("sensor-1", "Second", 2),
                        org.assertj.core.groups.Tuple.tuple("sensor-2", "Third", 2));
        verify(repository, never()).findByShiftDateAndSensorId(date, "sensor-5");
    }

    @Test
    void sensorSixReadsOnlyItsOwnExplanations() {
        StoppageRepository repository = mock(StoppageRepository.class);
        LocalDate date = LocalDate.of(2026, 8, 7);
        when(repository.findByShiftDateAndSensorId(date, "sensor-6"))
                .thenReturn(List.of(stoppage("sensor-6", 1, "Independent")));

        var result = new StoppageShiftExplanationAdapter(repository).findByInterval(date, "sensor-6");

        assertThat(result.get(1)).singleElement().satisfies(row -> {
            assertThat(row.sourceSensorId()).isEqualTo("sensor-6");
            assertThat(row.comment()).isEqualTo("Independent");
        });
        verify(repository).findByShiftDateAndSensorId(date, "sensor-6");
        verifyNoMoreInteractions(repository);
    }

    private Stoppage stoppage(String sensorId, int interval, String... comments) {
        long id = Math.abs(sensorId.hashCode()) + interval;
        List<LossExplanation> rows = java.util.stream.IntStream.range(0, comments.length)
                .mapToObj(index -> new LossExplanation((long) index + 1, id, LossCategory.ORGANIZATION,
                        comments[index], 2, 3)).toList();
        return new Stoppage(id, UUID.randomUUID(), id, sensorId, interval,
                LocalDateTime.of(2026, 8, 7, 9, 0), Duration.ofMinutes(10), 10, 20,
                DetectionType.FIXED, StoppageState.ACTIVE, rows, 0);
    }
}
