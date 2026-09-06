-- =========================================================
-- YoloHome v2 - variable actuator telemetry fields
-- Database-only migration. Existing columns are preserved.
-- =========================================================

ALTER TABLE sensor_readings
    ADD COLUMN IF NOT EXISTS led_r SMALLINT NOT NULL DEFAULT 255,
    ADD COLUMN IF NOT EXISTS led_g SMALLINT NOT NULL DEFAULT 0,
    ADD COLUMN IF NOT EXISTS led_b SMALLINT NOT NULL DEFAULT 0,
    ADD COLUMN IF NOT EXISTS fan_speed SMALLINT NOT NULL DEFAULT 0,
    ADD COLUMN IF NOT EXISTS control_mode VARCHAR(16) NOT NULL DEFAULT 'MANUAL';

ALTER TABLE sensor_readings
    DROP CONSTRAINT IF EXISTS chk_sensor_readings_led_r,
    ADD CONSTRAINT chk_sensor_readings_led_r CHECK (led_r BETWEEN 0 AND 255),
    DROP CONSTRAINT IF EXISTS chk_sensor_readings_led_g,
    ADD CONSTRAINT chk_sensor_readings_led_g CHECK (led_g BETWEEN 0 AND 255),
    DROP CONSTRAINT IF EXISTS chk_sensor_readings_led_b,
    ADD CONSTRAINT chk_sensor_readings_led_b CHECK (led_b BETWEEN 0 AND 255),
    DROP CONSTRAINT IF EXISTS chk_sensor_readings_fan_speed,
    ADD CONSTRAINT chk_sensor_readings_fan_speed CHECK (fan_speed BETWEEN 0 AND 100),
    DROP CONSTRAINT IF EXISTS chk_sensor_readings_control_mode,
    ADD CONSTRAINT chk_sensor_readings_control_mode CHECK (control_mode IN ('MANUAL', 'AUTO'));

-- Extend the existing PostgreSQL notification payload so downstream
-- consumers can observe the effective variable actuator state.
CREATE OR REPLACE FUNCTION notify_sensor_reading_inserted()
RETURNS TRIGGER AS $$
BEGIN
    PERFORM pg_notify(
        'sensor_reading_inserted',
        json_build_object(
            'id', NEW.id,
            'device_id', NEW.device_id,
            'recorded_at', NEW.recorded_at,
            'temperature', NEW.temperature,
            'humidity', NEW.humidity,
            'light', NEW.light,
            'led_state', NEW.led_state,
            'led_r', NEW.led_r,
            'led_g', NEW.led_g,
            'led_b', NEW.led_b,
            'fan_state', NEW.fan_state,
            'fan_speed', NEW.fan_speed,
            'control_mode', NEW.control_mode
        )::text
    );
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;
