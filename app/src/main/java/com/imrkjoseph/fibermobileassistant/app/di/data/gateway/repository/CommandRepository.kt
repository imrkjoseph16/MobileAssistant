package com.imrkjoseph.fibermobileassistant.app.di.data.gateway.repository

import com.imrkjoseph.fibermobileassistant.app.di.domain.CommandService
import javax.inject.Inject

class CommandRepository @Inject constructor(
    commandService: CommandService) {

    private val commandClient = commandService.commandDao()

    fun getCommandList() = commandClient.getCommandList()
}