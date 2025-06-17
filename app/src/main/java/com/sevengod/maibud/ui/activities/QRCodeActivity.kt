package com.sevengod.maibud.ui.activities

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.viewmodel.compose.viewModel
import com.bumptech.glide.Glide
import com.sevengod.maibud.R
import com.sevengod.maibud.data.viewmodels.QRCodeUiState
import com.sevengod.maibud.data.viewmodels.QRCodeViewModel
import com.sevengod.maibud.data.viewmodels.QRCodeViewModelFactory
import com.sevengod.maibud.repository.QRCodeRepository
import com.sevengod.maibud.ui.theme.MaiBudTheme

class QRCodeActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MaiBudTheme {
                val qrCodeViewModel: QRCodeViewModel = viewModel(
                    factory = QRCodeViewModelFactory(QRCodeRepository, this@QRCodeActivity)
                )

                QRCodeScreen(
                    viewModel = qrCodeViewModel
                )
            }
        }
    }
}

@Composable
fun QRCodeScreen(
    viewModel: QRCodeViewModel,
    modifier: Modifier = Modifier
) {
    QRCodeScreenStateless(
        pageUrl = viewModel.pageUrl,
        maiId = viewModel.maiId,
        uiState = viewModel.qrCodeUiState,
        onMaiIdChange = viewModel::onMaiIdChange,
        onGetQRCode = viewModel::fetchQRCode,
        onGetSaveStatus = viewModel::getStoreStatus,
        modifier = modifier
    )

}

@Composable
fun QRCodeScreenStateless(
    pageUrl: String,
    maiId: String,
    uiState: QRCodeUiState,
    onMaiIdChange: (String) -> Unit,
    onGetQRCode: () -> Unit,
    onGetSaveStatus: () -> Boolean,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    // 监听状态变化，显示Toast
    LaunchedEffect(uiState) {
        when (uiState) {
            is QRCodeUiState.Success -> {
                Toast.makeText(context, "二维码获取成功！", Toast.LENGTH_SHORT).show()
            }

            is QRCodeUiState.Error -> {
                Toast.makeText(context, uiState.message, Toast.LENGTH_LONG).show()
            }

            else -> {}
        }
    }
    // 进入界面时自动加载二维码
    LaunchedEffect(Unit) {
        onGetQRCode()
    }


    Scaffold(
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "获取登录二维码",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )


            OutlinedTextField(
                value = maiId,
                onValueChange = onMaiIdChange,
                label = { Text("MAI ID *") },
                placeholder = { Text("输入MAI ID（必填）") },
                modifier = Modifier.fillMaxWidth(),
                enabled = uiState !is QRCodeUiState.Loading
            )

            Button(
                onClick = onGetQRCode,
                modifier = Modifier.fillMaxWidth(),
                enabled = uiState !is QRCodeUiState.Loading// && maiId.isNotBlank() 这块边界条件后续应用其他方式修正
            ) {
                if (uiState is QRCodeUiState.Loading) {
                    CircularProgressIndicator(
                        modifier = Modifier
                            .size(16.dp)
                            .padding(end = 8.dp),
                        color = Color.White
                    )
                }
                Text("获取二维码")
            }

            val DBStatusText = if (onGetSaveStatus()) "数据库内存在数据,可直接获取" else "数据库中不存在数据,请填写MAID"

            Text(
                modifier = Modifier.fillMaxWidth(),
                text = DBStatusText
            )

            // 显示错误信息
            if (uiState is QRCodeUiState.Error) {
                Text(
                    text = uiState.message,
                    color = Color.Red,
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            // 显示二维码图片
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                when (uiState) {
                    is QRCodeUiState.Loading -> {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            CircularProgressIndicator()
                            Text(
                                text = "正在获取二维码...",
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.padding(top = 8.dp)
                            )
                        }
                    }

                    is QRCodeUiState.Success -> {
                        QRCodeImage(
                            imageUrl = uiState.imageUrl,
                            modifier = Modifier.size(300.dp)
                        )
                    }

                    else -> {
                        Text(
                            text = "请输入MAI ID并点击获取二维码",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.Gray
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun QRCodeImage(
    imageUrl: String,
    modifier: Modifier = Modifier
) {
    AndroidView(
        factory = { context ->
            android.widget.ImageView(context).apply {
                scaleType = android.widget.ImageView.ScaleType.FIT_CENTER
                Glide.with(context)
                    .load(imageUrl)
                    .placeholder(android.R.drawable.ic_menu_gallery) // 加载中占位图
                    .error(android.R.drawable.ic_menu_close_clear_cancel) // 错误占位图
                    .into(this)
            }
        },
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun QRCodeScreenPreview() {
    MaiBudTheme {
        QRCodeScreenStateless(
            pageUrl = "https://example.com",
            maiId = "TEST123",
            uiState = QRCodeUiState.Idle,
            onMaiIdChange = {},
            onGetQRCode = {},
            onGetSaveStatus = {false}
        )
    }
}
