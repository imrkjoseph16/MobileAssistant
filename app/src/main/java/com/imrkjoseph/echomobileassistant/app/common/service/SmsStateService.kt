package com.imrkjoseph.echomobileassistant.app.common.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.telephony.SmsMessage
import android.telephony.TelephonyManager
import android.util.Log
import com.imrkjoseph.echomobileassistant.app.common.data.SmsStateForm

open class SmsStateService(
    private val notifSmsListener: NotificationService.NotificationSmsListener
) : BroadcastReceiver() {

    override fun onReceive(
        context: Context?,
        intent: Intent
    ?) {
        //Read incoming new messages.
        executeSmsHandler(intent)

        //Read incoming ringing call.
        executeCallHandler(intent)
    }

    private fun executeSmsHandler(intent: Intent?) {
        val extras: Bundle? = intent?.extras
        if (extras != null) {
            val pdus = extras["pdus"] as Array<*>?
            if (pdus != null) {
                for (pdu in pdus) {
                    val smsMessage: SmsMessage = parseIncomingSms(pdu, extras)
                    notifSmsListener.onSmsReceived(
                        SmsStateForm(
                            senderName = smsMessage.displayOriginatingAddress,
                            smsMessage = smsMessage.displayMessageBody
                        )
                    )
                }
            }
        }
    }

    private fun executeCallHandler(intent: Intent?) {
        try {
            val state = intent?.getStringExtra(TelephonyManager.EXTRA_STATE)
            if (state == TelephonyManager.EXTRA_STATE_RINGING) {
                val incomingNumber = intent?.getStringExtra(TelephonyManager.EXTRA_INCOMING_NUMBER)
                notifSmsListener.onSmsReceived(
                    SmsStateForm(
                        smsNumber = incomingNumber,
                        isCalling = true
                    )
                )
            }
        } catch (e: Exception) {
            e.message
        }
    }

    private fun parseIncomingSms(`object`: Any?, bundle: Bundle): SmsMessage {
        val smsMessage: SmsMessage = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val format = bundle.getString("format")
            SmsMessage.createFromPdu(`object` as ByteArray, format)
        } else {
            SmsMessage.createFromPdu(`object` as ByteArray)
        }
        return smsMessage
    }
}