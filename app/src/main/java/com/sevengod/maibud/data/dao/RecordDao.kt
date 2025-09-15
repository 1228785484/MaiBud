package com.sevengod.maibud.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import androidx.room.Delete
import com.sevengod.maibud.data.entities.RecordEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface RecordDao {
    @Query("SELECT * FROM record")
    fun getAllRecords(): Flow<List<RecordEntity>>

    @Query("SELECT * FROM record")
    suspend fun getAllRecordsSync(): List<RecordEntity>

    @Query("SELECT * FROM record WHERE song_id = :songId")
    fun getRecordsBySongId(songId: Int): Flow<List<RecordEntity>>

    @Query("SELECT * FROM record WHERE song_id = :songId")
    suspend fun getRecordsBySongIdSync(songId: Int): List<RecordEntity>

    @Query("SELECT * FROM record WHERE song_id = :songId AND level_index = :levelIndex")
    fun getRecordBySongIdAndLevel(songId: Int, levelIndex: Int): Flow<RecordEntity?>

    @Query("SELECT * FROM record WHERE song_id = :songId AND level_index = :levelIndex")
    suspend fun getRecordBySongIdAndLevelSync(songId: Int, levelIndex: Int): RecordEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecord(record: RecordEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecords(records: List<RecordEntity>)

    @Update
    suspend fun updateRecord(record: RecordEntity)

    @Delete
    suspend fun deleteRecord(record: RecordEntity)

    @Query("DELETE FROM record")
    suspend fun deleteAllRecords()

    @Query("DELETE FROM record WHERE song_id = :songId")
    suspend fun deleteRecordsBySongId(songId: Int)

    @Query("SELECT COUNT(*) FROM record")
    suspend fun getRecordCount(): Int

    @Query("SELECT * FROM record ORDER BY ra DESC LIMIT 50")
    fun getTop50RecordsByRating(): Flow<List<RecordEntity>>

}