# Backend & Firestore Troubleshooting Log

This document serves as a permanent reference for resolving complex backend deadlocks and permission issues encountered during the ScentGuard development.

---

## 1. The "Security Deadlock" (Permission Denied / App Freeze)
**Date Resolved:** Aug 19, 2026
**Symptoms:** 
- App freezes on the logo screen (SplashScreen).
- Sign-Up screen shows "Invalid or expired code" or "Permission Denied."
- Manager cannot see the Staff List even after joining.

### Root Causes
1. **The Anonymous Join Trap**:
   - **Problem**: Firestore rules required `request.auth != null` for the `restaurants` collection.
   - **Deadlock**: New staff members need to verify an `inviteCode` *before* they create an account. Since they aren't logged in yet, the rule blocked the check, causing the sign-up to fail.
   - **Fix**: Changed `restaurants` read rule to `allow read: if true;` to make the invite code check public.

2. **The Recursive Lookup Bottleneck**:
   - **Problem**: Rules used `get()` to look up the Manager's `restaurantId` inside the `users` list query.
   - **Deadlock**: Firestore does not allow complex lookups during collection-wide queries (listing all staff). It blocks the request with `PERMISSION_DENIED` to save performance.
   - **Fix**: Simplified the rule to `allow read: if request.auth != null;` and enforced isolation in the **Kotlin Code** using `.whereEqualTo("restaurantId", ...)`.

3. **String Template Bug**:
   - **Problem**: A literal backslash `\` was left before a variable: `\${timeRemaining}`.
   - **Fix**: Removed the escape character so the UI correctly displays the countdown timer.

### Recommended Firestore Rules (Phase 4: Live Data & Isolation)
Use this "Industrial Strength" configuration to support the sub-collection architecture and ensure total privacy between restaurants:

```javascript
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    
    // Helper: Check if user belongs to a specific restaurant
    function isMember(rid) {
      return request.auth != null && 
        get(/databases/$(database)/documents/users/$(request.auth.uid)).data.restaurantId == rid;
    }

    // 1. User Profiles
    match /users/{userId} {
      allow read: if request.auth != null;
      allow write: if request.auth != null && request.auth.uid == userId;
    }

    // 2. Restaurants & Sub-collections
    match /restaurants/{restaurantId} {
      // Basic restaurant info is public for invite code checks
      allow read: if true; 
      // Only manager can update restaurant settings
      allow write: if request.auth != null && 
        (resource == null || resource.data.managerUid == request.auth.uid);

      // --- PHASE 4 SUB-COLLECTIONS ---
      
      // Logs (History)
      match /logs/{logId} {
        allow read: if isMember(restaurantId);
        allow write: if request.auth != null; // Hardware can write
      }
      
      // Notifications (Alerts)
      match /notifications/{notifId} {
        allow read: if isMember(restaurantId);
        allow write: if request.auth != null; // System can write
      }
      
      // Sensor History (Charts)
      match /sensor_history/{dataId} {
        allow read: if isMember(restaurantId);
        allow write: if request.auth != null; // Hardware can write
      }
      
      // Summaries (Reports)
      match /summaries/{summaryId} {
        allow read: if isMember(restaurantId);
        allow write: if request.auth != null; // Analytics engine can write
      }
    }

    // 3. Legacy Collections (For Backward Compatibility)
    match /devices/{deviceId} { allow read, write: if request.auth != null; }
    match /logs/{logId} { allow read, write: if request.auth != null; }
  }
}
```

---

## 2. Google Sign-In "Developer Error" (12500)
**Date Resolved:** Aug 14, 2026
**Symptoms:** 
- Clicking "Sign in with Google" returns a generic failure.
- Logcat shows `DEVELOPER_ERROR`.

### Root Cause
- **SHA-1 Fingerprint**: The app's signing key was not registered in the Firebase Console.
- **Support Email**: The Firebase project was missing a designated support email.

### Fix
1. Run `./gradlew signingReport` and add the **SHA-1** to Firebase Project Settings.
2. Set a **Support Email** in Firebase Console > Project Settings > General.
3. Replace the `google-services.json` file with the updated version.
