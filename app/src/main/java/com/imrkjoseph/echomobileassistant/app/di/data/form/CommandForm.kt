package com.imrkjoseph.echomobileassistant.app.di.data.form

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.imrkjoseph.echomobileassistant.app.common.Default.Companion.DB_COMMANDS
import com.imrkjoseph.echomobileassistant.app.common.Default.Companion.DB_INPUT
import com.imrkjoseph.echomobileassistant.app.common.Default.Companion.DB_OUTPUT
import com.imrkjoseph.echomobileassistant.app.common.Default.Companion.DB_TYPE

@Entity(tableName = DB_COMMANDS)
data class CommandForm(

    @PrimaryKey(autoGenerate = true)
    var id: Int? = null,

    @ColumnInfo(name = DB_INPUT)
    var input: String? = null,

    @ColumnInfo(name = DB_OUTPUT)
    var output: String? = null,

    @ColumnInfo(name = DB_TYPE)
    var type: String? = null
)