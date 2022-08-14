package com.imrkjoseph.fibermobileassistant.service

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.util.Log
import com.imrkjoseph.fibermobileassistant.app.di.data.form.CommandForm
import com.imrkjoseph.fibermobileassistant.app.di.data.gateway.repository.CommandRepository
import dagger.hilt.android.AndroidEntryPoint
import java.util.ArrayList
import javax.inject.Inject

@AndroidEntryPoint
open class ServiceViewModel : Service() {

    lateinit var onServiceState: (result: ServiceState) -> Unit

    @Inject
    lateinit var commandRepository: CommandRepository

    override fun onBind(p0: Intent?): IBinder? {
        return null
    }

    fun getCommandFunction(function: String) {
        try {
            val speakWord = function.split(":")
            val commands = mutableMapOf(
                "executeSpeaking" to Runnable {
                    onServiceState.invoke(ExecuteSpeak(wordSpeak = speakWord[1])
                ) },
                "getCurrentDateTime" to Runnable {
                    onServiceState.invoke(GetCurrentDateTime(value = speakWord[1]))
                },
                "adjustBrightness" to Runnable {
                    onServiceState.invoke(ExecuteBrightness(brightness = 20F))
                }
            )
            //Check if the function contains (":")
            //it means execute speak method.
            commands[if (function.contains(":")) {
                speakWord[0]
            } else {
                function
            }]?.run()
        } catch (e: Exception) { }
    }

    fun readCommandList(words: ArrayList<String>?) : CommandForm {
        var function = CommandForm()

        commandRepository.getCommandList().forEach {
            if (words.toString().contains(it.input.toString())) {
                function = it
            }
        }
        return function
    }
}