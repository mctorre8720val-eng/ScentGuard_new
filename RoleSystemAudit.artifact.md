# ScentGuard Role System Audit Report

**Date:** July 24, 2026
**Status:** Partial Implementation | Multi-Tenant Gaps Identified

## 1. Role-Based Dashboard & Permissions

### Current Implementation Status
*   **✅ Role Identification:** The app correctly distinguishes between `Manager` and `Staff` roles stored in Firestore.
*   **✅ UI Filtering:**
    *   **Dashboard:** The `ScentGuardFanControl` component is exclusively visible to Managers.
    *   **Navigation Drawer:** The "Reports" section is exclusively visible to Managers.
    *   **Floating Navigation:** The "History" section is exclusively visible to Managers.
*   **🟡 Navigation Security:** While UI elements are hidden, there are no internal role-validation checks inside `ReportsScreen` or `HistoryScreen`. A Staff user could theoretically access these screens if they knew the route.

### Recommended Staff Permissions
| Feature | Staff Access | Manager Access | Status |
| :--- | :--- | :--- | :--- |
| Live Dashboard | ✅ View Only | ✅ Full Access | Partially Implemented |
| Fan Control | ❌ Restricted | ✅ Full Access | ✅ Implemented (UI) |
| History Logs | ❌ Restricted | ✅ Full Access | ✅ Implemented (Nav) |
| Reports/Analytics| ❌ Restricted | ✅ Full Access | ✅ Implemented (Nav) |
| Profile | ✅ Edit Own | ✅ Edit Own | ✅ Implemented |
| Staff Management | ❌ Restricted | ✅ Full Access | ❌ Not Implemented |

---

## 2. Restaurant Staff Management

### Current Status: ❌ Not Implemented
*   **Navigation:** There is no "Staff" or "Employees" section in the Manager's navigation drawer.
*   **Features:** Managers currently have no way to view other users belonging to their restaurant.
*   **Logic:** There is no backend repository or Firestore query implemented to fetch "staff by restaurant".

### Implementation Plan Requirements
*   **NEW UI:** `StaffListScreen` for Managers.
*   **NEW Logic:** `UserRepository#getStaffByRestaurant(restaurantId)`
*   **NEW Logic:** `UserRepository#removeStaffFromRestaurant(staffUid)`

---

## 3. Restaurant Invitation System

### Current Status: ❌ Not Implemented
*   **Logic:** The current registration flow (`SignUpScreen`) uses a free-text `restaurantName` field.
*   **Issues:**
    *   Two users entering "ABC Restaurant" are not programmatically linked.
    *   No unique `restaurantId` or `inviteCode` exists in the `User` model.
    *   Staff cannot "join" an existing restaurant; they simply create a new user profile with the same name string.

### Planned Workflow
1.  **Manager Registration:** Creates a new `Restaurant` document and generates a unique `inviteCode`.
2.  **Staff Registration:** Replaces the `restaurantName` field with an `inviteCode` field. Entering a valid code automatically links the Staff to the Manager's `restaurantId`.

---

## 4. Discovered Bugs & Technical Debt
*   **Architecture Gap:** The `users` collection is the only implemented collection. A `restaurants` collection is needed for proper data isolation and invitation logic.
*   **User Model:** The `User` data class is missing `restaurantId`, which is essential for multi-tenant queries.
*   **Placeholder screens:** Several features (Notifications, Settings) are still using a generic `PlaceholderScreen`.

---

## 5. Prioritized Next Steps
1.  **[CRITICAL] Refactor User Model:** Add `restaurantId` and create a `Restaurant` data model.
2.  **[CRITICAL] Invitation System:** Implement `inviteCode` generation for Managers and `inviteCode` validation for Staff during signup.
3.  **[HIGH] Staff Management UI:** Add the "Staff" section to the Manager's drawer and implement the list/remove functionality.
4.  **[MEDIUM] Security Update:** Add role-checks inside `ReportsScreen` and `HistoryScreen` composables.
