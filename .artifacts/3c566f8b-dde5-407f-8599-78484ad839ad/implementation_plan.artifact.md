# Implementation Plan: Critical State Separation

Separate the display of incident history from the ability to submit new responses. Ensure that responses are only allowed when the environment is in a `DANGER` state and the incident is not `CLEARED`.

## User Review Required

> [!IMPORTANT]
> The `ActionViewModel` will now fetch the latest incident regardless of its status (`IN_PROGRESS` or `CLEARED`). This ensures that the response history for the most recent incident remains visible to staff even after the danger has passed.

## Proposed Changes

### Action & Data Layer

#### [MODIFY] [ActionViewModel.kt](file:///Users/michaelangelotorre/StudioProjects/ScentGuard_new/app/src/main/java/com/example/scentguard/viewmodel/ActionViewModel.kt)
- **Incident Fetching**: Update `startRealTimeIncidentListener` to remove the `.firstOrNull { it.status == "IN_PROGRESS" }` filter. It will now fetch the most recent incident from the `startTime` sorted list.
- **`sendResponse` Validation**:
    - Add checks for `liveRestaurantData.value?.airStatus == "SAFE"` and `incident.status == "CLEARED"`.
    - Return the specific error message: `"Environment is safe. No active alert requires a response."`
    - Retain the "synchronizing" check only for when the incident is actually null while in a `DANGER` state.

### UI Layer

#### [MODIFY] [CriticalAlertScreen.kt](file:///Users/michaelangelotorre/StudioProjects/ScentGuard_new/app/src/main/java/com/example/scentguard/ui/screens/alerts/CriticalAlertScreen.kt)
- **Response Permission Logic**: Introduce a `canRespond` boolean calculated as `restaurant.airStatus == "DANGER" && activeIncident?.status != "CLEARED"`.
- **`CriticalAlertContent`**: Pass `canRespond` to `ResponseInputSection`.
- **`ResponseInputSection`**:
    - Disable chips and text input if `!canRespond`.
    - Update placeholder text and helper text to reflect the "Safe" state.
- **`RecommendationCard`**: Ensure it reflects that monitoring is active or conditions are safe based on `airStatus`.

## Verification Plan

### Automated Tests
- Run `./gradlew app:assembleDebug` to ensure compilation and basic build integrity.

### Manual Verification
1.  **DANGER State**:
    - Verify alert header shows "CRITICAL ALERT".
    - Verify response controls are enabled.
    - Post a response and verify it appears in the feed.
2.  **Transition to SAFE**:
    - Simulate `airStatus` changing to `SAFE`.
    - Verify header changes to "ENVIRONMENT SAFE ✓".
    - Verify response history remains visible.
    - Verify response controls (chips and text box) are disabled.
    - Verify `sendResponse` rejects any attempts with the new error message.
3.  **CLEARED Incident**:
    - Verify that even if the incident is marked `CLEARED` in Firestore, the history is still visible but responses are blocked.
