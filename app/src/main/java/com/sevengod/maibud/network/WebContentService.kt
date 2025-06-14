package com.sevengod.maibud.network

import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Url

interface WebContentService {
    @GET
    suspend fun getWebContent(@Url url: String): Response<ResponseBody>
} 