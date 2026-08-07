package com.exempal.shiftcounter.testsupport;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@org.springframework.test.context.ActiveProfiles("test")
@org.junit.jupiter.api.Tag("integration")
class DatabaseCleanupIntegrationTest {

    @Autowired
    private DataSource dataSource;

    @Test
    void removesMutableRowsAndRestoresSettingsBaseline() {
        JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
        jdbcTemplate.update(
                "INSERT INTO signals (id, timestamp) VALUES (gen_random_uuid(), CURRENT_TIMESTAMP)"
        );
        jdbcTemplate.update(
                "UPDATE settings SET setting_value = '[]' WHERE setting_key = 'hours'"
        );

        new DatabaseCleaner(dataSource).clean();

        assertThat(jdbcTemplate.queryForObject("SELECT count(*) FROM signals", Integer.class)).isZero();
        assertThat(jdbcTemplate.queryForObject(
                "SELECT setting_value FROM settings WHERE setting_key = 'hours'",
                String.class
        )).isEqualTo("[\"08:00\",\"09:00\",\"10:00\",\"11:00\",\"12:30\",\"13:30\",\"14:30\",\"15:30\"]");
    }
}
