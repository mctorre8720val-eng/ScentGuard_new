package com.example.scentguard.service

import android.app.*
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.media.RingtoneManager
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.scentguard.MainActivity
import com.example.scentguard.R
import com.example.scentguard.ScentGuardApplication
import com.example.scentguard.data.model.HistoryItem
import com.example.scentguard.data.model.HistoryType
import com.google.firebase.Timestamp
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class ScentGuardWatcherService : Service() {

    private val tag = "ScentGuardWatcher"
    private var listenerRegistration: ListenerRegistration? = null
    private var lastKnownAirStatus: String? = null
    private var lastKnownFanStatus: String? = null
    
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    
    companion object {
        private const val NOTIFICATION_ID = 1001
        private const val ALERT_NOTIFICATION_ID = 1002
        private const val CHANNEL_ID = "scentguard_monitoring"
        private const val ALERT_CHANNEL_ID = "scentguard_alerts"
        
        const val ACTION_START = "ACTION_START"
        const val ACTION_STOP = "ACTION_STOP"
        const val EXTRA_RESTAURANT_ID = "EXTRA_RESTAURANT_ID"
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START -> {
                val restaurantId = intent.getStringExtra(EXTRA_RESTAURANT_ID)
                if (restaurantId != null) {
                    startMonitoring(restaurantId)
                }
            }
            ACTION_STOP -> stopSelf()
        }
        return START_STICKY
    }

    private fun startMonitoring(restaurantId: String) {
        createNotificationChannels()
        val notification = createPersistentNotification()
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(NOTIFICATION_ID, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC)
        } else {
            startForeground(NOTIFICATION_ID, notification)
        }

        // Start Firestore Listener
        val db = FirebaseFirestore.getInstance()
        listenerRegistration = db.collection("restaurants").document(restaurantId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e(tag, "Listen failed.", error)
                    return@addSnapshotListener
                }

                if (snapshot != null && snapshot.exists()) {
                    val airStatus = snapshot.getString("airStatus") ?: "SAFE"
                    val fanStatus = snapshot.getString("fanStatus") ?: "OFF"
                    val fanMode = snapshot.getString("fanMode") ?: "AUTO"
                    val gasPpm = snapshot.getLong("currentGasPpm") ?: 0
                    val lastSeen = snapshot.getTimestamp("lastSeen")
                    
                    // 1. Air Status Transitions
                    if (lastKnownAirStatus != null && lastKnownAirStatus != airStatus) {
                        handleAirStatusTransition(restaurantId, lastKnownAirStatus!!, airStatus, gasPpm.toInt(), lastSeen)
                    }
                    
                    // 2. Fan Status Transitions
                    if (lastKnownFanStatus != null && lastKnownFanStatus != fanStatus) {
                        handleFanStatusTransition(restaurantId, fanStatus, fanMode, gasPpm.toInt(), lastSeen)
                    }
                    
                    lastKnownAirStatus = airStatus
                    lastKnownFanStatus = fanStatus
                }
            }
    }

    private fun handleAirStatusTransition(rid: String, old: String, new: String, ppm: Int, ts: Timestamp?) {
        if (new == "DANGER") {
            triggerDangerAlert(ppm)
            logEvent(rid, "AIR_DANGER", "Hazardous Air Detected", "Critical gas concentration alert!", HistoryType.ALERT, ppm, "SYSTEM", ts)
        } else if (old == "DANGER" && (new == "SAFE" || new == "WARN")) {
            logEvent(rid, "AIR_SAFE", "Area Clear", "Gas levels returned to safe parameters", HistoryType.SUCCESS, ppm, "SYSTEM", ts)
        }
    }

    private fun handleFanStatusTransition(rid: String, new: String, mode: String, ppm: Int, ts: Timestamp?) {
        if (new == "ON") {
            val source = if (mode == "ON") "MANUAL" else "AUTOMATIC"
            val desc = if (mode == "ON") "Manual activation by Manager" else "Automatic safety trigger - High gas level"
            logEvent(rid, "FAN_ON", "Ventilation Activated", desc, HistoryType.INFO, ppm, source, ts)
        } else if (new == "OFF") {
            val source = if (mode == "OFF") "MANUAL" else "AUTOMATIC"
            logEvent(rid, "FAN_OFF", "Ventilation Stopped", "Air quality stabilized", HistoryType.INFO, ppm, source, ts)
        }
    }

    private fun logEvent(rid: String, type: String, title: String, desc: String, hType: HistoryType, ppm: Int, source: String, ts: Timestamp?) {
        val anchor = ts ?: Timestamp.now()
        val eventId = "log_${type}_${anchor.seconds}_${anchor.nanoseconds}"
        
        val logItem = HistoryItem(
            id = eventId,
            title = title,
            description = desc,
            timestamp = anchor,
            type = hType,
            value = "$ppm ppm",
            eventType = type,
            gasPpm = ppm,
            source = source,
        )

        serviceScope.launch {
            val app = application as? ScentGuardApplication
            app?.historyRepository?.addLogEntry(rid, logItem)
        }
    }

    private fun triggerDangerAlert(ppm: Int) {
        val intent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE)
        
        val soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM) ?: 
                       RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

        val notification = NotificationCompat.Builder(this, ALERT_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_scentguard_logo_vector)
            .setContentTitle("ScentGuard Critical Alert")
            .setContentText("Hazardous gas detected ($ppm ppm). Check storage immediately.")
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setSound(soundUri)
            .setVibrate(longArrayOf(0, 500, 200, 500, 200, 1000))
            .setFullScreenIntent(pendingIntent, true)
            .setAutoCancel(true)
            .build()

        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(ALERT_NOTIFICATION_ID, notification)
    }

    private fun createPersistentNotification(): Notification {
        val intent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE)

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_scentguard_logo_vector)
            .setContentTitle("ScentGuard is Active")
            .setContentText("Monitoring air quality in the background")
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .build()
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager = getSystemService(NotificationManager::class.java)
            
            // Persistent Channel
            val monitorChannel = NotificationChannel(
                CHANNEL_ID, "Background Monitoring", NotificationManager.IMPORTANCE_LOW
            )
            
            // Alert Channel
            val alertChannel = NotificationChannel(
                ALERT_CHANNEL_ID, "Critical Safety Alerts", NotificationManager.IMPORTANCE_HIGH
            ).apply {
                enableVibration(true)
                description = "Urgent alerts for hazardous conditions"
            }
            
            manager?.createNotificationChannel(monitorChannel)
            manager?.createNotificationChannel(alertChannel)
        }
    }

    override fun onDestroy() {
        listenerRegistration?.remove()
        super.onDestroy()
    }
}
