package com.imrkjoseph.echomobileassistant.app.common.callback

import android.view.MotionEvent
import android.view.View
import kotlin.math.abs

class EchoTouchListener(
    private val touchListener: TouchListener
) : View.OnTouchListener {

    private var lastAction = 0
    private var initialX = 0
    private var initialY = 0
    private var initialTouchX = 0f
    private var initialTouchY = 0f

    private var timeStart: Long = 0
    private var timeEnd: Long = 0

    override fun onTouch(v: View?, event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                timeStart = System.currentTimeMillis()

                touchListener.onActionDown { xParam, yParam ->
                    //Remember the initial position.
                    initialX = xParam
                    initialY = yParam
                }

                //Get the touch location
                initialTouchX = event.rawX
                initialTouchY = event.rawY
                lastAction = event.action
                return true
            }
            MotionEvent.ACTION_UP -> {
                timeEnd = System.currentTimeMillis()

                val xDiff: Float = event.rawX - initialTouchX
                val yDiff: Float = event.rawY - initialTouchY
                if (abs(xDiff) < 5 && abs(yDiff) < 5) {
                    if (timeEnd - timeStart < 300) {
                        touchListener.onActionUp()
                    }
                }
                lastAction = event.action
                return true
            }
            MotionEvent.ACTION_MOVE -> {
                //Calculate the X and Y coordinates of the view.
                val xParam = initialX + (event.rawX - initialTouchX).toInt()
                val yParam = initialY + (event.rawY - initialTouchY).toInt()

                touchListener.onActionMove(xParam, yParam)
                lastAction = event.action
                return true
            }
        }
        return false
    }

    interface TouchListener {
        fun onActionDown(onXParam: (xParam: Int, yParam: Int) -> Unit)
        fun onActionUp()
        fun onActionMove(initialX: Int, initialY: Int)
    }
}