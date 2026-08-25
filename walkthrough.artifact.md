# Walkthrough - Analytics Tab Visibility Fix

I have resolved the visual bug in the **Reports & Analytics** screen where the active tab selection was obscuring the text.

## Changes Made

### 1. Tab Indicator Layering
- **[ReportsScreen.kt](file:///Users/michaelangelotorre/StudioProjects/ScentGuard_new/app/src/main/java/com/example/scentguard/ui/screens/reports/ReportsScreen.kt)**: Applied `Modifier.zIndex(-1f)` to the selection indicator (the white pill).
- **Why this fixed it:** By default, the `TabRow` indicator is drawn *after* the tabs, which placed it on top of the text. Pushing it to a lower Z-index ensures it remains behind the text, allowing the primary green color and bold weight of the selected tab to shine through.

### 2. Contrast Refinement
- Updated the `unselectedContentColor` to use a slightly more muted alpha (0.6f). This creates a clearer visual hierarchy between the active "Pill" state and the inactive background state.

## Verification
- ✅ **Daily Tab:** When clicked, the white pill slides behind the "Daily" text, which turns Bold Green.
- ✅ **Weekly Tab:** When clicked, the white pill slides behind the "Weekly" text, which turns Bold Green.
- ✅ **Interactive Polish:** The transition remains smooth and the text is perfectly legible at all times.

---
> [!SUCCESS]
> **Conclusion:** The Analytics tabs now behave like a premium Material 3 component, providing clear and high-contrast feedback for user selection.
