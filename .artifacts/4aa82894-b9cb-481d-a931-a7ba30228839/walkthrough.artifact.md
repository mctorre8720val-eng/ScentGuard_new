# ESP32 Firmware Performance & Serial Monitoring Optimization

The ESP32 firmware has been refactored to prioritize responsiveness and provide real-time local monitoring independent of cloud connectivity.

## Key Improvements

### 1. Non-Blocking Architecture
The `loop()` function now uses `millis()`-based timers instead of `delay()`. This ensures that slow Firebase operations or Wi-Fi reconnections do not freeze the system. Sensors are read every cycle, and status logic executes instantly.

### 2. Fast Serial Monitoring
Serial output is now structured and frequent, updating every **1.5 seconds**.
**Example Output:**
```text
--------------------------------------
[SENSOR] Gas: 842 | Temp: 31.2°C | Status: SAFE
[FAN] Status: OFF | Mode: AUTO
[PUMP] Status: READY
[WIFI] Connected
[FIREBASE] Connected
```
This allows for real-time debugging even when the device is experiencing network latency.

### 3. DHT11 Temperature Integration
Full support for the DHT11 sensor has been added on **GPIO 4**. The system now tracks both temperature and humidity, which are displayed on the Serial Monitor and sent to Firebase.

### 4. Smart Wi-Fi Recovery
Implemented a 60-second Wi-Fi loss watchdog. If the device remains disconnected from the network for more than a minute:
- It automatically enters **BLE Provisioning Mode**.
- The existing **Restaurant Binding (RID) is preserved**.
- The Red LED blinks to signal recovery mode.

### 5. Telemetry & Cloud Sync
- **Telemetry**: Sent every 5 seconds.
- **Remote Config**: Synced every 5 seconds.
- **History Snapshots**: Uploaded every 60 seconds.

## Physical Verification Required
> [!IMPORTANT]
> While the code logic is verified, the following requires physical testing with the ESP32 hardware:
> 1. **DHT11 Reading Accuracy**: Verify temperature values match environmental conditions.
> 2. **60s Recovery Trigger**: Unplug the Wi-Fi router and verify the ESP32 enters BLE mode after exactly 60 seconds.
> 3. **Manual Reset**: Verify the 5-second hold on the BOOT button still clears NVS as expected.

## Files Modified
- [ScentGuard.ino](file:///Users/michaelangelotorre/StudioProjects/ScentGuard_new/firmware/ScentGuard.ino)
