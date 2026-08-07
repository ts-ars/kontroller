package com.exempal.shiftcounter.core;

import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("test")
public final class TestDatabaseSafetyGuard {

    public TestDatabaseSafetyGuard(DataSourceProperties properties) {
        String url = properties.getUrl();
        String username = properties.getUsername();

        if (url == null || !url.matches("^jdbc:postgresql://[^/]+/shiftcounter_test(?:\\?.*)?$")) {
            throw new IllegalStateException("Test profile requires the shiftcounter_test database");
        }
        if (!"shift_test".equals(username)) {
            throw new IllegalStateException("Test profile requires the shift_test database user");
        }
    }
}
