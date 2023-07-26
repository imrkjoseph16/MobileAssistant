package com.imrkjoseph.echomobileassistant.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import com.imrkjoseph.echomobileassistant.app.common.helper.Utils.Companion.getServiceState
import com.imrkjoseph.echomobileassistant.app.common.navigation.Actions

class EchoReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED
            && getServiceState(context) == ServiceEnum.STARTED
        ) {
            Intent(context, EchoService::class.java).also {
                it.action = Actions.START.name
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    context.startForegroundService(it)
                    return
                }
                context.startService(it)
            }
        }
    }
}