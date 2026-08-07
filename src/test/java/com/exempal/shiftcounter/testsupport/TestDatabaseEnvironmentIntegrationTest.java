package com.exempal.shiftcounter.testsupport;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.env.Environment;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@org.springframework.test.context.ActiveProfiles("test")
@org.junit.jupiter.api.Tag("integration")
class TestDatabaseEnvironmentIntegrationTest {

    @Autowired
    private Environment environment;

    @Autowired
    private DataSource dataSource;

    @Test
    void usesTestProfileDatabaseAndCredentials() throws Exception {
        assertThat(Arrays.asList(environment.getActiveProfiles())).contains("test");

        try (Connection connection = dataSource.getConnection()) {
            assertThat(connection.getCatalog()).isEqualTo("shiftcounter_test");
            assertThat(connection.getMetaData().getUserName()).isEqualTo("shift_test");
            assertThat(connection.getMetaData().getURL())
                    .contains("/shiftcounter_test")
                    .doesNotContain("shiftcounter_prod");
        }
    }

    @Test
    void testCredentialsCannotOpenAProductionDatabase() {
        String productionUrl = environment.getRequiredProperty("spring.datasource.url")
                .replace("/shiftcounter_test", "/shiftcounter_prod");

        assertThatThrownBy(() -> DriverManager.getConnection(
                productionUrl,
                environment.getRequiredProperty("spring.datasource.username"),
                environment.getRequiredProperty("spring.datasource.password")
        )).isInstanceOf(SQLException.class);
    }
}
