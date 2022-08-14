package com.imrkjoseph.fibermobileassistant.app.di

import android.content.Context
import com.imrkjoseph.fibermobileassistant.app.di.domain.CommandService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
class CommandModule {

    @Singleton
    @Provides
    fun executeRoomInstance(
        @ApplicationContext
        context: Context,
    ) = CommandService.getInstance(context)

    @Singleton
    @Provides
    fun executeCommandDao(
        commandService: CommandService
    ) = commandService.commandDao()
}