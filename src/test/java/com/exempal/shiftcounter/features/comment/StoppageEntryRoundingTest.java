package com.exempal.shiftcounter.features.comment;

import com.exempal.shiftcounter.features.comment.domain.StoppageEntry;
import com.exempal.shiftcounter.features.shift.infrastructure.ShiftEntity;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;

class StoppageEntryRoundingTest {
    @ParameterizedTest
    @CsvSource({"29,0", "30,1", "89,1", "90,2", "3599,60"})
    void roundsDurationToWholeMinutesHalfUp(long seconds, long expectedMinutes) {
        StoppageEntry entry = StoppageEntry.fixed(0, Duration.ofSeconds(seconds), new ShiftEntity());
        assertThat(entry.getMinutes()).isEqualTo(expectedMinutes);
    }
}
