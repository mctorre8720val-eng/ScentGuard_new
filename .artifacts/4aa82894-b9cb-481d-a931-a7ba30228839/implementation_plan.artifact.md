# Implementation Plan - Fix Hardcoded Calibration "Last Sync"

This plan removes the hardcoded "Last sync: 15d ago" string from the Settings screen and replaces it with the actual latest synchronization timestamp from the hardware.

## User Review Required

> [!NOTE]
> **Data Mapping**: I will use the `lastSeen` timestamp from the `Restaurant` model to represent "Last Sync". This is consistent with how "Last Sync" is displayed on the Device Details screen and accurately reflects the last time the ESP32 communicated with the cloud to send telemetry and receive configuration updates.
>
> **Display Logic**: If the hardware has never connected (timestamp is null), the UI will display "Not synced yet". Otherwise, it will show a formatted date/time of the last successful sync.

## Proposed Changes

### [UI Layer]

#### [MODIFY] [SettingsScreen.kt](file:///Users/michaelangelotorre/StudioProjects/ScentGuard_new/app/src/main/java/com/example/scentguard/ui/screens/settings/SettingsScreen.kt)
- Replace the hardcoded `description = "Last sync: 15d ago"` in the `ActionItem` for "Calibration".
- Implement a logic to format `liveData?.lastSeen` into a human-readable string.
- If `lastSeen` is null, show "Not synced yet".
- If `lastSeen` is within the last 60 seconds, show "Just now".
- Otherwise, show a formatted date (e.g., "Sep 11, 09:15 AM").

## Verification Plan

### Manual Verification
1.  **Never Connected**: Log into a new restaurant where no device has been provisioned. Navigate to Settings -> System and verify it says "Not synced yet".
2.  **Live Update**: Connect the ESP32 and wait for the first heartbeat. Verify the description updates to "Just now".
3.  **Delayed Sync**: Disconnect the ESP32 and wait for a few minutes. Verify the description reflects the last time it was seen (e.g., "2 minutes ago" or the absolute time).
4.  **Recomposition**: Navigate away and back to verify the value persists (via `liveData` state).
