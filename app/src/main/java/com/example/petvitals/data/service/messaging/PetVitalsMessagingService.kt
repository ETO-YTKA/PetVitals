package com.example.petvitals.data.service.messaging

import android.Manifest
import android.app.PendingIntent
import android.content.Intent
import android.content.pm.PackageManager
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.petvitals.MainActivity
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class PetVitalsMessagingService : FirebaseMessagingService() {
    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d("FCM_TOKEN", "New token generated: $token")
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        Log.d("FCM_MESSAGE", "From: ${remoteMessage.from}")

        // Check if message contains a data payload.
        remoteMessage.data.isNotEmpty().let {
            Log.d("FCM_MESSAGE", "Message data payload: " + remoteMessage.data)
            // You can handle the data payload here. For example, trigger a sync.
        }

        // Check if message contains a notification payload.
        remoteMessage.notification?.let {
            Log.d("FCM_MESSAGE", "Message Notification Body: ${it.body}")
            // If you want to show a custom notification while the app is in the
            // foreground, you would build and display it here.
            showCustomNotification(it.title, it.body)
        }
    }

    private fun showCustomNotification(title: String?, body: String?) {
        // You need to request permission for this to work on Android 13+
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            Log.w("FCM_SERVICE", "Notification permission not granted, cannot show notification.")
            return
        }

        // Create an explicit intent for an Activity in your app
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent: PendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE)

        val builder = NotificationCompat.Builder(this, "PET_REMINDERS_CHANNEL") // Use the channel ID
            //.setSmallIcon(R.drawable.ic_notification_icon) // Your notification icon
            .setContentTitle(title ?: "New Message")
            .setContentText(body)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent) // Set the intent that will fire when the user taps the notification
            .setAutoCancel(true) // Automatically removes the notification when the user taps it

        with(NotificationManagerCompat.from(this)) {
            // notificationId is a unique int for each notification that you must define
            val notificationId = System.currentTimeMillis().toInt()
            notify(notificationId, builder.build())
        }
    }
}