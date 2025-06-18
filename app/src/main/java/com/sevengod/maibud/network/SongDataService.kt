package com.sevengod.maibud.network

import com.sevengod.maibud.data.model.PlayerRecord
import com.sevengod.maibud.data.model.Song
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path

interface SongDataService {
    @GET("music_data")
    suspend fun getSongData(): Response<List<Song>>
    @GET("player/records")
    suspend fun getPlayerRecord(): Response<PlayerRecord>

}