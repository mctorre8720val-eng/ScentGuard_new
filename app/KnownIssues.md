# Known Issues

This document tracks intentionally deferred issues, bugs, and enhancements for ScentGuard.

---

## 1. Sign-Up Loading State
**Status:** In-Progress (Fix applied, pending verification)
**Priority:** High

### Description
* When the user taps **Sign Up**, the loading indicator continues indefinitely.
* The account is successfully created in Firebase Authentication.
* The application does not provide success feedback or complete the registration flow.
* The UI appears to be stuck even though the backend operation succeeds.

### Root Cause Identified
The issue was likely a race condition or coroutine cancellation caused by calling `authRepository.logout()` immediately after profile creation while simultaneously setting a new state. This conflicted with the `MainViewModel`'s observation of the Auth state. Additionally, there was a discrepancy between the user's previous request (redirect to Login) and the `MCP.md` specification (navigate to Dashboard).

### Fix Applied
* Reverted flow to follow `MCP.md` strictly: Successful registration now leads directly to the **Dashboard** without an intermediate logout.
* Enhanced error handling in `RegistrationViewModel` to ensure the `Loading` state is always cleared.
* Simplified the asynchronous flow to prevent potential hangs during Firestore operations.

### Current Status
A fix has been implemented. Verification is needed to confirm the loading indicator now stops and navigation occurs correctly.
