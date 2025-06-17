package com.sevengod.maibud.ui.activities

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBox
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.PreviewScreenSizes
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import android.content.Intent
import com.sevengod.maibud.data.viewmodels.DataInitState
import com.sevengod.maibud.data.viewmodels.MusicViewModel
import com.sevengod.maibud.data.viewmodels.MusicViewModelFactory
import com.sevengod.maibud.data.viewmodels.LoginViewModel
import com.sevengod.maibud.data.viewmodels.LoginViewModelFactory
import com.sevengod.maibud.repository.LoginRepository
import com.sevengod.maibud.ui.fragments.MusicListFragment
import com.sevengod.maibud.ui.fragments.ProfileFragment
import com.sevengod.maibud.ui.fragments.ToolsListFragment
import com.sevengod.maibud.ui.theme.MaiBudTheme
import com.sevengod.maibud.ui.activities.LoginActivity

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MaiBudTheme {
                val musicViewModel: MusicViewModel = viewModel(
                    factory = MusicViewModelFactory(this@MainActivity)
                )
                
                val loginViewModel: LoginViewModel = viewModel(
                    factory = LoginViewModelFactory(
                        LoginRepository,
                        this@MainActivity
                    )
                )
                
                MaiBudApp(
                    musicViewModel = musicViewModel,
                    loginViewModel = loginViewModel
                )
            }
        }
    }
}

@PreviewScreenSizes
@Composable
fun MaiBudApp(
    musicViewModel: MusicViewModel? = null,
    loginViewModel: LoginViewModel? = null
) {
    var currentDestination by rememberSaveable { mutableStateOf(AppDestinations.MUSIC_LIST) }
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current

    // 应用启动时检查登录状态
    LaunchedEffect(loginViewModel) {
        loginViewModel?.loadCurrentUserFromStorage()
    }
    
    // 登录Activity启动器
    val loginLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        // LoginActivity返回后，无论结果如何都重新检查登录状态
        // 这样可以确保ProfileFragment得到最新的登录状态
        loginViewModel?.loadCurrentUserFromStorage()
    }

    // 监听数据初始化状态
    musicViewModel?.let { viewModel ->
        LaunchedEffect(viewModel.dataInitState) {
            when (val state = viewModel.dataInitState) {
                is DataInitState.Success -> {
                    snackbarHostState.showSnackbar(
                        message = "数据初始化完成！歌曲数量: ${viewModel.getSongData()?.size ?: 0}",
                        duration = SnackbarDuration.Short
                    )
                }
                is DataInitState.Error -> {
                    snackbarHostState.showSnackbar(
                        message = "数据初始化失败: ${state.message}",
                        duration = SnackbarDuration.Long
                    )
                    viewModel.goInitState()
                }
                else -> { /* 不处理其他状态 */ }
            }
        }
    }

    NavigationSuiteScaffold(
        navigationSuiteItems = {
            AppDestinations.entries.forEach {
                item(
                    icon = {
                        Icon(
                            it.icon,
                            contentDescription = it.label
                        )
                    },
                    label = { Text(it.label) },
                    selected = it == currentDestination,
                    onClick = { currentDestination = it }
                )
            }
        }
    ) {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            snackbarHost = { SnackbarHost(snackbarHostState) },
            floatingActionButton = {
                // 只在数据不在加载状态时显示刷新按钮
                if (musicViewModel?.dataInitState !is DataInitState.Loading) {
                    FloatingActionButton(
                        onClick = {
                            musicViewModel?.forceRefreshData()
                        }
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = "刷新数据")
                    }
                }
            }
        ) { innerPadding ->
            // 主要内容
            when (currentDestination) {
                AppDestinations.MUSIC_LIST -> MusicListFragment(
                    modifier = Modifier.padding(innerPadding),
                    musicViewModel = musicViewModel
                )
                AppDestinations.TOOLS_LIST -> ToolsListFragment(
                    modifier = Modifier.padding(innerPadding)
                )
                AppDestinations.PROFILE -> ProfileFragment(
                    modifier = Modifier.padding(innerPadding),
                    loginViewModel = loginViewModel,
                    onLoginRequest = {
                        val intent = Intent(context, LoginActivity::class.java)
                        loginLauncher.launch(intent)
                    }
                )
            }
        }
    }
}

enum class AppDestinations(
    val label: String,
    val icon: ImageVector,
) {
    MUSIC_LIST("乐曲列表", Icons.Default.Menu),
    TOOLS_LIST("工具列表", Icons.Default.Build),
    PROFILE("个人信息", Icons.Default.AccountCircle),
}