# ScentGuard Project Status Report

**Date:** July 23, 2026
**Status:** Phase 1 Complete | Phase 2 In Progress (Frontend)

## 1. Executive Summary
The ScentGuard project has successfully established a robust mobile foundation (Phase 1). The authentication system, role-based navigation, and premium "Fresh Tech" UI are fully functional. However, the **multi-restaurant architecture** requested (unique IDs, shared workspaces) is currently missing, with the app relying on a simple `restaurantName` string field. Development is ready to move into Phase 2 IoT integration once the backend infrastructure (Firestore API) is confirmed active.

---

## 2. Feature Audit

### ✅ Completed Features
*   **Premium Splash Screen:** Brand identity and auto-navigation logic.
*   **Authentication System:** Firebase Auth (Login, Sign Up, Logout, Password Reset, Persistent Session).
*   **User Onboarding:** 3-page premium onboarding with vector illustrations and hybrid persistence (Firestore + DataStore).
*   **UI/UX Redesign:** Material Design 3 "Fresh Tech" theme with Light/Dark mode support.
*   **Role System:** Storing and retrieving "Manager" and "Staff" roles from Firestore.
*   **MVVM Architecture:** Clean separation between UI, ViewModel, Repository, and Firebase layers.
*   **Reusable Components:** Standardized buttons, cards, charts, and background elements.

### 🟡 Partially Implemented (Frontend UI with Mock Data)
*   **Dashboard:** High-polish UI showing air quality, fan status, and environmental metrics.
*   **History/Logs:** Searchable interface for system events.
*   **Reports/Analytics:** Visual charts and metrics for air quality trends.
*   **Profile Management:** User info display and logout functionality.
*   **Notifications:** UI for alert history and preferences.
*   **Settings:** Theme and notification toggles.

### ❌ Not Implemented
*   **IoT Hardware Integration:** ESP32 communication and live sensor data (Blocked).
*   **Manual Fan Control (Logic):** UI exists for Managers, but no backend logic/hardware link.
*   **Push Notifications:** Firebase Cloud Messaging not yet integrated.

---

## 3. Architecture & Role System Audit

### Role-Based Access Control (RBAC)
*   **Implementation:** ✅ Fully functional.
*   **Roles:** "Manager" and "Staff" are correctly stored in the `users` collection.
*   **UI Enforcement:**
    *   **Managers:** See all screens including Reports, History, and the Fan Control dashboard card.
    *   **Staff:** Restricted from Reports and Settings; Fan Control card is hidden.
*   **Verification:** `ScentGuardNavigationDrawer` and `ScentGuardFloatingNav` correctly use the `user.role` field to filter navigation items.

### Multi-Restaurant Architecture
*   **Current Status:** ❌ **Not Implemented.**
*   **Issues:**
    *   No `restaurantId` field exists in the `User` model or Firestore.
    *   Each user (Manager or Staff) currently enters a `restaurantName` as a free-text string.
    *   There is no system for Staff to "join" a Manager's workspace via ID or invite code.
    *   No data isolation exists at the restaurant level; all data is currently associated only with individual `uids`.

---

## 4. Firestore & Data Structure Audit

### Current Structure (`users` collection)
```
users / {uid}
  - fullName: String
  - restaurantName: String (Free text)
  - email: String
  - role: "Manager" | "Staff"
  - onboardingCompleted: Boolean
  - createdAt: Timestamp
```

### Gap Analysis
To support the intended multi-tenant architecture, the following changes are required:
1.  **New Collection:** `restaurants` to store workspace-level data.
2.  **Unique ID:** A generated `restaurantId` for each restaurant.
3.  **Staff Onboarding:** A "Join Restaurant" flow where Staff enter an ID/Invite Code instead of creating a new restaurant name.

---

## 5. Discovered Issues & Technical Debt
*   **Firestore API Permission:** `PERMISSION_DENIED` errors observed in logs suggest the Cloud Firestore API is disabled in the GCP Console.
*   **Registration Loading:** Sign-up may hang or timeout if the Firestore API is unreachable (mitigated by 10s timeout).
*   **Placeholder Fallback:** Some navigation routes still point to `PlaceholderScreen` instead of their dedicated feature screens (e.g., Settings).
*   **Missing Restaurant Logic:** The discrepancy between the "Planned Multi-Restaurant Architecture" and the actual "Single Field String" implementation.

---

## 6. Prioritized Next Steps

### 🚨 High Priority (Blockers)
1.  **Backend Verification:** Ensure Cloud Firestore API is enabled in Google Cloud Console.
2.  **Multi-Tenant Refactor:** Implement `restaurantId` and a `restaurants` collection to allow Staff to join Manager-created workspaces.

### 📈 Medium Priority (Infrastructure)
3.  **Phase 2 IoT Kickoff:** Begin integration with Firebase Realtime Database for live sensor monitoring.
4.  **Device Management:** Create the "Join Device" flow for Managers to pair with ESP32 units.

### 🛠️ Low Priority (Polish)
5.  **Settings Refactor:** Move settings from `PlaceholderScreen` to a dedicated implementation.
6.  **Accessibility Audit:** Complete the semantic review for screen readers.

---

## Where We Left Off
We successfully completed the "Fresh Tech" redesign and finalized the hybrid onboarding flow. The app is visually complete and ready for real-time data integration, pending the structural refactor for multi-restaurant support.
