package com.exempal.shiftcounter.features.comment;

import com.exempal.shiftcounter.features.comment.domain.Stoppage;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;

class StoppageRoundingTest {
    @ParameterizedTest
    @CsvSource({"29,0", "30,1", "89,1", "90,2", "3599,60"})
    void roundsDurationToWholeMinutesHalfUp(long seconds, int expectedMinutes) {
        assertThat(Stoppage.roundHalfUpMinutes(Duration.ofSeconds(seconds))).isEqualTo(expectedMinutes);
    }
}
