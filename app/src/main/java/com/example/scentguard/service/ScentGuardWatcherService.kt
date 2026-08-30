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
import com.example.scentguard.data.model.AlertSound
import com.example.scentguard.data.model.HistoryItem
import com.example.scentguard.data.model.HistoryType
import com.google.firebase.Timestamp
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class ScentGuardWatcherService : Service() {

    private val tag = "ScentGuardWatcher"
    private var listenerRegistration: ListenerRegistration? = null
    private var lastKnownAirStatus: String? = null
    private var lastKnownFanStatus: String? = null
    
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var alertAudioManager: AlertAudioManager? = null
    
    companion object {
        private const val NOTIFICATION_ID = 1001
        private const val ALERT_NOTIFICATION_ID = 1002
        private const val CHANNEL_ID = "scentguard_monitoring"
        private const val ALERT_CHANNEL_ID = "scentguard_alerts"
        
        const val ACTION_START = "ACTION_START"
        const val ACTION_STOP = "ACTION_STOP"
        const val ACTION_STOP_ALARM = "ACTION_STOP_ALARM"
        const val EXTRA_RESTAURANT_ID = "EXTRA_RESTAURANT_ID"
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val app = application as? ScentGuardApplication
        alertAudioManager = app?.alertAudioManager

        when (intent?.action) {
            ACTION_START -> {
                val restaurantId = intent.getStringExtra(EXTRA_RESTAURANT_ID)
                if (restaurantId != null) {
                    startMonitoring(restaurantId)
                }
            }
            ACTION_STOP_ALARM -> {
                alertAudioManager?.stopAlarm()
            }
            ACTION_STOP -> {
                alertAudioManager?.stopAlarm()
                stopSelf()
            }
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
                    val fanStatus = snapshot.getString("fanStatus") ?: "OFF"
                    val fanMode = snapshot.getString("fanMode") ?: "AUTO"
                    val gasPpm = snapshot.getLong("currentGasPpm") ?: 0
                    val temp = snapshot.getDouble("temperature")?.toFloat() ?: 0f
                    val lastSeen = snapshot.getTimestamp("lastSeen")
                    
                    // Dynamic thresholds from Firestore
                    val tWarn = snapshot.getLong("thresholdWarn")?.toInt() ?: 1000
                    val tDanger = snapshot.getLong("thresholdDanger")?.toInt() ?: 1500
                    val twTemp = snapshot.getDouble("tempThresholdWarn")?.toFloat() ?: 40f
                    val tdTemp = snapshot.getDouble("tempThresholdDanger")?.toFloat() ?: 50f

                    // Heartbeat check: Offline status must NEVER trigger the audio alarm
                    val isOnline = lastSeen?.let { (System.currentTimeMillis() - it.toDate().time) < 150000 } ?: false
                    
                    // 1. Air Status Transitions
                    val currentAirStatus = when {
                        gasPpm >= tDanger || temp >= tdTemp -> "DANGER"
                        gasPpm >= tWarn || temp >= twTemp -> "WARN"
                        else -> "SAFE"
                    }
                    
                    if (lastKnownAirStatus != null && lastKnownAirStatus != currentAirStatus) {
                        handleAirStatusTransition(restaurantId, lastKnownAirStatus!!, currentAirStatus, gasPpm.toInt(), temp, lastSeen)
                    }

                    // Audio Alarm Management
                    if (isOnline && currentAirStatus == "DANGER") {
                        // Critical + alarm not playing -> start
                        if (alertAudioManager?.isPlaying() == false) {
                            startAudioAlarm()
                        }
                    } else if (alertAudioManager?.isPlaying() == true) {
                        // No longer critical -> stop
                        alertAudioManager?.stopAlarm()
                    }
                    
                    // 2. Fan Status Transitions
                    if (lastKnownFanStatus != null && lastKnownFanStatus != fanStatus) {
                        handleFanStatusTransition(restaurantId, fanStatus, fanMode, gasPpm.toInt(), lastSeen)
                    }
                    
                    lastKnownAirStatus = currentAirStatus
                    lastKnownFanStatus = fanStatus
                }
            }
    }

    private fun handleAirStatusTransition(rid: String, old: String, new: String, ppm: Int, temp: Float, ts: Timestamp?) {
        if (new == "DANGER") {
            triggerDangerAlert(ppm, temp)
            val desc = if (ppm >= 1500) "Critical gas concentration alert!" else "Critical temperature threshold reached!"
            logEvent(rid, "AIR_DANGER", "Hazardous Conditions Detected", desc, HistoryType.ALERT, ppm, "SYSTEM", ts)
        } else if (old == "DANGER" && (new == "SAFE" || new == "WARN")) {
            logEvent(rid, "AIR_SAFE", "Area Clear", "Conditions returned to safe parameters", HistoryType.SUCCESS, ppm, "SYSTEM", ts)
        }
    }

    private fun startAudioAlarm() {
        serviceScope.launch {
            val app = application as? ScentGuardApplication
            val soundId = app?.preferencesManager?.selectedAlarmSoundId?.first() ?: "critical_alarm"
            val sound = AlertSound.getById(soundId)
            alertAudioManager?.startAlarm(sound.resId)
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

    private fun triggerDangerAlert(ppm: Int, temp: Float) {
        val intent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE)

        val stopIntent = Intent(this, ScentGuardWatcherService::class.java).apply {
            action = ACTION_STOP_ALARM
        }
        val stopPendingIntent = PendingIntent.getService(this, 0, stopIntent, PendingIntent.FLAG_IMMUTABLE)
        
        val soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM) ?: 
                       RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

        val message = "Hazardous conditions: $ppm ppm, \${String.format(java.util.Locale.getDefault(), \"%.1f\", temp)}°C. Check storage immediately."

        val notification = NotificationCompat.Builder(this, ALERT_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_scentguard_logo_vector)
            .setContentTitle("ScentGuard Critical Alert")
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setSound(soundUri)
            .setVibrate(longArrayOf(0, 500, 200, 500, 200, 1000))
            .setFullScreenIntent(pendingIntent, true)
            .addAction(R.drawable.ic_scentguard_logo_vector, "Stop Alarm", stopPendingIntent)
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
