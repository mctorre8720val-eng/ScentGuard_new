# Implementation Plan: Staff Response Distinction in History UI

Enhance the System Logs / History UI to visually distinguish Staff Response entries from standard system logs using specialized iconography, labeling, and container styling.

## Proposed Changes

### UI Layer

#### [MODIFY] [HistoryScreen.kt](file:///Users/michaelangelotorre/StudioProjects/ScentGuard_new/app/src/main/java/com/example/scentguard/ui/screens/history/HistoryScreen.kt)
- **Staff Response Detection**: Identify staff response entries using `item.eventType == "STAFF_UPDATE"`.
- **Icon Update**: Use `Icons.Outlined.Person` for staff response entries.
- **Container Styling**:
    - Apply a subtle `primaryContainer` background to Staff Response logs to distinguish them from the standard `surface` background of system logs.
    - Adjust border and shadow to maintain professional appearance.
- **Labeling & Formatting**:
    - Force the label to "Staff Response" for these entries.
    - Parse the `description` (format: `Name: Message`) to display the Staff Name and Message separately for better readability, matching the requested design.
- **Color Consistency**: Use `MaterialTheme.colorScheme.primary` as the accent color for staff responses to align with the ScentGuard M3 theme.

## Verification Plan

### Automated Tests
- Run `./gradlew app:assembleDebug` to ensure compilation and UI integrity.

### Manual Verification
1.  **Staff Response Identification**:
    - Trigger a staff response from the `CriticalAlertScreen`.
    - Navigate to `HistoryScreen`.
    - Verify the entry has a person icon, "Staff Response" label, and a distinct background color.
    - Verify the staff name is extracted and displayed above the message.
2.  **System Log Consistency**:
    - Verify that "Air status changed" or "Ventilation Activated" logs still use their standard icons (Info, Warning, etc.) and white/surface backgrounds.
3.  **Filter Verification**:
    - Ensure staff responses still appear under the "All" and relevant filters if applicable.
