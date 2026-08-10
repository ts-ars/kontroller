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
        jdbc.update("UPDATE settings_groups SET name = CASE id WHEN 'settings-group-1' THEN 'Sensors 1-4' "
                + "ELSE 'Sensors 5-6' END, enabled = TRUE");
        for (String group : new String[]{"settings-group-1", "settings-group-2"}) {
            String[] hours = {"08:00", "09:00", "10:00", "11:00", "12:30", "13:30", "14:30", "15:30"};
            int[] plans = {337, 450, 450, 450, 450, 450, 450, 337};
            for (int index = 0; index < hours.length; index++) {
                jdbc.update("INSERT INTO interval_settings(settings_group_id, order_index, start_time, plan) "
                        + "VALUES (?, ?, CAST(? AS TIME), ?)", group, index, hours[index], plans[index]);
            }
        }
    }
}
