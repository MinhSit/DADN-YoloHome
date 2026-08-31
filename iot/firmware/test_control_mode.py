# Logic-only test for YoloHome MANUAL/AUTO control model v2.
# No sensor, actuator, Wi-Fi, or MQTT module is required.
# Run this file and use the console output as evidence.

MODE_MANUAL = "MANUAL"
MODE_AUTO = "AUTO"

system_mode = MODE_MANUAL
led_state = False
fan_state = False


def set_mode(mode):
    global system_mode

    if mode != MODE_MANUAL and mode != MODE_AUTO:
        print("MODE REJECTED:", mode)
        return False

    system_mode = mode
    print("MODE ->", system_mode)
    return True


def manual_command(target, state):
    global led_state
    global fan_state

    if system_mode != MODE_MANUAL:
        print("MANUAL COMMAND IGNORED:", target, state, "mode=", system_mode)
        return False

    if target == "light":
        led_state = state
        print("LED ->", "ON" if led_state else "OFF", "[WEB]")
        return True

    if target == "fan":
        fan_state = state
        print("FAN ->", "ON" if fan_state else "OFF", "[WEB]")
        return True

    print("MANUAL COMMAND REJECTED: unknown target", target)
    return False


def auto_control(light_value, light_threshold, temp_value, temp_threshold):
    global led_state
    global fan_state

    if system_mode != MODE_AUTO:
        print("AUTO SKIPPED: mode=", system_mode)
        return False

    if light_value < light_threshold:
        led_state = True
    elif light_value > light_threshold:
        led_state = False

    if temp_value > temp_threshold:
        fan_state = True
    elif temp_value < temp_threshold:
        fan_state = False

    print(
        "AUTO APPLIED:",
        "LED=" + ("ON" if led_state else "OFF"),
        "FAN=" + ("ON" if fan_state else "OFF")
    )
    return True


def check(test_id, condition):
    if condition:
        print(test_id, "OK")
        return True

    print(test_id, "FAILED")
    return False


print("=== YoloHome Control Mode v2 logic test ===")
print("BOOT MODE ->", system_mode)

results = []

# LOGIC-01: default mode is MANUAL
results.append(check("LOGIC-01", system_mode == MODE_MANUAL))

# LOGIC-02: manual LED command is accepted in MANUAL
accepted = manual_command("light", True)
results.append(check("LOGIC-02", accepted and led_state is True))

# LOGIC-03: manual fan command is accepted in MANUAL
accepted = manual_command("fan", True)
results.append(check("LOGIC-03", accepted and fan_state is True))

# LOGIC-04: AUTO control must not change actuator state while in MANUAL
before_led = led_state
before_fan = fan_state
auto_ran = auto_control(
    light_value=90,
    light_threshold=30,
    temp_value=20,
    temp_threshold=30
)
results.append(
    check(
        "LOGIC-04",
        (not auto_ran)
        and led_state == before_led
        and fan_state == before_fan
    )
)

# LOGIC-05: switch to AUTO
results.append(check("LOGIC-05", set_mode(MODE_AUTO) and system_mode == MODE_AUTO))

# LOGIC-06: manual command is ignored in AUTO
before_led = led_state
accepted = manual_command("light", False)
results.append(check("LOGIC-06", (not accepted) and led_state == before_led))

# LOGIC-07: AUTO controls actuators in AUTO mode
# dark -> LED ON, cool -> FAN OFF
auto_ran = auto_control(
    light_value=10,
    light_threshold=30,
    temp_value=20,
    temp_threshold=30
)
results.append(
    check(
        "LOGIC-07",
        auto_ran and led_state is True and fan_state is False
    )
)

# LOGIC-08: AUTO -> MANUAL holds current state until a manual command
before_led = led_state
before_fan = fan_state
changed = set_mode(MODE_MANUAL)
results.append(
    check(
        "LOGIC-08A",
        changed
        and system_mode == MODE_MANUAL
        and led_state == before_led
        and fan_state == before_fan
    )
)

# After returning to MANUAL, manual command works again.
accepted = manual_command("fan", True)
results.append(check("LOGIC-08B", accepted and fan_state is True))

# LOGIC-09: invalid mode must be rejected and current mode preserved.
old_mode = system_mode
changed = set_mode("INVALID")
results.append(check("LOGIC-09", (not changed) and system_mode == old_mode))

print("----------------------------------------")
print("RESULT:", sum(1 for item in results if item), "/", len(results), "checks OK")
print("NOTE: Runtime PASS is only confirmed after this output is observed on the actual device/console.")
