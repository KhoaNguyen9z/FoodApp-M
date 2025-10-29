package com.example.foodapp.push

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.graphics.Color
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.foodapp.R
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class FoodMessagingService : FirebaseMessagingService() {

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        android.util.Log.d("FCM", "New token: $token")
        // TODO: Send this token to backend so it can target this device
    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)

        createChannelIfNeeded(this)

        val title = message.notification?.title ?: message.data["title"] ?: "Đơn hàng mới"
        val body = message.notification?.body ?: message.data["body"] ?: "Bạn có đơn hàng mới có thể nhận."

        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.foodking)
            .setContentTitle(title)
            .setContentText(body)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()

        NotificationManagerCompat.from(this).notify((System.currentTimeMillis() % 100000).toInt(), notification)
    }

    companion object {
        private const val CHANNEL_ID = "orders_channel"
        fun createChannelIfNeeded(context: Context) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                if (manager.getNotificationChannel(CHANNEL_ID) == null) {
                    val channel = NotificationChannel(
                        CHANNEL_ID,
                        context.getString(R.string.app_name) + " - Đơn hàng",
                        NotificationManager.IMPORTANCE_HIGH
                    ).apply {
                        description = "Thông báo khi có đơn hàng mới"
                        enableLights(true)
                        lightColor = Color.RED
                        enableVibration(true)
                    }
                    manager.createNotificationChannel(channel)
                }
            }
        }
    }
}


