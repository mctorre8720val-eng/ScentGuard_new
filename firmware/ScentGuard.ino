/**
 * ScentGuard ESP32 Firmware - PRODUCTION VERSION
 * With 2-Channel Relay Sanitation Logic
 *
 * Relay Functions:
 * CH1 (GPIO 23) -> Exhaust/Ventilation Fan
 * CH2 (GPIO 25) -> Sanitation Pump
 */

#include <WiFi.h>
#include <Firebase_ESP_Client.h>
#include <addons/TokenHelper.h>
#include <BLEDevice.h>
#include <BLEUtils.h>
#include <BLEServer.h>
#include <Preferences.h>
#include "time.h"

// =====================================================
// 1. CONFIGURATION
// =====================================================

#define API_KEY "AIzaSyC4J1wPaPlqn5i2UI46CcaaDnwmSu3BKUs"
#define PROJECT_ID "scentguard-4thyear"
#define DATABASE_URL "https://scentguard-4thyear-default-rtdb.asia-southeast1.firebasedatabase.app/"

// BLE UUIDs
#define SERVICE_UUID        "0000FF01-0000-1000-8000-00805F9B34FB"
#define SSID_CHAR_UUID      "0000FF02-0000-1000-8000-00805F9B34FB"
#define PASS_CHAR_UUID      "0000FF03-0000-1000-8000-00805F9B34FB"
#define RID_CHAR_UUID       "0000FF04-0000-1000-8000-00805F9B34FB"

// =====================================================
// 2. TIMING & PINS
// =====================================================

#define TELEMETRY_INTERVAL 10000UL
#define CONFIG_INTERVAL 5000UL
#define HISTORY_INTERVAL 60000UL // 1 Minute (TEST ONLY)
#define SANITATION_DURATION 60000UL

#define RELAY_CH1_PIN 23  // Exhaust Fan
#define RELAY_CH2_PIN 25  // Sanitation Pump
#define MQ135_PIN 34
#define GREEN_LED 18
#define RED_LED 19
#define BOOT_BUTTON 0

// =====================================================
// 3. GLOBALS
// =====================================================

Preferences preferences;
bool isProvisioning = false;
String receivedSSID = "";
String receivedPASS = "";
String receivedRID = "";

String activeRestaurantId = "";
int thresholdWarn = 1000;
int thresholdDanger = 1500;

FirebaseData fbdo_telem, fbdo_config, fbdo_hist;
FirebaseAuth auth;
FirebaseConfig config;

unsigned long lastTelem = 0, lastConfig = 0, lastHist = 0;
String currentFanMode = "AUTO";
bool isFanPhysicallyActive = false;

// Sanitation Logic Variables
unsigned long sanitationStartTime = 0;
bool isSanitationActive = false;
bool wasInDanger = false;
String lastInternalStatus = "SAFE";

// =====================================================
// RELAY CONTROL FUNCTIONS
// =====================================================

void setFan(bool enabled) {
    // Active LOW logic: LOW = ON, HIGH = OFF
    digitalWrite(RELAY_CH1_PIN, enabled ? LOW : HIGH);
    if (isFanPhysicallyActive != enabled) {
        isFanPhysicallyActive = enabled;
        Serial.print("Relay CH1 (Fan): ");
        Serial.println(enabled ? "ON" : "OFF");
    }
}

void setPump(bool enabled) {
    // Active LOW logic: LOW = ON, HIGH = OFF
    digitalWrite(RELAY_CH2_PIN, enabled ? LOW : HIGH);
    if (enabled) {
        Serial.println("Relay CH2 (Sanitation Pump): ON");
    } else {
        Serial.println("Relay CH2 (Sanitation Pump): OFF");
    }
}

// =====================================================
// BLE CALLBACKS
// =====================================================

class MyCallbacks : public BLECharacteristicCallbacks {
    void onWrite(BLECharacteristic *pCharacteristic) {
        String value = String(pCharacteristic->getValue().c_str());
        String uuid = pCharacteristic->getUUID().toString();

        if (uuid.equalsIgnoreCase(SSID_CHAR_UUID)) {
            receivedSSID = value; receivedSSID.trim();
            Serial.print("BLE: SSID Received: "); Serial.println(receivedSSID);
        } else if (uuid.equalsIgnoreCase(PASS_CHAR_UUID)) {
            receivedPASS = value; receivedPASS.trim();
            Serial.println("BLE: Password Received (Hidden)");
        } else if (uuid.equalsIgnoreCase(RID_CHAR_UUID)) {
            receivedRID = value; receivedRID.trim();
            Serial.print("BLE: Restaurant ID Received: "); Serial.println(receivedRID);
        }
    }
};

// =====================================================
// WIFI & PROVISIONING
// =====================================================

void startProvisioning() {
    isProvisioning = true;
    Serial.println("\nStarting BLE Setup Mode (ScentGuard-ESP32)");
    BLEDevice::init("ScentGuard-ESP32");
    BLEServer *pServer = BLEDevice::createServer();
    BLEServer *pServerInstance = pServer; // unused but for structure
    BLEService *pService = pServer->createService(SERVICE_UUID);
    BLECharacteristic *pSSID = pService->createCharacteristic(SSID_CHAR_UUID, BLECharacteristic::PROPERTY_WRITE);
    BLECharacteristic *pPASS = pService->createCharacteristic(PASS_CHAR_UUID, BLECharacteristic::PROPERTY_WRITE);
    BLECharacteristic *pRID = pService->createCharacteristic(RID_CHAR_UUID, BLECharacteristic::PROPERTY_WRITE);
    pSSID->setCallbacks(new MyCallbacks());
    pPASS->setCallbacks(new MyCallbacks());
    pRID->setCallbacks(new MyCallbacks());
    pService->start();
    BLEAdvertising *pAdvertising = BLEDevice::getAdvertising();
    pAdvertising->addServiceUUID(SERVICE_UUID);
    pAdvertising->setScanResponse(true);
    pAdvertising->start();
    Serial.println("Waiting for App connection...");
}

bool loadCredentials() {
    preferences.begin("scentguard", true);
    String ssid = preferences.getString("ssid", "");
    String pass = preferences.getString("pass", "");
    activeRestaurantId = preferences.getString("rid", "");
    preferences.end();
    if (ssid == "" || activeRestaurantId == "") return false;
    Serial.println("\nCredentials loaded. Connecting to: " + ssid);
    WiFi.begin(ssid.c_str(), pass.c_str());
    unsigned long start = millis();
    while (WiFi.status() != WL_CONNECTED && millis() - start < 15000) { delay(500); Serial.print("."); }
    return (WiFi.status() == WL_CONNECTED);
}

// =====================================================
// UTILS & RESTORED HISTORY LOGIC
// =====================================================

String getTimestamp() {
    time_t now = time(nullptr);
    if (now < 100000) return "";
    struct tm timeinfo; gmtime_r(&now, &timeinfo);
    char timestamp[32]; strftime(timestamp, sizeof(timestamp), "%Y-%m-%dT%H:%M:%SZ", &timeinfo);
    return String(timestamp);
}

String getSlotID() {
    struct tm timeinfo;
    if (!getLocalTime(&timeinfo)) return "snap_" + String(millis());
    int slotMin = (timeinfo.tm_min / 15) * 15;
    char buf[32]; strftime(buf, sizeof(buf), "snap_%Y%m%d_%H", &timeinfo);
    String id = String(buf);
    if (slotMin < 10) id += "0";
    id += String(slotMin);
    return id;
}

void syncTime() {
    configTime(0, 0, "pool.ntp.org", "time.nist.gov");
    Serial.print("Syncing Time");
    time_t now = time(nullptr);
    while (now < 8 * 3600 * 2) { delay(500); Serial.print("."); now = time(nullptr); }
    Serial.println("\nTime OK!");
}

void readRemoteConfig() {
    String path = "restaurants/" + activeRestaurantId;
    if (Firebase.Firestore.getDocument(&fbdo_config, PROJECT_ID, "", path.c_str(), "fanMode,thresholdWarn,thresholdDanger")) {
        FirebaseJson json; FirebaseJsonData res; json.setJsonData(fbdo_config.payload());
        if (json.get(res, "fields/fanMode/stringValue")) currentFanMode = res.stringValue;
        if (json.get(res, "fields/thresholdWarn/integerValue")) thresholdWarn = (int)res.intValue;
        if (json.get(res, "fields/thresholdDanger/integerValue")) thresholdDanger = (int)res.intValue;
        Serial.printf(">> Sync [RID:%s]: Mode=%s, Warn=%d, Danger=%d\n", activeRestaurantId.c_str(), currentFanMode.c_str(), thresholdWarn, thresholdDanger);
    }
}

void uploadTelemetry(int gasValue, String airStatus) {
    FirebaseJson content;
    content.set("fields/currentGasPpm/integerValue", gasValue);
    content.set("fields/airStatus/stringValue", airStatus);
    content.set("fields/fanStatus/stringValue", isFanPhysicallyActive ? "ON" : "OFF");
    String ts = getTimestamp(); if (ts != "") content.set("fields/lastSeen/timestampValue", ts);
    String path = "restaurants/" + activeRestaurantId;
    if (Firebase.Firestore.patchDocument(&fbdo_telem, PROJECT_ID, "", path.c_str(), content.raw(), "currentGasPpm,airStatus,fanStatus,lastSeen")) {
        Serial.println(">> Telemetry OK");
    }
}

void uploadHistorySnapshot(int gasValue, String airStatus) {
    Serial.println(">> UPLOADING HISTORY SNAPSHOT...");
    FirebaseJson content;
    content.set("fields/currentGasPpm/integerValue", gasValue);
    content.set("fields/airStatus/stringValue", airStatus);
    content.set("fields/fanStatus/stringValue", isFanPhysicallyActive ? "ON" : "OFF");
    content.set("fields/fanMode/stringValue", currentFanMode);
    String ts = getTimestamp(); if (ts != "") content.set("fields/timestamp/timestampValue", ts);

    String slotId = getSlotID();
    String path = "restaurants/" + activeRestaurantId + "/sensor_history/" + slotId;
    if (Firebase.Firestore.patchDocument(&fbdo_hist, PROJECT_ID, "", path.c_str(), content.raw(), "currentGasPpm,airStatus,fanStatus,fanMode,timestamp")) {
        Serial.println(">> History Snapshot OK: " + slotId);
    } else {
        Serial.println(">> History Snapshot FAILED: " + fbdo_hist.errorReason());
    }
}

// =====================================================
// SETUP & LOOP
// =====================================================

void setup() {
    Serial.begin(115200);

    // Initialize Relays (Active LOW: HIGH = OFF)
    pinMode(RELAY_CH1_PIN, OUTPUT); digitalWrite(RELAY_CH1_PIN, HIGH);
    pinMode(RELAY_CH2_PIN, OUTPUT); digitalWrite(RELAY_CH2_PIN, HIGH);

    pinMode(GREEN_LED, OUTPUT); pinMode(RED_LED, OUTPUT);
    pinMode(BOOT_BUTTON, INPUT_PULLUP);

    if (digitalRead(BOOT_BUTTON) == LOW) {
        Serial.println("Reset Mode... Hold 5s"); delay(5000);
        if (digitalRead(BOOT_BUTTON) == LOW) {
            preferences.begin("scentguard", false); preferences.clear(); preferences.end();
            Serial.println("NVS Cleared. Restarting..."); ESP.restart();
        }
    }

    if (!loadCredentials()) {
        startProvisioning();
    } else {
        syncTime();
        config.api_key = API_KEY; config.database_url = DATABASE_URL; config.token_status_callback = tokenStatusCallback;
        Firebase.signUp(&config, &auth, "", "");
        Firebase.begin(&config, &auth);
        Firebase.reconnectWiFi(true);
        Serial.println("Firebase Ready.");
    }
}

void loop() {
    if (isProvisioning) {
        digitalWrite(RED_LED, (millis() / 500) % 2 == 0);
        if (receivedSSID != "" && receivedPASS != "" && receivedRID != "") {
            Serial.println("\nTesting connection...");
            WiFi.disconnect(true); delay(1000);
            WiFi.begin(receivedSSID.c_str(), receivedPASS.c_str());
            unsigned long start = millis();
            while (WiFi.status() != WL_CONNECTED && millis() - start < 10000) { delay(500); Serial.print("."); }
            if (WiFi.status() == WL_CONNECTED) {
                preferences.begin("scentguard", false);
                preferences.putString("ssid", receivedSSID); preferences.putString("pass", receivedPASS); preferences.putString("rid", receivedRID);
                preferences.end();
                Serial.println("\nSuccess! Restarting..."); delay(2000); ESP.restart();
            } else {
                Serial.println("\nFailed. Waiting for new credentials...");
                receivedSSID = ""; receivedPASS = ""; receivedRID = "";
            }
        }
        return;
    }

    // 1. Air Quality Processing
    int gasValue = analogRead(MQ135_PIN);
    String airStatus = (gasValue >= thresholdDanger) ? "DANGER" : (gasValue >= thresholdWarn ? "WARN" : "SAFE");

    // Log status change
    if (airStatus != lastInternalStatus) {
        Serial.print("Air Status: ");
        Serial.println(airStatus);

        // Safety Interruption Logic
        if (airStatus != "SAFE" && isSanitationActive) {
            Serial.println("Sanitation interrupted: Air quality increased");
            isSanitationActive = false;
            setPump(false);
        }

        // Track DANGER state for sanitation triggering
        if (airStatus == "DANGER") {
            wasInDanger = true;
        }

        // Trigger Sanitation Cycle on DANGER -> SAFE transition
        if (wasInDanger && airStatus == "SAFE" && !isSanitationActive) {
            Serial.println("Sanitation cycle started (60 seconds)");
            isSanitationActive = true;
            sanitationStartTime = millis();
            wasInDanger = false;
            setPump(true);
        }

        lastInternalStatus = airStatus;
    }

    // 2. Sanitation Cycle Timer (Non-blocking)
    if (isSanitationActive) {
        if (millis() - sanitationStartTime >= SANITATION_DURATION) {
            Serial.println("Sanitation cycle completed");
            isSanitationActive = false;
            setPump(false);
        }
    }

    // 3. Fan Control Logic
    bool fanShouldBeOn = false;
    if (currentFanMode == "ON") {
        fanShouldBeOn = true;
    } else if (currentFanMode == "OFF") {
        fanShouldBeOn = false;
    } else {
        // AUTO Mode
        fanShouldBeOn = (airStatus != "SAFE");
    }

    // During Sanitation, Fan must be OFF unless overridden by ON mode or dangerous conditions
    if (isSanitationActive && currentFanMode == "AUTO") {
        fanShouldBeOn = false;
    }

    setFan(fanShouldBeOn);

    // 4. Indicator LEDs
    digitalWrite(GREEN_LED, (airStatus == "SAFE"));
    digitalWrite(RED_LED, (airStatus != "SAFE"));

    // 5. Cloud Sync & Telemetry
    if (Firebase.ready() && (lastConfig == 0 || millis() - lastConfig >= CONFIG_INTERVAL)) {
        lastConfig = millis(); readRemoteConfig();
    }

    if (Firebase.ready() && (lastTelem == 0 || millis() - lastTelem >= TELEMETRY_INTERVAL)) {
        lastTelem = millis(); uploadTelemetry(gasValue, airStatus);
    }

    if (Firebase.ready() && (lastHist == 0 || millis() - lastHist >= HISTORY_INTERVAL)) {
        lastHist = millis(); uploadHistorySnapshot(gasValue, airStatus);
    }

    delay(1000);
}/**
 * ScentGuard ESP32 Firmware - PRODUCTION VERSION
 * With 2-Channel Relay Sanitation Logic
 *
 * Relay Functions:
 * CH1 (GPIO 23) -> Exhaust/Ventilation Fan
 * CH2 (GPIO 25) -> Sanitation Pump
 */

#include <WiFi.h>
#include <Firebase_ESP_Client.h>
#include <addons/TokenHelper.h>
#include <BLEDevice.h>
#include <BLEUtils.h>
#include <BLEServer.h>
#include <Preferences.h>
#include "time.h"

// =====================================================
// 1. CONFIGURATION
// =====================================================

#define API_KEY "AIzaSyC4J1wPaPlqn5i2UI46CcaaDnwmSu3BKUs"
#define PROJECT_ID "scentguard-4thyear"
#define DATABASE_URL "https://scentguard-4thyear-default-rtdb.asia-southeast1.firebasedatabase.app/"

// BLE UUIDs
#define SERVICE_UUID        "0000FF01-0000-1000-8000-00805F9B34FB"
#define SSID_CHAR_UUID      "0000FF02-0000-1000-8000-00805F9B34FB"
#define PASS_CHAR_UUID      "0000FF03-0000-1000-8000-00805F9B34FB"
#define RID_CHAR_UUID       "0000FF04-0000-1000-8000-00805F9B34FB"

// =====================================================
// 2. TIMING & PINS
// =====================================================

#define TELEMETRY_INTERVAL 10000UL
#define CONFIG_INTERVAL 5000UL
#define HISTORY_INTERVAL 60000UL // 1 Minute (TEST ONLY)
#define SANITATION_DURATION 60000UL

#define RELAY_CH1_PIN 23  // Exhaust Fan
#define RELAY_CH2_PIN 25  // Sanitation Pump
#define MQ135_PIN 34
#define GREEN_LED 18
#define RED_LED 19
#define BOOT_BUTTON 0

// =====================================================
// 3. GLOBALS
// =====================================================

Preferences preferences;
bool isProvisioning = false;
String receivedSSID = "";
String receivedPASS = "";
String receivedRID = "";

String activeRestaurantId = "";
int thresholdWarn = 1000;
int thresholdDanger = 1500;

FirebaseData fbdo_telem, fbdo_config, fbdo_hist;
FirebaseAuth auth;
FirebaseConfig config;

unsigned long lastTelem = 0, lastConfig = 0, lastHist = 0;
String currentFanMode = "AUTO";
bool isFanPhysicallyActive = false;

// Sanitation Logic Variables
unsigned long sanitationStartTime = 0;
bool isSanitationActive = false;
bool wasInDanger = false;
String lastInternalStatus = "SAFE";

// =====================================================
// RELAY CONTROL FUNCTIONS
// =====================================================

void setFan(bool enabled) {
    // Active LOW logic: LOW = ON, HIGH = OFF
    digitalWrite(RELAY_CH1_PIN, enabled ? LOW : HIGH);
    if (isFanPhysicallyActive != enabled) {
        isFanPhysicallyActive = enabled;
        Serial.print("Relay CH1 (Fan): ");
        Serial.println(enabled ? "ON" : "OFF");
    }
}

void setPump(bool enabled) {
    // Active LOW logic: LOW = ON, HIGH = OFF
    digitalWrite(RELAY_CH2_PIN, enabled ? LOW : HIGH);
    if (enabled) {
        Serial.println("Relay CH2 (Sanitation Pump): ON");
    } else {
        Serial.println("Relay CH2 (Sanitation Pump): OFF");
    }
}

// =====================================================
// BLE CALLBACKS
// =====================================================

class MyCallbacks : public BLECharacteristicCallbacks {
    void onWrite(BLECharacteristic *pCharacteristic) {
        String value = String(pCharacteristic->getValue().c_str());
        String uuid = pCharacteristic->getUUID().toString();

        if (uuid.equalsIgnoreCase(SSID_CHAR_UUID)) {
            receivedSSID = value; receivedSSID.trim();
            Serial.print("BLE: SSID Received: "); Serial.println(receivedSSID);
        } else if (uuid.equalsIgnoreCase(PASS_CHAR_UUID)) {
            receivedPASS = value; receivedPASS.trim();
            Serial.println("BLE: Password Received (Hidden)");
        } else if (uuid.equalsIgnoreCase(RID_CHAR_UUID)) {
            receivedRID = value; receivedRID.trim();
            Serial.print("BLE: Restaurant ID Received: "); Serial.println(receivedRID);
        }
    }
};

// =====================================================
// WIFI & PROVISIONING
// =====================================================

void startProvisioning() {
    isProvisioning = true;
    Serial.println("\nStarting BLE Setup Mode (ScentGuard-ESP32)");
    BLEDevice::init("ScentGuard-ESP32");
    BLEServer *pServer = BLEDevice::createServer();
    BLEServer *pServerInstance = pServer; // unused but for structure
    BLEService *pService = pServer->createService(SERVICE_UUID);
    BLECharacteristic *pSSID = pService->createCharacteristic(SSID_CHAR_UUID, BLECharacteristic::PROPERTY_WRITE);
    BLECharacteristic *pPASS = pService->createCharacteristic(PASS_CHAR_UUID, BLECharacteristic::PROPERTY_WRITE);
    BLECharacteristic *pRID = pService->createCharacteristic(RID_CHAR_UUID, BLECharacteristic::PROPERTY_WRITE);
    pSSID->setCallbacks(new MyCallbacks());
    pPASS->setCallbacks(new MyCallbacks());
    pRID->setCallbacks(new MyCallbacks());
    pService->start();
    BLEAdvertising *pAdvertising = BLEDevice::getAdvertising();
    pAdvertising->addServiceUUID(SERVICE_UUID);
    pAdvertising->setScanResponse(true);
    pAdvertising->start();
    Serial.println("Waiting for App connection...");
}

bool loadCredentials() {
    preferences.begin("scentguard", true);
    String ssid = preferences.getString("ssid", "");
    String pass = preferences.getString("pass", "");
    activeRestaurantId = preferences.getString("rid", "");
    preferences.end();
    if (ssid == "" || activeRestaurantId == "") return false;
    Serial.println("\nCredentials loaded. Connecting to: " + ssid);
    WiFi.begin(ssid.c_str(), pass.c_str());
    unsigned long start = millis();
    while (WiFi.status() != WL_CONNECTED && millis() - start < 15000) { delay(500); Serial.print("."); }
    return (WiFi.status() == WL_CONNECTED);
}

// =====================================================
// UTILS & RESTORED HISTORY LOGIC
// =====================================================

String getTimestamp() {
    time_t now = time(nullptr);
    if (now < 100000) return "";
    struct tm timeinfo; gmtime_r(&now, &timeinfo);
    char timestamp[32]; strftime(timestamp, sizeof(timestamp), "%Y-%m-%dT%H:%M:%SZ", &timeinfo);
    return String(timestamp);
}

String getSlotID() {
    struct tm timeinfo;
    if (!getLocalTime(&timeinfo)) return "snap_" + String(millis());
    int slotMin = (timeinfo.tm_min / 15) * 15;
    char buf[32]; strftime(buf, sizeof(buf), "snap_%Y%m%d_%H", &timeinfo);
    String id = String(buf);
    if (slotMin < 10) id += "0";
    id += String(slotMin);
    return id;
}

void syncTime() {
    configTime(0, 0, "pool.ntp.org", "time.nist.gov");
    Serial.print("Syncing Time");
    time_t now = time(nullptr);
    while (now < 8 * 3600 * 2) { delay(500); Serial.print("."); now = time(nullptr); }
    Serial.println("\nTime OK!");
}

void readRemoteConfig() {
    String path = "restaurants/" + activeRestaurantId;
    if (Firebase.Firestore.getDocument(&fbdo_config, PROJECT_ID, "", path.c_str(), "fanMode,thresholdWarn,thresholdDanger")) {
        FirebaseJson json; FirebaseJsonData res; json.setJsonData(fbdo_config.payload());
        if (json.get(res, "fields/fanMode/stringValue")) currentFanMode = res.stringValue;
        if (json.get(res, "fields/thresholdWarn/integerValue")) thresholdWarn = (int)res.intValue;
        if (json.get(res, "fields/thresholdDanger/integerValue")) thresholdDanger = (int)res.intValue;
        Serial.printf(">> Sync [RID:%s]: Mode=%s, Warn=%d, Danger=%d\n", activeRestaurantId.c_str(), currentFanMode.c_str(), thresholdWarn, thresholdDanger);
    }
}

void uploadTelemetry(int gasValue, String airStatus) {
    FirebaseJson content;
    content.set("fields/currentGasPpm/integerValue", gasValue);
    content.set("fields/airStatus/stringValue", airStatus);
    content.set("fields/fanStatus/stringValue", isFanPhysicallyActive ? "ON" : "OFF");
    String ts = getTimestamp(); if (ts != "") content.set("fields/lastSeen/timestampValue", ts);
    String path = "restaurants/" + activeRestaurantId;
    if (Firebase.Firestore.patchDocument(&fbdo_telem, PROJECT_ID, "", path.c_str(), content.raw(), "currentGasPpm,airStatus,fanStatus,lastSeen")) {
        Serial.println(">> Telemetry OK");
    }
}

void uploadHistorySnapshot(int gasValue, String airStatus) {
    Serial.println(">> UPLOADING HISTORY SNAPSHOT...");
    FirebaseJson content;
    content.set("fields/currentGasPpm/integerValue", gasValue);
    content.set("fields/airStatus/stringValue", airStatus);
    content.set("fields/fanStatus/stringValue", isFanPhysicallyActive ? "ON" : "OFF");
    content.set("fields/fanMode/stringValue", currentFanMode);
    String ts = getTimestamp(); if (ts != "") content.set("fields/timestamp/timestampValue", ts);

    String slotId = getSlotID();
    String path = "restaurants/" + activeRestaurantId + "/sensor_history/" + slotId;
    if (Firebase.Firestore.patchDocument(&fbdo_hist, PROJECT_ID, "", path.c_str(), content.raw(), "currentGasPpm,airStatus,fanStatus,fanMode,timestamp")) {
        Serial.println(">> History Snapshot OK: " + slotId);
    } else {
        Serial.println(">> History Snapshot FAILED: " + fbdo_hist.errorReason());
    }
}

// =====================================================
// SETUP & LOOP
// =====================================================

void setup() {
    Serial.begin(115200);

    // Initialize Relays (Active LOW: HIGH = OFF)
    pinMode(RELAY_CH1_PIN, OUTPUT); digitalWrite(RELAY_CH1_PIN, HIGH);
    pinMode(RELAY_CH2_PIN, OUTPUT); digitalWrite(RELAY_CH2_PIN, HIGH);

    pinMode(GREEN_LED, OUTPUT); pinMode(RED_LED, OUTPUT);
    pinMode(BOOT_BUTTON, INPUT_PULLUP);

    if (digitalRead(BOOT_BUTTON) == LOW) {
        Serial.println("Reset Mode... Hold 5s"); delay(5000);
        if (digitalRead(BOOT_BUTTON) == LOW) {
            preferences.begin("scentguard", false); preferences.clear(); preferences.end();
            Serial.println("NVS Cleared. Restarting..."); ESP.restart();
        }
    }

    if (!loadCredentials()) {
        startProvisioning();
    } else {
        syncTime();
        config.api_key = API_KEY; config.database_url = DATABASE_URL; config.token_status_callback = tokenStatusCallback;
        Firebase.signUp(&config, &auth, "", "");
        Firebase.begin(&config, &auth);
        Firebase.reconnectWiFi(true);
        Serial.println("Firebase Ready.");
    }
}

void loop() {
    if (isProvisioning) {
        digitalWrite(RED_LED, (millis() / 500) % 2 == 0);
        if (receivedSSID != "" && receivedPASS != "" && receivedRID != "") {
            Serial.println("\nTesting connection...");
            WiFi.disconnect(true); delay(1000);
            WiFi.begin(receivedSSID.c_str(), receivedPASS.c_str());
            unsigned long start = millis();
            while (WiFi.status() != WL_CONNECTED && millis() - start < 10000) { delay(500); Serial.print("."); }
            if (WiFi.status() == WL_CONNECTED) {
                preferences.begin("scentguard", false);
                preferences.putString("ssid", receivedSSID); preferences.putString("pass", receivedPASS); preferences.putString("rid", receivedRID);
                preferences.end();
                Serial.println("\nSuccess! Restarting..."); delay(2000); ESP.restart();
            } else {
                Serial.println("\nFailed. Waiting for new credentials...");
                receivedSSID = ""; receivedPASS = ""; receivedRID = "";
            }
        }
        return;
    }

    // 1. Air Quality Processing
    int gasValue = analogRead(MQ135_PIN);
    String airStatus = (gasValue >= thresholdDanger) ? "DANGER" : (gasValue >= thresholdWarn ? "WARN" : "SAFE");

    // Log status change
    if (airStatus != lastInternalStatus) {
        Serial.print("Air Status: ");
        Serial.println(airStatus);

        // Safety Interruption Logic
        if (airStatus != "SAFE" && isSanitationActive) {
            Serial.println("Sanitation interrupted: Air quality increased");
            isSanitationActive = false;
            setPump(false);
        }

        // Track DANGER state for sanitation triggering
        if (airStatus == "DANGER") {
            wasInDanger = true;
        }

        // Trigger Sanitation Cycle on DANGER -> SAFE transition
        if (wasInDanger && airStatus == "SAFE" && !isSanitationActive) {
            Serial.println("Sanitation cycle started (60 seconds)");
            isSanitationActive = true;
            sanitationStartTime = millis();
            wasInDanger = false;
            setPump(true);
        }

        lastInternalStatus = airStatus;
    }

    // 2. Sanitation Cycle Timer (Non-blocking)
    if (isSanitationActive) {
        if (millis() - sanitationStartTime >= SANITATION_DURATION) {
            Serial.println("Sanitation cycle completed");
            isSanitationActive = false;
            setPump(false);
        }
    }

    // 3. Fan Control Logic
    bool fanShouldBeOn = false;
    if (currentFanMode == "ON") {
        fanShouldBeOn = true;
    } else if (currentFanMode == "OFF") {
        fanShouldBeOn = false;
    } else {
        // AUTO Mode
        fanShouldBeOn = (airStatus != "SAFE");
    }

    // During Sanitation, Fan must be OFF unless overridden by ON mode or dangerous conditions
    if (isSanitationActive && currentFanMode == "AUTO") {
        fanShouldBeOn = false;
    }

    setFan(fanShouldBeOn);

    // 4. Indicator LEDs
    digitalWrite(GREEN_LED, (airStatus == "SAFE"));
    digitalWrite(RED_LED, (airStatus != "SAFE"));

    // 5. Cloud Sync & Telemetry
    if (Firebase.ready() && (lastConfig == 0 || millis() - lastConfig >= CONFIG_INTERVAL)) {
        lastConfig = millis(); readRemoteConfig();
    }

    if (Firebase.ready() && (lastTelem == 0 || millis() - lastTelem >= TELEMETRY_INTERVAL)) {
        lastTelem = millis(); uploadTelemetry(gasValue, airStatus);
    }

    if (Firebase.ready() && (lastHist == 0 || millis() - lastHist >= HISTORY_INTERVAL)) {
        lastHist = millis(); uploadHistorySnapshot(gasValue, airStatus);
    }

    delay(1000);
}