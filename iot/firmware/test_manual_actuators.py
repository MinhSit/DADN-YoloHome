from yolobit import *
from aiot_rgbled import RGBLed
import time


# ==================================================
# HARDWARE USED BY THIS TEST ONLY
# ==================================================

# Current confirmed project mapping from existing firmware:
# RGB LED -> P0
# Mini Fan -> P10

rgb = RGBLed(pin0.pin, 4)


# ==================================================
# SAFE START
# ==================================================

rgb.off(0)
pin10.write_analog(0)

print("=== YoloHome MANUAL actuator hardware test ===")
print("Required hardware: RGB LED on P0, Mini Fan on P10")
print("No DHT20 / light sensor / LCD / PIR / IR / MQTT required")


# ==================================================
# TEST 1 - RGB LED
# ==================================================

print("ACT-01: LED should turn ON RED now")
rgb.show(0, (255, 0, 0))
time.sleep_ms(2000)

print("ACT-02: LED should turn OFF now")
rgb.off(0)
time.sleep_ms(2000)


# ==================================================
# TEST 2 - MINI FAN
# ==================================================

print("ACT-03: FAN should turn ON at about 50% now")
# Runtime-tested range in current project firmware: 0..1023
# 512 ~= 50%
pin10.write_analog(512)
time.sleep_ms(3000)

print("ACT-04: FAN should turn OFF now")
pin10.write_analog(0)
time.sleep_ms(2000)


# ==================================================
# SAFE END
# ==================================================

rgb.off(0)
pin10.write_analog(0)

print("----------------------------------------")
print("TEST SEQUENCE FINISHED")
print("PASS is confirmed only if physical LED/Fan behavior matches ACT-01..ACT-04")
