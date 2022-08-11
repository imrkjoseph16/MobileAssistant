package com.imrkjoseph.fibermobileassistant.app.common.widget

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.PixelFormat
import android.os.Build
import android.os.Bundle
import android.speech.SpeechRecognizer
import android.view.*
import com.github.zagum.speechrecognitionview.adapters.RecognitionListenerAdapter
import com.imrkjoseph.fibermobileassistant.app.Default.Companion.getRecognitionColor
import com.imrkjoseph.fibermobileassistant.app.Default.Companion.heightList
import com.imrkjoseph.fibermobileassistant.databinding.WidgetFiberViewBinding

class FiberFloatingView(
    var context: Context,
    var inflater: LayoutInflater,
    var windowManager: WindowManager? = null,
    var speechRecognizer: SpeechRecognizer? = null,
    var fiberListener: FiberFloatingListener
): RecognitionListenerAdapter() {

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
            recognitionView.setOnTouchListener(object : View.OnTouchListener {

                private var lastAction = 0
                private var initialX = 0
                private var initialY = 0
                private var initialTouchX = 0f
                private var initialTouchY = 0f

                override fun onTouch(v: View?, event: MotionEvent): Boolean {
                    when (event.action) {
                        MotionEvent.ACTION_DOWN -> {

                            //Remember the initial position.
                            initialX = params?.x!!
                            initialY = params?.y!!

                            //Get the touch location
                            initialTouchX = event.rawX
                            initialTouchY = event.rawY
                            lastAction = event.action
                            return true
                        }
                        MotionEvent.ACTION_UP -> {
                            lastAction = event.action
                            return true
                        }
                        MotionEvent.ACTION_MOVE -> {
                            //Calculate the X and Y coordinates of the view.
                            params?.x = initialX + (event.rawX - initialTouchX).toInt()
                            params?.y = initialY + (event.rawY - initialTouchY).toInt()

                            //Update the layout with new X & Y coordinate
                            windowManager?.updateViewLayout(fiberView.root, params)
                            lastAction = event.action
                            return true
                        }
                    }
                    return false
                }
            })
        }
    }

    override fun onResults(results: Bundle) {
        fiberListener.onResults(results)
    }

    override fun onError(error: Int) {
        fiberListener.onError(error)
    }

    interface FiberFloatingListener {
        fun onError(errorCode: Int)
        fun onResults(results: Bundle)
    }
}