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
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.PreviewScreenSizes
import com.sevengod.maibud.ui.fragments.MusicListFragment
import com.sevengod.maibud.ui.fragments.ProfileFragment
import com.sevengod.maibud.ui.fragments.ToolsListFragment
import com.sevengod.maibud.ui.theme.MaiBudTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MaiBudTheme {
                MaiBudApp()
            }
        }
    }
}

@PreviewScreenSizes
@Composable
fun MaiBudApp() {
    var currentDestination by rememberSaveable { mutableStateOf(AppDestinations.MUSIC_LIST) }

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
        Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
            when (currentDestination) {
                AppDestinations.MUSIC_LIST -> MusicListFragment(
                    modifier = Modifier.padding(innerPadding)
                )
                AppDestinations.TOOLS_LIST -> ToolsListFragment(
                    modifier = Modifier.padding(innerPadding)
                )
                AppDestinations.PROFILE -> ProfileFragment(
                    modifier = Modifier.padding(innerPadding)
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