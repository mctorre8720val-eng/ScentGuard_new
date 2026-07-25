# Staff Workflow Review

## Summary
The Staff experience is streamlined, focusing on monitoring and alert awareness without the complexity of management tools.

## Strengths
- **Simplicity:** Restricted navigation ensures Staff only see what they need (Dashboard, Alerts, Profile).
- **Safety:** Staff can see air quality but cannot manually override the fan, preventing unauthorized tampering.
- **Joining Flow:** The invitation code system is intuitive and links Staff to the restaurant immediately upon signup.

## Weaknesses
- **Limited Interaction:** Staff members are purely "passive" users; they cannot "acknowledge" alerts or leave notes for the Manager.

## Risks
- **Accidental Navigation:** Although navigation items are hidden, direct route access is only recently hardened with `LaunchedEffect` checks.

## Evidence
- Verified in `ScentGuardNavigationDrawer.kt` and `SignUpScreen.kt`.

## Severity
Low.

## Recommendations
- Implement "Alert Acknowledgement" so Staff can notify the Manager that an air quality issue is being addressed.

