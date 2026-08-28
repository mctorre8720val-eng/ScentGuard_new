# Task: Phase 5C - Hardware-Led History Pipeline

- `[x]` **Phase 5A: Local Proactive Alerting**
    - `[x]` Declare `FOREGROUND_SERVICE` and `DATA_SYNC` permissions in `AndroidManifest.xml`
    - `[x]` Implement `ScentGuardWatcherService.kt` (Background Firestore Listener)
    - `[x]` Implement start/stop logic in `MainActivity.kt`
    - `[x]` Verify local notification triggers (Foreground & Background)
- `[x]` **Phase 5B: Automated System Event Logging**
    - `[x]` Add `addLogEntry` to `HistoryRepository.kt`
    - `[x]` Implement state transition tracking in `ScentGuardWatcherService.kt`
    - `[x]` Implement Deterministic ID logic (`log_TYPE_SECONDS_NANOS`) for deduplication
- `[/]` **Phase 5C: Hardware-Led History Pipeline**
    - `[ ]` Update `ChartRepository.kt` to map `currentGasPpm` and `timestamp`
    - `[ ]` Provide updated ESP32 firmware with 15-minute sampling and deterministic IDs
    - `[ ]` Verify real data appears on the Analytics chart
