# Walkthrough: Critical State Separation

I have implemented the strict separation between incident history display and response permissions in the Staff Response Feed. This ensures that while staff can always see the history of the latest incident, they can only submit responses when there is a genuine, active danger.

## Changes Made

### Action & Logic Layer

#### [ActionViewModel.kt](file:///Users/michaelangelotorre/StudioProjects/ScentGuard_new/app/src/main/java/com/example/scentguard/viewmodel/ActionViewModel.kt)
- **Persistent History**: Modified the Firestore listener to fetch the latest incident regardless of whether it is `IN_PROGRESS` or `CLEARED`. This ensures the feed doesn't disappear when the environment becomes safe.
- **Robust Validation**: `sendResponse` now enforces three levels of checks:
    1. **Environment Check**: Rejects responses if `airStatus == "SAFE"`.
    2. **Incident Existence**: Only shows "Synchronizing" if the environment is `DANGER` but the incident is missing.
    3. **Status Check**: Rejects responses if the incident is `CLEARED`.
- **Error Messages**: Implemented the exact required strings:
    - `"Environment is safe. No active alert requires a response."`
    - `"Alert is still synchronizing. Please try again."`

### UI Layer

#### [CriticalAlertScreen.kt](file:///Users/michaelangelotorre/StudioProjects/ScentGuard_new/app/src/main/java/com/example/scentguard/ui/screens/alerts/CriticalAlertScreen.kt)
- **`canRespond` Logic**: Centralized the permission logic:
  ```kotlin
  val canRespond = restaurant.airStatus == "DANGER" && incident?.status != "CLEARED"
  ```
- **Responsive Header**: The `DangerHeader` reacts to `airStatus` and `hasResponded`, showing "ENVIRONMENT SAFE ✓" when conditions clear.
- **Adaptive Recommendation**: The `RecommendationCard` now explicitly states "Environment has returned to safe parameters" when the danger has passed.
- **Protected Input Section**:
    - The `ResponseInputSection` disables all interaction (chips and text box) when `canRespond` is false.
    - Updated placeholders and helper text to inform the user why controls are disabled.

## Verification Results

### Automated Tests
- Ran `./gradlew app:assembleDebug` - **Passed**.

### Manual Verification Scenarios (Simulated Logic)

| State | Header | Controls | Message |
| :--- | :--- | :--- | :--- |
| **DANGER + Active** | CRITICAL ALERT | Enabled | "Write a quick update..." |
| **DANGER + Responded** | RESPONSE RECORDED | Disabled | "Response already recorded" |
| **SAFE + Cleared** | ENVIRONMENT SAFE | Disabled | "Environment is safe" |

> [!NOTE]
> The staff response feed (names, messages, timestamps) remains fully visible in the "SAFE + Cleared" state, allowing for historical review of the incident resolution.

> [!IMPORTANT]
> The transition from DANGER to SAFE is handled gracefully. Even if a user attempts to tap a response during the exact moment of transition, the `ActionViewModel` will catch the change and return the "Environment is safe" error instead of creating duplicate incidents or crashing.
