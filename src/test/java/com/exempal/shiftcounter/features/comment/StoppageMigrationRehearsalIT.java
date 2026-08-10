package com.exempal.shiftcounter.features.comment;

import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.MigrationVersion;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import javax.sql.DataSource;
import java.sql.Timestamp;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@Tag("e2e")
class StoppageMigrationRehearsalIT {
    private static final String SCHEMA = "stage4_migration_rehearsal";

    @Test
    void upgradesLegacyCopyAndReportsAmbiguousRows() {
        DataSource dataSource = new DriverManagerDataSource(
                System.getenv("TEST_DB_URL"), System.getenv("TEST_DB_USERNAME"),
                System.getenv("TEST_DB_PASSWORD"));
        JdbcTemplate jdbc = new JdbcTemplate(dataSource);
        jdbc.execute("DROP SCHEMA IF EXISTS " + SCHEMA + " CASCADE");
        try {
            // V1 predates schema-safe migrations and contains unqualified DROP TABLE statements.
            // Shadows keep those statements inside the rehearsal copy without editing applied V1.
            jdbc.execute("CREATE SCHEMA " + SCHEMA);
            jdbc.execute("CREATE TABLE " + SCHEMA + ".settings(dummy INTEGER)");
            jdbc.execute("CREATE TABLE " + SCHEMA + ".shift(dummy INTEGER)");
            jdbc.execute("CREATE TABLE " + SCHEMA + ".comments(dummy INTEGER)");
            jdbc.execute("CREATE TABLE " + SCHEMA + ".stoppages(dummy INTEGER)");
            jdbc.execute("CREATE TABLE " + SCHEMA + ".signals(dummy INTEGER)");
            Flyway.configure().dataSource(dataSource).schemas(SCHEMA).defaultSchema(SCHEMA)
                    .baselineOnMigrate(true).baselineVersion(MigrationVersion.fromVersion("0"))
                    .target(MigrationVersion.fromVersion("2")).load().migrate();
            jdbc.execute("INSERT INTO " + SCHEMA + ".shift(id,date,actual) VALUES (1,DATE '2026-08-07',0)");
            jdbc.execute("INSERT INTO " + SCHEMA + ".shift_hour_labels(shift_id,order_index,label) " +
                    "VALUES (1,0,'08:00')");
            jdbc.execute("INSERT INTO " + SCHEMA + ".stoppages" +
                    "(id,shift_id,hour_index,minutes,cans,type,reason,minute_offset) VALUES " +
                    "(10,1,0,2.5,25,'FIXED','',1)," +
                    "(11,1,9,3,30,'TEMPO','',0)," +
                    "(12,1,0,1,10,'BREAKDOWN','belt',0)");

            Flyway.configure().dataSource(dataSource).schemas(SCHEMA).defaultSchema(SCHEMA).load().migrate();

            assertThat(jdbc.queryForObject("SELECT detection_type FROM " + SCHEMA +
                    ".stoppages WHERE id=10", String.class)).isEqualTo("FIXED");
            assertThat(jdbc.queryForObject("SELECT rounded_minutes FROM " + SCHEMA +
                    ".stoppages WHERE id=10", Integer.class)).isEqualTo(3);
            assertThat(jdbc.queryForObject("SELECT incident_key = detection_key FROM " + SCHEMA +
                    ".stoppages WHERE id=10", Boolean.class)).isTrue();
            assertThat(jdbc.queryForObject("SELECT started_at FROM " + SCHEMA +
                    ".stoppages WHERE id=10", Timestamp.class).toLocalDateTime())
                    .isEqualTo(LocalDateTime.of(2026, 8, 7, 8, 1));
            assertThat(jdbc.queryForObject("SELECT count(*) FROM " + SCHEMA +
                    ".stoppage_model_migration_report WHERE stoppage_id=11", Integer.class)).isEqualTo(1);
            assertThat(jdbc.queryForObject("SELECT count(*) FROM " + SCHEMA +
                    ".legacy_loss_explanation_migration_report WHERE stoppage_id=12", Integer.class)).isEqualTo(1);
            assertThat(jdbc.queryForObject("SELECT sensor_id FROM " + SCHEMA +
                    ".shift WHERE id=1", String.class)).isEqualTo("sensor-1");
            assertThat(jdbc.queryForObject("SELECT sensor_key FROM " + SCHEMA +
                    ".stoppages WHERE id=10", String.class)).isEqualTo("sensor-1");
            assertThat(jdbc.queryForObject("SELECT count(*) FROM " + SCHEMA +
                    ".sensors", Integer.class)).isEqualTo(6);
            assertThat(jdbc.queryForObject("SELECT to_regclass('" + SCHEMA
                    + ".counter_states') IS NOT NULL", Boolean.class)).isTrue();
        } finally {
            jdbc.execute("DROP SCHEMA IF EXISTS " + SCHEMA + " CASCADE");
        }
    }
}
