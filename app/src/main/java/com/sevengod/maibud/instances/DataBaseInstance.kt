package com.sevengod.maibud.instances

import androidx.room.Database
import androidx.room.RoomDatabase
import com.sevengod.maibud.data.dao.UserDao
import com.sevengod.maibud.data.entities.UserProfile

@Database(entities = [UserProfile::class], version = 3, exportSchema = false)
abstract class DataBaseInstance : RoomDatabase() {
    abstract fun userDao(): UserDao
}