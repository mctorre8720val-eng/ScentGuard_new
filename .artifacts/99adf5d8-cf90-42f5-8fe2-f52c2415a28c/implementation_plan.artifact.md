# Implementation Plan - Accelerated Hardware Offline Detection

The goal is to reduce the latency of hardware offline detection to approximately 10-15 seconds. This requires increasing the ESP32 heartbeat frequency and making the app's monitoring tickers more aggressive.

## User Review Required

> [!IMPORTANT]
> The offline timeout is set to **15 seconds** to provide a safe buffer for network jitter while still meeting the "approximately 10-15 seconds" requirement. This allows for up to two missed heartbeats (at 5s intervals) before declaring the device offline.

## Proposed Changes

### [firmware](file:///Users/michaelangelotorre/StudioProjects/ScentGuard_new/firmware)

#### [MODIFY] [ScentGuard.ino](file:///Users/michaelangelotorre/StudioProjects/ScentGuard_new/firmware/ScentGuard.ino)
- Update `TELEMETRY_INTERVAL` from `10000UL` to `5000UL` in both redundant code blocks. This ensures the ESP32 patches the `lastSeen` field in Firestore every 5 seconds.

### [viewmodel](file:///Users/michaelangelotorre/StudioProjects/ScentGuard_new/app/src/main/java/com/example/scentguard/viewmodel)

#### [MODIFY] [MainViewModel.kt](file:///Users/michaelangelotorre/StudioProjects/ScentGuard_new/app/src/main/java/com/example/scentguard/viewmodel/MainViewModel.kt)
- Update `startSignalStatusTicker` to check every **5 seconds** (reduced from 30s).
- Update `updateSignalStatus` logic:
    - **Active**: `diffMs < 10000` (Allows for 1 missed heartbeat + network delay).
    - **Weak**: `diffMs < 15000`.
    - **Offline**: `diffMs >= 15000` (The 15s timeout requested).

### [service](file:///Users/michaelangelotorre/StudioProjects/ScentGuard_new/app/src/main/java/com/example/scentguard/service)

#### [MODIFY] [ScentGuardWatcherService.kt](file:///Users/michaelangelotorre/StudioProjects/ScentGuard_new/app/src/main/java/com/example/scentguard/service/ScentGuardWatcherService.kt)
- Update the background ticker delay to **5 seconds** (reduced from 30s).
- Update `evaluateAlarmStatus` timeout to **15000ms** (15s) to match the UI and safety requirements.

### [ui](file:///Users/michaelangelotorre/StudioProjects/ScentGuard_new/app/src/main/java/com/example/scentguard/ui)

#### [MODIFY] [CriticalAlertScreen.kt](file:///Users/michaelangelotorre/StudioProjects/ScentGuard_new/app/src/main/java/com/example/scentguard/ui/screens/alerts/CriticalAlertScreen.kt)
- Update the `isStale` check for the offline banner from `150000` to **15000**.

## Verification Plan

### Automated Tests
- Build and compilation check.

### Manual Verification
1. **Normal Operation**: Connect ESP32. Verify UI shows "Active" (Green).
2. **Detection Latency**: Unplug ESP32. Use a stopwatch to verify the Dashboard UI transitions to "Offline" (Red) and the "SENSOR OFFLINE" banner appears in the Critical Alert screen within ~10-15 seconds.
3. **Alarm Suppression**: Trigger a `DANGER` alert, then unplug the ESP32. Verify the audio alarm stops automatically within the 15s window.
4. **Reconnection**: Replug ESP32. Verify status returns to "Active" immediately upon the next Firestore heartbeat update.
5. **No Regression**: Confirm Firebase Auth, Firestore schema, and multi-tenant logic remain untouched.
