# ScentGuard Project Progress Tracker

This document tracks the progress of the ScentGuard Vent project against the roadmap defined in `MCP.md`.

---

## Phase 1 — Mobile Application Foundation
**Status: ✅ Complete**

| Task | Status | Description |
| :--- | :--- | :--- |
| Splash Screen | ✅ Complete | Branding and auto-navigation logic implemented. |
| Login | ✅ Complete | Firebase Auth login with validation and persistent login support. |
| Sign Up | ✅ Complete | Firebase Auth registration, user profile creation in Firestore with roles. (Fix applied for loading issue). |
| Forgot Password | ✅ Complete | Password reset email functionality. |
| Persistent Login | ✅ Complete | Session persistence across app restarts. |
| Logout | ✅ Complete | Session clearing functionality. |
| Firebase Authentication | ✅ Complete | Fully integrated. |
| Cloud Firestore | ✅ Complete | `users` collection implemented with required fields. |
| User Profile Storage | ✅ Complete | User information is stored and retrieved from Firestore. |
| Manager & Staff Roles | ✅ Complete | Roles are correctly assigned and stored. |
| One Shared Dashboard | ✅ Complete | Single dashboard implementation for all roles. |
| Role-Based Navigation | ✅ Complete | Navigation drawer and floating nav items hidden based on role. |
| Navigation | ✅ Complete | Jetpack Compose navigation with centralized graph. |
| Material Design 3 UI | ✅ Complete | High-polish premium redesign inspired by "Now in Android". |
| Light & Dark Mode | ✅ Complete | Theme persistence using DataStore. Supports Light, Dark, and System Default. |
| MVVM Architecture | ✅ Complete | Clean separation of UI, ViewModel, Repository, and Firebase layers. |

---

## Phase 2 — IoT Integration (Frontend First Priority)
**Status: 🟡 Partially Complete**

| Task | Status | Description |
| :--- | :--- | :--- |
| Placeholder Screens Completion | ✅ Complete | Profile, History, Reports, Notifications, and Settings screens are fully built with mock data. |
| Profile Management UI | ✅ Complete | Full information display and logout. |
| Settings UI | ✅ Complete | Toggles for theme and notifications. |
| Notification Preferences UI | ✅ Complete | Integrated into Settings screen. |
| History/Logs Interface | ✅ Complete | Functional search and filtering with mock data. |
| Reports/Analytics Interface | ✅ Complete | Summary metrics with period toggling. |
| Search and Filtering | ✅ Complete | Implemented in History screen. |
| Empty / Loading / Error States | ✅ Complete | Standardized across all screens. |
| Form Validation | ✅ Complete | Enhanced regex-based email and password length validation. |
| Accessibility Improvements | 🟡 Partially Complete | Icons have content descriptions. Final semantic review pending. |
| Responsive Layouts | ✅ Complete | Using scroll states and flexible containers. |
| Reusable UI Components | ✅ Complete | `ScentGuardCard`, `ScentGuardButton`, `ScentGuardFloatingNav`, `ScentGuardChart`, `ScentGuardFanControl`, etc. |
| Analytics & Graph UI | ✅ Complete | UI-ready chart component with high-quality mock data. |
| Manual Fan Control UI | ✅ Complete | Three-state control component (On/Off/Auto) for Managers. |
| ESP32 / IoT Integration | ❌ Not Started | **Blocked** (Waiting for "Begin IoT Development" command). |

---

## Phase 3 — Monitoring & Analytics
**Status: ❌ Not Started**

| Task | Status | Description |
| :--- | :--- | :--- |
| Push Notifications | ❌ Not Started | Requires Phase 2 connectivity. |
| Alert History | ✅ Complete | Frontend UI built with mock data. |
| Historical Logs | ✅ Complete | Frontend UI built with mock data. |
| Reports (Daily/Weekly) | ✅ Complete | Frontend UI built with mock data. |
| Charts & Analytics | ✅ Complete | UI-ready component implemented. |

---

## Phase 4 — Finalization
**Status: ❌ Not Started**

---

## Audit Findings & Updates

### 1. Light & Dark Mode (Finalized)
*   Implemented `PreferencesManager` using DataStore.
*   Updated `SettingsViewModel` to handle theme persistence.
*   Updated `MainActivity` to observe and apply the theme globally.
*   Added a selection dialog in `SettingsScreen`.

### 2. Sign-Up Fix (Pending Verification)
*   Aligned the sign-up flow with `MCP.md` (direct to Dashboard).
*   Removed the conflicting logout call.
*   Enhanced error handling to prevent infinite loading.

### 3. Analytics (Finalized)
*   Chart component is fully integrated into the Reports and Device screens.
*   Mock data provided via `ChartRepository`.
