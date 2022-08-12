package com.imrkjoseph.fibermobileassistant.service

import android.app.Service
import android.content.Intent
import android.os.IBinder

open class ServiceViewModel : Service() {

    lateinit var onServiceState: (result: ServiceState) -> Unit

    override fun onBind(p0: Intent?): IBinder? {
        return null
    }

    fun getCommandFunction(function: String) {
        try {
            val speakWord = function.split(":")
            val commands = mutableMapOf(
                "executeSpeaking" to Runnable {
                    onServiceState.invoke(ExecuteSpeak(executeSpeak = speakWord[1])
                ) },
                "adjustBrightness" to Runnable {
                    onServiceState.invoke(ExecuteBrightness(brightness = 20F))
                }
            )
            //Check if the function has ":"
            //it means execute speak method.
            commands[if (function.contains(":")) {
                speakWord[0]
            } else {
                function
            }]?.run()
        } catch (e: Exception) { }
    }
}