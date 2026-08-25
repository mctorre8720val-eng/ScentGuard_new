# Implementation Plan - Use Case Diagram (Section 3.2.5)

This plan outlines the design and generation of the ScentGuard Use Case Diagram, ensuring it accurately represents the roles and functional requirements implemented in the application.

## Proposed Use Case Model

### Actors
1.  **Restaurant Manager (Primary Actor):** High-level user who manages the workspace.
2.  **Restaurant Staff (Primary Actor):** Operational user who monitors the environment.
3.  **ScentGuard Hardware (External Actor):** The physical unit that provides sensor telemetry.

### Use Cases
- **UC1: Account Management**: Login, Logout, Profile setup.
- **UC2: Restaurant Setup**: Create restaurant profile (Manager).
- **UC3: Staff Enrollment**: Join restaurant via invitation code (Staff).
- **UC4: Real-time Monitoring**: View PPM levels, temperature, and breathing aura (Both).
- **UC5: Manual Fan Control**: Force Fan ON/OFF (Manager).
- **UC6: Smart Automation**: System-led ventilation based on PPM thresholds (System Internal).
- **UC7: Staff Management**: Review and remove staff members (Manager).
- **UC8: Data Analytics**: View daily/weekly air quality reports (Manager).
- **UC9: System Audit**: View historical logs and notifications (Both).

### Logical Boundaries
- **System Boundary:** The ScentGuard Cloud-Mobile Ecosystem.
- **Relationships:** Showing "Include" for auth and "Extend" for alerts where appropriate.

---

## Proposed Changes

### 1. Documentation
#### [NEW] [scentguard_use_case_diagram.svg](file:///Users/michaelangelotorre/StudioProjects/ScentGuard_new/scentguard_use_case_diagram.svg)
- Generate a professional SVG diagram with clear Actor/System boundaries.

#### [NEW] [scentguard_use_case_mermaid.md](file:///Users/michaelangelotorre/StudioProjects/ScentGuard_new/scentguard_use_case_mermaid.md)
- Provide the Mermaid code for the Use Case diagram.

---

## Verification Plan

### Manual Verification
- **Role Accuracy:** Cross-check with `ScentGuardNavigationDrawer.kt` to ensure Manager-only features are correctly mapped to the Manager actor.
- **Functional Check:** Ensure "Manual Fan Control" and "Invite Codes" are represented as they are core to the multi-role system.
