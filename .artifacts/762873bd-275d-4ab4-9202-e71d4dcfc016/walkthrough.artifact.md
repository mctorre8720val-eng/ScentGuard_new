# Walkthrough: Critical Alert Navigation Fix

I have restored the minimum necessary infrastructure to make the "Immediate Action Required" alert banner tappable and functional. The Dashboard now correctly navigates to the Critical Alert screen when the environmental status is `DANGER`.

## Changes Made

### 1. Navigation Infrastructure
- **Route Definition**: Added `object CriticalAlert : Screen("critical_alert")` to [Screen.kt](file:///Users/michaelangelotorre/StudioProjects/ScentGuard_new/app/src/main/java/com/example/scentguard/navigation/Screen.kt).
- **Route Registration**: Registered `CriticalAlertScreen` in [NavGraph.kt](file:///Users/michaelangelotorre/StudioProjects/ScentGuard_new/app/src/main/java/com/example/scentguard/navigation/NavGraph.kt).

### 2. Data & Logic
- **Restored Incident Models**: Restored `Incident` and `StaffAction` in [Incident.kt](file:///Users/michaelangelotorre/StudioProjects/ScentGuard_new/app/src/main/java/com/example/scentguard/data/model/Incident.kt).
- **Repository Methods**: Added minimal methods to [HistoryRepository.kt](file:///Users/michaelangelotorre/StudioProjects/ScentGuard_new/app/src/main/java/com/example/scentguard/data/repository/HistoryRepository.kt) to fetch active incidents and post staff responses.
- **Action ViewModel**: Restored [ActionViewModel.kt](file:///Users/michaelangelotorre/StudioProjects/ScentGuard_new/app/src/main/java/com/example/scentguard/viewmodel/ActionViewModel.kt) to handle real-time incident syncing and response posting.

### 3. UI Implementation
- **Dashboard Banner**: Updated `AirQualityHero` in [DashboardScreen.kt](file:///Users/michaelangelotorre/StudioProjects/ScentGuard_new/app/src/main/java/com/example/scentguard/ui/screens/dashboard/DashboardScreen.kt) to accept an `onCriticalAction` callback and applied the `clickable` modifier to the alert banner.
- **Critical Alert Screen**: Restored the full [CriticalAlertScreen.kt](file:///Users/michaelangelotorre/StudioProjects/ScentGuard_new/app/src/main/java/com/example/scentguard/ui/screens/alerts/CriticalAlertScreen.kt) with live metrics and a response feed.

## Verification Results

### BUILD
**PASS**
- `./gradlew app:assembleDebug` completed successfully.

### MANUAL VERIFICATION (User Task)
The following steps should be performed to verify the fix:
1.  **Set Status to Danger**: In the Firebase Console, update `restaurants/{id}/currentGasPpm` to `1600`.
2.  **Dashboard Check**: Confirm the banner shows "Immediate action required" in red.
3.  **Navigation Check**: Tap the banner. The app should navigate to the **Critical Alert Feed**.
4.  **Response Check**: Type a response (e.g., "Fan turned on manually") and send. Verify it appears in the feed and Firestore.

> [!IMPORTANT]
> **No Firebase security rules, authentication logic, or telemetry schemas were modified during this fix.**
