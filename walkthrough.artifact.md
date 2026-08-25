# Walkthrough - Activity Diagram (Section 3.2.6)

I have successfully generated the **Activity Diagram** for your capstone paper. This diagram illustrates the complete logical lifecycle of ScentGuard, from raw sensor detection to real-time app updates and manual overrides.

## Key Logical Flows Illustrated

### 1. The Autonomous Safety Cycle
- **Detection:** Sensors constantly read PPM (gas) and Temp.
- **Decision:** If the mode is set to **AUTO**, the system automatically evaluates gas levels against safety thresholds.
- **Response:** If levels are hazardous, the system triggers the physical fan and generates a push alert simultaneously.

### 2. The Real-time Sync Path
- **Observation:** The Android app uses a "Snapshot Listener" to watch for changes in the cloud document.
- **UI Update:** As data flows from the sensors to the cloud, the app's dashboard (Gauge and Aura) updates instantly without user refresh.

### 3. The Managerial Override
- **Control:** Managers can bypass automated logic to force the fan **ON** or **OFF**.
- **Execution:** The command is written to Firestore, where the ESP32 hardware detects it and executes the relay toggle within seconds.

## Deliverables

- **[scentguard_activity_diagram.svg](file:///Users/michaelangelotorre/StudioProjects/ScentGuard_new/scentguard_activity_diagram.svg)**: A high-resolution, three-swimlane diagram (Hardware, Cloud, App) for visual documentation.
- **[scentguard_activity_mermaid.md](file:///Users/michaelangelotorre/StudioProjects/ScentGuard_new/scentguard_activity_mermaid.md)**: The Mermaid.js source code for dynamic rendering and future edits.

---
> [!TIP]
> **For your Panel Defense:** Use this diagram to explain how ScentGuard maintains a **Continuous Feedback Loop**. It proves that the system is not just "reading" data, but actively "responding" to environmental changes.
