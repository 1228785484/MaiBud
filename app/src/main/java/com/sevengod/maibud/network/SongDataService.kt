package com.sevengod.maibud.network

import com.sevengod.maibud.data.model.Song
import retrofit2.Response
import retrofit2.http.GET

interface SongDataService {
    @GET("music_data")
    suspend fun getSongData(): Response<List<Song>>

}