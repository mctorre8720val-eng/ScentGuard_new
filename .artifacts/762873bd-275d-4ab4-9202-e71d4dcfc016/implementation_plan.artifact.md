# Implementation Plan: Fix Wi-Fi Provisioning Error Handling

This plan addresses the issue where the ScentGuard app incorrectly reports a successful setup even when the ESP32 fails to connect to Wi-Fi (e.g., when a 5 GHz network is used).

## User Review Required

> [!IMPORTANT]
> The fix assumes the ESP32 firmware will be updated to communicate its Wi-Fi connection status back to the app via a new BLE characteristic (`0000FF05`).

## Proposed Changes

### 1. Permissions & Infrastructure

#### [MODIFY] [AndroidManifest.xml](file:///Users/michaelangelotorre/StudioProjects/ScentGuard_new/app/src/main/AndroidManifest.xml)
- Add `ACCESS_WIFI_STATE` and `ACCESS_NETWORK_STATE` to allow frequency detection.

### 2. Provisioning Logic

#### [MODIFY] [ProvisioningViewModel.kt](file:///Users/michaelangelotorre/StudioProjects/ScentGuard_new/app/src/main/java/com/example/scentguard/ui/screens/provisioning/ProvisioningViewModel.kt)
- **New States**: Add `VerifyingWifi` and `WifiError` to `ProvisioningState`.
- **Status Tracking**: Add `statusCharUuid` (`0000FF05-0000-1000-8000-00805F9B34FB`) to monitor Wi-Fi results from the ESP32.
- **Wi-Fi Detection**: Use `WifiManager` to detect if the current network is 5 GHz and expose `wifiWarning` flow.
- **Improved Workflow**:
    - Update `sendCredentials` to enable notifications on the status characteristic.
    - Wait for a specific success/failure code from the hardware instead of a blind 4-second delay.
    - Implement `resetToCredentials()` to allow retries without full device re-scans.

### 3. User Interface

#### [MODIFY] [ProvisioningScreen.kt](file:///Users/michaelangelotorre/StudioProjects/ScentGuard_new/app/src/main/java/com/example/scentguard/ui/screens/provisioning/ProvisioningScreen.kt)
- **Warning Banner**: Display a warning if the app detects a 5 GHz network before sending credentials.
- **Detailed Status**: Update the progress indicator to show "Testing Wi-Fi Connection..." during the verification phase.
- **Error Handling**:
    - Show a clear "Wi-Fi Connection Failed" screen on error.
    - Suggest using a 2.4 GHz network.
    - Provide a "Try Again" button that returns the user to the SSID/Password entry screen.

---

## Verification Plan

### Automated Tests
- Audit `ProvisioningViewModel` to ensure it correctly transitions states based on characteristic updates.
- Verify `WifiManager` logic for frequency detection.

### Manual Verification
1. **Test 1 — Valid 2.4 GHz Wi-Fi**
   - Verify flow: Scanning -> Discovered -> Transferring -> Verifying -> Success.
2. **Test 2 — 5 GHz-only Wi-Fi**
   - Verify that the app shows a warning before connecting.
   - Verify that when ESP32 fails, the app shows "Wi-Fi Connection Failed".
3. **Test 3 — Wrong Credentials**
   - Verify that the app handles the failure and allows a retry.
4. **Test 4 — Retry Flow**
   - Verify "Try Again" returns to the SSID/Password input without losing the BLE connection if possible.
