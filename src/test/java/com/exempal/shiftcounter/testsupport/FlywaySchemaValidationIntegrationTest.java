package com.exempal.shiftcounter.testsupport;

import jakarta.persistence.EntityManagerFactory;
import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@org.springframework.test.context.ActiveProfiles("test")
@org.junit.jupiter.api.Tag("integration")
class FlywaySchemaValidationIntegrationTest {

    private static final Set<String> EXPECTED_TABLES = Set.of(
            "settings",
            "shift",
            "shift_hourly_plan",
            "shift_hourly_actual",
            "shift_hour_labels",
            "shift_comments",
            "stoppages",
            "signals"
    );

    @Autowired
    private Flyway flyway;

    @Autowired
    private EntityManagerFactory entityManagerFactory;

    @Autowired
    private DataSource dataSource;

    @Test
    void flywayOwnsTheSchemaAndHibernateValidatesIt() {
        assertThat(flyway.info().applied())
                .extracting(info -> info.getVersion().getVersion())
                .containsExactly("1", "2");
        assertThat(entityManagerFactory.getProperties())
                .containsEntry("hibernate.hbm2ddl.auto", "validate");

        JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
        assertThat(jdbcTemplate.queryForList("""
                SELECT table_name
                FROM information_schema.tables
                WHERE table_schema = 'public'
                """, String.class)).containsAll(EXPECTED_TABLES);
    }
}
