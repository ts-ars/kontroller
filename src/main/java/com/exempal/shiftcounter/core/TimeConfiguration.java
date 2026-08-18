package com.exempal.shiftcounter.core;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.beans.factory.annotation.Value;

import java.time.Clock;
import java.time.ZoneId;

@Configuration
public class TimeConfiguration {
    @Bean
    public Clock applicationClock(@Value("${application.time-zone}") String timeZone) {
        return Clock.system(ZoneId.of(timeZone));
    }
}
