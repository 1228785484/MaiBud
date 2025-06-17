package com.sevengod.maibud.interceptors

import okhttp3.Interceptor
import okhttp3.Response
import java.io.IOException

/**
 * JWT Token拦截器
 * 对指定URL列表的请求自动添加JWT Token
 *
 * @param tokenProvider Token提供函数
 * @param urlList 需要添加Token的URL列表
 */
class JwtTokenInterceptor(
    private val tokenProvider: () -> String?,
    private val urlList: List<String>
) : Interceptor {

    @Throws(IOException::class)
    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        val requestUrl = originalRequest.url.toString()

        // 检查当前请求URL是否在需要验证的列表中
        val shouldAddToken = urlList.any { url ->
            requestUrl.contains(url, ignoreCase = true)
        }

        return if (shouldAddToken) {
            val token = tokenProvider()
            if (!token.isNullOrEmpty()) {
                // 构建带Token的新请求
                val newRequest = originalRequest.newBuilder()
                    .addHeader("Authorization", "Bearer $token")
                    .build()
                chain.proceed(newRequest)
            } else {
                // Token为空，继续原始请求
                chain.proceed(originalRequest)
            }
        } else {
            // URL不在列表中，继续原始请求
            chain.proceed(originalRequest)
        }
    }
}