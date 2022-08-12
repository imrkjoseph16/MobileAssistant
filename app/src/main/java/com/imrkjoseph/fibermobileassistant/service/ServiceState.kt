package com.imrkjoseph.fibermobileassistant.service

enum class ServiceEnum {
    STARTED,
    STOPPED,
}

sealed class ServiceState

data class ExecuteSpeak(
    var executeSpeak: String
) : ServiceState()

data class ExecuteBrightness(
    var brightness: Float
) : ServiceState()