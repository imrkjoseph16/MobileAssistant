package com.imrkjoseph.fibermobileassistant.app.common.callback

import android.speech.tts.UtteranceProgressListener

open class UtteranceProgressListener(
    private val onDoneResult: (result: String?) -> Unit
) : UtteranceProgressListener() {

    override fun onDone(result: String?) = onDoneResult.invoke(result)

    override fun onStart(result: String?) {}

    override fun onError(result: String?) {}
}