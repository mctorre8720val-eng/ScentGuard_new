package com.example.scentguard.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.scentguard.MainActivity
import com.example.scentguard.R
import com.example.scentguard.ScentGuardApplication
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class ScentGuardMessagingService : FirebaseMessagingService() {

    private val TAG = "ScentGuardMessaging"
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        Log.d(TAG, "From: \${remoteMessage.from}")

        // Check if message contains a notification payload.
        remoteMessage.notification?.let {
            Log.d(TAG, "Message Notification Body: \${it.body}")
            sendNotification(it.title ?: "ScentGuard Alert", it.body ?: "")
        }

        // Also check data payload (useful for background alerts)
        if (remoteMessage.data.isNotEmpty()) {
            Log.d(TAG, "Message data payload: \${remoteMessage.data}")
            val title = remoteMessage.data["title"] ?: "ScentGuard Alert"
            val body = remoteMessage.data["body"] ?: ""
            sendNotification(title, body)
        }
    }

    override fun onNewToken(token: String) {
        Log.d(TAG, "Refreshed token: \$token")
        sendTokenToServer(token)
    }

    private fun sendTokenToServer(token: String) {
        val app = application as? ScentGuardApplication ?: return
        val uid = app.authRepository.currentUser?.uid ?: return
        
        serviceScope.launch {
            app.userRepository.updateFcmToken(uid, token)
        }
    }

    private fun sendNotification(title: String, messageBody: String) {
        val intent = Intent(this, MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_IMMUTABLE
        )

        val channelId = "scentguard_alerts"
        val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        val notificationBuilder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.ic_scentguard_logo_vector)
            .setContentTitle(title)
            .setContentText(messageBody)
            .setAutoCancel(true)
            .setSound(defaultSoundUri)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setVibrate(longArrayOf(1000, 1000, 1000, 1000, 1000))

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "ScentGuard Critical Alerts",
                NotificationManager.IMPORTANCE_HIGH
            )
            channel.description = "Urgent notifications for hazardous gas detection"
            channel.enableVibration(true)
            channel.vibrationPattern = longArrayOf(1000, 1000, 1000, 1000, 1000)
            notificationManager.createNotificationChannel(channel)
        }

        notificationManager.notify(0, notificationBuilder.build())
    }
}
