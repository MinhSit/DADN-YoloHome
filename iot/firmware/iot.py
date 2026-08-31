from yolobit import *
from aiot_dht20 import DHT20
from aiot_rgbled import RGBLed
from mqtt import *
WIFI_SSID = "YOUR_WIFI_SSID"
WIFI_PASSWORD = "YOUR_WIFI_PASSWORD"
import time


# ==================================================
# HARDWARE
# ==================================================

dht = DHT20()
rgb = RGBLed(pin0.pin, 4)

led_state = False
fan_state = False


# ==================================================
# CONTROL MODE
#
# MANUAL: V12 actuator commands are allowed.
# AUTO:   sensor rules control actuators; V12 is ignored.
# ==================================================

system_mode = "MANUAL"


# ==================================================
# THRESHOLDS / CURRENT SENSOR VALUES
# ==================================================

temperature_threshold = None
humidity_threshold = None
light_threshold = None

current_temperature = None
current_light = None


# ==================================================
# V10: ONLY RESTORE ON STARTUP
# ==================================================

v10_restore_done = False


# ==================================================
# SAFE STATE BEFORE MQTT RETAINED CALLBACKS
# ==================================================

rgb.off(0)
pin10.write_analog(0)


# ==================================================
# OUTPUT HELPERS
# ==================================================

def set_led(state, source):
    global led_state

    if state:
        rgb.show(0, (255, 0, 0))
    else:
        rgb.off(0)

    if led_state != state:
        led_state = state
        print(
            "LED ->",
            "ON" if state else "OFF",
            "[" + source + "]"
        )


def set_fan(state, source):
    global fan_state

    if state:
        # Runtime-tested PWM range is 0..1023
        # 512 ~= 50%
        pin10.write_analog(512)
    else:
        pin10.write_analog(0)

    if fan_state != state:
        fan_state = state
        print(
            "FAN ->",
            "ON" if state else "OFF",
            "[" + source + "]"
        )


# ==================================================
# AUTO RULES
# ==================================================

def get_fan_auto_desired(temp):

    if temperature_threshold is None:
        return None

    if temp > temperature_threshold:
        return True

    if temp < temperature_threshold:
        return False

    return None


def get_led_auto_desired(light):

    if light_threshold is None:
        return None

    if light < light_threshold:
        return True

    if light > light_threshold:
        return False

    return None


def evaluate_auto_control():

    if system_mode != "AUTO":
        return

    if current_temperature is not None:
        fan_desired = get_fan_auto_desired(current_temperature)

        if fan_desired is not None:
            set_fan(fan_desired, "AUTO")

    if current_light is not None:
        led_desired = get_led_auto_desired(current_light)

        if led_desired is not None:
            set_led(led_desired, "AUTO")


# ==================================================
# SIMPLE JSON PARSERS
# ==================================================

def extract_number(msg, key):
    clean = msg.replace(" ", "")
    marker = '"' + key + '":'

    start = clean.find(marker)

    if start == -1:
        return None

    start = start + len(marker)
    end = start

    while end < len(clean):
        if clean[end] == ',' or clean[end] == '}':
            break
        end += 1

    try:
        return float(clean[start:end])
    except:
        return None


def extract_string(msg, key):
    clean = msg.replace(" ", "")
    marker = '"' + key + '":"'

    start = clean.find(marker)

    if start == -1:
        return None

    start = start + len(marker)
    end = clean.find('"', start)

    if end == -1:
        return None

    return clean[start:end]


# ==================================================
# MODE CONTROL
# ==================================================

def set_system_mode(new_mode, source):
    global system_mode

    if new_mode != "MANUAL" and new_mode != "AUTO":
        print("MODE -> INVALID:", new_mode)
        return

    if system_mode == new_mode:
        print("MODE ->", system_mode, "[UNCHANGED]")
        return

    old_mode = system_mode
    system_mode = new_mode

    print(
        "MODE ->",
        old_mode,
        "=>",
        system_mode,
        "[" + source + "]"
    )

    # Entering AUTO must immediately evaluate current sensors.
    if system_mode == "AUTO":
        evaluate_auto_control()


# ==================================================
# WIFI + MQTT
# ==================================================

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


# ==================================================
# V10
# RETAINED FULL ACTUATOR SNAPSHOT
#
# Only restore ONCE after startup.
# Runtime manual commands use V12.
# ==================================================

def on_command(msg):
    global v10_restore_done

    print("V10 RECEIVED:", msg)

    if v10_restore_done:
        print("V10 -> runtime snapshot ignored")
        return

    clean_msg = msg.replace(" ", "")

    if '"led_state":true' in clean_msg:
        set_led(True, "RESTORE")

    elif '"led_state":false' in clean_msg:
        set_led(False, "RESTORE")

    if '"fan_state":true' in clean_msg:
        set_fan(True, "RESTORE")

    elif '"fan_state":false' in clean_msg:
        set_fan(False, "RESTORE")

    v10_restore_done = True
    print("V10 STARTUP RESTORE -> DONE")


# ==================================================
# V11
# RETAINED CONTROL CONFIG
#
# Existing thresholds remain supported.
# Optional field:
#   "mode": "MANUAL" | "AUTO"
# ==================================================

def on_config(msg):
    global temperature_threshold
    global humidity_threshold
    global light_threshold

    print("V11 RECEIVED:", msg)

    temp_value = extract_number(
        msg,
        "temperature_threshold"
    )

    humidity_value = extract_number(
        msg,
        "humidity_threshold"
    )

    light_value = extract_number(
        msg,
        "light_threshold"
    )

    mode_value = extract_string(
        msg,
        "mode"
    )

    if temp_value is not None:
        temperature_threshold = temp_value

    if humidity_value is not None:
        humidity_threshold = humidity_value

    if light_value is not None:
        light_threshold = light_value

    print(
        "THRESHOLDS ->",
        temperature_threshold,
        humidity_threshold,
        light_threshold
    )

    if mode_value is not None:
        set_system_mode(mode_value.upper(), "CONFIG")


# ==================================================
# V12
# NON-RETAINED MANUAL ACTION EVENT
#
# Accepted only in MANUAL mode.
# ==================================================

def on_manual(msg):

    print("V12 RECEIVED:", msg)

    if system_mode != "MANUAL":
        print("V12 -> IGNORED, MODE=AUTO")
        return

    clean_msg = msg.replace(" ", "")

    if '"state":true' in clean_msg:
        manual_state = True

    elif '"state":false' in clean_msg:
        manual_state = False

    else:
        print("V12 -> INVALID STATE")
        return

    if '"target":"light"' in clean_msg:
        set_led(manual_state, "WEB")
        return

    if '"target":"fan"' in clean_msg:
        set_fan(manual_state, "WEB")
        return

    print("V12 -> INVALID TARGET")


# ==================================================
# MQTT SUBSCRIPTIONS
# ==================================================

mqtt.on_receive_message('V10', on_command)
mqtt.on_receive_message('V11', on_config)
mqtt.on_receive_message('V12', on_manual)


# ==================================================
# STARTUP STATUS
# ==================================================

print("CONTROL MODE ->", system_mode)


# ==================================================
# MAIN LOOP
# ==================================================

while True:

    # Nhận MQTT nếu có
    mqtt.check_message()

    # ------------------------------
    # SENSOR READ
    # ------------------------------

    dht.read_dht20()

    temperature = dht.dht20_temperature()
    humidity = dht.dht20_humidity()

    current_temperature = temperature

    light = round(
        translate(
            pin2.read_analog(),
            0,
            4095,
            0,
            100
        )
    )

    current_light = light


    # ------------------------------
    # CONTROL
    # ------------------------------

    evaluate_auto_control()


    # ------------------------------
    # TELEMETRY JSON
    # ------------------------------

    led_json = "true" if led_state else "false"
    fan_json = "true" if fan_state else "false"

    payload = (
        '{"device_id":"yolohome-01",'
        '"temperature":' + str(temperature) + ','
        '"humidity":' + str(humidity) + ','
        '"light":' + str(light) + ','
        '"led_state":' + led_json + ','
        '"fan_state":' + fan_json + ','
        '"mode":"' + system_mode + '"}'
    )

    print(payload)

    mqtt.publish('V6', payload)


    # ------------------------------
    # WAIT 5s BUT KEEP MQTT RESPONSIVE
    # ------------------------------

    for i in range(50):
        mqtt.check_message()
        time.sleep_ms(100)
