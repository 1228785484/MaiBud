package com.sevengod.maibud.repository

import com.sevengod.maibud.data.model.PlayerRecord
import com.sevengod.maibud.data.model.Song
import com.sevengod.maibud.instances.RetrofitInstance
import com.sevengod.maibud.network.SongDataService

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
}
