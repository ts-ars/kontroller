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
        jdbc.execute("TRUNCATE TABLE counter_states, loss_explanations, signals, stoppages, comments, interval_settings, "
                + "shift_hour_labels, shift_hourly_actual, shift_hourly_plan, shift "
                + "RESTART IDENTITY CASCADE");
        jdbc.update("UPDATE sensors SET settings_group_id = CASE WHEN id = 'sensor-6' "
                + "THEN 'settings-group-2' ELSE 'settings-group-1' END");
        jdbc.update("UPDATE settings_groups SET name = CASE id WHEN 'settings-group-1' "
                + "THEN 'Sensors 1-4 shared plan' ELSE 'Sensor 6 independent plan' END, enabled = TRUE");
        String[] hours = {"07:00", "08:00", "09:00", "10:00", "11:30", "12:30", "13:30", "14:30",
                "15:00", "16:00", "17:00", "18:00", "19:00", "20:30", "21:30", "22:30"};
        int[] shared = {450, 600, 500, 600, 600, 600, 500, 600, 300, 600, 600, 500, 600, 600, 500, 300};
        int[] sensor6 = {1600, 1920, 1600, 1920, 1920, 1920, 1600, 960,
                1920, 1920, 1600, 1920, 1920, 1920, 1600, 960};
        for (String group : new String[]{"settings-group-1", "settings-group-2"}) {
            int[] plans = group.equals("settings-group-1") ? shared : sensor6;
            for (int index = 0; index < hours.length; index++) {
                jdbc.update("INSERT INTO interval_settings(settings_group_id, order_index, start_time, plan) "
                        + "VALUES (?, ?, CAST(? AS TIME), ?)", group, index, hours[index], plans[index]);
            }
        }
    }
}
