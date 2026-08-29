-- =========================================================
-- Yolohome - schema bo sung cho backend (chay 1 lan tren DB)
-- Khong dong cham den bang sensor_readings da co san.
-- =========================================================

-- ---------- USERS (ho tro nhieu tai khoan) ----------
CREATE TABLE IF NOT EXISTS users (
    id BIGSERIAL PRIMARY KEY,
    email VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    name VARCHAR(255) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- ---------- DEVICE STATE (trang thai mong muon hien tai cua tung device) ----------
-- Day la "single source of truth" cho GET /devices tra ve status on/off,
-- duoc cap nhat moi khi co lenh dieu khien thanh cong qua POST /devices/{id}/command.
CREATE TABLE IF NOT EXISTS device_state (
    device_id VARCHAR(64) PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    device_type VARCHAR(32) NOT NULL DEFAULT 'toggle',
    led_state BOOLEAN NOT NULL DEFAULT FALSE,
    fan_state BOOLEAN NOT NULL DEFAULT FALSE,
    online BOOLEAN NOT NULL DEFAULT TRUE,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- Seed san 1 device mac dinh khop voi Yolo:Bit hien tai
INSERT INTO device_state (device_id, name, device_type)
VALUES ('yolohome-01', 'Yolohome Board', 'toggle')
ON CONFLICT (device_id) DO NOTHING;

-- ---------- ACTIVITY LOGS ----------
CREATE TABLE IF NOT EXISTS activity_logs (
    id BIGSERIAL PRIMARY KEY,
    occurred_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    log_type VARCHAR(32) NOT NULL,      -- 'Device' | 'Alert' | 'System'
    description VARCHAR(500) NOT NULL,
    device VARCHAR(64),
    status VARCHAR(32) NOT NULL         -- 'Success' | 'Warning' | 'Critical'
);

-- ---------- ALERTS ----------
CREATE TABLE IF NOT EXISTS alerts (
    id BIGSERIAL PRIMARY KEY,
    level VARCHAR(32) NOT NULL,         -- 'Info' | 'Warning' | 'Critical'
    message VARCHAR(500) NOT NULL,
    device_id VARCHAR(64),
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    is_read BOOLEAN NOT NULL DEFAULT FALSE
);

-- ---------- SETTINGS (1 hang duy nhat - cau hinh chung ca he thong) ----------
CREATE TABLE IF NOT EXISTS app_settings (
    id SMALLINT PRIMARY KEY DEFAULT 1,
    temperature_threshold REAL NOT NULL DEFAULT 30,
    humidity_threshold REAL NOT NULL DEFAULT 70,
    light_threshold REAL,
    adafruit_username VARCHAR(255),
    adafruit_aio_key VARCHAR(255),
    CONSTRAINT single_row CHECK (id = 1)
);

INSERT INTO app_settings (id) VALUES (1)
ON CONFLICT (id) DO NOTHING;


-- =========================================================
-- TRIGGER: bao cho backend biet moi khi co reading moi
-- Backend se LISTEN kenh 'sensor_reading_inserted' qua JDBC,
-- nhan payload la JSON gon cua ban ghi vua insert, roi tu
-- kiem tra nguong va tao alert/log tuong ung - khong can polling.
-- =========================================================

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
            'fan_state', NEW.fan_state
        )::text
    );
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

DROP TRIGGER IF EXISTS trg_sensor_reading_inserted ON sensor_readings;

CREATE TRIGGER trg_sensor_reading_inserted
AFTER INSERT ON sensor_readings
FOR EACH ROW
EXECUTE FUNCTION notify_sensor_reading_inserted();
