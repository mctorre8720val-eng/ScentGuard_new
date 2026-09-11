# Implementation Plan - Manager-only Delete Feature for System Logs

This plan adds administrative control over system logs, allowing Managers to delete individual entries or clear the entire log history for their restaurant.

## User Review Required

> [!IMPORTANT]
> **Manager-Only Restriction**: All delete actions (UI buttons and Firestore logic) will be strictly restricted to users with the `MANAGER` role.
> **Destructive Action**: Deleting logs is permanent. A confirmation dialog will be implemented for the "Delete All" action to prevent data loss.

## Proposed Changes

### Data & Repository

#### [MODIFY] [HistoryRepository.kt](file:///Users/michaelangelotorre/StudioProjects/ScentGuard_new/app/src/main/java/com/example/scentguard/data/repository/HistoryRepository.kt)
- Add `deleteLogEntry(restaurantId: String, logId: String)` to delete a specific document from the `logs` sub-collection.
- Add `deleteAllLogs(restaurantId: String)` to clear all documents in the `logs` sub-collection for the specified restaurant.

### ViewModel

#### [MODIFY] [HistoryViewModel.kt](file:///Users/michaelangelotorre/StudioProjects/ScentGuard_new/app/src/main/java/com/example/scentguard/viewmodel/HistoryViewModel.kt)
- Add `deleteLog(item: HistoryItem)`:
    - Calls `HistoryRepository.deleteLogEntry`.
    - Optimistically updates the `_historyState` by removing the deleted item from the list.
- Add `deleteAllLogs()`:
    - Calls `HistoryRepository.deleteAllLogs`.
    - Resets the `_historyState` and refreshes.
- Add `showDeleteAllDialog` state to manage the visibility of the confirmation dialog.

### UI Components

#### [MODIFY] [HistoryScreen.kt](file:///Users/michaelangelotorre/StudioProjects/ScentGuard_new/app/src/main/java/com/example/scentguard/ui/screens/history/HistoryScreen.kt)
- **Top Bar**: Add a "Delete All" icon button in the `actions` block, visible only if `user.role == "MANAGER"`.
- **Swipe-to-Delete**: Wrap `HistoryCard` within a `SwipeToDismissBox` (Material 3).
    - Enable swipe-left only.
    - Show a red background with a `Delete` icon when swiping.
    - Trigger `viewModel.deleteLog(item)` on dismissal.
    - Only enable swipe functionality if `user.role == "MANAGER"`.
- **Confirmation Dialog**: Implement a `Delete All` confirmation dialog that triggers `viewModel.deleteAllLogs()`.

## Verification Plan

### Automated Tests
- Build and run `app:compileDebugKotlin` to ensure syntax correctness.
- (Optional) Add unit tests for `HistoryViewModel` to verify that `deleteLog` correctly updates the state.

### Manual Verification
1. **Manager Role**:
    - Log in as a Manager.
    - Navigate to System Logs.
    - Verify that the "Delete All" button is visible in the top bar.
    - Swipe left on a log entry; verify it shows the delete action and removes the log upon completion.
    - Click "Delete All"; verify the confirmation dialog appears. Confirm and verify all logs are removed.
2. **Staff Role**:
    - Log in as a Staff member.
    - Navigate to System Logs.
    - Verify that the "Delete All" button is **NOT** visible.
    - Verify that swiping left on logs does nothing (swipe is disabled).
3. **Security**:
    - Verify through code that the `restaurantId` used for deletion is always taken from the authenticated user's session, preventing cross-restaurant deletion.
