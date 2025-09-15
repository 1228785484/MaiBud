package com.sevengod.maibud.repository

import android.content.Context
import androidx.lifecycle.LiveData
import com.sevengod.maibud.data.entities.ChartEntity
import com.sevengod.maibud.data.entities.SongEntity
import com.sevengod.maibud.data.entities.SongWithChartsEntity
import com.sevengod.maibud.data.model.PlayerRecord
import com.sevengod.maibud.data.model.Song
import com.sevengod.maibud.instances.DataBaseInstance
import com.sevengod.maibud.instances.RetrofitInstance
import com.sevengod.maibud.network.SongDataService
import com.sevengod.maibud.utils.SongUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object SongDataRepository {
    private val client = RetrofitInstance.instance
    suspend fun getSongData(): Result<List<Song>> {
        return try {
            val response = client.create(SongDataService::class.java).getSongData()
            if (response.isSuccessful) {
                val songData = response.body()
                if (songData != null) {
                    Result.success(songData)
                } else {
                    Result.failure(Exception("Song data is null"))
                }
            } else {
                Result.failure(Exception("Failed to fetch song data"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getPlayerRecord(): Result<PlayerRecord> {
        return try {
            val response = client.create(SongDataService::class.java).getPlayerRecord()
            if (response.isSuccessful) {
                val playerRecord = response.body()
                if (playerRecord != null) {
                    Result.success(playerRecord)
                } else {
                    Result.failure(Exception("Player record is null"))
                }
            } else {
                Result.failure(Exception("Failed to fetch player record"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    //保存所有乐曲到数据库
    suspend fun saveSongsToDatabase(
        context: Context,
        songs: List<Song>
    ) {
        return withContext(Dispatchers.IO) {
            val pair = SongUtil.mapSongsToEntities(songs)
            val DB = DataBaseInstance.getInstance(context)
            DB.songDao().clearSongs()
            DB.chartDao().clearCharts()
            DB.songDao().insertAll(pair.first)
            DB.chartDao().insertAll(pair.second)
        }
    }

    suspend fun searchSongs(
        context: Context,
        minDs: Double? = null,
        maxDs: Double? = null,
        name: String? = null,
        version: String? = null
    ): List<SongWithChartsEntity> {
        return withContext(Dispatchers.IO) {
            val songWithChartsDao = DataBaseInstance.getInstance(context).songWithChartsDao()
            songWithChartsDao.searchSongsWithCharts(name, minDs, maxDs,version)
        }
    }
}

