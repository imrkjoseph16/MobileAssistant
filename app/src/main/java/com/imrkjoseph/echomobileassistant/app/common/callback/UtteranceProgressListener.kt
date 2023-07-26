package com.imrkjoseph.echomobileassistant.app.common.callback

import android.speech.tts.UtteranceProgressListener

open class UtteranceProgressListener(
    private val onDoneResult: (result: UtteranceState) -> Unit
) : UtteranceProgressListener() {

    override fun onDone(result: String?) = onDoneResult.invoke(ExecuteDone)

    override fun onStart(result: String?) = onDoneResult.invoke(ExecuteStart)

    override fun onError(result: String?) = onDoneResult.invoke(ExecuteError)
}

sealed class UtteranceState

object ExecuteDone : UtteranceState()
object ExecuteStart : UtteranceState()
object ExecuteError : UtteranceState()