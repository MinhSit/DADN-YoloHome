# DADN YoloHome Demo Runbook

This runbook reproduces the verified local demonstration workflow. Follow the start order exactly and keep both terminal sessions open during the demo.

## A. Prerequisites

- PostgreSQL 18
- Python
- Java; the Maven wrapper is included with the backend
- VS Code with the Live Server extension
- OhStem web editor
- Yolo:Bit with expansion board
- DHT20
- Light sensor
- RGB LED
- Mini fan
- USB cable

## B. Hardware Wiring

| Component | Connection |
| --- | --- |
| DHT20 | I2C2 |
| Light Sensor | P2 |
| RGB LED | P0 |
| Mini Fan | P10/P13, control pin P10 |

Mount the Yolo:Bit on the expansion board with the LED matrix facing upward.

## C. Start Order

1. PostgreSQL
2. Terminal (1): subscriber
3. Terminal (2): backend
4. Frontend Live Server
5. Yolo:Bit / OhStem
6. Final smoke test

## D. Step 1 — PostgreSQL

In PowerShell:

```powershell
Get-Service *postgres*
```

Expected result: `postgresql-x64-18` is `Running`.

If it is not running, start the PostgreSQL service before continuing. Do not place the PostgreSQL password in this repository or in this document.

## E. Step 2 — Terminal (1): MQTT Subscriber

Open PowerShell and run:

```powershell
cd C:\Users\Admin\DADN-YoloHome\iot\subscriber

$secure = Read-Host "PostgreSQL password" -AsSecureString
$env:DB_PASSWORD = [System.Net.NetworkCredential]::new("", $secure).Password

python .\mqtt_subscriber.py
```

Expected output:

```text
PostgreSQL connected
Waiting for MQTT messages...
MQTT connected: Success
Subscribed to: YoloHome1024/feeds/V6
```

Keep Terminal (1) open.

## F. Step 3 — Terminal (2): Backend

Open a second PowerShell terminal and run this working block:

```powershell
cd C:\Users\Admin\DADN-YoloHome\backend

$env:CORS_ALLOWED_ORIGINS="http://localhost:5500,http://127.0.0.1:5500"

$secure = Read-Host "Postgres password" -AsSecureString
$env:DB_PASSWORD = [System.Net.NetworkCredential]::new("", $secure).Password

$env:MQTT_BROKER_URI="tcp://mqtt.ohstem.vn:1883"
$env:MQTT_USERNAME="YoloHome1024"
$env:MQTT_PASSWORD=""
$env:MQTT_COMMAND_TOPIC_PATTERN="YoloHome1024/feeds/V10"
$env:MQTT_CONFIG_TOPIC_PATTERN="YoloHome1024/feeds/V11"
$env:MQTT_MANUAL_TOPIC_PATTERN="YoloHome1024/feeds/V12"

.\mvnw.cmd spring-boot:run
```

The empty MQTT password is the current broker contract; do not replace it with another value.

Keep Terminal (2) open.

## G. Step 4 — Frontend

1. Open the VS Code workspace `C:\Users\Admin\DADN-YoloHome`.
2. Open `frontend\login.html`.
3. Select **Go Live**.

Expected port: `5500`.

Login URL:

[http://127.0.0.1:5500/frontend/login.html](http://127.0.0.1:5500/frontend/login.html)

Dashboard URL after a successful login:

[http://127.0.0.1:5500/frontend/dashboard.html](http://127.0.0.1:5500/frontend/dashboard.html)

Login account: `iot.test@example.com`. Enter the existing demo password locally; do not add it to this repository.

Expected result: login succeeds and redirects to the dashboard. Sensor cards refresh approximately every two seconds, so normal telemetry updates do not require pressing F5.

## H. Step 5 — Yolo:Bit

The working firmware used for the hardware demo is kept and run directly in the OhStem project.

The repository file `iot\firmware\iot.py` is public archival source. It contains the placeholders `YOUR_WIFI_SSID` and `YOUR_WIFI_PASSWORD`; it will not connect to Wi-Fi unchanged.

To run the verified demo:

1. Open the working firmware project in OhStem.
2. Connect the Yolo:Bit over USB.
3. Select **Run**.

Do not copy real Wi-Fi credentials into repository documentation.

Expected Terminal (1) activity:

```text
Raw payload: {...}
Inserted into PostgreSQL
```

Expected payload fields:

- `device_id`
- `temperature`
- `humidity`
- `light`
- `led_state`
- `fan_state`

## I. Step 6 — Final Smoke Test

- [ ] Temperature changes on the dashboard
- [ ] Humidity changes on the dashboard
- [ ] Light changes on the dashboard
- [ ] Telemetry updates without F5
- [ ] Terminal (1) receives `Raw payload`
- [ ] Terminal (1) reports `Inserted into PostgreSQL`
- [ ] Light ON turns the physical LED on
- [ ] Light OFF turns the physical LED off
- [ ] Fan ON turns the physical fan on
- [ ] Fan OFF turns the physical fan off

## J. Auto / Manual Test

### LED

1. Set the light threshold below the current ambient-light value.
2. Confirm AUTO turns the LED off.
3. Select manual **Light ON** and confirm the LED turns on and holds that state.
4. Cross the light threshold and return across it to allow AUTO to re-arm.

Verified behavior: manual control overrides AUTO, and LED AUTO re-arms after a threshold crossing.

### Fan

1. Set the temperature threshold below the current temperature.
2. Confirm AUTO turns the fan on.
3. Select manual **Fan OFF** and confirm the fan remains off while temperature is still above the threshold.

Verified behavior: manual control overrides AUTO.

**Fan AUTO re-arm after temperature threshold crossing: NOT TESTED.** A real temperature crossing needed for this test was not produced; do not record this behavior as PASS.

## K. Database Evidence

The source schema confirms `sensor_readings` as the telemetry table and `recorded_at` as its timestamp column. Run this safe query in the `yolohome` database:

```sql
SELECT *
FROM sensor_readings
ORDER BY recorded_at DESC
LIMIT 10;
```

Verify that recent rows correspond to payloads reported by Terminal (1). Do not change the database schema during the demo.

## L. Shutdown Order

1. OhStem: select **Stop**.
2. VS Code: stop Live Server.
3. Terminal (2), backend: press `Ctrl+C`.
4. Terminal (1), subscriber: press `Ctrl+C`.

PostgreSQL may remain running.

## M. Troubleshooting Quick Notes

### Subscriber reports `DB_PASSWORD is not set`

Enter `DB_PASSWORD` through the `Read-Host` block in Step 2, then start the subscriber again.

### Dashboard sensor values do not change

- Confirm the backend, Yolo:Bit firmware, and subscriber are running.
- Sensor cards poll approximately every two seconds.
- F5 is only a diagnostic step, not normal operation.

### An actuator does not respond

- Inspect the OhStem console and MQTT behavior.
- Check the V12 manual-command path.
- Test one actuator at a time.
