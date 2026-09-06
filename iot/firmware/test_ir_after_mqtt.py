from yolobit import *
from machine import Pin
from aiot_ir_receiver import *
from mqtt import *
import time

WIFI_SSID = "YOUR_WIFI_SSID"
WIFI_PASSWORD = "YOUR_WIFI_PASSWORD"

# Hypothesis test:
# IR standalone works, but production firmware initializes IR before Wi-Fi/MQTT.
# This file connects Wi-Fi/MQTT first, then initializes IR last.

mqtt.connect_wifi(
    WIFI_SSID,
    WIFI_PASSWORD
)

mqtt.connect_broker(
    server='mqtt.ohstem.vn',
    port=1883,
    username='YoloHome1024',
    password=''
)

print("MQTT READY")

# Initialize IR AFTER Wi-Fi/MQTT setup.
ir = IR_RX(Pin(pin1.pin, Pin.IN))

BUTTON_NAMES = {
    IR_REMOTE_A: "A",
    IR_REMOTE_B: "B",
    IR_REMOTE_C: "C",
    IR_REMOTE_D: "D",
    IR_REMOTE_E: "E",
    IR_REMOTE_F: "F",
    IR_REMOTE_UP: "UP",
    IR_REMOTE_DOWN: "DOWN",
    IR_REMOTE_LEFT: "LEFT",
    IR_REMOTE_RIGHT: "RIGHT",
    IR_REMOTE_SETUP: "SETUP",
}

RELEASE_GAP_MS = 220
active_code = None
last_frame_at = None

print("IR READY AFTER MQTT on P1")
print("Press A, B, C, D")

while True:
    mqtt.check_message()

    code = ir.get_code()

    if code is not None:
        now = time.ticks_ms()

        quiet_gap = (
            last_frame_at is None
            or time.ticks_diff(now, last_frame_at) > RELEASE_GAP_MS
        )

        if code != active_code or quiet_gap:
            print("IR ->", BUTTON_NAMES.get(code, "UNKNOWN"), "code=", code)
            active_code = code

        last_frame_at = now
        ir.clear_code()

    time.sleep_ms(20)
