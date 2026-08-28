# Walkthrough - Phase 5C: Hardware-Led Analytics Pipeline

I have successfully completed the final phase of the IoT integration. ScentGuard now features a fully automated analytics pipeline that populates your charts directly from the hardware, ensuring professional data tracking with zero operational costs.

## 🛠️ What was implemented

### 1. Autonomous Data Sampling (ESP32)
- **15-Minute Snapshots:** The ESP32 is now programmed to take a detailed "snapshot" of the environment every 15 minutes and save it as a permanent record in Firestore.
- **Deterministic ID Logic:** Every snapshot is named based on its time slot (e.g., `snap_20260828_1645`). This prevents data duplication and ensures the history remains clean even if the hardware reboots.
- **Rich Telemetry:** Each snapshot includes the Gas PPM, Air Status, physical Fan Status, and the current Fan Mode for a complete audit trail.

### 2. Live Analytics Mapping (Android)
- **[ChartRepository.kt](file:///Users/michaelangelotorre/StudioProjects/ScentGuard_new/app/src/main/java/com/example/scentguard/data/repository/ChartRepository.kt):** Updated to read the real hardware-generated fields.
- **Dynamic Labels:** Implemented a time-formatter that converts Firestore timestamps into human-readable labels (e.g., "14:30") on the chart's X-axis.
- **Auto-Sorting:** The app automatically sorts data by time to ensure the trend line always draws accurately from left to right.

### 3. Integrated Stability
- **Non-Blocking Operations:** The ESP32 uses three independent timers to handle real-time commands (5s), telemetry (10s), and history (15m) simultaneously without "lag."
- **Free Tier Compliant:** No Cloud Functions or paid billing required. All intelligence is distributed between your Android device and the ESP32 hardware.

---

## 🧪 Testing and Verification

### **Step 1: Rapid Testing Mode**
To see results immediately without waiting 15 minutes:
1. Open the ESP32 code.
2. Change `#define HISTORY_INTERVAL 900000` to **`60000`** (1 minute).
3. Upload the code.

### **Step 2: Monitor the Hardware**
- Open the **Serial Monitor**.
- Look for the message: `>> Logging History Snapshot: snap_2026...` followed by `>> Snapshot OK`.

### **Step 3: Verify the App**
- Open the **Analytics** screen on your phone.
- Verify that real data points are appearing on the "Gas Trend" chart.
- Tap (scrub) the chart to verify the specific PPM values and time labels match your environment.

### **Step 4: Restore Production Interval**
- Once verified, change the interval back to **`900000`** and re-upload.

---
> [!SUCCESS]
> **Conclusion:** With Phase 5C complete, ScentGuard is now a **Production-Ready IoT Ecosystem**. It monitors, alerts, logs, and analyzes air quality autonomously and securely.
