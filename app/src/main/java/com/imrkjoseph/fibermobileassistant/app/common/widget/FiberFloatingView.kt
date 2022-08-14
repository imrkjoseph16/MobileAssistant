package com.imrkjoseph.fibermobileassistant.app.common.widget

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.PixelFormat
import android.os.Build
import android.os.Bundle
import android.speech.SpeechRecognizer
import android.view.*
import com.github.zagum.speechrecognitionview.adapters.RecognitionListenerAdapter
import com.imrkjoseph.fibermobileassistant.app.common.Default.Companion.getRecognitionColor
import com.imrkjoseph.fibermobileassistant.app.common.Default.Companion.heightList
import com.imrkjoseph.fibermobileassistant.app.common.callback.FiberTouchListener
import com.imrkjoseph.fibermobileassistant.databinding.WidgetFiberViewBinding

class FiberFloatingView(
    var context: Context,
    var inflater: LayoutInflater,
    var windowManager: WindowManager? = null,
    var speechRecognizer: SpeechRecognizer? = null,
    var fiberListener: FiberFloatingListener
): RecognitionListenerAdapter(), FiberTouchListener.TouchListener {

    private val fiberView: WidgetFiberViewBinding by lazy {
         WidgetFiberViewBinding.inflate(inflater)
    }

    private var FLAG_LAYOUT = 0

    private var params: WindowManager.LayoutParams? = null

    init {
        setupWindowManager()
        setupRecognitionView()
        setupViewListener()
    }

    @SuppressLint("InflateParams")
    private fun setupWindowManager() {
        FLAG_LAYOUT = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        } else {
            WindowManager.LayoutParams.TYPE_PHONE
        }

        //Add the view to the window.
        params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            FLAG_LAYOUT,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        )

        //Specify the chat head position
        params?.gravity = Gravity.CENTER

        params?.x = 0
        params?.y = 100

        //Add the view to the window
        windowManager?.addView(fiberView.root, params)
    }

    private fun setupRecognitionView() {
        fiberView.apply {
            recognitionView.setSpeechRecognizer(speechRecognizer)
            recognitionView.setRecognitionListener(this@FiberFloatingView)

            recognitionView.setColors(getRecognitionColor(context))
            recognitionView.setBarMaxHeightsInDp(heightList)
            recognitionView.setCircleRadiusInDp(3)
            recognitionView.setSpacingInDp(2)
            recognitionView.setIdleStateAmplitudeInDp(2)
            recognitionView.setRotationRadiusInDp(10)
            recognitionView.play()
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun setupViewListener() {
        fiberView.apply {
            recognitionView.setOnTouchListener(FiberTouchListener(
                touchListener = this@FiberFloatingView
            ))
        }
    }

    override fun onReadyForSpeech(params: Bundle?) {
        fiberListener.onBeginReadySpeech()
    }

    override fun onBeginningOfSpeech() {
        fiberListener.onBeginReadySpeech()
    }

    override fun onResults(results: Bundle) {
        fiberListener.onResults(results)
    }

    override fun onError(error: Int) {
        fiberListener.onError(error)
    }

    override fun onEndOfSpeech() {
        fiberListener.onEndOfSpeech()
    }

    //Fiber Touch Listener
    override fun onActionDown(onXParam: (xParam: Int, yParam: Int) -> Unit) {
        onXParam.invoke(params?.x!!, params?.y!!)
    }

    override fun onActionUp() {
        fiberListener.onFiberClicked()
    }

    override fun onActionMove(initialX: Int, initialY: Int) {
        //Calculate the X and Y coordinates of the view.
        params?.x = initialX
        params?.y = initialY

        //Update the layout with new X & Y coordinate
        windowManager?.updateViewLayout(fiberView.root, params)
    }

    interface FiberFloatingListener {
        fun onBeginReadySpeech()
        fun onError(errorCode: Int)
        fun onResults(results: Bundle)
        fun onFiberClicked()
        fun onEndOfSpeech()
    }
}