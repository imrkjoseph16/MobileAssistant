package com.imrkjoseph.fibermobileassistant.service

import android.annotation.SuppressLint
import android.app.*
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.*
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import android.util.Log
import com.imrkjoseph.fibermobileassistant.R
import com.imrkjoseph.fibermobileassistant.activity.FacilityActivity
import com.imrkjoseph.fibermobileassistant.app.Actions
import com.imrkjoseph.fibermobileassistant.app.common.ServiceState
import com.imrkjoseph.fibermobileassistant.app.common.Utils.Companion.getErrorText
import com.imrkjoseph.fibermobileassistant.app.common.setServiceState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.util.*

class FiberService : Service(), RecognitionListener, TextToSpeech.OnInitListener {

    private var wakeLock: PowerManager.WakeLock? = null

    private var isServiceStarted = false

    private var textToSpeech: TextToSpeech? = null

    private var speech: SpeechRecognizer? = null

    private var recognizerIntent: Intent? = null

    override fun onBind(p0: Intent?): IBinder? {
        return null
    }

    override fun onCreate() {
        super.onCreate()

        val notification = setupNotification()
        startForeground(1, notification)

        //Text to Speech Recognizer
        setupTextToSpeech()

        //Setup Speech Recognizer
        setupSpeechRecognizer()
        speech?.startListening(recognizerIntent)
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

    private fun setupNotification(): Notification {
        val notificationChannelId = "FIBER SERVICE CHANNEL"
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            val channel = NotificationChannel(
                notificationChannelId,
                "Fiber service notifications channel",
                NotificationManager.IMPORTANCE_HIGH
            ).let {
                it.description = "Fiber Service Channel"
                it.enableLights(true)
                it.lightColor = Color.RED
                it.enableVibration(true)
                it.vibrationPattern = longArrayOf(100, 200, 300, 400, 500, 400, 300, 200, 400)
                it
            }
            notificationManager.createNotificationChannel(channel)
        }

        val pendingIntent: PendingIntent = Intent(this, 
            FacilityActivity::class.java).let { notificationIntent ->
            PendingIntent.getActivity(this, 0, notificationIntent, 0)
        }

        val builder: Notification.Builder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            Notification.Builder(this, notificationChannelId)
        else Notification.Builder(this)

        return builder
            .setContentTitle("Fiber Mobile Assistant")
            .setContentText("Please keep fiber running on background to continue listening...")
            .setContentIntent(pendingIntent)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setTicker("Fiber Assistant")
            .setPriority(Notification.PRIORITY_HIGH)
            .build()
    }

    private fun setupTextToSpeech() {
        textToSpeech = TextToSpeech(this, this, "com.google.android.tts")
        textToSpeech?.language = Locale.UK
    }

    private fun setupSpeechRecognizer() {
        speech = SpeechRecognizer.createSpeechRecognizer(this)
        speech?.setRecognitionListener(this)
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

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            val result: Int? = textToSpeech?.setLanguage(Locale.UK)
            if (result == TextToSpeech.LANG_MISSING_DATA ||
                result == TextToSpeech.LANG_NOT_SUPPORTED
            ) {
                Log.e("error", "This Language is not supported")
            }
        }
    }

    //Speech Recognizer Listeners
    override fun onResults(results: Bundle?) {
        val matches: ArrayList<String>? = results?.getStringArrayList(
            SpeechRecognizer.RESULTS_RECOGNITION
        )

        Log.d("Fiber", "OnResult: $matches")

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
        Log.d("Fiber", "Error: $errorMessage")
        speech?.startListening(recognizerIntent)
    }

    override fun onPartialResults(results: Bundle?) {}

    override fun onEvent(p0: Int, p1: Bundle?) {}

    override fun onReadyForSpeech(p0: Bundle?) {}

    override fun onBeginningOfSpeech() {}

    override fun onRmsChanged(p0: Float) {}

    override fun onBufferReceived(p0: ByteArray?) {}

    override fun onEndOfSpeech() {}
}