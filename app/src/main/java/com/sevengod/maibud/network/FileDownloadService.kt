package com.sevengod.maibud.network

import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Streaming
import retrofit2.http.Url

interface FileDownloadService {
    @Streaming
    @GET
    suspend fun downloadFile(@Url url: String): Response<ResponseBody>

}