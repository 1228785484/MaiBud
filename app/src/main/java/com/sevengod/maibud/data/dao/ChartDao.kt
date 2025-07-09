package com.sevengod.maibud.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.sevengod.maibud.data.entities.ChartEntity

@Dao
interface ChartDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(charts: List<ChartEntity>)
    @Query("DELETE FROM chart")
    suspend fun clearCharts()
}