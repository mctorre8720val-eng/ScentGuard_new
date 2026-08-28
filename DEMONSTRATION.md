# ScentGuard: Smart Air Quality & Ventilation System
## Capstone Demonstration Guide

This document provides a structured walkthrough for the ScentGuard system presentation. It is designed to show the seamless integration between hardware (ESP32), cloud (Firebase/Firestore), and the mobile application.

---

## 1. System Overview
**ScentGuard** is an automated IoT solution designed for commercial kitchens and restaurants. It monitors indoor air quality in real-time using an MQ135 gas sensor. When smoke, odors, or harmful gases reach unsafe levels, ScentGuard automatically activates the restaurant's ventilation system. Owners can manage multiple branches, adjust safety thresholds, and view long-term air quality trends directly from their smartphones.

---

## 2. Demo Setup
Before starting the presentation, ensure the following are ready:
1.  **Hardware**: ESP32 board powered on via USB or battery.
2.  **App**: ScentGuard Android App installed on a physical phone.
3.  **Connectivity**: A 2.4GHz Wi-Fi hotspot available for the ESP32.
4.  **Evidence**: Have the **Firestore Console** and **Serial Monitor** (115200 baud) open on a laptop to prove the backend data flow.
5.  **Trigger**: A small amount of Isopropyl alcohol (on a cotton bud) or a lighter (without igniting) to safely simulate a gas spike.

---

## 3. Demo Flow & Presentation Script

### **Phase 1: The "Zero-Code" Provisioning (The Unboxing Experience)**
*Show the panel how easy it is for a non-technical owner to set up the device.*

*   **Action**: Hold the BOOT button on the ESP32 for 5 seconds to clear previous settings. The Serial Monitor will say `Starting BLE Setup Mode`.
*   **Script**: "We start with a fresh ScentGuard unit. Notice that the owner never needs to touch a line of code. By using Bluetooth Low Energy, the hardware automatically identifies itself to our app. I simply scan, enter the restaurant's Wi-Fi, and the app securely 'claims' this device by assigning it a unique Restaurant ID."
*   **Result**: Serial Monitor shows `WiFi OK!` and `Firebase Ready`.

### **Phase 2: Real-Time Monitoring (The Dashboard)**
*Demonstrate the live connection and data visualization.*

*   **Action**: Open the Dashboard on the Android app.
*   **Script**: "Now that we are paired, you can see the Dashboard is 'Active'. ScentGuard is sending a heartbeat every 10 seconds. The gauge shows the live gas concentration in parts-per-million (PPM). Currently, the air is 'SAFE', as indicated by the green status ring."
*   **Evidence**: Point to the `lastSeen` timestamp in Firestore to prove it updates every 10 seconds.

### **Phase 3: Smart Automation (The Fan/Relay)**
*Prove the hardware responds to environmental changes and remote commands.*

*   **Action**: Bring the alcohol/lighter near the sensor until the gauge moves to **WARN** or **DANGER**.
*   **Script**: "Watch as I simulate a smoke spike. The system detects the change instantly. Once it crosses our safety threshold, the RED LED lights up, and the ventilation fan activates automatically. ScentGuard doesn't just watch the problem; it solves it."
*   **Action**: Switch the Fan Mode in the app from **AUTO** to **ON** then **OFF**.
*   **Script**: "For manual control, the Manager can override the system. Even from home, I can force the fans ON or OFF, and the hardware responds within 5 seconds."

### **Phase 4: Analytics & Multi-Tenancy (Business Intelligence)**
*Show long-term data and isolation.*

*   **Action**: Navigate to the **Analytics** screen.
*   **Script**: "ScentGuard takes a snapshot of the kitchen's health every 15 minutes. This creates the trend line you see here. Owners can identify peak hours for smoke or odors and optimize their kitchen staff schedules accordingly."
*   **Action**: Explain (or show) that a second restaurant account has its own independent data.
*   **Script**: "Because ScentGuard is a multi-tenant platform, this hardware belongs strictly to 'Restaurant A'. Even if we have a thousand restaurants, the data is isolated, secure, and perfectly mapped."

---

## 4. Firestore Verification (Technical Proof)
To satisfy technical questions, you can show these paths in the Firebase Console:
1.  **Telemetry**: `restaurants/{restaurantId}` -> Contains `currentGasPpm`, `airStatus`, and `fanStatus`.
2.  **History**: `restaurants/{restaurantId}/sensor_history/{snap_id}` -> Contains the 15-minute data points for the charts.
3.  **Logs**: `restaurants/{restaurantId}/logs` -> Contains individual event entries (e.g., "Fan Activated").

---

## 5. Expected Serial Monitor Output
During the demo, the Serial Monitor should confirm these milestones:
*   `Credentials loaded from NVS`: Proves the device remembers settings after power loss.
*   `WiFi OK!`: Proves successful internet handshake.
*   `>> Sync [RID:...]`: Proves the hardware is reading the correct restaurant settings.
*   `>> Telemetry OK`: Proves data is successfully reaching the cloud.

---

## 6. Backup Plan (What if...?)
*   **Problem**: The sensor isn't spiking or is stuck at 0.
*   **Explanation**: "MQ135 sensors require a 2-minute pre-heat period to stabilize. During this time, the system remains in a safe 'Standby' state to prevent false alarms."
*   **Demo Workaround**: Show the **sensor_history** in Firestore. "Even if we don't see a spike right now, we can see the historical trend from the last hour of testing, proving that the storage pipeline is fully functional."
*   **Problem**: Bluetooth won't connect.
*   **Quick Fix**: Ensure the phone's **GPS/Location** is turned ON. Android blocks Bluetooth scans if GPS is disabled.

---

> [!NOTE]
> **TEST DATA**: Any spikes created during the presentation using alcohol or lighters should be explained to the panel as "Controlled environmental simulations" to demonstrate the system's reactive logic.
