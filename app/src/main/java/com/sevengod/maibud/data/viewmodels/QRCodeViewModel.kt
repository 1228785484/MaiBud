package com.sevengod.maibud.data.viewmodels

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.sevengod.maibud.repository.QRCodeRepository
import com.sevengod.maibud.utils.DSUtils
import kotlinx.coroutines.launch

// 定义二维码UI状态
sealed interface QRCodeUiState {
    object Idle : QRCodeUiState
    object Loading : QRCodeUiState
    data class Success(val imageUrl: String) : QRCodeUiState
    data class Error(val message: String) : QRCodeUiState
}

class QRCodeViewModel(
    private val qrCodeRepository: QRCodeRepository,
    private val context: Context
) : ViewModel() {

    var qrCodeUiState by mutableStateOf<QRCodeUiState>(QRCodeUiState.Idle)
        private set

    var pageUrl = "http://wq.sys-allnet.cn/qrcode/req/"

    var maiId by mutableStateOf("")
        private set

    var isStored by mutableStateOf(false)
        private set

    fun onMaiIdChange(newMaiId: String) {
        maiId = newMaiId
        if (qrCodeUiState is QRCodeUiState.Error) {
            qrCodeUiState = QRCodeUiState.Idle
        }
    }

    // 非suspend的公共方法，供UI调用
    fun fetchQRCode() {
        viewModelScope.launch {
            fetchQRCodeInternal()
        }
    }

    fun linkToMaiId(link: String) {
        val extractedMaiId = extractMaiIdFromUrl(link)
        if (extractedMaiId.isNotBlank()) {
            maiId = extractedMaiId
            // 存储解析出的maiID到DataStore
            viewModelScope.launch {
                DSUtils.storeData(context, "maiId", extractedMaiId)
            }
        }
    }

    // 内部的suspend方法，包含实际的业务逻辑
    private suspend fun fetchQRCodeInternal() {
        var finalID = DSUtils.getData(context, "maiId") ?: ""

        // 如果找不到finalID就用maiID
        if (finalID.isBlank()) {
            // 检查用户输入的maiId是否是URL，如果是则解析出真正的maiId
            val processedMaiId = if (maiId.startsWith("http")) {
                extractMaiIdFromUrl(maiId)
            } else {
                maiId
            }
            
            finalID = processedMaiId
            // 存储处理后的maiID到DataStore
            if (processedMaiId.isNotBlank()) {
                DSUtils.storeData(context, "maiId", processedMaiId)
            }
        }

        // 检查最终的ID是否为空
        if (finalID.isBlank()) {
            qrCodeUiState = QRCodeUiState.Error("MAI ID不能为空")
            return
        }

        qrCodeUiState = QRCodeUiState.Loading

        try {
            // 构建完整的URL
            val timestamp = System.currentTimeMillis()/1000
            val fixedTextT =
                "E8889EE8908C4458202F20E4B8ADE4BA8CE88A82E5A58F20E799BBE585A5E4BA8CE7BBB4E7A081"
            val fixedTextD =
                "E68A8AE4B88BE696B9E4BA8CE7BBB4E7A081E5AFB9E58786E69CBAE58FB0E689ABE68F8FE5A484EFBC8CE58FAFE794A8E69CBAE58FB0E69C89E38090E8889EE8908C4458E38091E5928CE38090E4B8ADE4BA8CE88A82E5A58FE38091"

            val fullPageUrl =
                "${pageUrl}${finalID}.html?l=${timestamp}&t=${fixedTextT}&d=${fixedTextD}"

            // 只获取图片URL，不下载到本地
            val result = qrCodeRepository.fetchQRCodeImageUrl(fullPageUrl)

            result.fold(
                onSuccess = { imageUrl ->
                    qrCodeUiState = QRCodeUiState.Success(imageUrl)
                },
                onFailure = { exception ->
                    qrCodeUiState = QRCodeUiState.Error(
                        exception.message ?: "获取二维码失败"
                    )
                }
            )
        } catch (e: Exception) {
            qrCodeUiState = QRCodeUiState.Error(
                "发生意外错误: ${e.message}"
            )
        }
    }

    fun resetState() {
        qrCodeUiState = QRCodeUiState.Idle
    }

    fun getStoreStatus(): Boolean {
        viewModelScope.launch {
            isStored = DSUtils.getData(context, "maiId") != null
        }
        return isStored
    }

    /**
     * 从URL中提取maiID
     * URL格式: http://wq.sys-allnet.cn/qrcode/req/{MAID}.html?...
     */
    private fun extractMaiIdFromUrl(url: String): String {
        return try {
            if (url.contains("qrcode/req/")) {
                // 使用正则表达式提取MAID部分
                val regex = Regex("""/qrcode/req/([^.]+)\.html""")
                val matchResult = regex.find(url)
                matchResult?.groupValues?.get(1) ?: ""
            } else {
                // 如果不是预期的URL格式，返回原始输入
                url
            }
        } catch (e: Exception) {
            // 解析失败，返回原始输入
            url
        }
    }
}

/**
 * QRCodeViewModel 工厂类
 */
class QRCodeViewModelFactory(
    private val qrCodeRepository: QRCodeRepository,
    private val context: Context
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(QRCodeViewModel::class.java)) {
            return QRCodeViewModel(qrCodeRepository, context) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
} 