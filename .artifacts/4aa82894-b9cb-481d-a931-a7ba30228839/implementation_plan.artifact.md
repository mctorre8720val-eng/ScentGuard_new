# Implementation Plan - ESP32 Performance & Serial Monitoring Optimization

This plan focuses on making the ESP32 firmware non-blocking, ensuring fast local serial monitoring, and improving Firebase telemetry responsiveness.

## User Review Required

> [!IMPORTANT]
> **Non-Blocking Architecture**: I will refactor the `loop()` function to remove `delay()` calls and use `millis()` for timing. This ensures that a slow Firebase connection won't freeze the Serial Monitor or sensor readings.
>
> **DHT11 Integration**: I will add the missing DHT11 temperature sensor logic (using GPIO 4) to match the project requirements.
>
> **Fast Serial Output**: Serial Monitor updates will happen every 1.5 seconds, independent of Firebase sync.

## Proposed Changes

### [ESP32 Firmware]

#### [MODIFY] [ScentGuard.ino](file:///Users/michaelangelotorre/StudioProjects/ScentGuard_new/firmware/ScentGuard.ino)
- **Non-Blocking Logic**: Introduce `millis()` timers for:
    - Sensor reading & Serial output (1.5s interval).
    - Firebase Telemetry (5s interval).
    - Remote Config Sync (5s interval).
- **DHT11 Support**: Add `#include "DHT.h"`, define `DHTPIN 4`, and implement temperature/humidity reading.
- **Improved Serial Monitor**: Format the output to be clear and professional, showing Gas, Temp, Fan Status, Wi-Fi status, and Firebase status.
- **Wi-Fi Recovery**: Implement a 60-second timeout that triggers BLE provisioning mode if the device cannot reconnect to Wi-Fi.
- **Firebase Safety**: Ensure Firebase operations don't block the main loop by using shorter timeouts or checking `Firebase.ready()` correctly.

## Verification Plan

### Manual Verification (Serial Monitor)
1.  **Observe Timing**: Verify that sensor readings (Gas/Temp) print to Serial every 1.5 seconds even if Wi-Fi is disconnected.
2.  **Telemetry Sync**: Verify that "Telemetry OK" appears every 5 seconds when Wi-Fi is active.
3.  **Wi-Fi Loss Recovery**: Unplug the router, wait 60 seconds, and verify the ESP32 enters BLE mode (Red LED blinking).
4.  **Android Sync**: Verify that the "Active" status and sensor values on the Dashboard update within 3-5 seconds of the hardware reading change.
