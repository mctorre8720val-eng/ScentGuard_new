# Walkthrough - Staff Management & Consistency Fixes

I have resolved the permission errors, fixed the display bugs, and implemented the custom expiration feature for invitation codes.

## Key Fixes & Features

### 1. Data Consistency & Permissions
- **Uppercase Standardization:** Standardized all role checks and queries to use uppercase (`MANAGER`, `STAFF`). This ensures that the application's logic matches the Firestore security rules perfectly.
- **Enhanced Error Messages:** If a permission issue occurs (e.g., if the rules haven't been updated yet), the app will now explicitly tell you: **"Permission Denied: Please apply the new Firestore Rules provided in the plan."**
- **Robust Staff Query:** Fixed a logic gap in [UserRepository.kt](file:///Users/michaelangelotorre/StudioProjects/ScentGuard_new/app/src/main/java/com/example/scentguard/data/repository/UserRepository.kt) where it was looking for "Staff" instead of "STAFF".

### 2. Live Expiration System
- **Countdown Display:** Fixed the bug where the timer was showing as a literal variable name. It now correctly displays the live time remaining (e.g., "01h 45m").
- **Live Updates:** Added a timer to the [StaffViewModel.kt](file:///Users/michaelangelotorre/StudioProjects/ScentGuard_new/app/src/main/java/com/example/scentguard/viewmodel/StaffViewModel.kt) that refreshes the countdown every minute while the screen is active.

### 3. Manager Controls
- **Custom Expiration:** Managers can now select the duration of an invitation code before generating it. Options include **1h, 12h, 24h, and 7d**.
- **Staff List:** Verified the Manager's ability to view all staff members and remove them with a secure confirmation dialog.

## Mandatory Step
To permanently fix the "Permission Denied" error, please update your Firestore rules in the **Firebase Console** as described in the [Implementation Plan](file:///Users/michaelangelotorre/StudioProjects/ScentGuard_new/implementation_plan.artifact.md).

---
> [!TIP]
> **Test the Expiry:** Pick the "1h" duration and refresh the code. You should see the timer start immediately at "01h 00m".
