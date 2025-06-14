package com.sevengod.maibud.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.sevengod.maibud.data.entities.UserProfile
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateUserProfile(userProfile: UserProfile)
    @Query("SELECT * FROM user_profile WHERE username = :targetUsername LIMIT 1")
    fun getUserByUsername(targetUsername: String): Flow<UserProfile?>
}