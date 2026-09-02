# Implementation Plan: Audio Alert Overlap Fix

Resolve the issue where warning/danger alert sounds play multiple times or overlap due to listener leaks and thread safety issues.

## Proposed Changes

### Service Layer

#### [MODIFY] [ScentGuardWatcherService.kt](file:///Users/michaelangelotorre/StudioProjects/ScentGuard_new/app/src/main/java/com/example/scentguard/service/ScentGuardWatcherService.kt)
- **Prevent Listener Leaks**:
    - Update `startMonitoring` to call `listenerRegistration?.remove()` at the very beginning.
    - This ensures that only one active Firestore listener exists, even if the service is restarted or the UI recomposes.
- **Audio De-duplication**:
    - In `triggerDangerAlert`, remove the `.setSound(soundUri)` call.
    - The persistent audio alarm is already handled by `AlertAudioManager` in the monitoring loop.
    - Keep the `NotificationChannel` at `IMPORTANCE_HIGH` for visibility, but the app will control the audio loop for better management (stop/start/acknowledge).

### Audio Layer

#### [MODIFY] [AlertAudioManager.kt](file:///Users/michaelangelotorre/StudioProjects/ScentGuard_new/app/src/main/java/com/example/scentguard/service/AlertAudioManager.kt)
- **Thread Safety**:
    - Annotate `startAlarm`, `startPreview`, and `stopAlarm` with `@Synchronized`.
    - Ensure that checking `mediaPlayer?.isPlaying` and creating a new `MediaPlayer` happen atomically.
- **Single Instance Enforcement**:
    - Double-check that `stopAlarm()` is always called and completes before a new `MediaPlayer` is assigned to the `mediaPlayer` property.

## Verification Plan

### Automated Tests
- Run `./gradlew app:assembleDebug` to ensure compilation.

### Manual Verification
1.  **Recomposition Test**: Navigate between screens and verify that only one background listener remains active (checked via logs or lack of overlapping audio).
2.  **Danger Trigger**: Simulate a gas spike to `DANGER`. Verify that only the custom `critical_alarm` plays and that it stops immediately when "Stop Alarm" is pressed or the environment returns to `SAFE`.
3.  **Warning Transition**: Verify that transitioning from `SAFE` -> `WARN` -> `DANGER` triggers sounds exactly as defined without overlap.
