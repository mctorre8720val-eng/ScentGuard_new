# Task: Staff Permission & Consistency Fix

- `[/]` **Phase 1: Code Alignment**
    - `[ ]` Update `getStaffByRestaurant` query to use `"STAFF"` in `UserRepository.kt`
    - `[ ]` Standardize internal role checks to uppercase in `UserRepository.kt`
- `[ ]` **Phase 2: Security Verification**
    - `[ ]` Verify new Firestore Rules fix the `PERMISSION_DENIED` issue
- `[ ]` **Phase 3: Final Polish**
    - `[ ]` Confirm staff list UI displays names and emails correctly
