UPDATE sensors
SET settings_group_id = CASE
    WHEN id IN ('sensor-1', 'sensor-2', 'sensor-3', 'sensor-4', 'sensor-5') THEN 'settings-group-1'
    WHEN id = 'sensor-6' THEN 'settings-group-2'
END
WHERE id IN ('sensor-1', 'sensor-2', 'sensor-3', 'sensor-4', 'sensor-5', 'sensor-6');

UPDATE settings_groups
SET name = CASE id
    WHEN 'settings-group-1' THEN 'Sensors 1-4 shared plan'
    WHEN 'settings-group-2' THEN 'Sensor 6 independent plan'
END,
enabled = TRUE
WHERE id IN ('settings-group-1', 'settings-group-2');

DELETE FROM interval_settings
WHERE settings_group_id IN ('settings-group-1', 'settings-group-2');

WITH approved_rows(order_index, start_time, shared_plan, sensor6_plan) AS (
    VALUES
        (0,  TIME '07:00', 450, 1600),
        (1,  TIME '08:00', 600, 1920),
        (2,  TIME '09:00', 500, 1600),
        (3,  TIME '10:00', 600, 1920),
        (4,  TIME '11:30', 600, 1920),
        (5,  TIME '12:30', 600, 1920),
        (6,  TIME '13:30', 500, 1600),
        (7,  TIME '14:30', 600,  960),
        (8,  TIME '15:00', 300, 1920),
        (9,  TIME '16:00', 600, 1920),
        (10, TIME '17:00', 600, 1600),
        (11, TIME '18:00', 500, 1920),
        (12, TIME '19:00', 600, 1920),
        (13, TIME '20:30', 600, 1920),
        (14, TIME '21:30', 500, 1600),
        (15, TIME '22:30', 300,  960)
)
INSERT INTO interval_settings(settings_group_id, order_index, start_time, plan)
SELECT 'settings-group-1', order_index, start_time, shared_plan FROM approved_rows
UNION ALL
SELECT 'settings-group-2', order_index, start_time, sensor6_plan FROM approved_rows;
