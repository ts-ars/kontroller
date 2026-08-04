package com.exempal.shiftcounter.features.shift.application;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

class ShiftAnalyticsUseCaseTest {

    private final ShiftAnalyticsUseCase analytics = new ShiftAnalyticsUseCase();

    @Test
    void shouldCalculateLostCans() {
        int result = analytics.calculateLostCans(12.0, 5.0); // 12 минут простоя, 5 банок/мин
        assertThat(result).isEqualTo(60); // 12 * 5
    }

    @Test
    void shouldCalculateEfficiency() {
        int result = analytics.calculateEfficiency(180, 200); // 90%
        assertThat(result).isEqualTo(90);
    }

    @Test
    void shouldReturnZeroIfPlanIsZero() {
        int result = analytics.calculateEfficiency(180, 0); // деление на 0
        assertThat(result).isEqualTo(0);
    }
}
