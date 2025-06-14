package com.sevengod.maibud.repository

import com.sevengod.maibud.data.model.LoginRequest
import com.sevengod.maibud.data.model.LoginResponse
import com.sevengod.maibud.instances.RetrofitInstance
import com.sevengod.maibud.network.LoginService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.Dispatcher
import retrofit2.Response

object LoginRepository {
    val client = RetrofitInstance.instance;
    suspend fun getLoginToken(username: String, password: String): Result<LoginResponse> {
        return withContext(Dispatchers.IO) {

            try {
                val loginRequest = LoginRequest(username, password)
                val response: Response<LoginResponse> =
                    client.create(LoginService::class.java).login(loginRequest)

                if (response.isSuccessful) {
                    val headers = response.headers()
                    val token = headers["set-cookie"]

                    val responseBody = response.body()
                    val message = responseBody?.message
                    Result.success(
                        LoginResponse(
                            jwt = token,
                            message = message
                        )
                    )
                } else {
                    Result.failure(Exception("Login failed"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }
}