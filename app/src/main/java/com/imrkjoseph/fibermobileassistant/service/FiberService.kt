package com.imrkjoseph.fibermobileassistant.service

import android.annotation.SuppressLint
import android.app.*
import android.content.Context
import android.content.Intent
import android.os.*
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import android.util.Log
import android.view.LayoutInflater
import android.view.WindowManager
import com.imrkjoseph.fibermobileassistant.app.Default.Companion.LOG_TAG
import com.imrkjoseph.fibermobileassistant.app.common.callback.UtteranceProgressListener
import com.imrkjoseph.fibermobileassistant.app.common.helper.Utils.Companion.getErrorText
import com.imrkjoseph.fibermobileassistant.app.common.navigation.Actions
import com.imrkjoseph.fibermobileassistant.app.common.notification.NotificationBuilder.Companion.setupNotification
import com.imrkjoseph.fibermobileassistant.app.common.widget.FiberFloatingView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.util.*

class FiberService : Service(),
    FiberFloatingView.FiberFloatingListener,
    TextToSpeech.OnInitListener {

    private var wakeLock: PowerManager.WakeLock? = null

    private var windowManager: WindowManager? = null

    private var isServiceStarted = false

    private var textToSpeech: TextToSpeech? = null

    private var speech: SpeechRecognizer? = null

    private var recognizerIntent: Intent? = null

    private val fiberListener by lazy { this }

    override fun onBind(p0: Intent?): IBinder? {
        return null
    }

    override fun onCreate() {
        super.onCreate()

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        startForeground(1, setupNotification(this, notificationManager))

        //Text to Speech Recognizer
        setupTextToSpeech()

        //Setup Speech Recognizer
        setupSpeechRecognizer()
        speech?.startListening(recognizerIntent)

        //Setup Fiber Floating View
        setupFiberView()
    }

    @SuppressLint("UnspecifiedImmutableFlag")
    override fun onTaskRemoved(rootIntent: Intent?) = restartServiceIntent()

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent != null) when(intent.action) {
            Actions.START.name -> startService()
            Actions.START.name -> stopService()
        }
        return START_STICKY
    }

    private fun setupFiberView() {
        val inflater = getSystemService(LAYOUT_INFLATER_SERVICE) as LayoutInflater
        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
        FiberFloatingView(
            context = this,
            inflater = inflater,
            windowManager = windowManager,
            speechRecognizer = speech,
            fiberListener = fiberListener
        )
    }

    private fun restartServiceIntent() {
        val restartServiceIntent = Intent(applicationContext,
            FiberService::class.java).also {
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
        setServiceState(this, ServiceState.STARTED)

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
        setServiceState(this, ServiceState.STOPPED)
    }

    private fun setupTextToSpeech() {
        textToSpeech = TextToSpeech(this, this, "com.google.android.tts")
        textToSpeech?.setOnUtteranceProgressListener(
            UtteranceProgressListener {
            //Not yet implemented
        })
        textToSpeech?.language = Locale.UK
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

        GlobalScope.launch(Dispatchers.Main) {
            if (matches?.toString()?.toLowerCase()?.contains("fiber") == true
                || matches?.toString()?.toLowerCase()?.contains("viber") == true) {
                textToSpeech?.speak(matches[0], TextToSpeech.QUEUE_ADD, null)
            }
        }

        speech?.startListening(recognizerIntent)
    }

    override fun onError(errorCode: Int) {
        val errorMessage: String = getErrorText(errorCode)
        Log.d(LOG_TAG, "OnError: $errorMessage")
        speech?.startListening(recognizerIntent)
    }
}