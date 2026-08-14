# Task: Expiring Invite Codes & Staff Management

- `[x]` **Phase 1: Backend & Model**
    - `[x]` Add `inviteCodeExpiresAt` to `Restaurant.kt`
    - `[x]` Implement `refreshInviteCode` in `UserRepository.kt`
    - `[x]` Update `getRestaurantByInviteCode` with expiry check
- `[x]` **Phase 2: ViewModel & Logic**
    - `[x]` Implement code refresh logic in `StaffViewModel.kt`
    - `[x]` Implement removal confirmation state in `StaffViewModel.kt`
- `[x]` **Phase 3: UI Implementation**
    - `[x]` Redesign Invite Card in `StaffScreen.kt` (Timer + Refresh)
    - `[x]` Add "Remove Staff" confirmation dialog
    - `[x]` Update `ScentGuardNavigationDrawer.kt` visibility
- `[x]` **Phase 4: Verification**
    - `[x]` Test full "Join/Remove" lifecycle
    - `[x]` Test manual code refresh
