package com.imrkjoseph.fibermobileassistant.app.di.data.gateway.dao

import androidx.room.Dao
import androidx.room.Query
import com.imrkjoseph.fibermobileassistant.app.common.Default.Companion.DB_COMMANDS
import com.imrkjoseph.fibermobileassistant.app.di.data.form.CommandForm

@Dao
interface CommandDao {

    @Query("Select * from $DB_COMMANDS")
    fun getCommandList() : MutableList<CommandForm>
}