# Implementation Plan - Fix Duplicate Alert Sound / Double Notification

This plan addresses the "double sound" issue where the system notification sound plays simultaneously with the app's custom `critical_alarm.mp3`, and ensures that alerts are triggered exactly once per danger event.

## User Review Required

> [!IMPORTANT]
> **Silencing Notification Channel**: I will explicitly silence the `scentguard_alerts` notification channel. This ensures that only the app's managed `AlertAudioManager` plays the alarm sound, preventing the "double sound" effect.
>
> **Transition Robustness**: I will ensure that the air status transition logic is atomic to prevent race conditions during rapid Firestore updates.

## Proposed Changes

### [Services]

#### [MODIFY] [ScentGuardWatcherService.kt](file:///Users/michaelangelotorre/StudioProjects/ScentGuard_new/app/src/main/java/com/example/scentguard/service/ScentGuardWatcherService.kt)
- **Silence Alert Channel**: Update `createNotificationChannels()` to set the sound of `ALERT_CHANNEL_ID` to `null`.
- **Remove Manual Vibration from Builder**: Since the channel handles vibration, removing `.setVibrate()` from the `NotificationCompat.Builder` prevents redundant vibration requests.
- **Ensure Single Trigger**: Confirm that `lastKnownAirStatus` is updated immediately upon detection of a change to prevent re-triggering if the listener fires again before the block completes.

## Verification Plan

### Manual Verification
1.  **Trigger Danger State**: Manually set `currentGasPpm` to 1600 in Firestore.
2.  **Verify Sound**: Confirm that ONLY the `critical_alarm.mp3` plays, and NO system notification sound is heard.
3.  **Verify Notification**: Confirm that exactly one critical notification appears.
4.  **Verify Heartbeat Stability**: While the state remains DANGER, update the `lastSeen` timestamp in Firestore multiple times. Confirm that NO new notifications are posted and the alarm sound does not restart or stutter.
5.  **Verify Recovery**: Change gas to 500 (SAFE). Confirm the alarm stops. Change gas back to 1600. Confirm the alarm and notification trigger again as expected.
