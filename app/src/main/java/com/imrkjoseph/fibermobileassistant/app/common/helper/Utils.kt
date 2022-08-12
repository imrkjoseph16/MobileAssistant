package com.imrkjoseph.fibermobileassistant.app.common.helper

import android.content.Context
import android.content.SharedPreferences
import android.provider.Settings
import android.speech.SpeechRecognizer
import com.imrkjoseph.fibermobileassistant.app.Default.Companion.commandList
import com.imrkjoseph.fibermobileassistant.app.Default.Companion.key
import com.imrkjoseph.fibermobileassistant.app.Default.Companion.name
import com.imrkjoseph.fibermobileassistant.service.ServiceEnum

class Utils {
    companion object {

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

        fun readCommandList(word: String) : String {
            var function = ""
            commandList.forEach {
                if (word.contains(it.key)) function = it.value
            }
            return function
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
    }
}