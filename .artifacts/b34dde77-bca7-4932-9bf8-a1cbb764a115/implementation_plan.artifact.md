# Implementation Plan - Fix Offline Detection Bug

The goal is to fix a bug where the app fails to transition to an "Offline" state automatically when the ESP32 stops sending telemetry. The fix involves implementing an independent "Ticker" that re-evaluates the hardware connection status every 2 seconds, regardless of whether new data arrives from Firestore.

## User Review Required

> [!IMPORTANT]
> **Authoritative State**: I will tighten the "Active" and "Offline" thresholds to provide near-real-time feedback as requested:
> - **Active**: < 5s (Green)
> - **Weak**: 5s to 8s (Yellow)
> - **Offline**: > 8s (Red)
>
> **Unified UI**: The `CriticalAlertScreen` will be updated to use this same authoritative `signalStatus` StateFlow, replacing its internal, slower (2.5 minute) stale check.

## Proposed Changes

### Business Logic

#### [MODIFY] [MainViewModel.kt](file:///Users/michaelangelotorre/StudioProjects/ScentGuard_new/app/src/main/java/com/example/scentguard/viewmodel/MainViewModel.kt)
- **Signal Ticker**: Add a background coroutine in `viewModelScope` that executes every 2 seconds.
- **Dynamic Re-evaluation**: The ticker will call `updateSignalStatus(liveRestaurantData.value)` to re-calculate `diffMs` against the current system time.
- **Threshold Update**: Update the logic to:
    - `diffMs < 5000` -> **Active**
    - `diffMs < 8000` -> **Weak**
    - Else -> **Offline**

### User Interface

#### [MODIFY] [CriticalAlertScreen.kt](file:///Users/michaelangelotorre/StudioProjects/ScentGuard_new/app/src/main/java/com/example/scentguard/ui/screens/alerts/CriticalAlertScreen.kt)
- **State Injection**: Access `mainViewModel.signalStatus` to drive the "SENSOR OFFLINE" banner.
- **Consistency**: Remove the hardcoded `150000ms` (2.5m) check and replace it with `signalStatus == "Offline"`.

## Verification Plan

### Automated Tests
- Run `app:compileDebugKotlin` to ensure no syntax errors.

### Manual Verification
1. **Active**: Plug in ESP32, verify "Active" (Green) status in Dashboard and Devices screens.
2. **Weak Transition**: Unplug ESP32. Verify status changes to "Weak" (Yellow) within ~5 seconds without interacting with the app.
3. **Offline Transition**: Continue waiting. Verify status changes to "Offline" (Red) within ~8 seconds without interacting with the app.
4. **Alert Sync**: Open the Critical Alert feed during a danger event; unplug ESP32 and verify the "SENSOR OFFLINE" banner appears automatically within 8 seconds.
5. **Recovery**: Plug ESP32 back in. Verify all screens return to "Active" automatically within a few seconds.
