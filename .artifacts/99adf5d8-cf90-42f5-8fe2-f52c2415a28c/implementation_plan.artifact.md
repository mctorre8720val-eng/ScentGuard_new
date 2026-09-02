# Implementation Plan - Fix Duplicate Warning Alert Sound

The user reports that the warning alert sound overlaps or double-triggers when the system enters the WARNING/DANGER state. My project-wide audit revealed two simultaneous audio sources and a Firestore listener leak.

## Audio Audit Results

- **Internal Alarm**: `ScentGuardWatcherService` calls `AlertAudioManager.startAlarm()` which uses `MediaPlayer` for a looped alarm.
- **System Notification**: `ScentGuardWatcherService.triggerDangerAlert()` calls `.setSound(soundUri)`, which plays the system's default alarm/notification sound.
- **Overlap**: Both trigger simultaneously on `DANGER` transitions.
- **Concurrency**: `AlertAudioManager` lacks synchronization, and `ScentGuardWatcherService` leaks listeners, leading to multiple active audio instances.
- **No other audio sources** (SoundPool, other MediaPlayer calls) were found in the project.

## User Review Required

> [!IMPORTANT]
> I am disabling the default system notification sound for critical alerts because the app manages its own high-priority alarm sound via `AlertAudioManager`. This ensures there is only one source of audio, preventing the overlap issue.

## Proposed Changes

### [service](file:///Users/michaelangelotorre/StudioProjects/ScentGuard_new/app/src/main/java/com/example/scentguard/service)

#### [MODIFY] [ScentGuardWatcherService.kt](file:///Users/michaelangelotorre/StudioProjects/ScentGuard_new/app/src/main/java/com/example/scentguard/service/ScentGuardWatcherService.kt)
- **Fix Leaked Listeners**: Add `listenerRegistration?.remove()` at the beginning of `startMonitoring()` to ensure that if the service is restarted, old listeners are cleaned up.
- **Silent Notification**: Remove `.setSound(soundUri)` from `triggerDangerAlert()`.
- **Logic Cleanup**: Ensure `isAlarmAcknowledged` and `lastKnownAirStatus` are reset appropriately on service start/stop.

#### [MODIFY] [AlertAudioManager.kt](file:///Users/michaelangelotorre/StudioProjects/ScentGuard_new/app/src/main/java/com/example/scentguard/service/AlertAudioManager.kt)
- **Thread Safety**: Use `synchronized(this)` in `startAlarm`, `startPreview`, and `stopAlarm` to prevent simultaneous `MediaPlayer` creation.
- **Resource Management**: Ensure `mediaPlayer` is released before being overwritten.

## Verification Plan

### Automated Tests
- Build verification.

### Manual Verification
- **Sequence Test**: `SAFE` → `WARNING` → `DANGER` → `SAFE` → `WARNING`.
    - `SAFE`: No sound.
    - `WARNING`: No sound (Warning is visual/logging only).
    - `DANGER`: Internal alarm starts, notification appears silent.
    - `SAFE`: Internal alarm stops.
    - `WARNING`: No sound.
- **Concurrency Test**: Rapidly toggle Firestore state and verify only one audio instance is active.
- **Stop Alarm Test**: Verify the "Stop Alarm" notification action completely silences the audio.
