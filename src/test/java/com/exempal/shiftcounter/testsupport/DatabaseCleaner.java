package com.exempal.shiftcounter.testsupport;

import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;

public final class DatabaseCleaner {

    private final JdbcTemplate jdbcTemplate;

    public DatabaseCleaner(DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    public void clean() {
        jdbcTemplate.execute("""
                TRUNCATE TABLE
                    stoppages,
                    shift_comments,
                    shift_hourly_actual,
                    shift_hourly_plan,
                    shift_hour_labels,
                    shift,
                    signals
                RESTART IDENTITY CASCADE
                """);
        jdbcTemplate.update("""
                DELETE FROM settings
                WHERE setting_key NOT IN ('hours', 'hourlyPlans')
                """);
        jdbcTemplate.update("""
                INSERT INTO settings (setting_key, setting_value)
                VALUES ('hours', '["08:00","09:00","10:00","11:00","12:30","13:30","14:30","15:30"]')
                ON CONFLICT (setting_key)
                DO UPDATE SET setting_value = EXCLUDED.setting_value
                """);
        jdbcTemplate.update("""
                INSERT INTO settings (setting_key, setting_value)
                VALUES ('hourlyPlans', '[337,450,450,450,450,450,450,337]')
                ON CONFLICT (setting_key)
                DO UPDATE SET setting_value = EXCLUDED.setting_value
                """);
    }
}
