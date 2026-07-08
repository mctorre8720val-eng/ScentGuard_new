# Known Issues

This document tracks intentionally deferred issues, bugs, and enhancements for ScentGuard.

---

## 1. Sign-Up Loading State (RESOLVED)
**Status: ✅ Fixed**
**Priority: High**

### Description
* Previously, the loading indicator continued indefinitely during sign-up.
* The issue was a combination of state synchronization conflicts and unhandled Firestore exceptions.

### Fix
* Refactored `RegistrationViewModel` to provide a clean sequential flow.
* Integrated with the new **Hybrid Onboarding** architecture.
* Success now correctly triggers a navigation event to either the Onboarding screen or Dashboard.
* Added a safety net to ensure the `Loading` state is always cleared.
