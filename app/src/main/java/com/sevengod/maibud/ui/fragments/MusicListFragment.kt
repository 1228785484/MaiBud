package com.sevengod.maibud.ui.fragments

import android.widget.ImageView
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.SearchBar
import androidx.compose.material3.Slider
import androidx.compose.material3.TextField
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.TextStyle
import com.bumptech.glide.Glide
import com.sevengod.maibud.R
import com.sevengod.maibud.data.model.Difficulty
import kotlinx.coroutines.launch
import java.text.DecimalFormat
import kotlin.text.format

// 添加难度enum定义


@Composable
fun MusicListFragment(
    modifier: Modifier = Modifier,
    musicViewModel: MusicViewModel? = null
) {
    var isMenuVisible by remember { mutableStateOf(false) }

    Box(
        modifier = modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // 标题栏
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "乐曲列表",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )

                IconButton(
                    onClick = {
                        isMenuVisible = !isMenuVisible
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "搜索"
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 内容区域
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
                    IdleContent()
                }
            }
        }

        // 浮动菜单 - 独立于主内容布局
        AnimatedVisibility(
            visible = isMenuVisible,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 72.dp, start = 16.dp, end = 16.dp),
            enter = slideInVertically(
                initialOffsetY = { -it },
                animationSpec = tween(300, easing = FastOutSlowInEasing)
            ) + fadeIn(animationSpec = tween(300)),
            exit = slideOutVertically(
                targetOffsetY = { -it },
                animationSpec = tween(250, easing = FastOutLinearInEasing)
            ) + fadeOut(animationSpec = tween(250))
        ) {
            FilterMenu(
                musicViewModel = musicViewModel,
                onSearchRequest = {
                    isMenuVisible = false
                },
            )
        }
    }
}

@Composable
private fun LoadingContent(
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center
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
    //歌曲列表只需要管理songList即可
    songList: List<Song>, musicViewModel: MusicViewModel? = null, modifier: Modifier = Modifier
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
                items(songListWithoutYan(songList.reversed())) { song ->
                    var currentDifficulty by remember { mutableStateOf(Difficulty.MASTER) }
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
    errorMessage: String, onRetry: () -> Unit, modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center
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
        modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center
    ) {
        Text(
            text = "正在初始化...", style = MaterialTheme.typography.bodyLarge, color = Color.Gray
        )
    }
}

@Composable
private fun EmptyContent(
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center
    ) {
        Text(
            text = "暂无歌曲数据", style = MaterialTheme.typography.bodyLarge, color = Color.Gray
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
        initialPage = currentDifficulty.index, pageCount = { song.level.size })

    val nestedScrollConnection = remember { object : NestedScrollConnection {} }


    // 监听页面变化并更新难度
    LaunchedEffect(pagerState.currentPage) {
        val newDifficulty = Difficulty.values().getOrNull(pagerState.currentPage)
        if (newDifficulty != null && newDifficulty != currentDifficulty) {
            onDifficultyChange(newDifficulty)
        }
    }

    // 当外部难度变化时更新pager
    with(pagerState) {
        val coroutineScope = rememberCoroutineScope()
        LaunchedEffect(currentDifficulty) {
            coroutineScope.launch {
                if (currentPage != currentDifficulty.index) {
                    animateScrollToPage(
                        page = currentDifficulty.index, animationSpec = tween(
                            durationMillis = 500, easing = FastOutSlowInEasing
                        )
                    )
                }
            }
        }
    }

//    LaunchedEffect(currentDifficulty) {
//        if (pagerState.currentPage != currentDifficulty.index) {
//            pagerState.animateScrollToPage(currentDifficulty.index)
//        }
//    }

    // 整个Card作为Pager的内容
    HorizontalPager(
        state = pagerState,
        modifier = modifier.fillMaxWidth()
            .nestedScroll(nestedScrollConnection)
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
                    .padding(all = 16.dp)
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
                                        color = if (page == index) difficulty.color.copy(alpha = 0.7f)
                                        else Color.White, shape = RoundedCornerShape(4.dp)
                                    )
                                    .border(
                                        width = if (page == index) 2.dp else 1.dp,
                                        color = if (page == index) difficulty.color
                                        else Color.Gray,
                                        shape = RoundedCornerShape(4.dp)
                                    )
                                    .clickable {
                                        onDifficultyChange(difficulty)
                                    }) {
                                Text(
                                    text = song.ds[index].toString(),
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
                        key(song.id) {
                            MusicCoverImage(
                                "https://www.diving-fish.com/covers/${calculateRealId(song.id)}.png",
                                modifier = Modifier.size(96.dp)
                            )
                        }
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
                        modifier = Modifier.weight(1f), contentAlignment = Alignment.CenterStart
                    ) {
                        val achievementText = currentRecord?.let {
                            String.format("%.4f%%", it.achievements)
                        } ?: "未游玩"

                        // 100+分数显示彩虹色
                        if (currentRecord != null && currentRecord?.achievements!! >= 100.0) {
                            RainbowText(
                                text = achievementText,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold
                            )
                        } else {
                            Text(
                                text = achievementText,
                                style = MaterialTheme.typography.bodyMedium,
                                color = currentRecord?.let { record ->
                                    when {
                                        record.achievements >= 99.0 -> Color(0xFFFFD700) // 金色
                                        record.achievements >= 97.0 -> Color(0xFFC0C0C0) // 银色
                                        else -> Color.Black
                                    }
                                } ?: Color.Gray,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    // 中间：判定等级图片展示框（SSS/SS/S/A/B/C等） - 占用33%
                    Box(
                        modifier = Modifier.weight(1f), contentAlignment = Alignment.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .height(32.dp)
                                .width(60.dp)
                                .background(
                                    color = Color.Transparent, shape = RoundedCornerShape(4.dp)
                                )
                                .border(
                                    width = 1.dp,
                                    color = Color.Transparent,
                                    shape = RoundedCornerShape(4.dp)
                                ), contentAlignment = Alignment.Center
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
                        modifier = Modifier.weight(1f), contentAlignment = Alignment.Center
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            // FC状态图片展示框
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .background(
                                        color = Color.Transparent, shape = CircleShape
                                    ), contentAlignment = Alignment.Center
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
                                        color = Color.Transparent, shape = CircleShape
                                    ), contentAlignment = Alignment.Center
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
    imageUrl: String, modifier: Modifier = Modifier
) {
    AndroidView(
        factory = { context ->
            ImageView(context).apply {
                scaleType = ImageView.ScaleType.FIT_CENTER
                Glide.with(context).load(imageUrl)
                    .placeholder(android.R.drawable.ic_menu_gallery) // 加载中占位图
                    .error(android.R.drawable.ic_menu_close_clear_cancel) // 错误占位图
                    .into(this)
            }
        }, modifier = modifier
    )
}

//歌曲列表但是没宴会谱
fun songListWithoutYan(songList: List<Song>): List<Song> {
    return songList.filter { song ->
        song.basicInfo.genre != "宴会場"
    }
}

//筛选菜单
@Composable
private fun FilterMenu(
    musicViewModel: MusicViewModel?,
    onSearchRequest: () -> Unit
) {
    // State for min and max DS input fields
    // Initialize with default values
    var minDsSliderValue by remember { mutableFloatStateOf(1.0f) }
    var maxDsSliderValue by remember { mutableFloatStateOf(15.0f) }

    var searchText by remember { mutableStateOf("") }
    val dsValueRange = 1.0f..15.0f

    val steps =
        ((dsValueRange.endInclusive - dsValueRange.start) / 0.1f).toInt() - 1 // Calculate steps for 0.1 increments

    // DecimalFormat for displaying slider values with one decimal place
    val decimalFormat = remember { DecimalFormat("0.0") }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Gray)
            .padding(4.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = "乐曲名称")
            }
            //Todo 添加实际歌曲筛选逻辑
            TextField(
                value = searchText,
                onValueChange = { searchText = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("搜索歌曲") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = "搜索") }
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = "定数范围")
            }
            Text(
                text = "最低定数: ${decimalFormat.format(minDsSliderValue)}",
                style = MaterialTheme.typography.titleMedium
            )
            Slider(
                value = minDsSliderValue,
                onValueChange = { newValue ->
                    minDsSliderValue = newValue
                    // Ensure minDs is not greater than maxDs
                    if (minDsSliderValue > maxDsSliderValue) {
                        maxDsSliderValue = minDsSliderValue
                    }
                },
                valueRange = dsValueRange,
                steps = steps,
                modifier = Modifier.fillMaxWidth()
            )

            // --- 最高定数筛选 ---
            Text(
                text = "最高定数: ${decimalFormat.format(maxDsSliderValue)}",
                style = MaterialTheme.typography.titleMedium
            )
            Slider(
                value = maxDsSliderValue,
                onValueChange = { newValue ->
                    maxDsSliderValue = newValue
                    // Ensure maxDs is not less than minDs
                    if (maxDsSliderValue < minDsSliderValue) {
                        minDsSliderValue = maxDsSliderValue
                    }
                },
                valueRange = dsValueRange,
                steps = steps,
                modifier = Modifier.fillMaxWidth()
            )
            //暂时先不写
//            Row(
//                modifier = Modifier.fillMaxWidth(),
//                horizontalArrangement = Arrangement.SpaceBetween
//            ) {
//                Text(text = "难度 ")
//            }

        }
        Button(
            onClick = {
                // Convert Float to Double for the ViewModel
                val finalMinDs = minDsSliderValue.toDouble()
                val finalMaxDs = maxDsSliderValue.toDouble()

                // Call ViewModel's search function
                //Todo 添加名称筛选功能
                musicViewModel?.searchSongs(
                    name = searchText,
                    minDs = finalMinDs,
                    maxDs = finalMaxDs
                )
                onSearchRequest()
            },
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(bottom = 16.dp)
        ) {
            Text("确认")
        }
    }
}

@Composable
fun RainbowText(
    text: String,
    style: TextStyle = TextStyle.Default,
    fontWeight: FontWeight = FontWeight.Normal,
    modifier: Modifier = Modifier
) {
    val rainbowColors = listOf(
        Color(0xFFFF3366), // 亮红色
        Color(0xFFFF9933), // 亮橙色  
        Color(0xFFFFFF33), // 亮黄色
        Color(0xFF33FF33), // 亮绿色
        Color(0xFF3366FF), // 亮蓝色
        Color(0xFFCC33FF)  // 亮紫色
    )

    Text(
        text = text,
        style = style.copy(
            brush = Brush.linearGradient(
                colors = rainbowColors
            ),
            fontWeight = fontWeight
        ),
        modifier = modifier
    )
}
//以下为Preview

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
        artist = "示例艺术家", title = "ABC", genre = "D", bpm = 120, // 根据你的实际类型
        releaseDate = "2023-01-01", // 根据你的实际类型
        from = "示例来源", isNew = false
    )

    // 创建一个 Song 对象的样本数据
    // 确保所有 SongCard 中直接或间接用到的字段都被初始化
    val sampleChart1 = Chart(
        notes = listOf(100, 50, 20, 5, 0), // 示例 note 数量
        charter = "Charter A"
        // ... Chart 类的其他字段
    )
    val sampleChart2 = Chart(
        notes = listOf(200, 80, 30, 10, 1), charter = "Charter B"
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

//@Preview(showBackground = true, name = "FilterMenu Preview")
//@Composable
//fun FilterMenuPreview() {
//    MaiBudTheme {
//        FilterMenu()
//    }
//}


