# Walkthrough - Manager-only Delete Feature for System Logs

I have implemented the requested Manager-only delete functionality for System Logs. This enhancement provides administrators with the ability to maintain their log history while ensuring that standard Staff members cannot perform destructive actions.

## Changes Made

### Data Layer
- **[HistoryRepository.kt](file:///Users/michaelangelotorre/StudioProjects/ScentGuard_new/app/src/main/java/com/example/scentguard/data/repository/HistoryRepository.kt)**: Added `deleteLogEntry` to remove individual logs and `deleteAllLogs` to clear the entire restaurant's log collection using Firestore batches.

### Business Logic
- **[HistoryViewModel.kt](file:///Users/michaelangelotorre/StudioProjects/ScentGuard_new/app/src/main/java/com/example/scentguard/viewmodel/HistoryViewModel.kt)**:
    - Implemented `deleteLog` with **optimistic UI updates**, ensuring the log disappears immediately for a responsive feel.
    - Implemented `deleteAllLogs` with a loading state and full refresh.
    - Enforced `MANAGER` role check within the logic, ensuring only authorized users can trigger these repository methods.

### User Interface
- **[HistoryScreen.kt](file:///Users/michaelangelotorre/StudioProjects/ScentGuard_new/app/src/main/java/com/example/scentguard/ui/screens/history/HistoryScreen.kt)**:
    - **Top Bar**: Added a "Delete All" (`DeleteSweep`) icon button that only appears for Managers.
    - **Confirmation**: Integrated an `AlertDialog` that prompts for confirmation before performing a "Delete All" action.
    - **Swipe Actions**: Wrapped log entries in a `SwipeToDismissBox`.
        - **Direction**: Swipe-left (End-to-Start) only.
        - **Visuals**: A red background with a delete icon appears during the swipe.
        - **Permissions**: Swiping is programmatically disabled for Staff members.

## Verification Results

### Automated Tests
- Successfully executed `app:compileDebugKotlin`. The build passed with no syntax or reference errors.

### Security and Scope
- All delete actions use the `restaurantId` from the authenticated user's session, guaranteeing that managers can only affect logs belonging to their own restaurant.
- Role-based visibility and action enforcement ensure that Staff users have a read-only experience in the History section.
