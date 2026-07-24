# Implementation Plan - Multi-Tenant Role System

This plan addresses the gaps identified in the Role System Audit, focusing on the Multi-Restaurant architecture, Invitation System, and Staff Management.

## User Review Required

> [!IMPORTANT]
> **Data Migration:** Since the existing users only have a `restaurantName` (String) and no `restaurantId`, existing data will need to be manually updated or migrated once the new schema is active.
>
> **Firestore Structure Change:** We will introduce a `restaurants` collection. Each restaurant document will contain the `managerUid`, `inviteCode`, and `restaurantName`.

## Proposed Changes

### 1. Data Layer & Models
#### [MODIFY] [User.kt](file:///Users/michaelangelotorre/StudioProjects/ScentGuard_new/app/src/main/java/com/example/scentguard/data/model/User.kt)
* Add `restaurantId: String` to the `User` model.

#### [NEW] [Restaurant.kt](file:///Users/michaelangelotorre/StudioProjects/ScentGuard_new/app/src/main/java/com/example/scentguard/data/model/Restaurant.kt)
* Create a model for the new `restaurants` collection:
    * `id: String`
    * `name: String`
    * `managerUid: String`
    * `inviteCode: String`
    * `createdAt: Timestamp`

#### [MODIFY] [UserRepository.kt](file:///Users/michaelangelotorre/StudioProjects/ScentGuard_new/app/src/main/java/com/example/scentguard/data/repository/UserRepository.kt)
* Add `createRestaurant(name, managerUid)` to generate a restaurant and `inviteCode`.
* Add `getRestaurantByInviteCode(code)` for Staff validation.
* Add `getStaffByRestaurant(restaurantId)` to fetch employees.
* Add `removeStaff(staffUid)` to clear a user's `restaurantId`.

---

### 2. Authentication & Invitation System
#### [MODIFY] [RegistrationViewModel.kt](file:///Users/michaelangelotorre/StudioProjects/ScentGuard_new/app/src/main/java/com/example/scentguard/viewmodel/RegistrationViewModel.kt)
* Update `register()` logic:
    * **Manager:** Creates both a `User` and a `Restaurant` document.
    * **Staff:** Validates the `inviteCode` first, retrieves the `restaurantId`, then creates the `User` document linked to that restaurant.

#### [MODIFY] [SignUpScreen.kt](file:///Users/michaelangelotorre/StudioProjects/ScentGuard_new/app/src/main/java/com/example/scentguard/ui/screens/signup/SignUpScreen.kt)
* Dynamic UI:
    * If "Manager" is selected, show "Restaurant Name" field.
    * If "Staff" is selected, show "Invitation Code" field.

---

### 3. Staff Management (Manager Only)
#### [NEW] [StaffScreen.kt](file:///Users/michaelangelotorre/StudioProjects/ScentGuard_new/app/src/main/java/com/example/scentguard/ui/screens/staff/StaffScreen.kt)
* List view showing all staff members.
* "Remove from Restaurant" button for each staff member.

#### [MODIFY] [ScentGuardNavigationDrawer.kt](file:///Users/michaelangelotorre/StudioProjects/ScentGuard_new/app/src/main/java/com/example/scentguard/ui/components/ScentGuardNavigationDrawer.kt)
* Add "Staff" navigation item for Managers.

#### [MODIFY] [NavGraph.kt](file:///Users/michaelangelotorre/StudioProjects/ScentGuard_new/app/src/main/java/com/example/scentguard/navigation/NavGraph.kt)
* Add route for the new `StaffScreen`.

---

## Verification Plan

### Automated Tests
* I will verify that the project builds successfully after these architectural changes.

### Manual Verification (via UI Logic Check)
* **Registration Flow:** Verify that a Manager registration creates a `restaurantId`.
* **Staff Join Flow:** Verify that a Staff registration with a valid code inherits the Manager's `restaurantId` and `restaurantName`.
* **Role Enforcement:** Verify that the "Staff" nav item is hidden for non-manager users.
* **Staff Removal:** Verify that removing a staff member clears their `restaurantId` in Firestore.
