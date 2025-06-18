package com.sevengod.maibud.instances

import androidx.room.Database
import androidx.room.RoomDatabase
import com.sevengod.maibud.data.dao.UserDao
import com.sevengod.maibud.data.dao.RecordDao
import com.sevengod.maibud.data.entities.UserProfile
import com.sevengod.maibud.data.entities.RecordEntity

@Database(
    entities = [UserProfile::class, RecordEntity::class], 
    version = 4, 
    exportSchema = false
)
abstract class DataBaseInstance : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun recordDao(): RecordDao
}