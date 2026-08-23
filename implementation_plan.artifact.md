# Implementation Plan - Phase 4: Live Data Connectivity

This plan outlines the technical migration from hardcoded "Demo" data to a live, real-time connection with Firebase Firestore, enabling multi-tenant isolation and active hardware commands.

## User Review Required

> [!IMPORTANT]
> **Data Collection Design:** This plan assumes a Firestore structure where each restaurant has its own logs and notifications.
> **Real-time Cost:** Real-time listeners (Snapshots) stay active while the app is open. This provides the best UX but uses slightly more data/battery than one-shot fetches.

## Proposed Changes

### 1. Real-time Sensor Bridge
#### [MODIFY] [MainViewModel.kt](file:///Users/michaelangelotorre/StudioProjects/ScentGuard_new/app/src/main/java/com/example/scentguard/viewmodel/MainViewModel.kt)
- Create a `liveSensorData` StateFlow.
- Implement a Firestore Snapshot Listener on `restaurants/{restaurantId}`.
- Bind the Dashboard metrics (PPM, Temp, Humidity) to this flow.

### 2. Live Repository Migration
#### [MODIFY] [HistoryRepository.kt](file:///Users/michaelangelotorre/StudioProjects/ScentGuard_new/app/src/main/java/com/example/scentguard/data/repository/HistoryRepository.kt)
- Replace mock logic with: `db.collection("restaurants").document(rid).collection("logs").orderBy("timestamp", DESC)`.

#### [MODIFY] [NotificationRepository.kt](file:///Users/michaelangelotorre/StudioProjects/ScentGuard_new/app/src/main/java/com/example/scentguard/data/repository/NotificationRepository.kt)
- Replace mock logic with a real-time flow from `restaurants/{rid}/notifications`.

#### [MODIFY] [ChartRepository.kt](file:///Users/michaelangelotorre/StudioProjects/ScentGuard_new/app/src/main/java/com/example/scentguard/data/repository/ChartRepository.kt)
- Implement fetching the last 24 records from `restaurants/{rid}/sensor_history` to populate the trend chart.

### 3. Active Command Logic (Fan Control)
#### [MODIFY] [UserRepository.kt](file:///Users/michaelangelotorre/StudioProjects/ScentGuard_new/app/src/main/java/com/example/scentguard/data/repository/UserRepository.kt)
- Add `updateFanMode(restaurantId, mode)` function.
- This will write to `restaurants/{rid}/fan_mode`, which the ESP32 hardware will observe.

#### [MODIFY] [ScentGuardFanControl.kt](file:///Users/michaelangelotorre/StudioProjects/ScentGuard_new/app/src/main/java/com/example/scentguard/ui/components/ScentGuardFanControl.kt)
- Link the UI buttons to the new `updateFanMode` logic via a ViewModel.

---

## Verification Plan (The "Ghost Hardware" Test)

### Manual Verification
1.  **The Manual Pulse**:
    - Go to **Firebase Console > Firestore**.
    - Open your specific restaurant document.
    - Manually change the `currentGasPpm` field.
    - **Verification**: Confirm the Dashboard gauge in the app moves instantly.
2.  **Log Injection**:
    - Manually add a document to the `logs` sub-collection in Firebase.
    - **Verification**: Confirm it appears in the "System Logs" screen without refreshing.
3.  **Command Echo**:
    - Tap "ON" in the app's Fan Control.
    - **Verification**: Confirm the `fan_mode` field in Firestore updates to `"ON"`.
