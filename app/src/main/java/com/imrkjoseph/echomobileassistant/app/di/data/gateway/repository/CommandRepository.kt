package com.imrkjoseph.echomobileassistant.app.di.data.gateway.repository

import com.imrkjoseph.echomobileassistant.app.di.domain.CommandService
import javax.inject.Inject

class CommandRepository @Inject constructor(
    commandService: CommandService) {

    private val commandClient = commandService.commandDao()

    fun getCommandList() = commandClient.getCommandList()
}