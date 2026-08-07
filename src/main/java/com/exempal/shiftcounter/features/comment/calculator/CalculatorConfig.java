package com.exempal.shiftcounter.features.comment.calculator;

import com.exempal.shiftcounter.features.comment.application.StoppageDetector;
import com.exempal.shiftcounter.features.shift.application.ShiftTimeHelper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

@Configuration
public class CalculatorConfig {

    // StoppageDetector помечен @Component и найдётся сканером — отдельный @Bean не нужен.

    @Bean
    public StoppageFixedLossCalculator stoppageFixedLossCalculator(
            StoppageDetector detector,
            @Value("${loss.fixed.min-gap:PT1M}") Duration minGap
    ) {
        // тонкая обёртка, использующая твой StoppageDetector и minGap из свойства
        return new StoppageFixedLossCalculator(detector, minGap);
    }

    @Bean
    public StoppageTempoLossCalculator stoppageTempoLossCalculator(ShiftTimeHelper timeHelper) {
        return new StoppageTempoLossCalculator(timeHelper);
    }

    @Bean
    public StoppageCalculator stoppageCalculator(
            StoppageFixedLossCalculator fixed,
            StoppageTempoLossCalculator tempo
    ) {
        return new StoppageCalculatorImpl(fixed, tempo);
    }
}
