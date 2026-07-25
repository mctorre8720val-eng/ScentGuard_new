# Restaurant Simulation Scenarios

## Summary
Simulated usage of the application under realistic restaurant conditions reveals a highly usable interface with some potential network-related bottlenecks.

## Scenario 1: Opening Shift
- **Behavior:** Manager logs in successfully. The `SplashScreen` fetches the profile in < 3s. Dashboard shows "Excellent" air quality.
- **Verdict:** ✅ **Pass.**

## Scenario 2: Staff Morning
- **Behavior:** Staff logs in. Attempting to click "Reports" in the drawer is impossible as the item is hidden. Navigating to `reports` via internal routes triggers an immediate redirect to Dashboard.
- **Verdict:** ✅ **Pass.**

## Scenario 3: Lunch Rush (Rapid Navigation)
- **Behavior:** Switching between Dashboard and Alerts via `ScentGuardFloatingNav` is instantaneous due to state restoration.
- **Verdict:** ✅ **Pass.**

## Scenario 4: Poor Internet / Offline
- **Behavior:** App starts, `fetchUserProfile` hits the 7s timeout. Snackbar shows "Failed to fetch profile". App stays on Splash or shows an error state.
- **Verdict:** 🟡 **Partial Pass.** (User is informed but can't use the app even for cached data).

## Scenario 5: Unexpected Situations (Orientation Change)
- **Behavior:** ViewModels survive orientation changes correctly.
- **Verdict:** ✅ **Pass.**

## Severity
Low.

## Recommendations
- Implement a **Loading Overlay** for network-heavy transitions to prevent "Double Taps" from impatient users.

