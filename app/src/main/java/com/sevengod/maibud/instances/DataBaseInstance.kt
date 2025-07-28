package com.sevengod.maibud.instances

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.sevengod.maibud.data.dao.ChartDao
import com.sevengod.maibud.data.dao.UserDao
import com.sevengod.maibud.data.dao.RecordDao
import com.sevengod.maibud.data.dao.SongDao
import com.sevengod.maibud.data.dao.SongWithChartsDao
import com.sevengod.maibud.data.entities.ChartEntity
import com.sevengod.maibud.data.entities.UserProfile
import com.sevengod.maibud.data.entities.RecordEntity
import com.sevengod.maibud.data.entities.SongEntity

@Database(
    entities = [UserProfile::class, RecordEntity::class, SongEntity::class, ChartEntity::class],
    version = 5,
    exportSchema = false
)
abstract class DataBaseInstance : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun recordDao(): RecordDao
    abstract fun songDao(): SongDao
    abstract fun chartDao(): ChartDao
    abstract fun songWithChartsDao(): SongWithChartsDao

    companion object {
        const val DATABASE_VERSION = 1
        const val DATABASE_NAME = "maibud_app_database"

        @Volatile
        private lateinit var instance: DataBaseInstance
        fun getInstance(context: Context): DataBaseInstance {
            if (!::instance.isInitialized) {
                synchronized(this) {
                    instance = Room.databaseBuilder(
                        context.applicationContext,
                        DataBaseInstance::class.java,
                        DATABASE_NAME
                    ).build()
                }
            }
            return instance
        }
    }
}