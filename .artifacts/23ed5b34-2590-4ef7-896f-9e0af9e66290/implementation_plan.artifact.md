# Implementation Plan - Mascot Selection Feedback Flow

Add a confirmation dialog and success feedback (Snackbar) when the user changes their ScentGuard mascot.

## User Review Required

> [!NOTE]
> The Snackbar will be displayed on the `ProfileScreen` after the `AvatarPickerSheet` is dismissed, ensuring visibility after the UI updates.

## Proposed Changes

### UI Components

#### [MODIFY] [AvatarPickerSheet.kt](file:///Users/michaelangelotorre/StudioProjects/ScentGuard_new/app/src/main/java/com/example/scentguard/ui/components/AvatarPickerSheet.kt)
- Add state for confirmation dialog: `showConfirmation` (Boolean) and `pendingMascot` (`MascotAvatar?`).
- Implement `AlertDialog` (Material 3) with the specified title, message, and buttons.
- Update `MascotTile` to:
    - Add a subtle checkmark indicator (`Icons.Filled.CheckCircle`) when `isSelected` is true.
    - Only trigger the confirmation dialog if the selected mascot is different from the current one.
    - If the current mascot is tapped, do nothing.
- Only call `onAvatarSelected` after the user confirms in the dialog.

### Profile Screen

#### [MODIFY] [ProfileScreen.kt](file:///Users/michaelangelotorre/StudioProjects/ScentGuard_new/app/src/main/java/com/example/scentguard/ui/screens/profile/ProfileScreen.kt)
- Add `SnackbarHostState` to manage Snakbar messages.
- Add `SnackbarHost` to the `Scaffold` in `ProfileScreen`.
- In the `AvatarPickerSheet` callback `onAvatarSelected`, launch a coroutine to show the success Snackbar: `"✓ Guardian changed to [Mascot Name]"`.

## Verification Plan

### Automated Tests
- N/A (UI-driven logic verification).

### Manual Verification
1. Open Profile Screen.
2. Tap the "Edit Avatar" button.
3. Tap the *currently selected* mascot: Verify NO dialog appears and the picker stays open.
4. Tap a *different* mascot:
    - Verify "Change Guardian?" dialog appears.
    - Tap "Cancel": Dialog closes, nothing changes.
    - Tap "Confirm": Dialog closes, Picker closes, UI updates, and Snackbar appears with the correct mascot name.
