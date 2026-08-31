# YoloHome Control State Model v2

## 1. Goal

Replace the legacy `AUTO + manual override + re-arm` behavior with two explicit system modes:

```text
MANUAL
AUTO
```

This document defines only the first control-architecture slice. RGB color, fan speed, PIR occupancy, humidity control, IR remote, alerts, and persistent configuration are added later as separate tested increments.

## 2. Mode rules

### MANUAL

- Web/MQTT manual commands are allowed to change LED/Fan state.
- AUTO rules must not change actuator state.
- Default startup mode for this v2 slice is `MANUAL`.
- When entering MANUAL from AUTO, current actuator states are held until a manual command changes them.

### AUTO

- Manual Web/MQTT actuator commands are ignored.
- LED is controlled by the existing light-threshold rule.
- Fan is controlled by the existing temperature-threshold rule.
- When entering AUTO, the firmware immediately re-evaluates current sensor values if they are already available; otherwise AUTO evaluates on the next sensor loop.

## 3. Command source model

Current sources in this slice:

```text
WEB
AUTO
SYSTEM
RESTORE
CONFIG
```

Planned later:

```text
IR
```

## 4. MQTT contract for this slice

### V10 - retained actuator startup snapshot

Existing behavior is preserved for compatibility.

Example:

```json
{
  "led_state": true,
  "fan_state": false
}
```

### V11 - retained control configuration

Existing threshold fields remain supported. An optional `mode` field is added.

Example:

```json
{
  "temperature_threshold": 30,
  "humidity_threshold": 70,
  "light_threshold": 30,
  "mode": "AUTO"
}
```

Mode-only payload is also valid:

```json
{
  "mode": "MANUAL"
}
```

Accepted values:

```text
MANUAL
AUTO
```

Invalid mode values must not change the current mode.

### V12 - non-retained manual actuator event

Existing payload is preserved:

```json
{
  "target": "light",
  "state": true
}
```

or:

```json
{
  "target": "fan",
  "state": false
}
```

Behavior:

```text
MODE=MANUAL -> apply command
MODE=AUTO   -> ignore command
```

### V6 - telemetry

The existing telemetry fields are preserved and `mode` is added as reported state.

## 5. State transitions

```text
MANUAL -- mode=AUTO --> AUTO
AUTO   -- mode=MANUAL --> MANUAL
```

### MANUAL -> AUTO

1. Set system mode to AUTO.
2. If current temperature exists, evaluate fan AUTO rule.
3. If current light exists, evaluate LED AUTO rule.
4. Continue normal AUTO evaluation every sensor loop.

### AUTO -> MANUAL

1. Set system mode to MANUAL.
2. Stop AUTO actuator changes.
3. Hold current LED/Fan physical state.
4. Wait for explicit manual command.

## 6. Legacy behavior removed

The following variables/behavior are superseded by explicit modes:

```text
fan_override_active
fan_override_rearmed
fan_manual_state
led_override_active
led_override_rearmed
led_manual_state
manual override re-arm/release logic
```

Legacy `Fan AUTO re-arm` remains `NOT TESTED / SUPERSEDED`; it is not converted to PASS.

## 7. Phase 1 test plan

All tests start as `NOT TESTED` until runtime evidence exists.

| ID | Test | Expected |
|---|---|---|
| MODE-01 | Boot firmware | System reports `MANUAL` default mode |
| MODE-02 | MANUAL + Web LED ON/OFF | LED follows V12 command |
| MODE-03 | MANUAL + Web Fan ON/OFF | Fan follows V12 command |
| MODE-04 | MANUAL while sensor crosses threshold | AUTO does not change LED/Fan |
| MODE-05 | Publish `mode=AUTO` | Mode changes to AUTO |
| MODE-06 | AUTO + threshold conditions | LED/Fan follow AUTO rules |
| MODE-07 | Send V12 while AUTO | Command is logged as ignored and actuator remains AUTO-controlled |
| MODE-08 | AUTO -> MANUAL | Current actuator state is held; subsequent V12 works |

## 8. PASS rule

No test is PASS from code review alone. Evidence must come from actual runtime, such as:

- Yolo:Bit console output;
- physical LED state;
- physical fan state;
- MQTT message/payload;
- optional DB observation where relevant.
