package com.sevengod.maibud.repository

import android.content.Context
import com.sevengod.maibud.data.model.LoginRequest
import com.sevengod.maibud.data.model.LoginResponse
import com.sevengod.maibud.instances.RetrofitInstance
import com.sevengod.maibud.network.FileDownloadService
import com.sevengod.maibud.network.LoginService
import com.sevengod.maibud.network.WebContentService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jsoup.Jsoup
import retrofit2.Response
import java.io.File
import java.io.FileOutputStream

object QRCodeRepository {
    private val client = RetrofitInstance.instance
    private val fileService = client.create(FileDownloadService::class.java)
    private val webContentService = client.create(WebContentService::class.java)

    /**
     * 从指定URL获取网页内容并解析出二维码图片链接
     */
    suspend fun getQRCodeImageUrl(pageUrl: String): Result<String> {
        return withContext(Dispatchers.IO) {
            try {
                val response = webContentService.getWebContent(pageUrl)
                if (response.isSuccessful) {
                    val htmlContent = response.body()?.string()
                    if (htmlContent != null) {
                        // 使用 Jsoup 解析 HTML
                        val document = Jsoup.parse(htmlContent)
                        val imgElement = document.select("img").first()
                        
                        if (imgElement != null) {
                            val imgSrc = imgElement.attr("src")
                            // 处理相对路径，构建完整的图片URL
                            val fullImageUrl = if (imgSrc.startsWith("../img/")) {
                                val imageName = imgSrc.replace("../img/", "")
                                "http://wq.sys-allnet.cn/qrcode/img/$imageName"
                            } else {
                                imgSrc
                            }
                            Result.success(fullImageUrl)
                        } else {
                            Result.failure(Exception("未找到图片标签"))
                        }
                    } else {
                        Result.failure(Exception("网页内容为空"))
                    }
                } else {
                    Result.failure(Exception("获取网页失败: ${response.code()}"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    /**
     * 下载并保存二维码图片到本地
     */
    suspend fun downloadAndSaveQRCode(context: Context, imageUrl: String, filename: String): Result<String> {
        return withContext(Dispatchers.IO) {
            try {
                val response = fileService.downloadFile(imageUrl)
                if (response.isSuccessful) {
                    val body = response.body()
                    if (body != null) {
                        // 创建文件保存路径
                        val file = File(context.cacheDir, "$filename.png")
                        val inputStream = body.byteStream()
                        val outputStream = FileOutputStream(file)
                        
                        inputStream.use { input ->
                            outputStream.use { output ->
                                input.copyTo(output)
                            }
                        }
                        
                        Result.success(file.absolutePath)
                    } else {
                        Result.failure(Exception("下载内容为空"))
                    }
                } else {
                    Result.failure(Exception("下载失败: ${response.code()}"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    /**
     * 获取二维码图片URL（不下载，直接返回URL用于临时显示）
     */
    suspend fun fetchQRCodeImageUrl(pageUrl: String): Result<String> {
        return try {
            getQRCodeImageUrl(pageUrl)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * 获取并下载二维码的完整流程（保留原方法以备后用）
     */
    suspend fun fetchAndDownloadQRCode(context: Context, pageUrl: String, maiId: String): Result<String> {
        return try {
            val imageUrlResult = getQRCodeImageUrl(pageUrl)
            imageUrlResult.fold(
                onSuccess = { imageUrl ->
                    downloadAndSaveQRCode(context, imageUrl, "qr_$maiId")
                },
                onFailure = { exception ->
                    Result.failure(exception)
                }
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}