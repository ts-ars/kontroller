package com.exempal.shiftcounter.features.signal.adapter;

import com.exempal.shiftcounter.features.signal.adapter.persistence.SignalJpaRepository;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class ReportSignalQueryAdapterTest {
    @Test
    void delegatesExactHalfOpenRangeIncludingTheOvernightPartOfTheProductionDay() {
        SignalJpaRepository repository = mock(SignalJpaRepository.class);
        LocalDateTime from = LocalDateTime.of(2026, 8, 10, 7, 0);
        LocalDateTime to = LocalDateTime.of(2026, 8, 11, 7, 0);
        when(repository.countInHalfOpenRange("sensor-4", from, to)).thenReturn(17L);

        long total = new ReportSignalQueryAdapter(repository).count("sensor-4", from, to);

        assertThat(total).isEqualTo(17);
        verify(repository).countInHalfOpenRange("sensor-4", from, to);
    }
}
