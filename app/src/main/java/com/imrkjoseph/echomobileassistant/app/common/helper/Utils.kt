package com.imrkjoseph.echomobileassistant.app.common.helper

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import android.os.AsyncTask
import android.os.CountDownTimer
import android.os.PowerManager
import android.provider.Settings
import android.speech.SpeechRecognizer
import com.imrkjoseph.echomobileassistant.R
import com.imrkjoseph.echomobileassistant.app.common.Default.Companion.COUNTDOWN_INTERVAL
import com.imrkjoseph.echomobileassistant.app.common.Default.Companion.DELAY_SECONDS
import com.imrkjoseph.echomobileassistant.app.common.Default.Companion.ERROR_WORD
import com.imrkjoseph.echomobileassistant.app.common.Default.Companion.key
import com.imrkjoseph.echomobileassistant.app.common.Default.Companion.name
import com.imrkjoseph.echomobileassistant.service.ServiceEnum
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

        fun executeDelay(delay: Long, executeDelay: (
            timer: Boolean
        ) -> Unit) {
            object : CountDownTimer(delay, COUNTDOWN_INTERVAL) {
                override fun onFinish() = executeDelay.invoke(true)
                override fun onTick(millisUntilFinished: Long) { }
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

        @SuppressLint("StaticFieldLeak")
        fun wakeupScreen(powerManager: PowerManager) {
            object : AsyncTask<Void?, Void?, java.lang.Exception?>() {
                @SuppressLint("WakelockTimeout")
                override fun doInBackground(vararg p0: Void?): java.lang.Exception? {
                    try {
                        @SuppressLint("InvalidWakeLockTag") val fullWakeLock =
                            powerManager.newWakeLock(
                                PowerManager.SCREEN_BRIGHT_WAKE_LOCK or PowerManager.FULL_WAKE_LOCK or PowerManager.ACQUIRE_CAUSES_WAKEUP,
                                "Loneworker - FULL WAKE LOCK"
                            )
                        fullWakeLock.acquire() //Turn on
                        try {
                            Thread.sleep(8000) //Turning on duration
                        } catch (e: InterruptedException) {
                            e.printStackTrace()
                        }
                        fullWakeLock.release()
                    } catch (e: java.lang.Exception) {
                        return e
                    }
                    return null
                }
            }.execute()
        }

        fun formatString(
            context: Context,
            smsDescription: String,
            senderName: String
        ) : String {
            return String.format(
                context.getString(R.string.param_speak_message),
                smsDescription,
                senderName
            )
        }
    }
}