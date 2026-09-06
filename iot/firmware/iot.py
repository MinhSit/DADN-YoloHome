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


# ==================================================
# ACTUATOR STATE
# ==================================================

led_state = False
led_color = (255, 0, 0)

fan_state = False
fan_speed = 0
manual_fan_speed = 50
AUTO_FAN_SPEED = 50


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
# HELPERS
# ==================================================

def clamp_int(value, minimum, maximum):
    value = int(round(value))

    if value < minimum:
        return minimum

    if value > maximum:
        return maximum

    return value


# ==================================================
# OUTPUT HELPERS
# ==================================================

def set_led_color(r, g, b, source):
    global led_color

    new_color = (
        clamp_int(r, 0, 255),
        clamp_int(g, 0, 255),
        clamp_int(b, 0, 255)
    )

    changed = led_color != new_color
    led_color = new_color

    if led_state:
        rgb.show(0, led_color)

    if changed:
        print(
            "LED COLOR ->",
            led_color,
            "[" + source + "]"
        )


def set_led(state, source):
    global led_state

    if state:
        rgb.show(0, led_color)
    else:
        rgb.off(0)

    if led_state != state:
        led_state = state
        print(
            "LED ->",
            "ON" if state else "OFF",
            "[" + source + "]"
        )


def apply_fan_speed(speed, source):
    global fan_state
    global fan_speed

    new_speed = clamp_int(speed, 0, 100)
    new_state = new_speed > 0

    pwm_value = int(round(new_speed * 1023 / 100))
    pin10.write_analog(pwm_value)

    if fan_speed != new_speed or fan_state != new_state:
        fan_speed = new_speed
        fan_state = new_state

        print(
            "FAN ->",
            "ON" if fan_state else "OFF",
            "speed=" + str(fan_speed) + "%",
            "[" + source + "]"
        )


def set_manual_fan_speed(speed, source):
    global manual_fan_speed

    new_speed = clamp_int(speed, 0, 100)

    if new_speed > 0:
        manual_fan_speed = new_speed

    apply_fan_speed(new_speed, source)


def set_fan(state, source):
    if state:
        if source == "AUTO":
            apply_fan_speed(AUTO_FAN_SPEED, source)
        else:
            apply_fan_speed(manual_fan_speed, source)
    else:
        apply_fan_speed(0, source)


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


def extract_bool(msg, key):
    clean = msg.replace(" ", "")

    if '"' + key + '":true' in clean:
        return True

    if '"' + key + '":false' in clean:
        return False

    return None


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

    led_restore = extract_bool(msg, "led_state")
    fan_restore = extract_bool(msg, "fan_state")

    if led_restore is not None:
        set_led(led_restore, "RESTORE")

    if fan_restore is not None:
        set_fan(fan_restore, "RESTORE")

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
# Legacy commands remain valid:
#   {"target":"light","state":true}
#   {"target":"fan","state":true}
#
# Variable actuator commands:
#   {"target":"light","r":0,"g":0,"b":255}
#   {"target":"light","state":true,"r":0,"g":0,"b":255}
#   {"target":"fan","speed":80}
#
# Accepted only in MANUAL mode.
# ==================================================

def on_manual(msg):

    print("V12 RECEIVED:", msg)

    if system_mode != "MANUAL":
        print("V12 -> IGNORED, MODE=AUTO")
        return

    target = extract_string(msg, "target")
    manual_state = extract_bool(msg, "state")

    if target == "light":
        r = extract_number(msg, "r")
        g = extract_number(msg, "g")
        b = extract_number(msg, "b")

        has_any_color = r is not None or g is not None or b is not None

        if has_any_color:
            if r is None or g is None or b is None:
                print("V12 -> INVALID RGB")
                return

            set_led_color(r, g, b, "WEB")

        if manual_state is not None:
            set_led(manual_state, "WEB")
            return

        if has_any_color:
            return

        print("V12 -> INVALID LIGHT COMMAND")
        return

    if target == "fan":
        speed = extract_number(msg, "speed")

        if speed is not None:
            set_manual_fan_speed(speed, "WEB")
            return

        if manual_state is not None:
            set_fan(manual_state, "WEB")
            return

        print("V12 -> INVALID FAN COMMAND")
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

    # Receive MQTT if available.
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
        '"led_r":' + str(led_color[0]) + ','
        '"led_g":' + str(led_color[1]) + ','
        '"led_b":' + str(led_color[2]) + ','
        '"fan_state":' + fan_json + ','
        '"fan_speed":' + str(fan_speed) + ','
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
