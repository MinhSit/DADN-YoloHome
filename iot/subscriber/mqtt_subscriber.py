import os
import json

import paho.mqtt.client as mqtt
import psycopg


# =========================
# MQTT CONFIG
# =========================

BROKER = os.getenv("MQTT_BROKER", "mqtt.ohstem.vn")
PORT = int(os.getenv("MQTT_PORT", "1883"))
USERNAME = os.getenv("MQTT_USERNAME", "YoloHome1024")
PASSWORD = os.getenv("MQTT_PASSWORD", "")
TOPIC = os.getenv("MQTT_TOPIC", "YoloHome1024/feeds/V6")


# =========================
# POSTGRESQL CONFIG
# =========================

DB_HOST = os.getenv("DB_HOST", "localhost")
DB_PORT = int(os.getenv("DB_PORT", "5432"))
DB_NAME = os.getenv("DB_NAME", "yolohome")
DB_USER = os.getenv("DB_USER", "postgres")
DB_PASSWORD = os.getenv("DB_PASSWORD")

if not DB_PASSWORD:
    raise RuntimeError(
        "DB_PASSWORD is not set. Set the environment variable before running."
    )


conn = psycopg.connect(
    host=DB_HOST,
    port=DB_PORT,
    dbname=DB_NAME,
    user=DB_USER,
    password=DB_PASSWORD
)

print("PostgreSQL connected")


def on_connect(client, userdata, flags, reason_code, properties):
    print("MQTT connected:", reason_code)
    client.subscribe(TOPIC)
    print("Subscribed to:", TOPIC)


def on_message(client, userdata, msg):
    payload = msg.payload.decode("utf-8")

    print("Raw payload:", payload)

    data = json.loads(payload)

    device_id = data["device_id"]
    temperature = data["temperature"]
    humidity = data["humidity"]
    light = data["light"]
    led_state = data["led_state"]
    fan_state = data["fan_state"]

    with conn.cursor() as cur:
        cur.execute(
            """
            INSERT INTO sensor_readings (
                device_id,
                temperature,
                humidity,
                light,
                led_state,
                fan_state
            )
            VALUES (%s, %s, %s, %s, %s, %s)
            """,
            (
                device_id,
                temperature,
                humidity,
                light,
                led_state,
                fan_state
            )
        )

    conn.commit()

    print("Inserted into PostgreSQL")
    print(
        device_id,
        temperature,
        humidity,
        light,
        led_state,
        fan_state
    )
    print("-" * 50)


client = mqtt.Client(
    mqtt.CallbackAPIVersion.VERSION2
)

client.username_pw_set(
    USERNAME,
    PASSWORD
)

client.on_connect = on_connect
client.on_message = on_message

client.connect(
    BROKER,
    PORT,
    60
)

print("Waiting for MQTT messages...")
client.loop_forever()