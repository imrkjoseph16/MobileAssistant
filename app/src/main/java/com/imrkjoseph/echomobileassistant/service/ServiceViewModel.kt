package com.imrkjoseph.echomobileassistant.service

import android.app.Service
import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.IBinder
import android.os.PowerManager
import android.provider.ContactsContract
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.imrkjoseph.echomobileassistant.app.common.Default.Companion.DB_EXECUTE_SPEAK
import com.imrkjoseph.echomobileassistant.app.common.Default.Companion.DB_TYPE_WORD
import com.imrkjoseph.echomobileassistant.app.common.Default.Companion.INCOMING_CALL_WORD
import com.imrkjoseph.echomobileassistant.app.common.Default.Companion.INCOMING_NEW_SMS
import com.imrkjoseph.echomobileassistant.app.common.Default.Companion.INCOMING_SMS
import com.imrkjoseph.echomobileassistant.app.common.Default.Companion.INCOMING_UNKNOWN_CALL_WORD
import com.imrkjoseph.echomobileassistant.app.common.Default.Companion.INCOMING_UNKNOWN_SMS
import com.imrkjoseph.echomobileassistant.app.common.Default.Companion.OUTGOING_CALL
import com.imrkjoseph.echomobileassistant.app.common.Default.Companion.PHONE_STATE
import com.imrkjoseph.echomobileassistant.app.common.Default.Companion.PH_FIRST_NUMBER_FORMAT
import com.imrkjoseph.echomobileassistant.app.common.Default.Companion.PH_NUMBER_FORMAT
import com.imrkjoseph.echomobileassistant.app.common.Default.Companion.SMS_RECEIVED
import com.imrkjoseph.echomobileassistant.app.common.data.NotificationForm
import com.imrkjoseph.echomobileassistant.app.common.data.SmsStateForm
import com.imrkjoseph.echomobileassistant.app.common.helper.Utils.Companion.formatString
import com.imrkjoseph.echomobileassistant.app.common.helper.Utils.Companion.removeWordEcho
import com.imrkjoseph.echomobileassistant.app.common.helper.Utils.Companion.wakeupScreen
import com.imrkjoseph.echomobileassistant.app.common.service.SmsStateService
import com.imrkjoseph.echomobileassistant.app.di.data.form.CommandForm
import com.imrkjoseph.echomobileassistant.app.di.data.gateway.repository.CommandRepository
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
open class ServiceViewModel : Service() {

    lateinit var onServiceState: (result: ServiceState) -> Unit

    @Inject
    lateinit var commandRepository: CommandRepository

    override fun onBind(p0: Intent?): IBinder? = null

    fun registerReceiver(
        context: Context,
        smsStateService: SmsStateService) {
        smsStateService.apply {
            registerReceiver(this, IntentFilter(SMS_RECEIVED))
            registerReceiver(this, IntentFilter(PHONE_STATE))
            registerReceiver(this, IntentFilter(OUTGOING_CALL))
            LocalBroadcastManager.getInstance(context)
                .registerReceiver(smsStateService, IntentFilter("Msg"))
        }
    }

    fun getCommandFunction(
        commandForm: CommandForm,
        words: ArrayList<String>?
    ) {
        val function = commandForm.output

        if (function == null) {
            // Check if function return null,
            // it means echo words on commandList is not found,
            // and echo needs to learn a new response.
            onServiceState.invoke(LearnNewResponse(removeWordEcho(words.toString())))
        } else {
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
                    },
                    "readNotification" to Runnable {
                        onServiceState.invoke(ReadNotification)
                    }
                )
                // Check if the function contains (":")
                // it means execute speak method.
                commands[if (function.contains(":")) {
                    speakWord[0]
                } else {
                    function
                }]?.run()
            } catch (e: Exception) { }
        }
    }

    fun addNewResponse(commandForm: CommandForm) : Boolean {
        val rowCount = commandRepository.addNewResponse(commandForm)
        return rowCount.toInt() != -1
    }

    fun mapNewCommandForm(
        newKeyWord: String,
        newResponse: String?
    ) = CommandForm(
        input = newKeyWord,
        output = "$DB_EXECUTE_SPEAK:$newResponse",
        type = DB_TYPE_WORD
    )

    fun readCommandList(words: ArrayList<String>?) : CommandForm {
        var function = CommandForm()

        commandRepository.getCommandList().forEach {
            if (words.toString().contains(it.input.toString())) function = it
        }
        return function
    }

    fun handlingSmsState(
        context: Context,
        smsForm: SmsStateForm?
    ) {
        val powerManager = getSystemService(Context.POWER_SERVICE) as PowerManager
        wakeupScreen(powerManager)

        var contactName = smsForm?.senderName.toString()
        val smsType: String?

        if (contactName.substring(0, 3).contains(PH_NUMBER_FORMAT)) {
            contactName = contactName.replace(PH_NUMBER_FORMAT, "0")
        }

        val cr: ContentResolver = context.contentResolver
        //  Fetch the matching number
        val cursor = cr.query(
            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
            null,
            ContactsContract.CommonDataKinds.Phone.NUMBER + " = ?",
            arrayOf(contactName), null
        )

        if (contactName.substring(0, 2) == PH_FIRST_NUMBER_FORMAT) {
            if (cursor!!.count > 0) {
                while (cursor.moveToNext()) {
                    contactName = cursor.getString(cursor.getColumnIndex(
                            ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME
                        )
                    )
                }
                cursor.close()
                smsType = INCOMING_SMS
            } else {
                smsType = INCOMING_UNKNOWN_SMS
            }
        } else {
            smsType = INCOMING_NEW_SMS
        }

        onServiceState.invoke(HandleNotification(
            notificationForm = NotificationForm(
                packageName = smsForm?.senderName,
                title = formatString(
                    context,
                    smsType,
                    contactName
                ),
                description = smsForm?.smsMessage
            )
        ))
    }

    fun handlingCallState(
        context: Context,
        callerNumber: String?
    ) {
        var phoneName = ""

        val cr: ContentResolver = context.contentResolver

        // Fetch the matching number from contacts.
        val cursor = cr.query(
            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
            null, ContactsContract.CommonDataKinds.Phone.NUMBER + " = ?",
            arrayOf(callerNumber), null
        )

        if (cursor!!.count > 0) {
            while (cursor.moveToNext()) {
                phoneName = cursor.getString(cursor.getColumnIndex(
                    ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME
                ))
            }
            cursor.close()
            onServiceState.invoke(ExecuteSpeak(
                wordSpeak = "$INCOMING_CALL_WORD $phoneName"
            ))
        } else {
            onServiceState.invoke(ExecuteSpeak(
                wordSpeak = "$INCOMING_UNKNOWN_CALL_WORD, $callerNumber"
            ))
        }
    }
}