package com.exempal.shiftcounter.test;

import com.exempal.shiftcounter.features.signal.adapter.adam.AdamModbusAdapter;
import com.exempal.shiftcounter.features.signal.adapter.event.AdamEventEmitter;
import com.exempal.shiftcounter.features.signal.adapter.http.HttpSignalAdapter;
import com.exempal.shiftcounter.features.signal.adapter.web.SignalController;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.core.env.Environment;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@Tag("e2e")
@SpringBootTest
class TestEnvironmentIsolationIT {

    @Autowired Environment environment;
    @Autowired DataSource dataSource;
    @Autowired ApplicationContext context;
    @Autowired JdbcTemplate jdbc;

    @Test
    void usesOnlyTestProfileDatabaseAndAdapters() throws Exception {
        assertThat(Arrays.asList(environment.getActiveProfiles())).contains("test");
        try (var connection = dataSource.getConnection()) {
            assertThat(connection.getCatalog()).isEqualTo("shiftcounter_test");
            assertThat(connection.getMetaData().getUserName()).startsWith("shift_test");
        }

        assertThat(context.getBeansOfType(AdamModbusAdapter.class)).isEmpty();
        assertThat(context.getBeansOfType(AdamEventEmitter.class)).isEmpty();
        assertThat(context.getBeansOfType(HttpSignalAdapter.class)).hasSize(1);
        assertThat(context.getBeansOfType(SignalController.class)).hasSize(1);
        assertThat(jdbc.queryForObject("SELECT count(*) FROM flyway_schema_history", Integer.class)).isPositive();
    }

    @Test
    void testUserCannotConnectToProductionDatabase() {
        String prodUrl = System.getenv("PROD_DB_URL");
        String testPassword = System.getenv("TEST_DB_PASSWORD");

        assertThat(prodUrl).as("PROD_DB_URL must be provided").isNotBlank();
        assertThat(testPassword).as("TEST_DB_PASSWORD must be provided").isNotBlank();
        assertThatThrownBy(() -> DriverManager.getConnection(prodUrl, "shift_test", testPassword))
                .isInstanceOf(SQLException.class);
    }
}
