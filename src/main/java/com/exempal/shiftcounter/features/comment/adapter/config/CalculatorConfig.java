package com.exempal.shiftcounter.features.comment.adapter.config;

import com.exempal.shiftcounter.features.comment.application.calculator.StoppageCalculator;
import com.exempal.shiftcounter.features.comment.application.calculator.StoppageCalculatorImpl;
import com.exempal.shiftcounter.features.comment.application.calculator.StoppageFixedLossCalculator;
import com.exempal.shiftcounter.features.comment.application.calculator.StoppageTempoLossCalculator;

import com.exempal.shiftcounter.features.comment.application.StoppageDetector;
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
    public StoppageTempoLossCalculator stoppageTempoLossCalculator() {
        return new StoppageTempoLossCalculator();
    }

    @Bean
    public StoppageCalculator stoppageCalculator(
            StoppageFixedLossCalculator fixed,
            StoppageTempoLossCalculator tempo
    ) {
        return new StoppageCalculatorImpl(fixed, tempo);
    }
}
