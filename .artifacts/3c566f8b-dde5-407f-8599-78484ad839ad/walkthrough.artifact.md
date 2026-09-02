# Walkthrough: 2-Channel Relay & Sanitation Cycle Implementation

I have integrated the advanced sanitation pump logic into the ScentGuard ESP32 firmware. This update enables the system to utilize a secondary relay channel for automated deodorization specifically after a hazardous event.

## 1. Hardware Mapping

I have configured the relay channels to the following GPIOs on the ESP32:

- **Relay CH1 (GPIO 23)**: Exhaust/Ventilation Fan.
- **Relay CH2 (GPIO 25)**: Sanitation Pump.

> [!NOTE]
> **GPIO 25** was selected as it is a safe, unused general-purpose IO pin that does not conflict with the MQ135 sensor (GPIO 34) or the onboard indicator LEDs.

## 2. Sanitation Cycle Logic

The system now detects the conclusion of a hazardous event to trigger the sanitation pump.

- **DANGER → SAFE Transition**: When the environment returns to a safe state after being in DANGER, the system automatically starts a **60-second** sanitation cycle.
- **Non-Blocking Operation**: The sanitation timer uses `millis()`. This ensures that Firebase telemetry, Wi-Fi connectivity, and sensor monitoring continue to operate smoothly during the cycle.
- **Safety Interruption**: If the gas concentration rises to **WARN** or **DANGER** levels while the pump is active, the sanitation cycle is immediately aborted, the pump is disabled, and the ventilation fan is activated.

## 3. Relay Polarity

The firmware follows **Active LOW** logic for both channels:
- `digitalWrite(PIN, LOW)`: Relay ON (Fan/Pump Active).
- `digitalWrite(PIN, HIGH)`: Relay OFF (Idle).

## 4. Integration Integrity

I have preserved 100% of the existing production features:
- **BLE/Wi-Fi Provisioning**: The device still supports app-based setup.
- **Cloud Sync**: Remote configuration for thresholds and fan modes remains functional.
- **Telemetry**: The `fanStatus` field in Firestore continues to reflect the physical state of the CH1 ventilation fan.

## Serial Monitor Verification

You will now see descriptive state transitions in the Serial Monitor:

```text
Air Status: DANGER
Relay CH1 (Fan): ON
Relay CH2 (Sanitation Pump): OFF

Air Status: SAFE
Relay CH1 (Fan): OFF
Relay CH2 (Sanitation Pump): ON
Sanitation cycle started (60 seconds)

Sanitation cycle completed
Relay CH2 (Sanitation Pump): OFF
```

The updated firmware is located at [ScentGuard.ino](file:///Users/michaelangelotorre/StudioProjects/ScentGuard_new/firmware/ScentGuard.ino).
