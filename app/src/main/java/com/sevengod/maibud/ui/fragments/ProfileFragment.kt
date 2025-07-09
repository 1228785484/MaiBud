package com.sevengod.maibud.ui.fragments

import android.content.Intent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBox
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.compose.ui.platform.LocalLifecycleOwner
import kotlinx.coroutines.flow.MutableStateFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.sevengod.maibud.data.entities.UserProfile
import com.sevengod.maibud.ui.activities.LoginActivity
import com.sevengod.maibud.ui.theme.MaiBudTheme
import com.sevengod.maibud.data.viewmodels.LoginViewModel
import com.sevengod.maibud.data.viewmodels.MusicViewModel


@Composable
fun ProfileFragment(
    modifier: Modifier = Modifier,
    loginViewModel: LoginViewModel? = null,
    onLoginRequest: () -> Unit = {},
    musicViewModel: MusicViewModel? = null
) {
    val ctx = LocalContext.current
    
    // 观察登录状态
    val currentUser by remember(loginViewModel) {
        if (loginViewModel != null) {
            loginViewModel.currentUser
        } else {
            MutableStateFlow<UserProfile?>(null)
        }
    }.collectAsState()
    
    // 监听登录状态变化
    LaunchedEffect(currentUser) {
        // 这里可以处理登录状态变化时的逻辑
        // 比如显示欢迎消息、数据刷新等
        if (currentUser != null) {
            // 用户登录了
            // 可以在这里触发一些登录后的操作
        } else {
            // 用户登出了
            // 可以在这里处理登出后的清理工作
        }
    }
    
    // 在页面启动时从DSUtils加载用户状态
    LaunchedEffect(loginViewModel) {
        loginViewModel?.loadCurrentUserFromStorage()
    }
    
    // 监听生命周期，当页面重新获得焦点时检查登录状态
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                // 页面恢复时重新检查登录状态
                loginViewModel?.loadCurrentUserFromStorage()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }
    
    val isLoggedIn = currentUser != null

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (isLoggedIn && musicViewModel != null) {
            LoggedInProfile(
                musicViewModel = musicViewModel,
                currentUser = currentUser!!,
                onLogout = { loginViewModel?.logout() }
            )
        } else {
            NotLoggedInProfile(
                onLoginRequest = onLoginRequest
            )
        }
    }
}

@Composable
private fun LoggedInProfile(
    musicViewModel: MusicViewModel,
    currentUser: UserProfile,
    onLogout: () -> Unit
) {
    val userName = currentUser.username
    
    // 获取用户rating
    LaunchedEffect(Unit) {
        musicViewModel.getUserRating()
    }
    
    val userRating = musicViewModel.rating

    // 个人信息顶部卡片
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.AccountBox,
                contentDescription = "用户头像",
                modifier = Modifier.padding(bottom = 8.dp)
            )
            
            Text(
                text = "用户名：$userName",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Rating：$userRating",
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(top = 4.dp)
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            OutlinedButton(
                onClick = onLogout
            ) {
                Text("退出")
            }
        }
    }
    
    Spacer(modifier = Modifier.height(24.dp))
    
    // 横排信息卡片
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // 测试数据卡片
        Card(
            modifier = Modifier.weight(1f),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(
                modifier = Modifier.padding(12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Todos",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "可能会在这边放些UI",
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }
    }
}

@Composable
private fun NotLoggedInProfile(
    onLoginRequest: () -> Unit
) {
    // 个人信息顶部卡片 - 未登录状态
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.AccountBox,
                contentDescription = "用户头像",
                modifier = Modifier.padding(bottom = 8.dp)
            )
            
            Text(
                text = "未登录",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Button(
                onClick = onLoginRequest
            ) {
                Text("登录")
            }
        }
    }
    
    Spacer(modifier = Modifier.height(24.dp))
    
    // 未登录状态显示提示信息
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Text(
            text = "登录后查看更多个人信息",
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(16.dp)
        )
    }
}

@Preview(showBackground = true)
@Composable
fun ProfilePreview() {
    MaiBudTheme {
        ProfileFragment()
    }
} 