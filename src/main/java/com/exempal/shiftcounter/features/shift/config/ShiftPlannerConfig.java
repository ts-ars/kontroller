package com.exempal.shiftcounter.features.shift.config;

import com.exempal.shiftcounter.features.shift.application.ShiftPlannerUseCase;
import com.exempal.shiftcounter.features.shift.domain.ActualDataPort;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ShiftPlannerConfig {

    @Bean
    public ShiftPlannerUseCase shiftPlannerUseCase(ActualDataPort actualDataPort) {
        return new ShiftPlannerUseCase(actualDataPort);
    }
}
