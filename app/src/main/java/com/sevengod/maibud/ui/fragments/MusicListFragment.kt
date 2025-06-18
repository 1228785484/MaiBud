package com.sevengod.maibud.ui.fragments

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.sevengod.maibud.data.model.BasicInfo
import com.sevengod.maibud.data.model.Chart
import com.sevengod.maibud.data.model.Song
import com.sevengod.maibud.data.model.Record
import com.sevengod.maibud.data.entities.RecordEntity
import com.sevengod.maibud.data.viewmodels.DataInitState
import com.sevengod.maibud.data.viewmodels.MusicViewModel
import com.sevengod.maibud.ui.theme.MaiBudTheme
import androidx.compose.foundation.clickable
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.viewinterop.AndroidView
import com.bumptech.glide.Glide
import com.sevengod.maibud.R

// 添加难度enum定义
enum class Difficulty(val index: Int, val displayName: String, val color: Color) {
    BASIC(0, "Basic", Color(0xFF34D399)),      // #34d399 绿色
    ADVANCED(1, "Advanced", Color(0xFFFBBF24)), // #fbbf24 黄色
    EXPERT(2, "Expert", Color(0xFFEF4444)),     // #ef4444 红色
    MASTER(3, "Master", Color(0xFF8B5CF6)),     // #8b5cf6 紫色
    REMASTER(4, "Re:MASTER", Color(0xFFBC6DE0))  // #ec4899 粉色
}

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
                SuccessContent(
                    songList = songList,
                    musicViewModel = musicViewModel
                )
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
    musicViewModel: MusicViewModel? = null,
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
                    var currentDifficulty by remember { mutableStateOf(Difficulty.EXPERT) }
                    SongCard(
                        song = song,
                        currentDifficulty = currentDifficulty,
                        onDifficultyChange = { newDifficulty ->
                            currentDifficulty = newDifficulty
                        },
                        musicViewModel = musicViewModel
                    )
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
    currentDifficulty: Difficulty = Difficulty.EXPERT,
    onDifficultyChange: (Difficulty) -> Unit = {},
    musicViewModel: MusicViewModel? = null,
    modifier: Modifier = Modifier
) {
    // 创建 Pager 状态
    val pagerState = rememberPagerState(
        initialPage = currentDifficulty.index,
        pageCount = { song.level.size }
    )
    
    // 监听页面变化并更新难度
    LaunchedEffect(pagerState.currentPage) {
        val newDifficulty = Difficulty.values().getOrNull(pagerState.currentPage)
        if (newDifficulty != null && newDifficulty != currentDifficulty) {
            onDifficultyChange(newDifficulty)
        }
    }
    
    // 当外部难度变化时更新pager
    LaunchedEffect(currentDifficulty) {
        if (pagerState.currentPage != currentDifficulty.index) {
            pagerState.animateScrollToPage(currentDifficulty.index)
        }
    }

    // 整个Card作为Pager的内容
    HorizontalPager(
        state = pagerState,
        modifier = modifier.fillMaxWidth()
    ) { page ->
        val currentPageDifficulty = Difficulty.values().getOrNull(page) ?: Difficulty.EXPERT
        
        // 获取当前页面对应的记录
        var currentRecord by remember(song.id, page) { 
            mutableStateOf<Record?>(null) 
        }
        
        // 监听页面变化，获取对应记录
        LaunchedEffect(song.id, page) {
            currentRecord = musicViewModel?.localRecordData?.find { record ->
                record.songId == song.id && record.levelIndex == page
            }
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(all=16.dp)
            ) {
                //第一层,难度选择方框 - 显示当前页面的难度
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    repeat(song.level.size) { index ->
                        val difficulty = Difficulty.values().getOrNull(index)
                        if (difficulty != null) {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .size(40.dp)
                                    .background(
                                        color = if (page == index) 
                                            difficulty.color.copy(alpha = 0.7f) 
                                        else Color.White,
                                        shape = RoundedCornerShape(4.dp)
                                    )
                                    .border(
                                        width = if (page == index) 2.dp else 1.dp,
                                        color = if (page == index) 
                                            difficulty.color 
                                        else Color.Gray,
                                        shape = RoundedCornerShape(4.dp)
                                    )
                                    .clickable { 
                                        onDifficultyChange(difficulty)
                                    }
                            ){
                                Text(
                                    text=song.level[index],
                                    color = if (page == index) Color.White else Color.Black,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }

                //第二层,歌曲图片+谱师+曲师 - 显示当前页面的信息
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(96.dp)
                            .background(
                                color = Color(0xFF4A90A4), // 深蓝绿色背景
                                shape = RoundedCornerShape(8.dp)
                            )
                    ) {
                        MusicCoverImage(
                            "https://www.diving-fish.com/covers/${calculateRealId(song.id)}.png",
                            modifier = Modifier.size(96.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .height(96.dp), // 与图片高度保持一致
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = song.title,
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )

                        Text(
                            text = "曲师:${song.basicInfo.artist}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.padding(top = 4.dp)
                        )

                        Text(
                            text = "谱师:${song.charts.getOrNull(page)?.charter ?: "无"}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
                
                //第三层,完成率显示和FC/FS状态按钮 - 显示当前页面的记录
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // 左侧：完成率显示 - 占用33%
                    Box(
                        modifier = Modifier.weight(1f),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        Text(
                            text = currentRecord?.let { 
                                String.format("%.4f%%", it.achievements)
                            } ?: "未游玩",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.Black,
                            //这块后续可以根据完成率来改变颜色
                            fontWeight = FontWeight.Bold
                        )
                    }

                    // 中间：判定等级图片展示框（SSS/SS/S/A/B/C等） - 占用33%
                    Box(
                        modifier = Modifier.weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .height(32.dp)
                                .width(60.dp)
                                .background(
                                    color = Color.Transparent,
                                    shape = RoundedCornerShape(4.dp)
                                )
                                .border(
                                    width = 1.dp,
                                    color = Color.Transparent,
                                    shape = RoundedCornerShape(4.dp)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            currentRecord?.let { record ->
                                // 使用RecordEntity的getRateIcon方法
                                val recordEntity = RecordEntity(
                                    achievements = record.achievements,
                                    ds = record.ds,
                                    dxScore = record.dxScore,
                                    fc = record.fc,
                                    fs = record.fs,
                                    level = record.level,
                                    levelIndex = record.levelIndex,
                                    levelLabel = record.levelLabel,
                                    ra = record.ra,
                                    rate = record.rate,
                                    songId = record.songId,
                                    title = record.title,
                                    type = record.type
                                )
                                
                                Image(
                                    painter = painterResource(id = recordEntity.getRateIcon()),
                                    contentDescription = "评级: ${record.rate}",
                                    modifier = Modifier.fillMaxSize()
                                )
                            } ?: run {
                                // 未游玩时显示占位图
                                Image(
                                    painter = painterResource(id = R.drawable.d),
                                    contentDescription = "未游玩",
                                    modifier = Modifier.fillMaxSize()
                                )
                            }
                        }
                    }

                    // 右侧：FC和FS状态图片展示框 - 占用33%
                    Box(
                        modifier = Modifier.weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            // FC状态图片展示框
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .background(
                                        color = Color.Transparent,
                                        shape = CircleShape
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                currentRecord?.let { record ->
                                    val recordEntity = RecordEntity(
                                        achievements = record.achievements,
                                        ds = record.ds,
                                        dxScore = record.dxScore,
                                        fc = record.fc,
                                        fs = record.fs,
                                        level = record.level,
                                        levelIndex = record.levelIndex,
                                        levelLabel = record.levelLabel,
                                        ra = record.ra,
                                        rate = record.rate,
                                        songId = record.songId,
                                        title = record.title,
                                        type = record.type
                                    )
                                    
                                    Image(
                                        painter = painterResource(id = recordEntity.getFcIcon()),
                                        contentDescription = "FC状态: ${record.fc}",
                                        modifier = Modifier.size(40.dp)
                                    )
                                } ?: run {
                                    Image(
                                        painter = painterResource(id = R.drawable.blank),
                                        contentDescription = "未游玩",
                                        modifier = Modifier.size(40.dp)
                                    )
                                }
                            }

                            // FS状态图片展示框
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .background(
                                        color = Color.Transparent,
                                        shape = CircleShape
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                currentRecord?.let { record ->
                                    val recordEntity = RecordEntity(
                                        achievements = record.achievements,
                                        ds = record.ds,
                                        dxScore = record.dxScore,
                                        fc = record.fc,
                                        fs = record.fs,
                                        level = record.level,
                                        levelIndex = record.levelIndex,
                                        levelLabel = record.levelLabel,
                                        ra = record.ra,
                                        rate = record.rate,
                                        songId = record.songId,
                                        title = record.title,
                                        type = record.type
                                    )
                                    
                                    Image(
                                        painter = painterResource(id = recordEntity.getFsIcon()),
                                        contentDescription = "FS状态: ${record.fs}",
                                        modifier = Modifier.size(40.dp)
                                    )
                                } ?: run {
                                    Image(
                                        painter = painterResource(id = R.drawable.blank),
                                        contentDescription = "未游玩",
                                        modifier = Modifier.size(40.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

fun calculateRealId(id: Int): String {
    return if (id < 10000) {
        String.format("%05d", id)  // 补足5位，不足补0
    } else {
        id.toString()  // 直接转换为字符串，而不是强制类型转换
    }
}

@Composable
fun MusicCoverImage(
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