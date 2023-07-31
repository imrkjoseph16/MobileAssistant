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
import com.imrkjoseph.echomobileassistant.app.common.Default.Companion.DB_TYPE_LEARN
import com.imrkjoseph.echomobileassistant.app.common.Default.Companion.DB_TYPE_QUESTION
import com.imrkjoseph.echomobileassistant.app.common.Default.Companion.DELAY_SECONDS
import com.imrkjoseph.echomobileassistant.app.common.Default.Companion.HOUR_TO_MILLIS
import com.imrkjoseph.echomobileassistant.app.common.Default.Companion.LEARN_RESPONSE_WORD
import com.imrkjoseph.echomobileassistant.app.common.Default.Companion.LOG_TAG
import com.imrkjoseph.echomobileassistant.app.common.Default.Companion.NOTIFICATION_WORD
import com.imrkjoseph.echomobileassistant.app.common.Default.Companion.SUCCESS_LEARN_RESPONSE
import com.imrkjoseph.echomobileassistant.app.common.Default.Companion.TEXT_TO_SPEECH_ID
import com.imrkjoseph.echomobileassistant.app.common.callback.ExecuteDone
import com.imrkjoseph.echomobileassistant.app.common.callback.ExecuteError
import com.imrkjoseph.echomobileassistant.app.common.callback.ExecuteStart
import com.imrkjoseph.echomobileassistant.app.common.callback.UtteranceProgressListener
import com.imrkjoseph.echomobileassistant.app.common.data.NotificationForm
import com.imrkjoseph.echomobileassistant.app.common.data.SmsStateForm
import com.imrkjoseph.echomobileassistant.app.common.helper.Utils.Companion.adjustBrightness
import com.imrkjoseph.echomobileassistant.app.common.helper.Utils.Companion.checkIfResetInteract
import com.imrkjoseph.echomobileassistant.app.common.helper.Utils.Companion.checkIfUserInteract
import com.imrkjoseph.echomobileassistant.app.common.helper.Utils.Companion.checkIsWordEcho
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
    EchoFloatingView.EchoFloatingListener,
    TextToSpeech.OnInitListener,
    NotificationService.NotificationSmsListener {

    private val smsStateService by lazy { SmsStateService(this) }

    private lateinit var notificationForm: NotificationForm

    private var wakeLock: PowerManager.WakeLock? = null

    private var windowManager: WindowManager? = null

    private var textToSpeech: TextToSpeech? = null

    private var speech: SpeechRecognizer? = null

    private var recognizerIntent: Intent? = null

    private var isServiceStarted = false

    private var isListeningState = false

    private var isListeningResult = true

    private var commandRecentType = ""

    private var learnNewCommand = ""

    private val echoListener by lazy { this }

    private val delayListener by lazy {
        executeDelay(DELAY_SECONDS) {
            commandRecentType = ""
        }
    }

    override fun onCreate() {
        super.onCreate()

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        startForeground(1, setupNotification(this, notificationManager))

        // Text to Speech Recognizer
        setupTextToSpeech()

        // Setup Speech Recognizer
        setupSpeechRecognizer()
        executeListening()

        // Setup Echo Floating View
        setupEchoView()

        // Checking every 1 hour if the speech listener
        // has being running or stopped.
        checkSpeechTimer()

        // ViewModel Observer
        executeObserver()
    }

    private fun executeObserver() {
        // Execute notification service thread.
        NotificationService(this)

        // Handling the state management from commands.
        onServiceState = {
            when(it) {
                is ReadNotification -> readNotification()
                is ExecuteSpeak -> executeSpeaking(word = it.wordSpeak)
                is LearnNewResponse -> learnNewResponse(words = it.words)
                is GetCurrentDateTime -> executeSpeaking(word = getCurrentDateTime(it.value))
                is HandleNotification -> handleNotification(notification = it.notificationForm)
                is ExecuteBrightness -> adjustBrightness(
                    brightness = it.brightness,
                    context = this
                )
            }
        }
    }

    fun checkSpeechTimer() {
        val timer = object: CountDownTimer(HOUR_TO_MILLIS, COUNTDOWN_INTERVAL) {
            override fun onTick(millisUntilFinished: Long) {}
            override fun onFinish() {
                checkSpeechTimer()

                executeListening().takeIf { isListeningState.not() }
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

    private fun setupEchoView() {
        val inflater = getSystemService(LAYOUT_INFLATER_SERVICE) as LayoutInflater
        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
        EchoFloatingView(
            context = this,
            inflater = inflater,
            windowManager = windowManager,
            speechRecognizer = speech,
            echoListener = echoListener
        )
    }

    private fun restartServiceIntent() {
        val restartServiceIntent = Intent(applicationContext,
            EchoService::class.java).also {
            it.setPackage(packageName)
        }

        val restartServicePendingIntent: PendingIntent = PendingIntent.getService(
            this, 1, restartServiceIntent, PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_IMMUTABLE)
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
                    "EchoService::lock").apply {
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
                when(it) {
                    is ExecuteDone, ExecuteError -> resetInteraction(userInteract = checkIfResetInteract(commandRecentType))
                    is ExecuteStart -> stopListening()
                }
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

    private fun stopListening() {
        isListeningResult = false
    }

    // Text To Speech Initializer
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

    // Speech Recognizer Listener
    override fun onResults(results: Bundle) {
        val matches: ArrayList<String>? = results.getStringArrayList(
            SpeechRecognizer.RESULTS_RECOGNITION
        )

        Log.d(LOG_TAG, "OnResult: $matches")

        // Handling the onResult data from recognizer.
        handleResultState(matches)

        // Listen again after result handling.
        executeListening()
    }

    private fun handleResultState(
        words: ArrayList<String>?
    ) {
        setCoroutine(Main).launch {
            // Check the commandRecentType if equals to "question" or "learn",
            // It means echo needs to interact again to the user.
            val userInteract = checkIfUserInteract(commandRecentType)

            if (checkIsWordEcho(words) || userInteract) {

                // Cancel or stop the delayListener "isUserInteract",
                // for echo to listen again for 5 seconds.
                delayListener.cancel()

                if (isListeningResult) {
                    launch(IO) {
                        when (commandRecentType) {
                            DB_TYPE_LEARN -> addResetNewResponse(newResponse = words?.get(0))
                            else -> readCommands(words = words)
                        }
                    }
                }
            }
        }
    }

    private fun readCommands(words: ArrayList<String>?) {
        val commandForm = readCommandList(words)

        getCommandFunction(commandForm, words)
        if (commandForm.type != null) commandRecentType = commandForm.type.toString()
    }

    private fun resetInteraction(userInteract: Boolean) {
        // Execute listen again after text to speech
        // recognizer finished talking.
        isListeningResult = true

        // Reset commandRecentType after 5 seconds,
        // If the user are not responding.
        if (userInteract) delayListener.start()
    }

    private fun learnNewResponse(words: String) {
        executeSpeaking(word = "$LEARN_RESPONSE_WORD $words?")

        // Set the commandRecentType to type "learn"
        // to identify if echo needs to learn new response.
        commandRecentType = DB_TYPE_LEARN
        learnNewCommand = words
    }

    private fun addResetNewResponse(newResponse: String?) {
        executeSpeaking(word = SUCCESS_LEARN_RESPONSE)

        // Adding the newResponse for new keyWord to database.
        addNewResponse(transformNewCommandForm(
            newKeyWord = learnNewCommand,
            newResponse = newResponse
        ))

        // Reset commandType and newCommand variables,
        // to recognize new words.
        commandRecentType = ""
        learnNewCommand = ""
    }

    private fun handleNotification(notification: NotificationForm) {
        if (notification.packageName != null) {
            executeSpeaking(notification.title.toString())

            // Set the commandRecentType from "question"
            // to interact again with user because in this thread
            // echo will ask, if he will "read" the notification.
            commandRecentType = DB_TYPE_QUESTION

            // Getting the notification data to read,
            // if the user says the word "read"
            notificationForm = notification
        }
    }

    private fun readNotification() {
        if (this::notificationForm.isInitialized) {
            // Check if the echo is speaking then stop it,
            // then execute speak method.
            textToSpeech?.apply { if (isSpeaking) stop() }

            executeSpeaking(
                word = notificationForm.description.toString()
            )
        }
    }

    override fun onBeginReadySpeech() {
        isListeningState = true
    }

    override fun onError(errorCode: Int) {
        val errorMessage: String = getErrorText(errorCode)
        Log.d(LOG_TAG, "OnError: $errorMessage")
        isListeningState = false
        executeListening()
    }

    override fun onEchoClicked() {
        executeListening()
    }

    override fun onEndOfSpeech() {
        isListeningState = false
    }

    // Handling notification and sms/call receiver.
    override fun onNotificationReceived(
        notification: NotificationForm
    ) {
        handleNotification(notification = NotificationForm(
            title = formatString(
                context = this,
                smsDescription = NOTIFICATION_WORD,
                senderName = notification.title.toString()
            ),
            description = notification.description
        ))
    }

    override fun onSmsReceived(smsForm: SmsStateForm) {
        when(smsForm.isCalling) {
            true -> handlingCallState(context = this, callerNumber = smsForm.smsNumber)
            else -> handlingSmsState(context = this, smsForm = smsForm)
        }
    }
}