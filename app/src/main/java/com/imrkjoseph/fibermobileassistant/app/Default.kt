package com.imrkjoseph.fibermobileassistant.app

import android.content.Context
import androidx.core.content.ContextCompat
import com.imrkjoseph.fibermobileassistant.R

class Default {
    companion object {
        const val PERMISSIONS_RECORD_AUDIO = 1
        const val PERMISSION_DRAW_OVER_OVERLAY = 2
        const val LOG_TAG = "FIBER"

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