# Known Issues

This document tracks intentionally deferred issues, bugs, and enhancements for ScentGuard.

---

## 1. Sign-Up Loading State
**Status:** Blocked (GCP Configuration Required)
**Priority:** High

### Description
* When the user taps **Sign Up**, the loading indicator continues indefinitely.
* The account is successfully created in Firebase Authentication.

### Root Cause (Found in Logcat)
The app is receiving a `PERMISSION_DENIED` error from Firestore:
`Cloud Firestore API has not been used in project scentguard-63353 before or it is disabled.`

### Required Action for User
1. Go to the [Google Cloud Console](https://console.developers.google.com/apis/api/firestore.googleapis.com/overview?project=scentguard-63353).
2. Ensure the **Cloud Firestore API** is enabled.
3. Wait a few minutes for the changes to propagate.

### Fixes Applied in Code
* Added a **10-second timeout** to all Firestore operations (`saveUserProfile`, `getUserProfile`, etc.).
* If the API is disabled, the app will now show an "Unexpected error" or timeout message after 10 seconds instead of hanging infinitely.
* Standardized navigation routes to prevent `SerializationException` crashes.
