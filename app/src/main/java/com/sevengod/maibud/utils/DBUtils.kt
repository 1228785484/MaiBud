package com.sevengod.maibud.utils

import android.content.Context
import androidx.room.Room
import com.sevengod.maibud.instances.DataBaseInstance


object DBUtils {
    @Volatile
    private var INSTANCE: DataBaseInstance? = null

    fun getDatabase(context: Context): DataBaseInstance {
        return INSTANCE ?: synchronized(this) {
            val instance = Room.databaseBuilder(
                context.applicationContext,
                DataBaseInstance::class.java,
                "maibud_app_database"
            )
            .fallbackToDestructiveMigration() // 简单处理：在结构变化时重建数据库
            .build()
            INSTANCE = instance
            instance
        }
    }
}