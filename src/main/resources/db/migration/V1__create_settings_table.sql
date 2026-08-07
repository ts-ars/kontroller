-- ⚙️ Настройки системы
DROP TABLE IF EXISTS settings;
CREATE TABLE settings (
                          setting_key VARCHAR PRIMARY KEY,
                          setting_value VARCHAR NOT NULL
);

-- 👇 JSON-массивы, готовые к парсингу через ObjectMapper
INSERT INTO settings (setting_key, setting_value) VALUES
                                                      ('hours', '["08:00","09:00","10:00","11:00","12:30","13:30","14:30","15:30"]'),
                                                      ('hourlyPlans', '[337,450,450,450,450,450,450,337]');

-- 📅 Основная таблица смен
DROP TABLE IF EXISTS shift;
CREATE TABLE shift (
                       id BIGSERIAL PRIMARY KEY,
                       date DATE UNIQUE NOT NULL,
                       actual INTEGER
);

-- 📊 Почасовой план
CREATE TABLE shift_hourly_plan (
                                   shift_id BIGINT NOT NULL,
                                   order_index INTEGER NOT NULL,         -- 💥 добавлено
                                   value INTEGER,
                                   PRIMARY KEY (shift_id, order_index),  -- 💥 порядок гарантирован
                                   FOREIGN KEY (shift_id) REFERENCES shift(id) ON DELETE CASCADE
);

-- 📈 Почасовой факт
CREATE TABLE shift_hourly_actual (
                                     shift_id BIGINT NOT NULL,
                                     order_index INTEGER NOT NULL,         -- 💥 добавлено
                                     value INTEGER,
                                     PRIMARY KEY (shift_id, order_index),  -- 💥 порядок гарантирован
                                     FOREIGN KEY (shift_id) REFERENCES shift(id) ON DELETE CASCADE
);

-- ⏱️ Метки времени (почасовые метки)
CREATE TABLE shift_hour_labels (
                                   shift_id BIGINT NOT NULL,
                                   order_index INTEGER NOT NULL,
                                   label VARCHAR(255) NOT NULL,
                                   PRIMARY KEY (shift_id, order_index),  -- 💥 критически важно
                                   FOREIGN KEY (shift_id) REFERENCES shift(id) ON DELETE CASCADE
);


-- 💬 Комментарии к смене
DROP TABLE IF EXISTS comments;
CREATE TABLE comments (
                          id BIGSERIAL PRIMARY KEY,
                          shift_id BIGINT NOT NULL,
                          message TEXT,
                          type VARCHAR(32),
                          FOREIGN KEY (shift_id) REFERENCES shift(id)
);

-- ⛔ Автоматически вычисляемые остановки
DROP TABLE IF EXISTS stoppages;
CREATE TABLE stoppages (
                           id BIGSERIAL PRIMARY KEY,
                           shift_id BIGINT NOT NULL,
                           hour_index INTEGER NOT NULL,
                           minutes INTEGER NOT NULL,
                           cans INTEGER NOT NULL,
                           type VARCHAR(32),
                           reason TEXT,
                           minute_offset INTEGER DEFAULT 0,
                           CONSTRAINT fk_shift FOREIGN KEY (shift_id) REFERENCES shift(id) ON DELETE CASCADE
);

-- 📡 Сигналы от сенсора (для расчёта потерь)
DROP TABLE IF EXISTS signals;
CREATE TABLE signals (
                         id UUID PRIMARY KEY,
                         timestamp TIMESTAMP NOT NULL
);
