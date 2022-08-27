package com.imrkjoseph.echomobileassistant.app.di.data.gateway.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.imrkjoseph.echomobileassistant.app.common.Default.Companion.DB_COMMANDS
import com.imrkjoseph.echomobileassistant.app.di.data.form.CommandForm

@Dao
interface CommandDao {

    @Query("Select * from $DB_COMMANDS")
    fun getCommandList() : MutableList<CommandForm>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insertNewResponse(commandForm: CommandForm) : Long
}