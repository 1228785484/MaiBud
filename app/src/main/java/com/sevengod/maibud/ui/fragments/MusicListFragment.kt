package com.sevengod.maibud.ui.fragments

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.sevengod.maibud.data.model.BasicInfo
import com.sevengod.maibud.data.model.Chart
import com.sevengod.maibud.data.model.Song
import com.sevengod.maibud.data.viewmodels.DataInitState
import com.sevengod.maibud.data.viewmodels.MusicViewModel
import com.sevengod.maibud.ui.theme.MaiBudTheme

@Composable
fun MusicListFragment(
    modifier: Modifier = Modifier,
    musicViewModel: MusicViewModel? = null
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "乐曲列表",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        
        // 根据数据初始化状态显示不同内容
        when (val initState = musicViewModel?.dataInitState) {
            is DataInitState.Loading -> {
                LoadingContent()
            }
            is DataInitState.Success -> {
                val songList = musicViewModel.getSongData() ?: emptyList()
                SuccessContent(songList = songList)
            }
            is DataInitState.Error -> {
                ErrorContent(
                    errorMessage = initState.message,
                    onRetry = { musicViewModel?.initializeData() }
                )
            }
            is DataInitState.Idle, null -> {
                // 如果状态是Idle或ViewModel为null，显示初始化提示
                IdleContent()
            }
        }
    }
}

@Composable
private fun LoadingContent(
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            CircularProgressIndicator(
                modifier = Modifier.size(48.dp)
            )
            Text(
                text = "正在加载乐曲数据...",
                style = MaterialTheme.typography.bodyLarge,
                color = Color.Gray,
                modifier = Modifier.padding(top = 16.dp)
            )
        }
    }
}

@Composable
private fun SuccessContent(
    songList: List<Song>,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        // 显示歌曲数量信息
        Text(
            text = "共 ${songList.size} 首乐曲",
            style = MaterialTheme.typography.bodyMedium,
            color = Color.Gray,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        
        if (songList.isEmpty()) {
            // 空状态显示
            EmptyContent()
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(songList) { song ->
                    SongCard(song = song)
                }
            }
        }
    }
}

@Composable
private fun ErrorContent(
    errorMessage: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "加载失败",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = Color.Red
            )
            Text(
                text = errorMessage,
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Gray,
                modifier = Modifier.padding(top = 8.dp, bottom = 16.dp)
            )
            Button(
                onClick = onRetry
            ) {
                Text("重试")
            }
        }
    }
}

@Composable
private fun IdleContent(
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "正在初始化...",
            style = MaterialTheme.typography.bodyLarge,
            color = Color.Gray
        )
    }
}

@Composable
private fun EmptyContent(
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "暂无歌曲数据",
            style = MaterialTheme.typography.bodyLarge,
            color = Color.Gray
        )
    }
}

@Composable
private fun SongCard(
    song: Song,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = song.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            Text(
                text = "艺术家: ${song.basicInfo.artist}",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Gray,
                modifier = Modifier.padding(top = 4.dp)
            )
            
            Text(
                text = "类型: ${song.basicInfo.genre} | BPM: ${song.basicInfo.bpm}",
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray,
                modifier = Modifier.padding(top = 2.dp)
            )
            
            if (song.level.isNotEmpty()) {
                Text(
                    text = "难度: ${song.level.joinToString(" / ")}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun MusicListPreview() {
    MaiBudTheme {
        MusicListFragment()
    }
}
// 新增的 SongCard Preview
@Preview(showBackground = true, name = "SongCard Preview")
@Composable
fun SongCardPreview() {
    // 创建一个 BasicInfo 样本数据，确保它不为 null
    // 并且其内部字段也有合理的默认值，以避免预览时因 null 导致的问题
    val sampleBasicInfo = BasicInfo(
        artist = "示例艺术家",
        title = "ABC",
        genre = "D",
        bpm = 120, // 根据你的实际类型
        releaseDate = "2023-01-01", // 根据你的实际类型
        from = "示例来源",
        isNew = false
    )

    // 创建一个 Song 对象的样本数据
    // 确保所有 SongCard 中直接或间接用到的字段都被初始化
    val sampleChart1 = Chart(
        notes = listOf(100, 50, 20, 5, 0), // 示例 note 数量
        charter = "Charter A"
        // ... Chart 类的其他字段
    )
    val sampleChart2 = Chart(
        notes = listOf(200, 80, 30, 10, 1),
        charter = "Charter B"
        // ...
    )

    // 3. 创建 Song 的样本数据，替换所有 TODO()
    val sampleSong = Song(
        id = 5,                         // Int
        title = "星屑ユートピア",        // String - 这是 Song 级别的标题
        type = "DX",                    // String
        ds = listOf(7.0, 9.5, 12.5, 13.0, 14.1), // List<Double>
        level = listOf("7", "9+", "12+", "13", "14.1"), // List<String>
        cids = listOf(101, 102, 103, 104, 105),    // List<Int>
        charts = listOf(sampleChart1, sampleChart2), // List<Chart> - 使用上面创建的样本 Chart
        basicInfo = sampleBasicInfo       // BasicInfo - 使用上面创建的非空 BasicInfo
    )

    MaiBudTheme { // 使用你的应用主题来包裹预览
        SongCard(song = sampleSong)
    }
}