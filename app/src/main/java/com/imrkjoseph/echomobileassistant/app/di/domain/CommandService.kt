package com.imrkjoseph.echomobileassistant.app.di.domain

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.fstyle.library.helper.AssetSQLiteOpenHelperFactory
import com.imrkjoseph.echomobileassistant.app.common.Default.Companion.DB_COMMAND_PATH
import com.imrkjoseph.echomobileassistant.app.di.data.form.CommandForm
import com.imrkjoseph.echomobileassistant.app.di.data.gateway.dao.CommandDao

@Database(entities = [CommandForm::class], version = 1)
abstract class CommandService : RoomDatabase() {

    companion object {

        @Volatile
        private var DB_INSTANCE: CommandService? = null

        fun getInstance(context: Context) = DB_INSTANCE ?: synchronized(this) {
            DB_INSTANCE ?: buildDatabase(context).also { DB_INSTANCE = it }
        }

        private fun buildDatabase(context: Context) : CommandService {
            synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    CommandService::class.java,
                    DB_COMMAND_PATH)
                    .openHelperFactory(AssetSQLiteOpenHelperFactory())
                    .allowMainThreadQueries()
                    .build()

                DB_INSTANCE = instance
                return instance
            }
        }
    }

    abstract fun commandDao() : CommandDao
}