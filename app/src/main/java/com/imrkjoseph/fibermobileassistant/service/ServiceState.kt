package com.imrkjoseph.fibermobileassistant.service

enum class ServiceEnum {
    STARTED,
    STOPPED,
}

sealed class ServiceState

data class ExecuteSpeak(
    var wordSpeak: String
) : ServiceState()

data class GetCurrentDateTime(
    var value: String
) : ServiceState()

data class ExecuteBrightness(
    var brightness: Float
) : ServiceState()