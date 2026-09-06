from yolobit import *
from machine import Pin
from aiot_ir_receiver import *
import time


# ==================================================
# IR REMOTE STANDALONE TEST
# Hardware: IR receiver -> P1
# No Wi-Fi / MQTT / DHT20 / RGB / Fan required by this file.
# ==================================================

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
    IR_REMOTE_0: "0",
    IR_REMOTE_1: "1",
    IR_REMOTE_2: "2",
    IR_REMOTE_3: "3",
    IR_REMOTE_4: "4",
    IR_REMOTE_5: "5",
    IR_REMOTE_6: "6",
    IR_REMOTE_7: "7",
    IR_REMOTE_8: "8",
    IR_REMOTE_9: "9",
}

DEBOUNCE_MS = 300

print("=== YoloHome IR Remote Test ===")
print("IR receiver: P1")
print("Point remote directly at receiver and press buttons")
print("Try A, B, C, D, UP, DOWN")
print("Debounce:", DEBOUNCE_MS, "ms")

last_accepted_code = None
last_accepted_at = 0

while True:
    code = ir.get_code()

    if code is not None:
        now = time.ticks_ms()

        same_button_too_soon = (
            code == last_accepted_code
            and time.ticks_diff(now, last_accepted_at) < DEBOUNCE_MS
        )

        if not same_button_too_soon:
            name = BUTTON_NAMES.get(code, "UNKNOWN")
            print("IR ->", name, "code=", code)
            last_accepted_code = code
            last_accepted_at = now

        # Always clear the current decoded frame. NEC repeat/full frames may
        # arrive again while the physical button is still being pressed, so
        # acceptance is controlled by the time gate above instead of by None.
        ir.clear_code()

    time.sleep_ms(20)
