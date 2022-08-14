package com.imrkjoseph.fibermobileassistant.app.common

import android.content.Context
import androidx.core.content.ContextCompat
import com.imrkjoseph.fibermobileassistant.R

class Default {

    companion object {
        const val ECHO_NAME = "echo"
        const val ERROR_WORD = "Something is wrong, Please try again"
        const val LOG_TAG = "FIBER"

        const val name = "SERVICE_KEY"
        const val key = "SERVICE_STATE"

        //Command Database Variables
        const val DB_COMMANDS = "EchoCommands"
        const val DB_COMMAND_PATH = "commandlist.db"
        const val DB_INPUT = "input"
        const val DB_OUTPUT = "output"
        const val DB_TYPE = "type"
        const val DB_TYPE_QUESTION = "question"

        const val PERMISSIONS_RECORD_AUDIO = 1
        const val PERMISSION_DRAW_OVER_OVERLAY = 2
        const val COUNTDOWN_INTERVAL = 1000L
        const val HOUR_TO_MILLIS = 3600000L
        const val DELAY_SECONDS = 4000L

        val heightList = intArrayOf(22, 26, 20, 25, 18)

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