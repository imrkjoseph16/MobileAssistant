package com.imrkjoseph.echomobileassistant.service

import com.imrkjoseph.echomobileassistant.app.common.data.NotificationForm

enum class ServiceEnum {
    STARTED,
    STOPPED,
}

sealed class ServiceState

//Data Classes
data class ExecuteSpeak(
    var wordSpeak: String
) : ServiceState()

data class ExecuteBrightness(
    var brightness: Float
) : ServiceState()

data class GetCurrentDateTime(
    var value: String
) : ServiceState()

data class HandleNotification(
    var notificationForm: NotificationForm
) : ServiceState()

//Objects
object ReadNotification : ServiceState()