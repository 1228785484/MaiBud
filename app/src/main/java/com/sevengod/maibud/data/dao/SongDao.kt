package com.sevengod.maibud.data.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.sevengod.maibud.data.entities.SongEntity

@Dao
interface SongDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(songs: List<SongEntity>)

    @Query("SELECT * FROM song_data")
    suspend fun getAllSongs(): List<SongEntity>

    @Query("SELECT * FROM song_data WHERE id = :id")
    suspend fun getSongById(id: Int): SongEntity?

    @Query("DELETE FROM song_data")
    suspend fun clearSongs()

    @Query("SELECT COUNT(*) FROM song_data")
    suspend fun getSongCount(): Int
}