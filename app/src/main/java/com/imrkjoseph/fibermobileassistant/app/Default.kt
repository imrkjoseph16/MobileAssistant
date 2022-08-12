package com.imrkjoseph.fibermobileassistant.app

import android.content.Context
import androidx.core.content.ContextCompat
import com.imrkjoseph.fibermobileassistant.R

class Default {

    companion object {
        const val PERMISSIONS_RECORD_AUDIO = 1
        const val PERMISSION_DRAW_OVER_OVERLAY = 2
        const val HOUR_TO_MILLIS = 3600000L
        const val ERROR_WORD = "Something is wrong, Please try again"
        const val LOG_TAG = "FIBER"

        const val TYPE_TIME = "time"
        const val TYPE_DATE = "date"
        const val TYPE_DATETIME = "dateTime"

        const val name = "SERVICE_KEY"
        const val key = "SERVICE_STATE"

        val heightList = intArrayOf(22, 26, 20, 25, 18)

        val commandList = hashMapOf(
            "hello" to "executeSpeaking:Hi, How are you?",
            "time" to "getCurrentDateTime:$TYPE_TIME",
            "date" to "getCurrentDateTime:$TYPE_DATE",
            "date and time" to "getCurrentDateTime:$TYPE_DATETIME",
            "adjust brightness" to "adjustBrightness"
        )

        fun getRecognitionColor(context: Context): IntArray {
            return intArrayOf(
                ContextCompat.getColor(context, R.color.color1),
                ContextCompat.getColor(context, R.color.color2),
                ContextCompat.getColor(context, R.color.color3),
                ContextCompat.getColor(context, R.color.color4),
                ContextCompat.getColor(context, R.color.color5)
            )
        }
    }
}