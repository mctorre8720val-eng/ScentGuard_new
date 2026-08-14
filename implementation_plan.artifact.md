# Implementation Plan - Expiring Invite Codes & Staff Management

This plan outlines the transition to a professional, time-limited invitation system and enhances the Staff Management experience for Managers.

## User Review Required

> [!IMPORTANT]
> **Invite Code Expiration:** Codes will now expire after **24 hours**. Managers can manually refresh them from the Staff screen.
> **Staff Removal:** Managers will have the ability to permanently remove staff members from their restaurant. This will clear the staff member's `restaurantId`, effectively "kicking" them from the store's data view.
> **Confirmation:** I will add a "Confirmation Dialog" for staff removal to prevent accidental deletions.

## Proposed Changes

### 1. Data Model & Repository
#### [MODIFY] [Restaurant.kt](file:///Users/michaelangelotorre/StudioProjects/ScentGuard_new/app/src/main/java/com/example/scentguard/data/model/Restaurant.kt)
- Add `inviteCodeExpiresAt: Timestamp?`.

#### [MODIFY] [UserRepository.kt](file:///Users/michaelangelotorre/StudioProjects/ScentGuard_new/app/src/main/java/com/example/scentguard/data/repository/UserRepository.kt)
- **`refreshInviteCode`**: Logic to generate a new 6-char code and set `inviteCodeExpiresAt` to `now + 24h`.
- **`getRestaurantByInviteCode`**: Update to check if `inviteCodeExpiresAt` is in the future.
- **`removeStaffFromRestaurant`**: Ensure it also clears the `restaurantName` and any relevant session data for that user in Firestore.

---

### 2. Staff Management UI
#### [MODIFY] [StaffScreen.kt](file:///Users/michaelangelotorre/StudioProjects/ScentGuard_new/app/src/main/java/com/example/scentguard/ui/screens/staff/StaffScreen.kt)
- **Invite Section:**
  - Show remaining time for the current code.
  - "Generate New Code" button.
- **Staff List Section:**
  - High-polish list of current members.
  - **Remove Action:** A clear "Remove" button per staff member.
  - **Confirmation Dialog:** A Material 3 AlertDialog to confirm the removal.

---

### 3. Logic & Navigation
#### [MODIFY] [StaffViewModel.kt](file:///Users/michaelangelotorre/StudioProjects/ScentGuard_new/app/src/main/java/com/example/scentguard/viewmodel/StaffViewModel.kt)
- Add state for the "Removing..." process.
- Expose the invitation timer logic.

#### [MODIFY] [ScentGuardNavigationDrawer.kt](file:///Users/michaelangelotorre/StudioProjects/ScentGuard_new/app/src/main/java/com/example/scentguard/ui/components/ScentGuardNavigationDrawer.kt)
- Strictly enforce `MANAGER` role visibility for the "Staff" menu item.

---

## Verification Plan

### Manual Verification
- **Invite Cycle:** Manager creates restaurant -> Sees 24h code -> Code expires -> Manager clicks refresh -> New code appears.
- **Staff Join:** Staff uses valid code -> Joins successfully. Staff uses expired code -> Error message shown.
- **Removal Flow:** Manager clicks "Remove" on Staff A -> Dialog appears -> Click "Confirm" -> Staff A disappears from list and can no longer see store data.
