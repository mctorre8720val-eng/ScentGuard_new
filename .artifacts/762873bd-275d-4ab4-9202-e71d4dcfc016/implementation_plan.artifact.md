# Implementation Plan: Eliminate Firestore Composite Index Requirements

This plan fixes the `FAILED_PRECONDITION` index errors by refactoring queries that combine filters and ordering into single-field compatible queries with Kotlin-side filtering.

## User Review Required

> [!IMPORTANT]
> To avoid creating composite indexes, the app will now fetch a slightly larger window of recent records and filter them in memory. This preserves the existing "No Index" architecture while maintaining data integrity.

## Identified Problem Queries

1.  **ActionViewModel.kt**: `whereEqualTo("status", "IN_PROGRESS")` combined with `orderBy("startTime", DESC)`.
2.  **HistoryRepository.kt**: `whereIn(category)` combined with `orderBy("timestamp", DESC)`.
3.  **ReportViewModel.kt (Calculations)**: Manual inspection confirmed `sensor_history` query uses `orderBy("timestamp")` without filters, which is safe. However, I will re-verify the `eventType` usage mentioned in the prompt.

---

## Proposed Changes

### [Core ViewModel]

#### [MODIFY] [ActionViewModel.kt](file:///Users/michaelangelotorre/StudioProjects/ScentGuard_new/app/src/main/java/com/example/scentguard/viewmodel/ActionViewModel.kt)
- **Refactor**: Remove `.whereEqualTo("status", "IN_PROGRESS")` from the snapshot listener.
- **Refactor**: Use `.orderBy("startTime", Query.Direction.DESCENDING).limit(10)`.
- **Logic**: In the listener, use `snapshot?.toObjects(Incident::class.java)?.firstOrNull { it.status == "IN_PROGRESS" }` to find the active incident.

### [Data Repository]

#### [MODIFY] [HistoryRepository.kt](file:///Users/michaelangelotorre/StudioProjects/ScentGuard_new/app/src/main/java/com/example/scentguard/data/repository/HistoryRepository.kt)
- **Refactor**: Remove all `whereIn` category filters from the Firestore `Query` object.
- **Refactor**: Ensure the query always uses `.orderBy("timestamp", Query.Direction.DESCENDING)`.
- **Logic**: Move the `when(category)` filtering logic into a post-processing step in Kotlin.
- **Pagination**: To preserve pagination, the method will fetch a buffer of records (e.g., 200) to ensure enough matching items are found for the requested `limit`.

---

## Verification Plan

### Automated Tests
- Run `./gradlew app:assembleDebug` to verify compilation.

### Manual Verification
1.  **Staff Response Feed**: Open a critical alert. Verify the feed loads and updates in real-time when a response is posted.
2.  **History Screen**: Toggling "Alerts", "Fan", etc., should still correctly filter the list without an index error in Logcat.
3.  **Sensor Stability Test**: Verify that keeping the gas at 1600 PPM continues to show the same active incident (deduplication check).
4.  **Logcat Audit**: Confirm no `FAILED_PRECONDITION` or `The query requires an index` messages appear during navigation.

## Critical Constraints
- **NO** changes to Firebase Authentication or Rules.
- **NO** changes to Firestore schema or restaurantId logic.
- **NO** manual composite index creation.
