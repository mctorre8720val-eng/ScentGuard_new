# Implementation Plan: Staff Response Workflow

Implement the Staff Response workflow to separate human responses from environmental status and prevent endless staff-work loops.

## User Review Required

> [!IMPORTANT]
> This plan restores the automated incident lifecycle (creation/clearing) and updates the UI to show a "Response Recorded" state, ensuring staff can return to their duties while the system continues monitoring.

## Proposed Changes

### Data & Logic [Component]

#### [MODIFY] [HistoryRepository.kt](file:///Users/michaelangelotorre/StudioProjects/ScentGuard_new/app/src/main/java/com/example/scentguard/data/repository/HistoryRepository.kt)
- Restore `createIncidentIfMissing(incident: Incident)`: Ensures only one incident exists per danger cycle.
- Restore `clearActiveIncident(restaurantId: String, clearanceTime: Timestamp)`: Marks an incident as `CLEARED` when the environment returns to `SAFE`.

#### [MODIFY] [ScentGuardWatcherService.kt](file:///Users/michaelangelotorre/StudioProjects/ScentGuard_new/app/src/main/java/com/example/scentguard/service/ScentGuardWatcherService.kt)
- Update `handleAirStatusTransition`:
    - Call `createIncidentIfMissing` when status becomes `DANGER`.
    - Call `clearActiveIncident` when status becomes `SAFE`.

### UI Components [Component]

#### [MODIFY] [CriticalAlertScreen.kt](file:///Users/michaelangelotorre/StudioProjects/ScentGuard_new/app/src/main/java/com/example/scentguard/ui/screens/alerts/CriticalAlertScreen.kt)
- **Status Header**:
    - If status is `DANGER` and NO response recorded: Show "CRITICAL ALERT".
    - If status is `DANGER` and response RECORDED: Show "RESPONSE RECORDED ✓" and "ENVIRONMENT STILL DANGER".
    - If status is `SAFE`: Show "ENVIRONMENT SAFE ✓".
- **Recommendation Card**:
    - If response RECORDED: Show "ScentGuard continues monitoring independently." instead of the recommended physical action.
- **Visual Labels**:
    - Add clear status indicators for "Monitoring: ACTIVE" and "Response: RECORDED".

## Verification Plan

### Automated Tests
- Audit the transition logic in `ScentGuardWatcherService` to ensure `SAFE` status triggers `clearActiveIncident`.
- Audit `HistoryRepository` to ensure `createIncidentIfMissing` uses `getActiveIncident` for deduplication.

### Manual Verification
1. **DANGER Trigger**:
    - Set `currentGasPpm = 1600`.
    - Verify Critical Alert appears with recommended action.
2. **Staff Response**:
    - Send "Done Removing Waste".
    - Verify UI shows **"Response Recorded ✓"**.
    - Verify UI shows **"Environment Still DANGER — Continue Monitoring"**.
3. **Persistent DANGER**:
    - Keep `currentGasPpm = 1600` for 1-2 minutes.
    - Verify no new incidents/alerts are triggered.
    - Verify the screen remains in the "Recorded" state.
4. **SAFE Transition**:
    - Set `currentGasPpm = 800`.
    - Verify UI shows **"Environment Safe ✓"**.
    - Verify incident is marked as `CLEARED` in Firestore logs (conceptually).
