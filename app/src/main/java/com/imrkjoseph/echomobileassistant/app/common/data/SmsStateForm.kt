package com.imrkjoseph.echomobileassistant.app.common.data

data class SmsStateForm(
    val senderName: String? = null,
    val smsMessage: String? = null,
    val smsNumber: String? = null,
    val isCalling: Boolean? = false
)