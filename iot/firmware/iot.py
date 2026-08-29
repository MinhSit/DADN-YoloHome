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
# THRESHOLDS / CURRENT SENSOR VALUES
# ==================================================

temperature_threshold = None
humidity_threshold = None
light_threshold = None

current_temperature = None
current_light = None


# ==================================================
# FAN MANUAL OVERRIDE
# ==================================================

fan_override_active = False
fan_override_rearmed = False
fan_manual_state = False


# ==================================================
# LED MANUAL OVERRIDE
# ==================================================

led_override_active = False
led_override_rearmed = False
led_manual_state = False


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
# FAN AUTO
#
# temp > threshold -> FAN ON
# temp < threshold -> FAN OFF
# equal            -> HOLD
# ==================================================

def get_fan_auto_desired(temp):

    if temperature_threshold is None:
        return None

    if temp > temperature_threshold:
        return True

    if temp < temperature_threshold:
        return False

    return None


def evaluate_fan_auto(temp):
    global fan_override_active
    global fan_override_rearmed

    desired = get_fan_auto_desired(temp)

    if desired is None:
        return

    # Manual override đang active
    if fan_override_active:

        # Auto phải đi qua phía giống manual trước
        if not fan_override_rearmed:

            if desired == fan_manual_state:
                fan_override_rearmed = True
                print("FAN OVERRIDE -> REARMED")

            return

        # Đã re-arm, khi auto đổi sang phía ngược manual
        # thì trả quyền lại cho AUTO
        if desired != fan_manual_state:
            fan_override_active = False
            fan_override_rearmed = False

            print("FAN OVERRIDE -> RELEASED")
            set_fan(desired, "AUTO")

        return

    # Không có manual override
    set_fan(desired, "AUTO")


# ==================================================
# LED AUTO
#
# light < threshold -> LED ON
# light > threshold -> LED OFF
# equal             -> HOLD
# ==================================================

def get_led_auto_desired(light):

    if light_threshold is None:
        return None

    if light < light_threshold:
        return True

    if light > light_threshold:
        return False

    return None


def evaluate_led_auto(light):
    global led_override_active
    global led_override_rearmed

    desired = get_led_auto_desired(light)

    if desired is None:
        return

    # Manual override đang active
    if led_override_active:

        # Auto phải đi qua phía giống manual trước
        if not led_override_rearmed:

            if desired == led_manual_state:
                led_override_rearmed = True
                print("LED OVERRIDE -> REARMED")

            return

        # Đã re-arm, auto đổi sang phía ngược manual
        # thì trả quyền lại cho AUTO
        if desired != led_manual_state:
            led_override_active = False
            led_override_rearmed = False

            print("LED OVERRIDE -> RELEASED")
            set_led(desired, "AUTO")

        return

    # Không có manual override
    set_led(desired, "AUTO")


# ==================================================
# SIMPLE JSON NUMBER PARSER
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
# Runtime commands use V12.
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
# RETAINED THRESHOLDS
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


# ==================================================
# V12
# NON-RETAINED MANUAL ACTION EVENT
# ==================================================

def on_manual(msg):
    global fan_override_active
    global fan_override_rearmed
    global fan_manual_state

    global led_override_active
    global led_override_rearmed
    global led_manual_state

    print("V12 RECEIVED:", msg)

    clean_msg = msg.replace(" ", "")


    # ------------------------------------------------
    # MANUAL LIGHT
    # ------------------------------------------------

    if '"target":"light"' in clean_msg:

        if '"state":true' in clean_msg:
            manual_state = True

        elif '"state":false' in clean_msg:
            manual_state = False

        else:
            return

        # Manual phải tác động ngay
        set_led(manual_state, "MANUAL")

        led_manual_state = manual_state

        desired = None

        if current_light is not None:
            desired = get_led_auto_desired(current_light)

        # Manual khác AUTO hiện tại
        # -> giữ manual override
        if desired is None or desired != manual_state:

            led_override_active = True
            led_override_rearmed = False

            print(
                "LED OVERRIDE -> ACTIVE",
                "manual=",
                manual_state
            )

        else:
            # Manual trùng AUTO
            # -> không cần override
            led_override_active = False
            led_override_rearmed = False

            print("LED OVERRIDE -> NOT NEEDED")

        return


    # ------------------------------------------------
    # MANUAL FAN
    # ------------------------------------------------

    if '"target":"fan"' in clean_msg:

        if '"state":true' in clean_msg:
            manual_state = True

        elif '"state":false' in clean_msg:
            manual_state = False

        else:
            return

        # Manual phải tác động ngay
        set_fan(manual_state, "MANUAL")

        fan_manual_state = manual_state

        desired = None

        if current_temperature is not None:
            desired = get_fan_auto_desired(
                current_temperature
            )

        # Manual khác AUTO hiện tại
        # -> giữ manual override
        if desired is None or desired != manual_state:

            fan_override_active = True
            fan_override_rearmed = False

            print(
                "FAN OVERRIDE -> ACTIVE",
                "manual=",
                manual_state
            )

        else:
            # Manual trùng AUTO
            # -> không cần override
            fan_override_active = False
            fan_override_rearmed = False

            print("FAN OVERRIDE -> NOT NEEDED")


# ==================================================
# MQTT SUBSCRIPTIONS
# ==================================================

mqtt.on_receive_message('V10', on_command)
mqtt.on_receive_message('V11', on_config)
mqtt.on_receive_message('V12', on_manual)


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
    # AUTO CONTROL
    # ------------------------------

    evaluate_fan_auto(temperature)
    evaluate_led_auto(light)


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
        '"fan_state":' + fan_json + '}'
    )

    print(payload)

    mqtt.publish('V6', payload)


    # ------------------------------
    # WAIT 5s BUT KEEP MQTT RESPONSIVE
    # ------------------------------

    for i in range(50):
        mqtt.check_message()
        time.sleep_ms(100)