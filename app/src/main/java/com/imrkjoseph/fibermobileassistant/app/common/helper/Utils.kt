package com.imrkjoseph.fibermobileassistant.app.common.helper

import android.content.Context
import android.content.SharedPreferences
import android.os.CountDownTimer
import android.provider.Settings
import android.speech.SpeechRecognizer
import com.imrkjoseph.fibermobileassistant.app.common.Default.Companion.COUNTDOWN_INTERVAL
import com.imrkjoseph.fibermobileassistant.app.common.Default.Companion.DELAY_SECONDS
import com.imrkjoseph.fibermobileassistant.app.common.Default.Companion.ERROR_WORD
import com.imrkjoseph.fibermobileassistant.app.common.Default.Companion.key
import com.imrkjoseph.fibermobileassistant.app.common.Default.Companion.name
import com.imrkjoseph.fibermobileassistant.service.ServiceEnum
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*
import kotlin.coroutines.CoroutineContext


class Utils {
    companion object {

        fun setCoroutine(dispatcher: CoroutineDispatcher): CoroutineScope {
            val job = Job()
            return object : CoroutineScope {
                override val coroutineContext: CoroutineContext
                    get() = job + dispatcher
            }
        }

        fun setServiceState(context: Context, state: ServiceEnum) {
            val sharedPrefs = getPreferences(context)
            sharedPrefs.edit().let {
                it.putString(key, state.name)
                it.apply()
            }
        }

        fun getServiceState(context: Context): ServiceEnum {
            val sharedPrefs = getPreferences(context)
            val value = sharedPrefs.getString(key, ServiceEnum.STOPPED.name)
            return ServiceEnum.valueOf(value.toString())
        }

        private fun getPreferences(context: Context): SharedPreferences {
            return context.getSharedPreferences(name, 0)
        }

        fun executeDelay(executeDelay: (
            timer: Boolean
        ) -> Unit) {
            object : CountDownTimer(DELAY_SECONDS, COUNTDOWN_INTERVAL) {
                override fun onFinish() = executeDelay.invoke(true)
                override fun onTick(millisUntilFinished: Long) = executeDelay.invoke(false)
            }.start()
        }

        fun getErrorText(errorCode: Int): String {
            val message: String = when (errorCode) {
                SpeechRecognizer.ERROR_AUDIO -> "Audio recording error"
                SpeechRecognizer.ERROR_CLIENT -> "Client side error"
                SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "Insufficient permissions"
                SpeechRecognizer.ERROR_NETWORK -> "Network error"
                SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> "Network timeout"
                SpeechRecognizer.ERROR_NO_MATCH -> "No match"
                SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> "RecognitionService busy"
                SpeechRecognizer.ERROR_SERVER -> "error from server"
                SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "No speech input"
                else -> "Didn't understand, please try again."
            }
            return message
        }

        fun adjustBrightness(brightness: Float, context: Context) : Boolean {
            return try {
                Settings.System.putInt(
                    context.contentResolver,
                    Settings.System.SCREEN_BRIGHTNESS,
                    brightness.toInt()
                )
                true
            } catch (e: Exception) {
                false
            }
        }

        fun getCurrentDateTime(type: String): String {
            var dateFormat: DateFormat? = null

            when(type) {
                "time" -> dateFormat = SimpleDateFormat("h:mm a", Locale.getDefault())
                "date" -> dateFormat = SimpleDateFormat("MM/dd/yyyy", Locale.getDefault())
                "dateTime" -> dateFormat = SimpleDateFormat("MM/dd/yyyy, h:mm a", Locale.getDefault())
            }

            return dateFormat?.format(Date()) ?: ERROR_WORD
        }
    }
}