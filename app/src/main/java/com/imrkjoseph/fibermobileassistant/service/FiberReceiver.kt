package com.imrkjoseph.fibermobileassistant.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import com.imrkjoseph.fibermobileassistant.app.Actions
import com.imrkjoseph.fibermobileassistant.app.common.ServiceState
import com.imrkjoseph.fibermobileassistant.app.common.getServiceState

class FiberReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED
            && getServiceState(context) == ServiceState.STARTED
        ) {
            Intent(context, FiberService::class.java).also {
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