# Walkthrough - Hardware-Level Offline Detection

I have implemented a periodic heartbeat check to ensure that the ESP32 connectivity status transitions to **OFFLINE** automatically when heartbeats stop, even if the Firestore document remains static.

## Changes Made

### [MainViewModel.kt](file:///Users/michaelangelotorre/StudioProjects/ScentGuard_new/app/src/main/java/com/example/scentguard/viewmodel/MainViewModel.kt)
- **Signal Status Ticker**: Added a background coroutine that recalculates the `signalStatus` every 30 seconds.
- **Dynamic Re-evaluation**: This ensures that as the `lastSeen` timestamp becomes stale, the UI automatically updates from **Active** → **Weak** → **Offline** without requiring a new Firestore emission.

### [ScentGuardWatcherService.kt](file:///Users/michaelangelotorre/StudioProjects/ScentGuard_new/app/src/main/java/com/example/scentguard/service/ScentGuardWatcherService.kt)
- **Heartbeat Safety Ticker**: Implemented a periodic check (every 30 seconds) within the foreground service.
- **Automatic Alarm Suppression**: If the device is detected as **OFFLINE** (heartbeat older than 2.5 minutes), any active audio alarm is automatically stopped. This prevents "ghost" alarms from continuing when fresh sensor data is no longer being received.
- **Leak Prevention**: Ensured that tickers and listeners are properly cancelled and cleaned up during service restarts or destruction.

## Verification Results

### Simulated Connectivity States
- **Heartbeat < 45s**: UI shows **Active** (Green).
- **Heartbeat 45s - 150s**: UI shows **Weak** (Orange).
- **Heartbeat > 150s**: UI shows **Offline** (Red) and any active alarm is silenced.

### Stability & Resource Usage
- **Local Calculation**: The offline detection is performed locally using the existing `lastSeen` timestamp; no additional Firestore reads or writes are triggered.
- **Cancellation**: Verified that coroutines are correctly scoped to `viewModelScope` and `serviceScope`, preventing memory leaks and duplicate background tasks.
