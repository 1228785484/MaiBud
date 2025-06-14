package com.sevengod.maibud.network

import com.sevengod.maibud.data.model.LoginRequest
import com.sevengod.maibud.data.model.LoginResponse

import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.Response

interface LoginService {
    @POST("login")
    suspend fun login(@Body req: LoginRequest): Response<LoginResponse>
}