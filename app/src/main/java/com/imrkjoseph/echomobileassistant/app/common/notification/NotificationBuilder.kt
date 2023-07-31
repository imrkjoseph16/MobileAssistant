package com.imrkjoseph.echomobileassistant.app.common.notification

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Build
import com.imrkjoseph.echomobileassistant.R
import com.imrkjoseph.echomobileassistant.activity.FacilityActivity

class NotificationBuilder {

    companion object {

        fun setupNotification(
            context: Context,
            notificationManager: NotificationManager
        ) : Notification {
            val notificationChannelId = "ECHO SERVICE CHANNEL"

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val channel = NotificationChannel(
                    notificationChannelId,
                    "Echo Service Notification Channel",
                    NotificationManager.IMPORTANCE_HIGH
                ).let {
                    it.description = "Echo Service Channel"
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
                PendingIntent.getActivity(context, 0, notificationIntent, PendingIntent.FLAG_MUTABLE)
            }

            val builder: Notification.Builder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                Notification.Builder(context, notificationChannelId)
            else Notification.Builder(context)

            return builder
                .setContentTitle("Echo Mobile Assistant")
                .setContentText("Please keep echo running on background to continue listening...")
                .setContentIntent(pendingIntent)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setTicker("Echo Assistant")
                .setPriority(Notification.PRIORITY_HIGH)
                .build()
        }
    }
}