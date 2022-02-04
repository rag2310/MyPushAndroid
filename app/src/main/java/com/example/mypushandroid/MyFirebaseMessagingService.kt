package com.example.mypushandroid

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Intent
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class MyFirebaseMessagingService : FirebaseMessagingService() {

    private var broadcaster: LocalBroadcastManager? = null

    override fun onCreate() {
        broadcaster = LocalBroadcastManager.getInstance(this)
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d("Main",token);
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        handleMessage(remoteMessage)

        val notificationId = 2000  // TODO This ID should be managed wisely?


        val notification = NotificationCompat.Builder(this.application, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle("¡Se ha asignado un cargamento!")
            .setContentText("Dirigete al punto de carga. Abre la notificación para ver la ubicación.")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setVibrate(LongArray(2000))
            .setColorized(true)
            .setAutoCancel(true)
            .setChannelId(CHANNEL_ID)

        val manager = NotificationManagerCompat.from(this.application)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Channel human readable title",
                NotificationManager.IMPORTANCE_HIGH
            )
            manager.createNotificationChannel(channel)
        }

        manager.notify(notificationId, notification.build())
    }

    private fun handleMessage(remoteMessage: RemoteMessage) {
        val handler = Handler(Looper.getMainLooper())
        handler.post(Runnable {
            remoteMessage.notification?.let {
                val intent = Intent("MyData")
                intent.putExtra("message", remoteMessage.data["text"]);
                broadcaster?.sendBroadcast(intent);
            }
            Toast.makeText(
                baseContext, getString(R.string.handle_notification_now),
                Toast.LENGTH_LONG
            ).show()
        }
        )
    }

    companion object {
        const val CHANNEL_ID = "NotificationsMessagingService"
        const val REQUEST_ACCEPT = "ShipmentAssigned"
    }
}