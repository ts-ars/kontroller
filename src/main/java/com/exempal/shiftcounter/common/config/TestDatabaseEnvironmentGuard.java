package com.exempal.shiftcounter.common.config;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.regex.Pattern;

@Component
@Profile("test")
public class TestDatabaseEnvironmentGuard implements InitializingBean {

    private static final String TEST_DATABASE = "shiftcounter_test";
    private static final String TEST_USER = "shift_test";
    private static final Pattern TEST_JDBC_URL = Pattern.compile(
            "^jdbc:postgresql://[^/]+/" + TEST_DATABASE + "(?:\\?.*)?$"
    );

    private final String jdbcUrl;
    private final String username;

    public TestDatabaseEnvironmentGuard(
            @Value("${spring.datasource.url}") String jdbcUrl,
            @Value("${spring.datasource.username}") String username
    ) {
        this.jdbcUrl = jdbcUrl;
        this.username = username;
    }

    @Override
    public void afterPropertiesSet() {
        validate(jdbcUrl, username);
    }

    public static void validate(String jdbcUrl, String username) {
        if (!TEST_JDBC_URL.matcher(jdbcUrl).matches()) {
            throw new IllegalStateException(
                    "The test profile may connect only to PostgreSQL database " + TEST_DATABASE
            );
        }
        if (!TEST_USER.equals(username)) {
            throw new IllegalStateException(
                    "The test profile may connect only as PostgreSQL user " + TEST_USER
            );
        }
    }
}
