package com.exempal.shiftcounter.core;

import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.core.env.Environment;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.Arrays;

@Component
@Profile("prod")
public final class ProductionStartupGuard {

    public ProductionStartupGuard(Environment environment, DataSourceProperties datasource) {
        if (Arrays.stream(environment.getActiveProfiles()).anyMatch("test"::equals)) {
            throw new IllegalStateException("Production must not start with the test profile");
        }
        String url = required(datasource.getUrl(), "DB_URL");
        String username = required(datasource.getUsername(), "DB_USERNAME");
        required(datasource.getPassword(), "DB_PASSWORD");
        if (!url.matches("^jdbc:postgresql://[^/]+/shiftcounter_prod(?:\\?.*)?$")) {
            throw new IllegalStateException("Production profile requires the shiftcounter_prod database");
        }
        if (!"shift_prod".equals(username)) {
            throw new IllegalStateException("Production profile requires the shift_prod database user");
        }
    }

    private static String required(String value, String name) {
        if (value == null || value.isBlank()) throw new IllegalStateException(name + " is required");
        return value;
    }
}
