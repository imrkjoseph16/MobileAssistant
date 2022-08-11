package com.imrkjoseph.fibermobileassistant.app.common.notification

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Build
import com.imrkjoseph.fibermobileassistant.R
import com.imrkjoseph.fibermobileassistant.activity.FacilityActivity

class NotificationBuilder {

    companion object {

        fun setupNotification(
            context: Context,
            notificationManager: NotificationManager
        ) : Notification {
            val notificationChannelId = "FIBER SERVICE CHANNEL"

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val channel = NotificationChannel(
                    notificationChannelId,
                    "Fiber service notifications channel",
                    NotificationManager.IMPORTANCE_HIGH
                ).let {
                    it.description = "Fiber Service Channel"
                    it.enableLights(true)
                    it.lightColor = Color.RED
                    it.enableVibration(true)
                    it.vibrationPattern = longArrayOf(100, 200, 300, 400, 500, 400, 300, 200, 400)
                    it
                }
                notificationManager.createNotificationChannel(channel)
            }

            val pendingIntent: PendingIntent = Intent(context,
                FacilityActivity::class.java).let { notificationIntent ->
                PendingIntent.getActivity(context, 0, notificationIntent, 0)
            }

            val builder: Notification.Builder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                Notification.Builder(context, notificationChannelId)
            else Notification.Builder(context)

            return builder
                .setContentTitle("Fiber Mobile Assistant")
                .setContentText("Please keep fiber running on background to continue listening...")
                .setContentIntent(pendingIntent)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setTicker("Fiber Assistant")
                .setPriority(Notification.PRIORITY_HIGH)
                .build()
        }
    }
}