package com.exempal.shiftcounter.test;

import org.springframework.core.Ordered;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.TestContext;
import org.springframework.test.context.support.AbstractTestExecutionListener;

public final class DatabaseResetTestExecutionListener extends AbstractTestExecutionListener {

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE + 100;
    }

    @Override
    public void beforeTestMethod(TestContext testContext) {
        if (!testContext.getApplicationContext().containsBean("dataSource")) {
            return;
        }

        JdbcTemplate jdbc = testContext.getApplicationContext().getBean(JdbcTemplate.class);
        jdbc.execute("TRUNCATE TABLE signals, stoppages, comments, "
                + "shift_hour_labels, shift_hourly_actual, shift_hourly_plan, shift "
                + "RESTART IDENTITY CASCADE");
        jdbc.update("DELETE FROM settings");
        jdbc.update("INSERT INTO settings(setting_key, setting_value) VALUES (?, ?)",
                "hours", "[\"08:00\",\"09:00\",\"10:00\",\"11:00\",\"12:30\",\"13:30\",\"14:30\",\"15:30\"]");
        jdbc.update("INSERT INTO settings(setting_key, setting_value) VALUES (?, ?)",
                "hourlyPlans", "[337,450,450,450,450,450,450,337]");
    }
}
