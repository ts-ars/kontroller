package com.exempal.shiftcounter.common.config;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.core.Ordered;
import org.springframework.core.env.ConfigurableEnvironment;

import java.util.Arrays;

public final class TestDatabaseEnvironmentPostProcessor implements EnvironmentPostProcessor, Ordered {

    @Override
    public void postProcessEnvironment(
            ConfigurableEnvironment environment,
            SpringApplication application
    ) {
        if (!Arrays.asList(environment.getActiveProfiles()).contains("test")) {
            return;
        }

        TestDatabaseEnvironmentGuard.validate(
                environment.getRequiredProperty("spring.datasource.url"),
                environment.getRequiredProperty("spring.datasource.username")
        );
    }

    @Override
    public int getOrder() {
        return Ordered.LOWEST_PRECEDENCE;
    }
}
