package com.imrkjoseph.echomobileassistant.app.common.service

import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log
import com.imrkjoseph.echomobileassistant.app.common.data.NotificationForm
import com.imrkjoseph.echomobileassistant.app.common.data.SmsStateForm

class NotificationService(
    private val notifSmsListener: NotificationSmsListener
) : NotificationListenerService() {

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        super.onNotificationPosted(sbn)
        notifSmsListener.onNotificationReceived(parseNotification(sbn))
    }

    private fun parseNotification(
        sbn: StatusBarNotification?
    ) : NotificationForm {

        val extras = sbn?.notification?.extras

        return NotificationForm(
            id = sbn?.id.toString(),
            packageName = sbn?.packageName,
            ticker = sbn?.notification?.tickerText.toString(),
            title = extras?.getString("android.title"),
            description = extras?.getCharSequence("android.text").toString()
        )
    }

    interface NotificationSmsListener {
        fun onNotificationReceived(notification: NotificationForm)
        fun onSmsReceived(smsForm: SmsStateForm)
    }
}