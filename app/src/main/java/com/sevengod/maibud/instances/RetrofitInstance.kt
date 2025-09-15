package com.sevengod.maibud.instances

import android.app.Application
import android.content.Context
import com.sevengod.maibud.interceptors.JwtTokenInterceptor
import com.sevengod.maibud.utils.DSUtils
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.runBlocking

object RetrofitInstance {
    private const val BASE_URL = "https://www.diving-fish.com/api/maimaidxprober/"

    val reqAuthList = listOf("player/")
    
    // 需要传入Context来初始化，通常在Application中调用
    @Volatile
    private var appContext: Context? = null
    
    /**
     * 初始化Context，应该在Application中调用
     */
    fun initialize(context: Context) {
        appContext = context.applicationContext
    }
    val instance: Retrofit by lazy {
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
        
        val okHttpClient = OkHttpClient.Builder()
            .addInterceptor(JwtTokenInterceptor(
                tokenProvider = { getJwtToken() },
                urlList = reqAuthList
            ))
            .addInterceptor(loggingInterceptor)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()
            
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .client(okHttpClient)
            .build()
    }
    
    /**
     * 获取JWT Token，供拦截器使用
     */
    private fun getJwtToken(): String? {
        return try {
            appContext?.let { context ->
                runBlocking {
                    DSUtils.getData(context, "current_user_jwt")
                }
            }
        } catch (e: Exception) {
            // 如果获取失败，返回null，拦截器会继续进行原始请求
            null
        }
    }

}