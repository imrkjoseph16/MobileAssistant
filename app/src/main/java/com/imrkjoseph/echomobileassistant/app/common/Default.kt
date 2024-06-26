package com.imrkjoseph.echomobileassistant.app.common

import android.Manifest
import android.content.Context
import androidx.core.content.ContextCompat
import com.imrkjoseph.echomobileassistant.R

class Default {

    companion object {
        const val LOG_TAG = "ECHO"
        const val KEYWORD_ECHO = "echo"
        const val KEYWORD_ECO = "eco"
        const val TEXT_TO_SPEECH_ID = "UniqueID"
        const val ERROR_WORD = "Something is wrong, Please try again"
        const val NOTIFICATION_WORD = "You have a new notification from"
        const val INCOMING_CALL_WORD = "You have an incoming call from"
        const val INCOMING_SMS = "You have received a message from"
        const val INCOMING_NEW_SMS = "You have a new message from "
        const val INCOMING_UNKNOWN_SMS = "You have a new message from unknown number,"
        const val INCOMING_UNKNOWN_CALL_WORD = "You have an incoming call from unknown number"
        const val LEARN_RESPONSE_WORD = "I'm not sure how to response, can you teach me what I can respond to"
        const val SUCCESS_LEARN_RESPONSE = "Noted on this, next time you ask me, I know what I will answer"

        const val PH_NUMBER_FORMAT = "+63"
        const val PH_FIRST_NUMBER_FORMAT = "09"

        const val name = "SERVICE_KEY"
        const val key = "SERVICE_STATE"

        //Command Database Variables
        const val DB_COMMANDS = "EchoCommands"
        const val DB_COMMAND_PATH = "commandlist.db"
        const val DB_INPUT = "input"
        const val DB_OUTPUT = "output"
        const val DB_TYPE = "type"
        const val DB_TYPE_QUESTION = "question"
        const val DB_TYPE_LEARN = "learn"
        const val DB_EXECUTE_SPEAK = "executeSpeaking"
        const val DB_TYPE_WORD = "word"
        const val DB_TYPE_WORD_ARRAY = "wordArray"

        //SMS Permission
        const val SMS_RECEIVED = "android.provider.Telephony.SMS_RECEIVED"
        const val PHONE_STATE = "android.intent.action.PHONE_STATE"
        const val OUTGOING_CALL = "android.intent.action.NEW_OUTGOING_CALL"

        const val ERROR_NO_MATCH = "No match"

        const val PERMISSIONS_ECHO = 1
        const val COUNTDOWN_INTERVAL = 1000L
        const val HOUR_TO_MILLIS = 3600000L
        const val DELAY_SECONDS = 5000L

        val heightList = intArrayOf(22, 26, 20, 25, 18)
        val echoNameList = arrayListOf("echo", "eco")

        fun getRecognitionColor(context: Context) = intArrayOf(
            ContextCompat.getColor(context, R.color.color1),
            ContextCompat.getColor(context, R.color.color2),
            ContextCompat.getColor(context, R.color.color3),
            ContextCompat.getColor(context, R.color.color4),
            ContextCompat.getColor(context, R.color.color5)
        )

        fun getEchoPermissions() = arrayOf(
            Manifest.permission.READ_SMS,
            Manifest.permission.CALL_PHONE,
            Manifest.permission.RECEIVE_SMS,
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.READ_CONTACTS)
    }
}