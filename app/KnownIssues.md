# Known Issues

This document tracks intentionally deferred issues, bugs, and enhancements for ScentGuard.

---

## 1. Sign-Up Loading State
**Status:** RESOLVED (Aug 19, 2026)
**Priority:** High

### Description
* When the user taps **Sign Up**, the loading indicator continues indefinitely.
* App appears frozen on the logo screen.

### Resolution
This was caused by a **Security Deadlock** where the database blocked anonymous users from checking invite codes. 

**Fix Detail:** See [Backend_Troubleshooting.md](file:///Users/michaelangelotorre/StudioProjects/ScentGuard_new/Backend_Troubleshooting.md) for the full technical solution and corrected Firestore rules.

