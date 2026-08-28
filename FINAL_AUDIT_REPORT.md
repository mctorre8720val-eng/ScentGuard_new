# ScentGuard Final Data & Metrics Audit

This report maps every visible UI metric to its technical Firestore path, calculation logic, and hardware/software source. ScentGuard is now 100% data-driven.

## 1. Metrics & Data Source Mapping

| UI Metric | Firestore Path | Field(s) | Calculation Logic | Source |
| :--- | :--- | :--- | :--- | :--- |
| **Current Gas Level** | `restaurants/{id}` | `currentGasPpm` | Direct Read (Telemetry) | ESP32 |
| **Air Status Badge** | `restaurants/{id}` | `airStatus` | Direct Read (SAFE/WARN/DANGER) | ESP32 |
| **Fan Status** | `restaurants/{id}` | `fanStatus` | Direct Read (ON/OFF) | ESP32 |
| **Signal Status** | `restaurants/{id}` | `lastSeen` | `currentTime - lastSeen` (Heartbeat) | System |
| **Recent Activity** | `restaurants/{id}/logs` | `title`, `description` | Last 3 discrete event documents | Android Watcher |
| **Average Gas** | `restaurants/{id}/sensor_history` | `currentGasPpm` | Mean of last 24 snapshots | ESP32 History |
| **Total Alerts** | `restaurants/{id}/sensor_history` | `airStatus` | Count where status is "DANGER" | ESP32 History |
| **Performance Index** | `restaurants/{id}/sensor_history` | `airStatus` | `100 - (DangerSnaps / TotalSnaps * 100)` | Computed |
| **Fan Runtime** | `restaurants/{id}/sensor_history` | `fanStatus` | `ValidSnaps(ON) * 15 minutes` | ESP32 History |

---

## 2. Dynamic vs. Static Audit

- **Dynamic (100% Data-Driven):**
    - Dashboard Gas Gauge & Status Aura.
    - Dashboard Hardware Status (Temp, Gas, Signal).
    - Recent Activity (Real logs only).
    - Analytics Performance Score & Status Label (Excellent/Good/Stabilizing).
    - Analytics Trend Chart (Real historical points).
    - Analytics Insights Summary (Avg Gas, Runtime, Alerts).

- **Calculated Locally:**
    - **Performance Index:** Derived from the density of danger points in the history collection.
    - **Signal Status:** Determined by the recency of the ESP32 heartbeat (30s window).
    - **Fan Runtime:** Reconstructed by summing 15-minute intervals where the fan was reported ON.

- **Static / UI Only:**
    - "System default" theme labels.
    - "About" version strings.
    - Static icons/illustrations.

---

## 3. Verification Test Results

### Performance Index Test
- **Scenario:** 10 snapshots uploaded, 2 are marked `DANGER`.
- **Logic:** `100 - (2 / 10 * 100) = 80`.
- **Result:** **PASSED**. UI displays **80** with status **"Good"**.

### Fan Runtime Test
- **Scenario:** 4 snapshots marked as `fanStatus: "ON"`.
- **Logic:** `4 * 15 minutes = 60 minutes`.
- **Result:** **PASSED**. UI displays **"1h 0m"**.

### Signal Status Test
- **Active:** `lastSeen` updated 10s ago. UI shows **"Active" (Green)**.
- **Weak:** `lastSeen` updated 45s ago. UI shows **"Weak" (Orange)**.
- **Offline:** `lastSeen` missing or > 2m old. UI shows **"Offline" (Red)**.

---
> [!SUCCESS]
> **Conclusion:** ScentGuard has successfully transitioned to a technically accurate, data-transparent IoT ecosystem. All metrics are verifiable against the Firestore database.
