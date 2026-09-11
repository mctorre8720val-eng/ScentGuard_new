# Implementation Plan - Sanitation Pump Redesign & Enhancement

This plan outlines the redesign of the Sanitation Pump section into a polished, dedicated feature within the ScentGuard application.

## User Review Required

> [!IMPORTANT]
> **New Navigation**: A dedicated "Sanitation Pump" screen will be added. It will be accessible by clicking the "Sanitation Pump" card on the Dashboard.
>
> **Data Model Expansion**: We will add `pumpMode` (AUTO/OFF) and `manualSanitationTrigger` to Firestore. This allows the app to request a sanitation cycle which the ESP32 can then execute.
>
> **Role-Based Access**: Manual control and mode switching will be restricted to users with the **Manager** role, consistent with Fan control.

## Proposed Changes

### [Data Layer]

#### [MODIFY] [Restaurant.kt](file:///Users/michaelangelotorre/StudioProjects/ScentGuard_new/app/src/main/java/com/example/scentguard/data/model/Restaurant.kt)
- Add `pumpMode: String` (default "AUTO").
- Add `manualSanitationTrigger: Timestamp?`.
- Add `lastSanitationTime: Timestamp?`.
- Add `sanitationDurationSeconds: Int` (to support progress indicators if fixed).

#### [MODIFY] [UserRepository.kt](file:///Users/michaelangelotorre/StudioProjects/ScentGuard_new/app/src/main/java/com/example/scentguard/data/repository/UserRepository.kt)
- Add `updatePumpMode(restaurantId, mode)`.
- Add `triggerManualSanitation(restaurantId)`.

### [UI Layer]

#### [NEW] [SanitationPumpScreen.kt](file:///Users/michaelangelotorre/StudioProjects/ScentGuard_new/app/src/main/java/com/example/scentguard/ui/screens/sanitation/SanitationPumpScreen.kt)
- **Hero Status Card**: Large card with pump icon, status indicator, and animated "flow" effect when `pumpStatus == "ON"`.
- **Pump Control**: Segmented control for AUTO/OFF modes. "Start Sanitation" button for manual triggers.
- **Runtime & Metrics**: Cards for "Last Cycle", "Total Cycles" (if tracked), and "Current Runtime".
- **Sanitation History**: A list of recent sanitation events pulled from the restaurant logs.
- **Safety Notice**: A subtle informational section about pump safety.
- **Offline Handling**: Integration with `signalStatus` to disable controls if hardware is offline.

#### [NEW] [SanitationViewModel.kt](file:///Users/michaelangelotorre/StudioProjects/ScentGuard_new/app/src/main/java/com/example/scentguard/ui/screens/sanitation/SanitationViewModel.kt)
- Manage state for the Sanitation screen.
- Handle manual trigger logic with confirmation states.
- Fetch history filtered specifically for sanitation events.

#### [MODIFY] [DashboardScreen.kt](file:///Users/michaelangelotorre/StudioProjects/ScentGuard_new/app/src/main/java/com/example/scentguard/ui/screens/dashboard/DashboardScreen.kt)
- Make the "Sanitation Pump" metric card clickable to navigate to the new screen.

#### [MODIFY] [Screen.kt](file:///Users/michaelangelotorre/StudioProjects/ScentGuard_new/app/src/main/java/com/example/scentguard/navigation/Screen.kt) & [NavGraph.kt](file:///Users/michaelangelotorre/StudioProjects/ScentGuard_new/app/src/main/java/com/example/scentguard/navigation/NavGraph.kt)
- Register the new `sanitation_pump` route.

## Verification Plan

### Manual Verification
1.  **Navigation**: Click "Sanitation Pump" on the Dashboard and verify it opens the new screen.
2.  **Status Sync**: Change `pumpStatus` in Firestore manually and verify the Hero card reflects the change (Icon change, text change, animation).
3.  **Control**: As a Manager, change `pumpMode` and click "Start Sanitation". Verify Firestore updates.
4.  **Permissions**: Log in as a Staff member and verify that Pump Control buttons are disabled/hidden.
5.  **Offline State**: Disconnect the ESP32 (or change `lastSeen` to old) and verify the screen shows "Hardware Offline" and disables controls.
6.  **History**: Add a manual log entry for a sanitation event and verify it appears in the history list.
