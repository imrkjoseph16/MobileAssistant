package com.imrkjoseph.echomobileassistant.service

import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import android.os.PowerManager
import android.os.SystemClock
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import android.util.Log
import android.view.LayoutInflater
import android.view.WindowManager
import com.imrkjoseph.echomobileassistant.app.common.Default.Companion.COUNTDOWN_INTERVAL
import com.imrkjoseph.echomobileassistant.app.common.Default.Companion.DB_TYPE_QUESTION
import com.imrkjoseph.echomobileassistant.app.common.Default.Companion.DELAY_SECONDS
import com.imrkjoseph.echomobileassistant.app.common.Default.Companion.ECHO_NAME
import com.imrkjoseph.echomobileassistant.app.common.Default.Companion.HOUR_TO_MILLIS
import com.imrkjoseph.echomobileassistant.app.common.Default.Companion.LOG_TAG
import com.imrkjoseph.echomobileassistant.app.common.Default.Companion.NOTIFICATION_WORD
import com.imrkjoseph.echomobileassistant.app.common.Default.Companion.TEXT_TO_SPEECH_ID
import com.imrkjoseph.echomobileassistant.app.common.callback.UtteranceProgressListener
import com.imrkjoseph.echomobileassistant.app.common.data.NotificationForm
import com.imrkjoseph.echomobileassistant.app.common.data.SmsStateForm
import com.imrkjoseph.echomobileassistant.app.common.helper.Utils.Companion.adjustBrightness
import com.imrkjoseph.echomobileassistant.app.common.helper.Utils.Companion.executeDelay
import com.imrkjoseph.echomobileassistant.app.common.helper.Utils.Companion.formatString
import com.imrkjoseph.echomobileassistant.app.common.helper.Utils.Companion.getCurrentDateTime
import com.imrkjoseph.echomobileassistant.app.common.helper.Utils.Companion.getErrorText
import com.imrkjoseph.echomobileassistant.app.common.helper.Utils.Companion.setCoroutine
import com.imrkjoseph.echomobileassistant.app.common.helper.Utils.Companion.setServiceState
import com.imrkjoseph.echomobileassistant.app.common.navigation.Actions
import com.imrkjoseph.echomobileassistant.app.common.notification.NotificationBuilder.Companion.setupNotification
import com.imrkjoseph.echomobileassistant.app.common.service.NotificationService
import com.imrkjoseph.echomobileassistant.app.common.service.SmsStateService
import com.imrkjoseph.echomobileassistant.app.common.widget.EchoFloatingView
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.launch
import java.util.*

class EchoService : ServiceViewModel(),
    EchoFloatingView.FiberFloatingListener,
    TextToSpeech.OnInitListener,
    NotificationService.NotificationSmsListener {

    private val smsStateService by lazy { SmsStateService(this) }

    private val fiberListener by lazy { this }

    private var wakeLock: PowerManager.WakeLock? = null

    private var windowManager: WindowManager? = null

    private var textToSpeech: TextToSpeech? = null

    private var speech: SpeechRecognizer? = null

    private var recognizerIntent: Intent? = null

    private var isServiceStarted = false

    private var isListening = false

    private var commandRecentType = ""

    lateinit var notificationForm: NotificationForm

    override fun onCreate() {
        super.onCreate()

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        startForeground(1, setupNotification(this, notificationManager))

        //Text to Speech Recognizer
        setupTextToSpeech()

        //Setup Speech Recognizer
        setupSpeechRecognizer()
        executeListening()

        //Setup Fiber Floating View
        setupFiberView()

        //Checking every 1 hour if the speech listener
        //has being running or stopped.
        checkSpeechTimer()

        //ViewModel Observer
        executeObserver()
    }

    private fun executeObserver() {
        //Execute notification service thread.
        NotificationService(this)

        //Handling the state management from commands.
        onServiceState = {
            when(it) {
                is ReadNotification -> readNotification()
                is HandleNotification -> handleNotification(it.notificationForm)
                is ExecuteSpeak -> executeSpeaking(word = it.wordSpeak)
                is GetCurrentDateTime -> executeSpeaking(
                    word = getCurrentDateTime(it.value)
                )
                is ExecuteBrightness -> {
                    adjustBrightness(
                        brightness = it.brightness,
                        context = this
                    )
                }
            }
        }
    }

    fun checkSpeechTimer() {
        val timer = object: CountDownTimer(HOUR_TO_MILLIS, COUNTDOWN_INTERVAL) {
            override fun onTick(millisUntilFinished: Long) {}
            override fun onFinish() {
                checkSpeechTimer()

                if (!isListening) executeListening()
            }
        }
        timer.start()
    }

    @SuppressLint("UnspecifiedImmutableFlag")
    override fun onTaskRemoved(rootIntent: Intent?) = restartServiceIntent()

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        try { registerReceiver(this, smsStateService) } catch (e: Exception) { }

        if (intent != null) when(intent.action) {
            Actions.START.name -> startService()
            Actions.STOP.name -> stopService()
        }
        return START_STICKY
    }

    private fun setupFiberView() {
        val inflater = getSystemService(LAYOUT_INFLATER_SERVICE) as LayoutInflater
        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
        EchoFloatingView(
            context = this,
            inflater = inflater,
            windowManager = windowManager,
            speechRecognizer = speech,
            fiberListener = fiberListener
        )
    }

    private fun restartServiceIntent() {
        val restartServiceIntent = Intent(applicationContext,
            EchoService::class.java).also {
            it.setPackage(packageName)
        }

        val restartServicePendingIntent: PendingIntent = PendingIntent.getService(
            this, 1, restartServiceIntent, PendingIntent.FLAG_ONE_SHOT)
        applicationContext.getSystemService(Context.ALARM_SERVICE)
        val alarmService: AlarmManager = applicationContext.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarmService.set(AlarmManager.ELAPSED_REALTIME, SystemClock.elapsedRealtime() + 1000, restartServicePendingIntent)
    }

    private fun startService() {
        if (isServiceStarted) return
        isServiceStarted = true
        setServiceState(this, ServiceEnum.STARTED)

        wakeLock =
            (getSystemService(Context.POWER_SERVICE) as PowerManager).run {
                newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,
                    "FiberService::lock").apply {
                    acquire()
                }
            }
    }

    private fun stopService() {
        try {
            wakeLock?.let {
                if (it.isHeld) it.release()
            }
            stopForeground(true)
            stopSelf()
        } catch (e: Exception) { }

        isServiceStarted = false
        setServiceState(this, ServiceEnum.STOPPED)
    }

    private fun setupTextToSpeech() {
        textToSpeech = TextToSpeech(this, this, "com.google.android.tts")
        textToSpeech?.setOnUtteranceProgressListener(
            UtteranceProgressListener {
            setCoroutine(Main).launch {
                //Reset commandRecentType after 5 seconds,
                //If the user are not responding.
                resetInteraction(
                    userInteract = commandRecentType == DB_TYPE_QUESTION,
                    DELAY_SECONDS
                )
            }
        })
        textToSpeech?.language = Locale.UK
    }

    private fun executeSpeaking(word: String) {
        val bundle = Bundle()
        bundle.putString(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "")
        textToSpeech?.speak(word, TextToSpeech.QUEUE_FLUSH, bundle, TEXT_TO_SPEECH_ID)
    }

    private fun setupSpeechRecognizer() {
        speech = SpeechRecognizer.createSpeechRecognizer(this)
        recognizerIntent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
        recognizerIntent!!.putExtra(
            RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE,
            "en"
        )
        recognizerIntent!!.putExtra(
            RecognizerIntent.EXTRA_LANGUAGE_MODEL,
            RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
        )
    }

    private fun executeListening() {
        speech?.startListening(recognizerIntent)
    }

    //Text To Speech Initializer
    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            val result: Int? = textToSpeech?.setLanguage(Locale.UK)
            if (result == TextToSpeech.LANG_MISSING_DATA ||
                result == TextToSpeech.LANG_NOT_SUPPORTED
            ) {
                Log.e(LOG_TAG, "OnError: This Language is not supported")
            }
        }
    }

    //Speech Recognizer Listener
    override fun onResults(results: Bundle) {
        val matches: ArrayList<String>? = results.getStringArrayList(
            SpeechRecognizer.RESULTS_RECOGNITION
        )

        Log.d(LOG_TAG, "OnResult: $matches")

        //Handling the onResult data from recognizer.
        handleResultState(matches)

        //Listen again after result handling.
        executeListening()
    }

    private fun handleResultState(
        matches: ArrayList<String>?
    ) {
        setCoroutine(Main).launch {
            //Check the commandRecentType if equals to "question",
            //It means echo needs to interact again to the user.
            val userInteract = commandRecentType == DB_TYPE_QUESTION

            if (matches.toString().contains(ECHO_NAME) ||
                userInteract
            ) {
                launch(IO) {
                    val commandForm = readCommandList(matches)

                    getCommandFunction(commandForm.output.toString())
                    commandRecentType = commandForm.type.toString()
                }
            }
        }
    }

    private fun resetInteraction(
        userInteract: Boolean,
        delaySeconds: Long
    ) {
        if (userInteract) {
            executeDelay(delaySeconds) { commandRecentType = "" }
        }
    }

    private fun handleNotification(notification: NotificationForm) {
        if (notification.packageName != null) {
            executeSpeaking(notification.title.toString())

            //Set the commandRecentType from "question"
            //to interact again with user because in this thread
            //echo will ask, if he will "read" the notification.
            commandRecentType = DB_TYPE_QUESTION

            //Getting the notification data to read,
            //if the user says the word "read"
            notificationForm = notification
        }
    }

    private fun readNotification() {
        if (this::notificationForm.isInitialized) {
            //Check if the echo is speaking then stop it,
            //then execute speak method.
            textToSpeech?.apply { if (isSpeaking) stop() }

            executeSpeaking(
                word = notificationForm.description.toString()
            )
        }
    }

    override fun onBeginReadySpeech() {
        isListening = true
    }

    override fun onError(errorCode: Int) {
        val errorMessage: String = getErrorText(errorCode)
        Log.d(LOG_TAG, "OnError: $errorMessage")
        isListening = false
        executeListening()
    }

    override fun onFiberClicked() {
        executeListening()
    }

    override fun onEndOfSpeech() {
        isListening = false
    }

    //Handling notification and sms/call receiver.
    override fun onNotificationReceived(
        notification: NotificationForm
    ) {
        handleNotification(notification = NotificationForm(
            title = formatString(this,
                NOTIFICATION_WORD,
                notification.title.toString()
            ),
            description = notification.description
        ))
    }

    override fun onSmsReceived(smsForm: SmsStateForm) {
        when(smsForm.isCalling) {
            true -> handlingCallState(this, smsForm.smsNumber)
            false -> handlingSmsState(this, smsForm)
        }
    }
}