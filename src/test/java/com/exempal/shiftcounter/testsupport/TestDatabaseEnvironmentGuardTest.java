package com.exempal.shiftcounter.testsupport;

import com.exempal.shiftcounter.common.config.TestDatabaseEnvironmentGuard;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@org.junit.jupiter.api.Tag("unit")
class TestDatabaseEnvironmentGuardTest {

    @Test
    void acceptsOnlyTheDedicatedTestDatabaseAndUser() {
        assertThatCode(() -> TestDatabaseEnvironmentGuard.validate(
                "jdbc:postgresql://localhost:5433/shiftcounter_test",
                "shift_test"
        )).doesNotThrowAnyException();
    }

    @Test
    void rejectsProductionDatabase() {
        assertThatThrownBy(() -> TestDatabaseEnvironmentGuard.validate(
                "jdbc:postgresql://localhost:5432/shiftcounter_prod",
                "shift_test"
        )).isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("shiftcounter_test");
    }

    @Test
    void rejectsProductionUser() {
        assertThatThrownBy(() -> TestDatabaseEnvironmentGuard.validate(
                "jdbc:postgresql://localhost:5433/shiftcounter_test",
                "shift_prod"
        )).isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("shift_test");
    }
}
