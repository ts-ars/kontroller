package com.exempal.shiftcounter.features.signal.adapter.persistence;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class SignalJpaAdapterTest {
    @Test
    void delegatesToStrictHalfOpenRangeQuery() {
        SignalJpaRepository repository = mock(SignalJpaRepository.class);
        LocalDateTime start = LocalDateTime.of(2026, 8, 10, 23, 30);
        LocalDateTime end = LocalDateTime.of(2026, 8, 11, 0, 30);
        when(repository.findAllInHalfOpenRange("sensor-1", start, end)).thenReturn(List.of(
                new SignalEntity(UUID.randomUUID(), start)));

        var result = new SignalJpaAdapter(repository).findBySensorAndRange("sensor-1", start, end);

        assertThat(result).singleElement().extracting(value -> value.timestamp()).isEqualTo(start);
        verify(repository).findAllInHalfOpenRange("sensor-1", start, end);
    }
}
