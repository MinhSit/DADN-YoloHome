# DADN YoloHome

DADN YoloHome is a Smart Home integration project built with Yolo:Bit/Yolo:Home, MQTT, Spring Boot, PostgreSQL, and a frontend dashboard.

This repository brings together the backend, frontend, public archival IoT firmware, MQTT-to-PostgreSQL subscriber, and database-related assets.

## Architecture

```text
Yolo:Bit
  -> MQTT broker
  -> Python subscriber -> PostgreSQL
  -> Spring Boot backend
  -> Frontend dashboard
```

Command and control flow:

```text
Frontend
  -> Backend
  -> MQTT V10/V11/V12
  -> Yolo:Bit
  -> LED / Fan
```

## Project Structure

```text
DADN-YoloHome/
├── backend/                  Spring Boot backend
├── frontend/                 Web dashboard
├── iot/
│   ├── firmware/iot.py       Public archival firmware source
│   └── subscriber/           MQTT-to-PostgreSQL subscriber
├── database/                 Database-related assets
├── DEMO.md                   Verified demo runbook
└── README.md
```

## Features

- Temperature, humidity, and ambient-light telemetry
- Realtime sensor cards
- LED and fan control
- Threshold automation and manual override
- PostgreSQL persistence

## Security

- Do not commit database passwords or Wi-Fi credentials.
- `DB_PASSWORD` is provided through the runtime environment.
- `iot/firmware/iot.py` uses `YOUR_WIFI_SSID` and `YOUR_WIFI_PASSWORD` placeholders.

## Quick Start

See [DEMO.md](DEMO.md) for the complete verified run procedure.

## Testing Status

- Final smoke test: **PASS**
- Telemetry to PostgreSQL: **PASS**
- Frontend login: **PASS**
- Realtime sensor refresh: **PASS**
- LED ON/OFF: **PASS**
- Fan ON/OFF: **PASS**
- Manual override over Auto: **PASS**
- LED AUTO re-arm after threshold crossing: **PASS**
- Fan AUTO re-arm after temperature threshold crossing: **NOT TESTED**

This repository documents a verified project demo workflow. It does not claim production readiness or a cloud deployment.
