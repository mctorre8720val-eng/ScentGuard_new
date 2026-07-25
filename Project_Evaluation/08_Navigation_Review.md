# Navigation Review

## Summary
The application uses Jetpack Compose Navigation with a centralized graph. It features a modern dual-navigation system: a Modal Drawer for primary routes and a Floating Glassmorphic bar for quick actions.

## Strengths
- **Dual Nav:** The combination of `ScentGuardNavigationDrawer` and `ScentGuardFloatingNav` provides excellent ergonomics.
- **Role-Awareness:** Navigation items are dynamically hidden/shown based on the `user.role`.
- **State Preservation:** Navigation uses `saveState = true` and `restoreState = true` to maintain UI state across screen switches.

## Weaknesses
- **Deep Linking:** No evidence of deep linking support for future notification-to-alert routing.

## Risks
- **Backstack Complexity:** Rapidly switching between Drawer and Floating Nav items could lead to a cluttered backstack if not using `launchSingleTop`.

## Evidence
- Verified in `NavGraph.kt`, `ScentGuardNavigationDrawer.kt`, and `ScentGuardFloatingNav.kt`.

## Severity
Low.

## Recommendations
- Define Deep Link routes for Alerts to support Phase 3 push notifications.
- Ensure the Splash Screen handles session expiration by clearing the backstack correctly.

