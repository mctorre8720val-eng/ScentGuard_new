# Implementation Plan - Fixing Staff Permissions & Data Consistency

This plan addresses the `PERMISSION_DENIED` error in the Staff Management screen by aligning the Firestore security rules with the application's query patterns and ensuring data casing consistency.

## User Review Required

> [!IMPORTANT]
> **Firestore Security Rules:** You MUST update your rules in the Firebase Console with the version provided below. The previous rules were too restrictive, preventing Managers from querying their staff list.
> **Data Consistency:** I am standardizing all "Role" data to uppercase (`MANAGER`, `STAFF`) to match the new security logic.

## Proposed Changes

### 1. Data Consistency Fixes
#### [MODIFY] [UserRepository.kt](file:///Users/michaelangelotorre/StudioProjects/ScentGuard_new/app/src/main/java/com/example/scentguard/data/repository/UserRepository.kt)
- Update `getStaffByRestaurant` query to use uppercase `"STAFF"` to match the registration logic.
- Standardize all internal role checks to uppercase.

### 2. Security Rules Overhaul
I have redesigned the Firestore rules to specifically support the "Staff List" query. These rules allow a user to read other user profiles IF they both belong to the same `restaurantId`.

---

## Security Rules (Action Required)
Paste these into **Firebase Console > Firestore > Rules**:

```javascript
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {

    // User Profile Rules
    match /users/{userId} {
      // 1. Allow reading your own profile
      // 2. Allow reading other profiles IF they share your restaurantId (Crucial for Staff List)
      allow read: if request.auth != null && (
        request.auth.uid == userId ||
        resource.data.restaurantId == get(/databases/$(database)/documents/users/$(request.auth.uid)).data.restaurantId
      );
      // Only allow users to write to their own profile
      allow write: if request.auth != null && request.auth.uid == userId;
    }

    // Restaurant Rules
    match /restaurants/{restaurantId} {
      allow read: if request.auth != null;
      // Only the designated manager can update the restaurant (e.g., refresh invite code)
      allow write: if request.auth != null && (
        !exists(/databases/$(database)/documents/restaurants/$(restaurantId)) ||
        request.auth.uid == get(/databases/$(database)/documents/restaurants/$(restaurantId)).data.managerUid
      );
    }

    // Device & Log Rules (Isolated by Restaurant ID)
    match /devices/{deviceId} {
      allow read, write: if request.auth != null &&
        get(/databases/$(database)/documents/users/$(request.auth.uid)).data.restaurantId == resource.data.restaurantId;
    }
    match /logs/{logId} {
      allow read, write: if request.auth != null &&
        get(/databases/$(database)/documents/users/$(request.auth.uid)).data.restaurantId == resource.data.restaurantId;
    }
  }
}
```

## Verification Plan
- **Staff List:** Log in as a Manager. Verify the "Current Staff" list loads successfully without the permission error.
- **Invite Code:** Verify the code and timer show correctly.
- **Role Enforcement:** Log in as Staff and verify you cannot access the Manager-only Staff Management screen.
