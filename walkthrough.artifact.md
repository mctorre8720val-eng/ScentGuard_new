# Walkthrough - Phase 5A: FCM & Danger Alerts

I have successfully established the infrastructure for **Proactive Alerting**. ScentGuard is now capable of delivering high-priority push notifications to Managers and Staff when hazardous gas levels are detected, even if the application is not running.

## Infrastructure Highlights

### 1. Android FCM Service
- **[ScentGuardMessagingService.kt](file:///Users/michaelangelotorre/StudioProjects/ScentGuard_new/app/src/main/java/com/example/scentguard/service/ScentGuardMessagingService.kt)**: A new background service that handles unique device tokens and incoming alert payloads.
- **Token Registration**: The app now automatically captures the device's FCM token and saves it to the user's Firestore profile (`users/{uid}/fcmToken`) upon login. This ensures alerts are targeted precisely to the correct individuals.

### 2. High-Priority Notifications
- **Alerts Channel**: Created a dedicated system notification channel with **High Importance**. This triggers "Heads-up" banners, custom sound, and a strong vibration pattern for critical gas alerts.
- **Permission Handling**: Implemented the Android 13+ `POST_NOTIFICATIONS` permission flow in [MainActivity.kt](file:///Users/michaelangelotorre/StudioProjects/ScentGuard_new/app/src/main/java/com/example/scentguard/MainActivity.kt).

### 3. Server-Side Intelligence (Cloud Function)
- **Document Trigger**: Provided the Node.js source code for a Cloud Function that monitors the `airStatus` of every restaurant.
- **Intelligent Alerting**: The logic specifically detects state transitions (e.g., `SAFE` -> `DANGER`). It will send an alert the moment a hazard is detected, but will **not spam** users with repeated notifications every 5 seconds while the status remains dangerous.
- **Multi-Tenant Isolation**: The function queries only users belonging to the specific `restaurantId` that triggered the alert, ensuring Restaurant A never receives alerts for Restaurant B.

## 🧪 Testing and Deployment

### **How to Deploy the Cloud Function**
1. Ensure you have the [Firebase CLI](https://firebase.google.com/docs/cli) installed.
2. Initialize functions in your project: `firebase init functions`.
3. Copy the code from **[index.js](file:///Users/michaelangelotorre/StudioProjects/ScentGuard_new/cloud_functions/functions/index.js)** into your local functions directory.
4. Deploy: `firebase deploy --only functions`.

### **Verification Checklist**
- ✅ **Permissions**: Launch the app; verify the notification permission request appears.
- ✅ **Token Sync**: Check your user document in Firestore to confirm the `fcmToken` field is populated.
- ✅ **Console Test**: Use the **Firebase Messaging Console** to send a "Test Message" to your device. Verify it appears in the tray with sound and vibration.
- ✅ **Logic Test**: Manually change your restaurant's `airStatus` to `DANGER` in Firestore. If the Cloud Function is deployed, you will receive the official alert instantly.

---
> [!IMPORTANT]
> **Next Phase:** Now that the alerting backbone is ready, I am prepared to begin **Phase 5B: Automated System Event Logging**, which will create a permanent audit trail of every fan activation and safety event.
