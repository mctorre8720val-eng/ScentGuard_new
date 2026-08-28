# Task: Phase 5A - FCM Infrastructure & Danger Alerts

- `[x]` **Phase 5A: FCM Infrastructure + DANGER Alert**
    - `[x]` Add `firebase-messaging` to `libs.versions.toml` and `app/build.gradle.kts`
    - `[x]` Declare `POST_NOTIFICATIONS` and register service in `AndroidManifest.xml`
    - `[x]` Implement `ScentGuardMessagingService.kt`
    - `[x]` Add `updateFcmToken` logic to `UserRepository.kt`
    - `[x]` Implement notification channel and permission request in `MainActivity.kt`
    - `[x]` Provide Cloud Function source code for deployment
- `[/]` **Phase 5B: Automated System Event Logging**
    - `[ ]` Implement state transition tracking in `MainViewModel.kt`
    - `[ ]` Add `addLogEntry` to `HistoryRepository.kt`
- `[ ]` **Phase 5C: Analytics/Sensor History Pipeline**
    - `[ ]` Implement sampling logic for `sensor_history`
    - `[ ]` Verify charts display the new historical data
