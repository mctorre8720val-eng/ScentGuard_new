# Implementation Plan - Phase 5C: Hardware-Led Analytics Pipeline

This plan establishes the automated data sampling pipeline where the ESP32 hardware populates the `sensor_history` collection, allowing the Android app to display historical air quality trends.

## 1. Hardware Strategy (ESP32)
The ESP32 will act as the autonomous data logger, ensuring a consistent history even without user app activity.

### **Sampling Logic**
- **Interval:** 15 minutes (`900,000 ms`).
- **Deduplication:** Use deterministic document IDs based on 15-minute time slots (e.g., `snap_20260828_1645`).
- **Data Payload:**
    - `currentGasPpm` (Int)
    - `airStatus` (String)
    - `fanStatus` (String)
    - `fanMode` (String)
    - `timestamp` (Firestore Server Timestamp)

## 2. Android Refinement
Update the app to consume the real hardware-generated history.

#### [MODIFY] [ChartRepository.kt](file:///Users/michaelangelotorre/StudioProjects/ScentGuard_new/app/src/main/java/com/example/scentguard/data/repository/ChartRepository.kt)
- Change field mapping from `gas` to `currentGasPpm`.
- Convert the Firestore `timestamp` field into a human-readable "HH:mm" string for the chart X-axis labels.
- Ensure the query sorts by `timestamp` in ascending order to draw the trend line correctly.

## 3. Constraints & Safety
- **Free Tier:** No Cloud Functions or Blaze plan required.
- **Non-Blocking:** Uses `millis()` timers to ensure fan control and telemetry remain responsive (5s polling).
- **Isolation:** Strictly follows the `restaurants/{restaurantId}` sub-collection structure.

---

## Verification Plan

### 1. ESP32 Verification
- Monitor Serial Monitor for "History Snapshot: Success".
- Verify the new document appears in `restaurants/{id}/sensor_history` via Firebase Console.

### 2. Android App Verification
- Open the **Analytics** screen.
- Verify the chart draws points using real `currentGasPpm` values.
- Verify X-axis labels show correct times (e.g., 14:15, 14:30).

### 3. Stability Check
- Verify **Phase 5A** notifications still trigger on DANGER.
- Verify **Phase 5B** manual logs still appear on fan toggle.
