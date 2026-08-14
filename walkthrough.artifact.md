# Walkthrough - Expiring Invite Codes & Staff Management

I have implemented the professional invitation system with code expiration and the enhanced staff management dashboard for managers.

## Key Features Implemented

### 1. Expiring Invitation System
- **Time-Limited Access:** Every restaurant invitation code now has a built-in **24-hour expiration**. This ensures that old codes cannot be used by unauthorized personnel.
- **Backend Enforcement:** The `UserRepository` now automatically rejects any registration attempt using an expired code.
- **Manager Refresh:** Managers can instantly generate a **fresh 6-character code** from the Staff screen, which resets the 24-hour timer.

### 2. Enhanced Staff Management
- **Live Staff List:** Managers can now see a real-time list of all employees linked to their restaurant, including names and emails.
- **Secure Removal:** I've added a **"Remove Staff"** action. Managers can kick any staff member from the restaurant's workspace.
- **Safety Dialogs:** To prevent accidents, a confirmation dialog now appears before any staff member is removed.
- **Instant Revocation:** Once removed, a staff member immediately loses access to all restaurant-specific data (Devices, Logs, Reports).

### 3. Role-Based UI Hardening
- **Drawer Visibility:** The "Staff" management and "Reports" menu items are now strictly controlled by the `MANAGER` role check, using standardized uppercase comparison to prevent bugs.
- **Visual Feedback:** The Staff screen now features a "Timer Badge" showing exactly how much time is left before the current code expires.

## Technical Details
- **Data Model:** Updated `Restaurant` model with `inviteCodeExpiresAt`.
- **Logic:** Integrated `SessionManager` and `AuthRepository` to ensure the session remains synced with these permission changes.
- **UI:** Used Material 3 `AlertDialog` for a professional, native look during staff removal.

---
> [!TIP]
> **Check it out:** Go to the **Staff** section in your navigation drawer (if logged in as Manager) to see the new live timer and refresh your invitation code!
