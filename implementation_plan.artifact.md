# Implementation Plan - Phase 5A: FCM Infrastructure & Danger Alerts

This phase establishes the backbone for proactive safety alerts. It ensures that critical "DANGER" events from the hardware trigger reliable push notifications to all restaurant members (Manager and Staff), regardless of app state.

## 1. Android Infrastructure
**Goal:** Prepare the app to receive and display high-priority notifications.

### Dependency Update
#### [MODIFY] [libs.versions.toml](file:///Users/michaelangelotorre/StudioProjects/ScentGuard_new/gradle/libs.versions.toml)
- Add `firebase-messaging` to the libraries section.

#### [MODIFY] [build.gradle.kts](file:///Users/michaelangelotorre/StudioProjects/ScentGuard_new/app/build.gradle.kts)
- Add `implementation(libs.firebase.messaging)` to the dependencies block.

### Messaging Service
#### [NEW] [ScentGuardMessagingService.kt](file:///Users/michaelangelotorre/StudioProjects/ScentGuard_new/app/src/main/java/com/example/scentguard/service/ScentGuardMessagingService.kt)
- **`onNewToken`**: Trigger `UserRepository.updateFcmToken()` to store the unique device identifier in Firestore.
- **`onMessageReceived`**:
    - Handle high-priority data payloads.
    - Build and show a system notification using the "Alerts" channel.
    - Configure for "Heads-up" display, loud sound, and distinct vibration pattern.

### Permission & Channel Setup
#### [MODIFY] [MainActivity.kt](file:///Users/michaelangelotorre/StudioProjects/ScentGuard_new/app/src/main/java/com/example/scentguard/MainActivity.kt)
- **Channel Creation**: Create a `NotificationChannel` with `IMPORTANCE_HIGH`.
- **Permission Request**: Implement a one-time request for `Manifest.permission.POST_NOTIFICATIONS` on Android 13+.

#### [MODIFY] [AndroidManifest.xml](file:///Users/michaelangelotorre/StudioProjects/ScentGuard_new/app/src/main/AndroidManifest.xml)
- Declare `POST_NOTIFICATIONS` permission.
- Register `ScentGuardMessagingService` with the `com.google.firebase.MESSAGING_EVENT` intent filter.

---

## 2. Server-Side Alert Logic
**Goal:** Reliable, app-independent triggering of notifications.

### Cloud Function Strategy
#### [NEW] `index.js` (FCM Trigger Logic)
- **Trigger**: `onDocumentUpdated` on `restaurants/{restaurantId}`.
- **Logic**:
    1. Check if `before.airStatus != "DANGER"` and `after.airStatus == "DANGER"`.
    2. If true, query the `users` collection where `restaurantId == updatedRestaurantId`.
    3. Retrieve all `fcmToken` values for matching users.
    4. Construct a multicast FCM message with:
        - Title: `ScentGuard Alert`
        - Body: `Dangerous gas level detected. Gas reading: {currentGasPpm}. Check the garbage storage room immediately.`
    5. Dispatch the message and handle token cleanup (remove expired tokens).

---

## 3. Data Integration
#### [MODIFY] [UserRepository.kt](file:///Users/michaelangelotorre/StudioProjects/ScentGuard_new/app/src/main/java/com/example/scentguard/data/repository/UserRepository.kt)
- Add `updateFcmToken(uid: String, token: String)` function.

---

## Verification Plan

### Technical Checks
1. **Compilation**: Ensure the app builds without errors after adding Messaging libraries.
2. **Permission Flow**: Verify the Android 13+ notification prompt appears correctly.
3. **Token Registration**: Verify a token appears in the `users/{uid}/fcmToken` field in Firestore after login.

### Functional Checks
4. **Foreground/Background**: Send a test message via Firebase Console and verify it appears while the app is in all states (Open, Background, Killed).
5. **Logic Isolation**:
    - Manually change `airStatus` from `SAFE` to `DANGER` in Firestore.
    - Verify ONE notification is sent.
    - Update `currentGasPpm` while status remains `DANGER`.
    - Verify NO duplicate notification is sent.
6. **Restaurant Targeting**: Verify only users associated with the specific `restaurantId` receive the alert.
