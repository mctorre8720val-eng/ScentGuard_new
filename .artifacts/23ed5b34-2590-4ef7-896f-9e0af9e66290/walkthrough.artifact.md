# History Scaling & DHT11 Integration Walkthrough

I have enhanced ScentGuard with scalable system logging and full integration for DHT11 temperature monitoring on Android.

## Android Changes Made

### 1. History Scaling (Pagination & Filtering)
- **[MODIFY] [HistoryRepository.kt](file:///Users/michaelangelotorre/StudioProjects/ScentGuard_new/app/src/main/java/com/example/scentguard/data/repository/HistoryRepository.kt)**:
    - Replaced full-collection fetch with a paginated query using `limit(50)` and `startAfter()`.
    - Added backend filtering for **Categories** (Alerts, Fan, Devices, Users, System) and **Date Ranges** (Today, 7D, 30D).
- **[MODIFY] [HistoryViewModel.kt](file:///Users/michaelangelotorre/StudioProjects/ScentGuard_new/app/src/main/java/com/example/scentguard/viewmodel/HistoryViewModel.kt)**:
    - Implemented pagination state management (`lastDocument`, `hasReachedEnd`).
    - Added `loadNextPage()` and `refreshHistory()` logic.
- **[MODIFY] [HistoryScreen.kt](file:///Users/michaelangelotorre/StudioProjects/ScentGuard_new/app/src/main/java/com/example/scentguard/ui/screens/history/HistoryScreen.kt)**:
    - Added two rows of `FilterChips` for Categories and Date Ranges.
    - Added a "Load More" button that appears at the bottom of the list when more data is available.

### 2. DHT11 Temperature Integration
- **[MODIFY] [DashboardScreen.kt](file:///Users/michaelangelotorre/StudioProjects/ScentGuard_new/app/src/main/java/com/example/scentguard/ui/screens/dashboard/DashboardScreen.kt)**:
    - Updated the "System Temp" card to display the live value from `liveData?.temperature` with one decimal place accuracy (e.g., `28.4°C`).
- **[MODIFY] [ChartRepository.kt](file:///Users/michaelangelotorre/StudioProjects/ScentGuard_new/app/src/main/java/com/example/scentguard/data/repository/ChartRepository.kt)**:
    - Added `getTemperatureHistory()` to fetch historical temperature points from the `sensor_history` collection.
- **[MODIFY] [ReportViewModel.kt](file:///Users/michaelangelotorre/StudioProjects/ScentGuard_new/app/src/main/java/com/example/scentguard/viewmodel/ReportViewModel.kt)**:
    - Integrated temperature chart fetching and calculated the **Average Temperature** for the summary report.
- **[MODIFY] [ReportsScreen.kt](file:///Users/michaelangelotorre/StudioProjects/ScentGuard_new/app/src/main/java/com/example/scentguard/ui/screens/reports/ReportsScreen.kt)**:
    - Added a new **System Temperature Trend** chart below the odor concentration trend.
    - Added "Average Temp" as a new metric in the Insights summary.

---

## ESP32 Firmware Instructions (Apply Locally)

To enable temperature monitoring on your hardware, update your ESP32 project with the following logic:

### 1. Add DHT Library
Include the DHT library at the top of your main `.ino` file:
```cpp
#include "DHT.h"
#define DHTPIN 4     // Pin where DHT11 is connected
#define DHTTYPE DHT11
DHT dht(DHTPIN, DHTTYPE);
```

### 2. Update Telemetry JSON
In your `sendTelemetry()` function (where you send data to Firestore), include the `temperature` and `humidity` fields:
```cpp
float t = dht.readTemperature(); // Read Celsius
float h = dht.readHumidity();

if (!isnan(t) && !isnan(h)) {
    // Add to your existing JSON payload
    json["temperature"] = t;
    json["humidity"] = h;
}
```

### 3. Log Protection
Ensure your hardware **only** updates the `restaurants/{id}` document for high-frequency telemetry. Do **not** create documents in the `/logs` collection for every sensor reading.

> [!TIP]
> Use the Android app's existing `ScentGuardWatcherService` to automatically generate logs when gas levels cross thresholds, keeping the hardware logic simple.

---

## Verification Results

### Build Verification
- Successfully compiled the Android project using `./gradlew :app:assembleDebug`.

### UI/UX Improvements
- **History**: Verify that the log list is now manageable with filters and that "Load More" fetches previous events.
- **Temperature**: Observe the real-time temperature on the Dashboard and the historical trend in the Analytics section.

render_diffs(file:///Users/michaelangelotorre/StudioProjects/ScentGuard_new/app/src/main/java/com/example/scentguard/data/repository/HistoryRepository.kt)
render_diffs(file:///Users/michaelangelotorre/StudioProjects/ScentGuard_new/app/src/main/java/com/example/scentguard/ui/screens/dashboard/DashboardScreen.kt)
render_diffs(file:///Users/michaelangelotorre/StudioProjects/ScentGuard_new/app/src/main/java/com/example/scentguard/ui/screens/reports/ReportsScreen.kt)
render_diffs(file:///Users/michaelangelotorre/StudioProjects/ScentGuard_new/app/src/main/java/com/example/scentguard/viewmodel/HistoryViewModel.kt)
