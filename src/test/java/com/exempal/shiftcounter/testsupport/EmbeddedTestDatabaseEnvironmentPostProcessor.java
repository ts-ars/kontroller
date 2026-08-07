package com.exempal.shiftcounter.testsupport;

import io.zonky.test.db.postgres.embedded.EmbeddedPostgres;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.core.Ordered;
import org.springframework.core.env.ConfigurableEnvironment;

import java.net.URI;
import java.sql.Connection;
import java.sql.Statement;
import java.util.Arrays;

public final class EmbeddedTestDatabaseEnvironmentPostProcessor
        implements EnvironmentPostProcessor, Ordered {

    private static final Object START_MONITOR = new Object();
    private static volatile EmbeddedPostgres postgres;

    @Override
    public void postProcessEnvironment(
            ConfigurableEnvironment environment,
            SpringApplication application
    ) {
        if (!Arrays.asList(environment.getActiveProfiles()).contains("test")) {
            return;
        }
        if (!environment.getProperty("shift.test.embedded-postgres", Boolean.class, false)) {
            return;
        }

        synchronized (START_MONITOR) {
            if (postgres != null) {
                return;
            }
            startPostgres(environment);
        }
    }

    private static void startPostgres(ConfigurableEnvironment environment) {
        String jdbcUrl = environment.getRequiredProperty("spring.datasource.url");
        String username = environment.getRequiredProperty("spring.datasource.username");
        String password = environment.getRequiredProperty("spring.datasource.password");
        URI databaseUri = URI.create(jdbcUrl.substring("jdbc:".length()));
        int port = databaseUri.getPort();
        String database = databaseUri.getPath().substring(1);

        if (!"shiftcounter_test".equals(database) || !"shift_test".equals(username)) {
            throw new IllegalStateException("Embedded PostgreSQL is restricted to shiftcounter_test/shift_test");
        }

        try {
            postgres = EmbeddedPostgres.builder()
                    .setPort(port)
                    .setServerConfig("unix_socket_directories", "")
                    .start();
            try (Connection connection = postgres.getPostgresDatabase().getConnection();
                 Statement statement = connection.createStatement()) {
                statement.execute("CREATE ROLE shift_test LOGIN PASSWORD " + quoteLiteral(password));
                statement.execute("CREATE DATABASE shiftcounter_test OWNER shift_test");
                statement.execute("REVOKE CONNECT ON DATABASE postgres FROM PUBLIC");
                statement.execute("REVOKE ALL ON DATABASE shiftcounter_test FROM PUBLIC");
                statement.execute("GRANT CONNECT, TEMPORARY ON DATABASE shiftcounter_test TO shift_test");
            }
        } catch (Exception exception) {
            throw new IllegalStateException("Unable to provision embedded PostgreSQL test database", exception);
        }
    }

    private static String quoteLiteral(String value) {
        return "'" + value.replace("'", "''") + "'";
    }

    @Override
    public int getOrder() {
        return Ordered.LOWEST_PRECEDENCE - 10;
    }
}
