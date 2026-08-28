package com.example.scentguard.ui.screens.provisioning

import android.annotation.SuppressLint
import android.app.Application
import android.bluetooth.*
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanFilter
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.content.Context
import android.location.LocationManager
import android.os.ParcelUuid
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.*

sealed class ProvisioningState {
    object Idle : ProvisioningState()
    object Scanning : ProvisioningState()
    data class DeviceDiscovered(val device: BluetoothDevice) : ProvisioningState()
    object Connecting : ProvisioningState()
    object Transferring : ProvisioningState()
    object Verifying : ProvisioningState()
    object Success : ProvisioningState()
    data class Error(val message: String) : ProvisioningState()
}

@SuppressLint("MissingPermission")
class ProvisioningViewModel(application: Application) : AndroidViewModel(application) {
    private val tag = "ProvisioningVM"
    private val bluetoothManager = application.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
    private val locationManager = application.getSystemService(Context.LOCATION_SERVICE) as LocationManager
    private val adapter = bluetoothManager.adapter

    private val _state = MutableStateFlow<ProvisioningState>(ProvisioningState.Idle)
    val state: StateFlow<ProvisioningState> = _state.asStateFlow()

    private var bluetoothGatt: BluetoothGatt? = null
    private val serviceUuid = UUID.fromString("0000FF01-0000-1000-8000-00805F9B34FB")
    private val ssidCharUuid = UUID.fromString("0000FF02-0000-1000-8000-00805F9B34FB")
    private val passCharUuid = UUID.fromString("0000FF03-0000-1000-8000-00805F9B34FB")
    private val ridCharUuid = UUID.fromString("0000FF04-0000-1000-8000-00805F9B34FB")

    private val scanCallback = object : ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult) {
            val device = result.device
            val scanRecord = result.scanRecord
            val rssi = result.rssi
            val address = device.address
            
            // CRITICAL FIX: Extract name from ScanRecord if device.name is null
            val name = scanRecord?.deviceName ?: try { device.name } catch (e: SecurityException) { null } ?: "Unknown"
            val uuids = scanRecord?.serviceUuids

            Log.d(tag, ">>> SCAN RESULT: Name=$name | Address=$address | RSSI=$rssi | UUIDs=$uuids")

            // Hybrid Matching: Match by name or service UUID
            val isScentGuard = name.contains("ScentGuard", ignoreCase = true) || 
                              uuids?.any { it.uuid == serviceUuid } == true

            if (isScentGuard) {
                Log.i(tag, ">>> SUCCESS: ScentGuard-ESP32 identified! Matching via ${if (name.contains("ScentGuard")) "Name" else "UUID"}")
                _state.value = ProvisioningState.DeviceDiscovered(device)
                stopScan()
            }
        }

        override fun onScanFailed(errorCode: Int) {
            Log.e(tag, ">>> SCAN FAILED: Error Code $errorCode")
            _state.value = ProvisioningState.Error("Scan failed: $errorCode")
        }
    }

    fun startScan() {
        val adapterActive = adapter?.isEnabled == true
        val locationActive = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || 
                             locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
        
        Log.d(tag, ">>> START SCAN REQUESTED. BT: $adapterActive, GPS: $locationActive")
        
        if (!adapterActive) {
            _state.value = ProvisioningState.Error("Bluetooth is disabled")
            return
        }
        
        if (!locationActive) {
            _state.value = ProvisioningState.Error("Location services (GPS) must be ON")
            return
        }

        _state.value = ProvisioningState.Scanning
        
        try {
            val scanner = adapter.bluetoothLeScanner
            if (scanner == null) {
                Log.e(tag, ">>> ERROR: BluetoothLeScanner is NULL")
                _state.value = ProvisioningState.Error("Scanner unavailable")
                return
            }

            Log.d(tag, ">>> INITIATING AGGRESSIVE UNFILTERED SCAN...")
            val settings = ScanSettings.Builder()
                .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
                .setMatchMode(ScanSettings.MATCH_MODE_AGGRESSIVE)
                .setCallbackType(ScanSettings.CALLBACK_TYPE_ALL_MATCHES)
                .build()
                
            // Use null filters to ensure we see EVERYTHING
            scanner.startScan(null, settings, scanCallback)
            Log.i(tag, ">>> SCAN LIVE")
            
        } catch (e: SecurityException) {
            Log.e(tag, ">>> SECURITY EXCEPTION: Permissions missing", e)
            _state.value = ProvisioningState.Error("Permissions missing")
        } catch (e: Exception) {
            Log.e(tag, ">>> GENERAL EXCEPTION: ${e.message}", e)
            _state.value = ProvisioningState.Error("Failed to start scan")
        }
        
        // Timeout after 30 seconds
        viewModelScope.launch {
            delay(30000)
            if (_state.value is ProvisioningState.Scanning) {
                Log.w(tag, ">>> SCAN TIMEOUT REACHED")
                stopScan()
                _state.value = ProvisioningState.Error("No ScentGuard found. Reset ESP32 and try again.")
            }
        }
    }

    fun stopScan() {
        Log.d(tag, ">>> STOP SCAN REQUESTED")
        try {
            adapter?.bluetoothLeScanner?.stopScan(scanCallback)
        } catch (e: Exception) {
            Log.e(tag, "Error stopping scan: ${e.message}")
        }
    }

    fun connectAndProvision(device: BluetoothDevice, ssid: String, pass: String, rid: String) {
        val deviceName = try { device.name ?: "ESP32" } catch (e: SecurityException) { "ESP32" }
        Log.d(tag, "Connecting to GATT: $deviceName (${device.address}) | Assigning RID: $rid")
        _state.value = ProvisioningState.Connecting
        
        bluetoothGatt = device.connectGatt(getApplication(), false, object : BluetoothGattCallback() {
            override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {
                if (newState == BluetoothProfile.STATE_CONNECTED) {
                    Log.i(tag, "GATT connected! Waiting 1s before service discovery...")
                    viewModelScope.launch {
                        delay(1000) // Reliability delay
                        gatt.discoverServices()
                    }
                } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                    Log.w(tag, "GATT disconnected. Status: $status")
                    if (_state.value !is ProvisioningState.Success) {
                        _state.value = ProvisioningState.Error("Connection failed ($status)")
                    }
                }
            }

            override fun onServicesDiscovered(gatt: BluetoothGatt, status: Int) {
                if (status == BluetoothGatt.GATT_SUCCESS) {
                    Log.i(tag, "Services discovered successfully")
                    val service = gatt.getService(serviceUuid)
                    if (service != null) {
                        Log.i(tag, "ScentGuard Service found ($serviceUuid)")
                        _state.value = ProvisioningState.Transferring
                        sendCredentials(gatt, service, ssid, pass, rid)
                    } else {
                        Log.e(tag, "ScentGuard Service NOT found in GATT server. Checking all services...")
                        gatt.services.forEach { s -> Log.d(tag, "Found service: ${s.uuid}") }
                        _state.value = ProvisioningState.Error("Invalid hardware version")
                    }
                } else {
                    Log.e(tag, "Service discovery failed with status: $status")
                    _state.value = ProvisioningState.Error("GATT Service error")
                }
            }
        })
    }

    private fun sendCredentials(gatt: BluetoothGatt, service: BluetoothGattService, ssid: String, pass: String, rid: String) {
        viewModelScope.launch {
            try {
                val ssidChar = service.getCharacteristic(ssidCharUuid)
                val passChar = service.getCharacteristic(passCharUuid)
                val ridChar = service.getCharacteristic(ridCharUuid)

                if (ssidChar != null && passChar != null && ridChar != null) {
                    Log.d(tag, "Transmitting credentials and Restaurant ID...")
                    
                    // 1. Write SSID
                    ssidChar.setValue(ssid)
                    gatt.writeCharacteristic(ssidChar)
                    delay(1000) 

                    // 2. Write Password
                    passChar.setValue(pass)
                    gatt.writeCharacteristic(passChar)
                    delay(1000)

                    // 3. Write Restaurant ID
                    ridChar.setValue(rid)
                    gatt.writeCharacteristic(ridChar)
                    delay(1000)

                    Log.i(tag, "All data sent. Waiting for hardware restart...")
                    _state.value = ProvisioningState.Verifying
                    delay(4000) 
                    
                    Log.i(tag, "Provisioning Success!")
                    _state.value = ProvisioningState.Success
                    gatt.disconnect()
                } else {
                    Log.e(tag, "GATT Characteristics missing! SSID: ${ssidChar!=null}, PASS: ${passChar!=null}, RID: ${ridChar!=null}")
                    _state.value = ProvisioningState.Error("Incompatible firmware")
                }
            } catch (e: Exception) {
                Log.e(tag, "Credential transfer error: ${e.message}")
                _state.value = ProvisioningState.Error("Transfer failed")
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        try {
            bluetoothGatt?.disconnect()
            bluetoothGatt?.close()
        } catch (e: Exception) {}
        stopScan()
    }
}
