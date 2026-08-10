package com.exempal.shiftcounter.core;

import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.mock.env.MockEnvironment;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ProductionStartupGuardTest {
    @Test
    void acceptsOnlyTheProductionDatabaseAndRole() {
        DataSourceProperties datasource = datasource(
                "jdbc:postgresql://db.internal:5432/shiftcounter_prod", "shift_prod", "secret");
        MockEnvironment environment = new MockEnvironment();
        environment.setActiveProfiles("prod");
        assertThatCode(() -> new ProductionStartupGuard(environment, datasource)).doesNotThrowAnyException();
    }

    @Test
    void rejectsTestProfileDatabaseAndMissingSecret() {
        MockEnvironment mixed = new MockEnvironment();
        mixed.setActiveProfiles("prod", "test");
        assertThatThrownBy(() -> new ProductionStartupGuard(mixed,
                datasource("jdbc:postgresql://db/shiftcounter_prod", "shift_prod", "secret")))
                .hasMessageContaining("test profile");
        assertThatThrownBy(() -> new ProductionStartupGuard(new MockEnvironment(),
                datasource("jdbc:postgresql://db/shiftcounter_test", "shift_test", "")))
                .hasMessageContaining("DB_PASSWORD");
    }

    private static DataSourceProperties datasource(String url, String username, String password) {
        DataSourceProperties properties = new DataSourceProperties();
        properties.setUrl(url);
        properties.setUsername(username);
        properties.setPassword(password);
        return properties;
    }
}
