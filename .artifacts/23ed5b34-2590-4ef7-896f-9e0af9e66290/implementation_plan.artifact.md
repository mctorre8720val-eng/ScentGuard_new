# Implementation Plan - History Scaling & DHT11 Integration

Enhance ScentGuard with scalable system logs (pagination/filtering) and full DHT11 temperature monitoring across Dashboard and Analytics.

## User Review Required

> [!IMPORTANT]
> **ESP32 Firmware**: No firmware source files were found in the current project root. I will provide the updated ESP32 code logic (DHT11 reading + telemetry structure) in the Walkthrough. You will need to apply this to your existing ESP32 project.

## Proposed Changes

### 1. Scalable System Logs (History)

#### [MODIFY] [HistoryRepository.kt](file:///Users/michaelangelotorre/StudioProjects/ScentGuard_new/app/src/main/java/com/example/scentguard/data/repository/HistoryRepository.kt)
- Update `getHistory` to support pagination:
    - Add `limit` parameter (default 50).
    - Add `lastDocument` parameter for `startAfter()`.
    - Add `category` filter (mapping to `eventType` or `type`).
    - Add `dateRange` filter (Today, 7D, 30D).

#### [MODIFY] [HistoryViewModel.kt](file:///Users/michaelangelotorre/StudioProjects/ScentGuard_new/app/src/main/java/com/example/scentguard/viewmodel/HistoryViewModel.kt)
- Add state for pagination: `lastDocument`, `isLastPage`.
- Add `loadNextPage()` method.
- Add `filter` state: `selectedCategory`, `selectedDateRange`.
- Logic to reset and re-fetch when filters change.

#### [MODIFY] [HistoryScreen.kt](file:///Users/michaelangelotorre/StudioProjects/ScentGuard_new/app/src/main/java/com/example/scentguard/ui/screens/history/HistoryScreen.kt)
- Add horizontal scrolling `FilterChip` row for categories: **All | Alerts | Devices | Fan | Users | System**.
- Add `FilterChip` row for date ranges: **Today | 7 Days | 30 Days**.
- Update `HistoryList` to include a "Load More" button at the bottom (or implement scroll-to-load).

---

### 2. DHT11 System Temperature Monitoring

#### [MODIFY] [Restaurant.kt](file:///Users/michaelangelotorre/StudioProjects/ScentGuard_new/app/src/main/java/com/example/scentguard/data/model/Restaurant.kt)
- Ensure fields `temperature` and `humidity` are correctly mapped (already present in the model).

#### [MODIFY] [DashboardScreen.kt](file:///Users/michaelangelotorre/StudioProjects/ScentGuard_new/app/src/main/java/com/example/scentguard/ui/screens/dashboard/DashboardScreen.kt)
- Update the "System Temp" `MetricCard` to use `liveData?.temperature ?: 0f`.
- Add a visual indicator (thermometer icon) and styling for temperature.

#### [MODIFY] [ChartRepository.kt](file:///Users/michaelangelotorre/StudioProjects/ScentGuard_new/app/src/main/java/com/example/scentguard/data/repository/ChartRepository.kt)
- Add `getTemperatureHistory(restaurantId: String): Result<ChartData>`.
- This will fetch from the same `sensor_history` collection but map the `temperature` field.

#### [MODIFY] [ReportViewModel.kt](file:///Users/michaelangelotorre/StudioProjects/ScentGuard_new/app/src/main/java/com/example/scentguard/viewmodel/ReportViewModel.kt)
- Add `temperatureChartState`.
- Update `computeSummaryFromData` to handle temperature averages.

#### [MODIFY] [ReportsScreen.kt](file:///Users/michaelangelotorre/StudioProjects/ScentGuard_new/app/src/main/java/com/example/scentguard/ui/screens/reports/ReportsScreen.kt)
- Add a "Temperature Trend" section below the Gas Concentration trend.
- Use `ScentGuardChart` to display the temperature data.

---

### 3. ESP32 Telemetry (Firmware Logic)

- **Implementation details to be provided in Walkthrough**:
    - Integration of `DHT.h` library.
    - Adding `temperature` field to the telemetry JSON sent to Firestore.
    - Ensuring logs are only created for meaningful events (e.g., threshold crossing), not for every telemetry update.

## Verification Plan

### Automated Tests
- Build verification using `gradlew :app:assembleDebug`.

### Manual Verification (User)
1. **History**:
    - Change filters (Alerts, Fan, etc.) and verify only relevant logs appear.
    - Change date filters (Today, 7D) and verify temporal filtering.
    - Scroll to bottom/tap "Load More" and verify pagination.
2. **Temperature**:
    - Verify Dashboard shows real-time temperature from Firestore.
    - Verify Analytics/Reports shows the Temperature Trend chart.
    - Verify that no new logs are created for normal temperature updates.
