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

# NEC remotes commonly emit repeat/full frames while the same physical button
# is still held. Treat a same-button frame as a new press only after a quiet gap.
RELEASE_GAP_MS = 220

print("=== YoloHome IR Remote Test ===")
print("IR receiver: P1")
print("Point remote directly at receiver and press buttons")
print("Try A, B, C, D, UP, DOWN")
print("Same-button re-arm gap:", RELEASE_GAP_MS, "ms")

active_code = None
last_frame_at = None

while True:
    code = ir.get_code()

    if code is not None:
        now = time.ticks_ms()

        quiet_gap = (
            last_frame_at is None
            or time.ticks_diff(now, last_frame_at) > RELEASE_GAP_MS
        )

        # Different button: accept immediately.
        # Same button: only accept again after the remote has been quiet long
        # enough to infer that the previous physical press was released.
        if code != active_code or quiet_gap:
            name = BUTTON_NAMES.get(code, "UNKNOWN")
            print("IR ->", name, "code=", code)
            active_code = code

        # Update on every decoded frame, including suppressed repeats.
        # While a button is held, repeat frames keep moving this timestamp,
        # so the same press cannot retrigger the action.
        last_frame_at = now
        ir.clear_code()

    time.sleep_ms(20)
