# Mascot Avatar Visual Adjustment Plan

This plan aims to increase the visual scale of mascot avatars across the ScentGuard application to make them more prominent, while ensuring they remain centered and uncropped.

## Proposed Changes

### UI Components

#### [NEW] [ScentGuardMascotAvatar.kt](file:///Users/michaelangelotorre/StudioProjects/ScentGuard_new/app/src/main/java/com/example/scentguard/ui/components/ScentGuardMascotAvatar.kt)
- Create a shared Composable to unify mascot rendering.
- Set the internal mascot scale to **0.85f** of the container size (increased from approximately 0.70f–0.75f).
- Ensure `ContentScale.Fit` is used to prevent cropping while maximizing size.

### Screens & Components

#### [MODIFY] [DashboardScreen.kt](file:///Users/michaelangelotorre/StudioProjects/ScentGuard_new/app/src/main/java/com/example/scentguard/ui/screens/dashboard/DashboardScreen.kt)
- Replace inline mascot rendering in `DashboardHeader` with `ScentGuardMascotAvatar`.

#### [MODIFY] [ProfileScreen.kt](file:///Users/michaelangelotorre/StudioProjects/ScentGuard_new/app/src/main/java/com/example/scentguard/ui/screens/profile/ProfileScreen.kt)
- Replace inline mascot rendering in `ProfileHeader` with `ScentGuardMascotAvatar`.

#### [MODIFY] [ScentGuardNavigationDrawer.kt](file:///Users/michaelangelotorre/StudioProjects/ScentGuard_new/app/src/main/java/com/example/scentguard/ui/components/ScentGuardNavigationDrawer.kt)
- Replace inline mascot rendering in the drawer header with `ScentGuardMascotAvatar`.

#### [MODIFY] [AvatarPickerSheet.kt](file:///Users/michaelangelotorre/StudioProjects/ScentGuard_new/app/src/main/java/com/example/scentguard/ui/components/AvatarPickerSheet.kt)
- Replace inline mascot rendering in `MascotTile` with `ScentGuardMascotAvatar`.

#### [MODIFY] [StaffScreen.kt](file:///Users/michaelangelotorre/StudioProjects/ScentGuard_new/app/src/main/java/com/example/scentguard/ui/screens/staff/StaffScreen.kt)
- Update `StaffCard` to support mascot avatars using `ScentGuardMascotAvatar`, ensuring consistency for team member displays.

## Verification Plan

### Manual Verification
- Deploy the app and visually inspect:
    - **Dashboard Header**: Mascot should appear larger but fit within its circle.
    - **Profile Screen**: Main avatar should be more prominent.
    - **Navigation Drawer**: Drawer avatar should match the new style.
    - **Avatar Picker**: All selection tiles should show the updated scale.
    - **Staff Management**: Team member mascots (if set) should follow the same scaling.
- Confirm no cropping occurs on ears or heads of mascots like the fox or owl.
