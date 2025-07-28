package com.sevengod.maibud.utils

import android.content.Context
import android.util.Log
import androidx.compose.ui.input.key.type
import androidx.datastore.preferences.core.stringPreferencesKey
import com.google.gson.Gson
import com.sevengod.maibud.data.dao.ChartDao
import com.sevengod.maibud.data.dao.SongDao
import com.sevengod.maibud.data.model.Song
import com.sevengod.maibud.data.model.PlayerRecord
import com.sevengod.maibud.repository.SongDataRepository
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn
import com.sevengod.maibud.data.entities.SongEntity
import com.sevengod.maibud.data.entities.ChartEntity
import com.sevengod.maibud.data.entities.DifficultyType
import com.sevengod.maibud.data.entities.SongWithChartsEntity
import com.sevengod.maibud.data.model.BasicInfo
import com.sevengod.maibud.data.model.Chart
import com.sevengod.maibud.repository.RecordRepository

object SongUtil {
    const val LAST_SONG_UPDATE_DATE = "last_song_update_date"
    const val SONG_DATA = "song_data"
    const val LAST_PLAYER_RECORD_UPDATE_DATE = "last_player_record_update_date"
    const val PLAYER_RECORD_DATA = "player_record_data"
    const val TAG = "song_util"
    private val gson = Gson()

    suspend fun getSongData(context: Context) {
        val today: LocalDate = Clock.System.todayIn(TimeZone.currentSystemDefault())
        val todayDateString: String = today.toString()
        val lastUpdateDate = DSUtils.getData(context, LAST_SONG_UPDATE_DATE)

        if (lastUpdateDate != null) {
            // 检查是否需要更新数据
            if (lastUpdateDate != todayDateString) {
                Log.d(TAG, "数据需要更新，上次更新日期：$lastUpdateDate，今天：$todayDateString")
                getActualDataLogic(context)
                // 更新最后更新日期
                DSUtils.storeData(context, LAST_SONG_UPDATE_DATE, todayDateString)
            } else {
                Log.d(TAG, "数据是最新的，无需更新")
                // 可选：验证本地数据是否存在
                val localData = DSUtils.getData(context, SONG_DATA)
                if (localData == null) {
                    Log.w(TAG, "本地数据丢失，重新获取")
                    getActualDataLogic(context)
                }
            }
        } else {
            // 首次运行，获取数据并存储日期
            Log.d(TAG, "首次运行，获取歌曲数据")
            getActualDataLogic(context)
            DSUtils.storeData(context, LAST_SONG_UPDATE_DATE, todayDateString)
        }
    }

    private suspend fun getActualDataLogic(context: Context) {
        Log.d(TAG, "开始获取歌曲数据...")
        val result: Result<List<Song>> = SongDataRepository.getSongData()
        result.onSuccess { songs ->
            Log.d(TAG, "成功获取 ${songs.size} 首歌曲")
            val json = gson.toJson(songs)
            DSUtils.storeData(context, SONG_DATA, json)
            SongDataRepository.saveSongsToDatabase(context,songs)
            Log.d(TAG, "歌曲数据已保存到本地")
        }.onFailure { e ->
            Log.e(TAG, "拉取失败: ${e.message}", e)
        }
    }

    /**
     * 从本地存储获取歌曲数据
     */
    suspend fun getLocalSongData(context: Context): List<Song>? {
        return try {
            val jsonData = DSUtils.getData(context, SONG_DATA)
            if (jsonData != null) {
                val songs = gson.fromJson(jsonData, Array<Song>::class.java).toList()
                Log.d(TAG, "从本地加载了 ${songs.size} 首歌曲")
                songs
            } else {
                Log.w(TAG, "本地没有歌曲数据")
                null
            }
        } catch (e: Exception) {
            Log.e(TAG, "解析本地歌曲数据失败: ${e.message}", e)
            null
        }
    }

    /**
     * 强制刷新歌曲数据
     */
    suspend fun forceRefreshSongData(context: Context) {
        Log.d(TAG, "强制刷新歌曲数据")
        val today: LocalDate = Clock.System.todayIn(TimeZone.currentSystemDefault())
        val todayDateString: String = today.toString()

        getActualDataLogic(context)
        DSUtils.storeData(context, LAST_SONG_UPDATE_DATE, todayDateString)
    }

    /**
     * 清除本地歌曲数据
     */
    suspend fun clearLocalSongData(context: Context) {
        Log.d(TAG, "清除本地歌曲数据")
        DSUtils.removeData(context, SONG_DATA)
        DSUtils.removeData(context, LAST_SONG_UPDATE_DATE)
    }

    // ==================== 玩家记录相关方法 ====================

    suspend fun getPlayerRecordData(context: Context) {
        val today: LocalDate = Clock.System.todayIn(TimeZone.currentSystemDefault())
        val todayDateString: String = today.toString()
        val lastUpdateDate = DSUtils.getData(context, LAST_PLAYER_RECORD_UPDATE_DATE)

        if (lastUpdateDate != null) {
            // 检查是否需要更新数据
            if (lastUpdateDate != todayDateString) {
                Log.d(TAG, "玩家记录数据需要更新，上次更新日期：$lastUpdateDate，今天：$todayDateString")
                getActualPlayerRecordLogic(context)
                // 更新最后更新日期
                DSUtils.storeData(context, LAST_PLAYER_RECORD_UPDATE_DATE, todayDateString)
            } else {
                Log.d(TAG, "玩家记录数据是最新的，无需更新")
                // 可选：验证本地数据是否存在
                val localData = DSUtils.getData(context, PLAYER_RECORD_DATA)
                if (localData == null) {
                    Log.w(TAG, "本地玩家记录数据丢失，重新获取")
                    getActualPlayerRecordLogic(context)
                }
            }
        } else {
            // 首次运行，获取数据并存储日期
            Log.d(TAG, "首次运行，获取玩家记录数据")
            getActualPlayerRecordLogic(context)
            DSUtils.storeData(context, LAST_PLAYER_RECORD_UPDATE_DATE, todayDateString)
        }
    }

    private suspend fun getActualPlayerRecordLogic(context: Context) {
        Log.d(TAG, "开始获取玩家记录数据...")
        val result: Result<PlayerRecord> = SongDataRepository.getPlayerRecord()
        result.onSuccess { playerRecords ->
            Log.d(TAG, "成功获取 ${playerRecords.records.size} 条玩家记录")
            val json = gson.toJson(playerRecords)
            DSUtils.storeData(context, PLAYER_RECORD_DATA, json)
            Log.d(TAG, "玩家记录数据已保存到本地")
            
            // 自动保存到数据库
            try {
                RecordRepository.savePlayerRecordToDB(context, playerRecords)
                Log.d(TAG, "玩家记录数据已保存到数据库")
            } catch (e: Exception) {
                Log.e(TAG, "保存玩家记录到数据库失败", e)
                // 不抛出异常，因为本地存储已经成功
            }
        }.onFailure { e ->
            Log.e(TAG, "拉取玩家记录失败: ${e.message}", e)
        }
    }

    /**
     * 从本地存储获取玩家记录数据
     */
    suspend fun getLocalPlayerRecordData(context: Context): PlayerRecord? {
        return try {
            val jsonData = DSUtils.getData(context, PLAYER_RECORD_DATA)
            if (jsonData != null) {
                val playerRecords = gson.fromJson(jsonData, PlayerRecord::class.java)
                Log.d(TAG, "从本地加载了 ${playerRecords.records.size} 条玩家记录")
                playerRecords
            } else {
                Log.w(TAG, "本地没有玩家记录数据")
                null
            }
        } catch (e: Exception) {
            Log.e(TAG, "解析本地玩家记录数据失败: ${e.message}", e)
            null
        }
    }

    /**
     * 强制刷新玩家记录数据
     */
    suspend fun forceRefreshPlayerRecordData(context: Context) {
        Log.d(TAG, "强制刷新玩家记录数据")
        val today: LocalDate = Clock.System.todayIn(TimeZone.currentSystemDefault())
        val todayDateString: String = today.toString()

        getActualPlayerRecordLogic(context)
        DSUtils.storeData(context, LAST_PLAYER_RECORD_UPDATE_DATE, todayDateString)
    }

    /**
     * 清除本地玩家记录数据
     */
    suspend fun clearLocalPlayerRecordData(context: Context) {
        Log.d(TAG, "清除本地玩家记录数据")
        DSUtils.removeData(context, PLAYER_RECORD_DATA)
        DSUtils.removeData(context, LAST_PLAYER_RECORD_UPDATE_DATE)
    }

    /**
     * 清除所有本地数据（歌曲数据 + 玩家记录数据）
     */
    suspend fun clearAllLocalData(context: Context) {
        Log.d(TAG, "清除所有本地数据")
        clearLocalSongData(context)
        clearLocalPlayerRecordData(context)
    }

    suspend fun mapSongsToEntities(songs: List<Song>): Pair<List<SongEntity>,List<ChartEntity>>{
        val songEntities = mutableListOf<SongEntity>()
        val chartEntities = mutableListOf<ChartEntity>()

        // 难度映射关系，索引对应难度
        val difficultyMapping = listOf(
            DifficultyType.BASIC,
            DifficultyType.ADVANCED,
            DifficultyType.EXPERT,
            DifficultyType.MASTER,
            DifficultyType.REMASTER
        )

        songs.forEach { song ->
            // 1. 映射 SongEntity
            val songEntity = SongEntity(
                id = song.id,
                title = song.title,
                artist = song.basicInfo.artist,
                genre = song.basicInfo.genre,
                bpm = song.basicInfo.bpm,
                from = song.basicInfo.from,
                type = song.type,
                isNew = song.basicInfo.isNew,
                // 根据标题判断是否为协谱
                buddy = if (song.title.startsWith("[協]")) "協" else null
            )
            songEntities.add(songEntity)

            // 2. 映射 ChartEntity
            if (song.basicInfo.genre == "宴会場") {
                // 特殊处理 "宴会場"
                val utageDifficulty = if (song.charts.size >= 2) DifficultyType.UTAGE2P else DifficultyType.UTAGE
                song.charts.forEachIndexed { index, chart ->
                    val notes = chart.notes
                    // 按照 tap, hold, slide, touch, break 顺序解析
                    val notesTap = notes.getOrElse(0) { 0 }
                    val notesHold = notes.getOrElse(1) { 0 }
                    val notesSlide = notes.getOrElse(2) { 0 }
                    val notesTouch = notes.getOrElse(3) { 0 }
                    val notesBreak = notes.getOrElse(4) { 0 }

                    val chartEntity = ChartEntity(
                        songId = song.id,
                        difficulty = utageDifficulty,
                        type = song.type,
                        ds = song.ds.getOrElse(index) { 0.0 },
                        oldDs = null,
                        level = song.level.getOrElse(index) { "" },
                        charter = chart.charter,
                        notesTap = notesTap,
                        notesHold = notesHold,
                        notesSlide = notesSlide,
                        notesTouch = notesTouch,
                        notesBreak = notesBreak,
                        notesTotal = notes.sum()
                    )
                    chartEntities.add(chartEntity)
                }
            } else {
                // 标准歌曲处理
                song.charts.forEachIndexed { index, chart ->
                    if (index < difficultyMapping.size) {
                        val notes = chart.notes
                        // 按照 tap, hold, slide, touch, break 顺序解析
                        val notesTap = notes.getOrElse(0) { 0 }
                        val notesHold = notes.getOrElse(1) { 0 }
                        val notesSlide = notes.getOrElse(2) { 0 }
                        val notesTouch = notes.getOrElse(3) { 0 }
                        val notesBreak = notes.getOrElse(4) { 0 }

                        val chartEntity = ChartEntity(
                            songId = song.id,
                            difficulty = difficultyMapping[index],
                            type = song.type,
                            ds = song.ds.getOrElse(index) { 0.0 },
                            oldDs = null,
                            level = song.level.getOrElse(index) { "" },
                            charter = chart.charter,
                            notesTap = notesTap,
                            notesHold = notesHold,
                            notesSlide = notesSlide,
                            notesTouch = notesTouch,
                            notesBreak = notesBreak,
                            notesTotal = notes.sum()
                        )
                        chartEntities.add(chartEntity)
                    }
                }
            }
        }
        return Pair(songEntities,chartEntities)
    }
    fun mapSongWithChartsEntitiesToSongs(songWithChartsEntities: List<SongWithChartsEntity>): List<Song> {
        return songWithChartsEntities.map { songWithChartsEntity ->
            mapSongWithChartsEntityToSong(songWithChartsEntity)
        }
    }

    /**
     * 将单个 SongWithChartsEntity 对象转换回 Song (原始数据模型)
     */
    fun mapSongWithChartsEntityToSong(songWithChartsEntity: SongWithChartsEntity): Song {
        val songEntity = songWithChartsEntity.song
        val chartEntities = songWithChartsEntity.charts

        // 1. 创建 BasicInfo from SongEntity
        val basicInfo = BasicInfo(
            title = songEntity.title,
            artist = songEntity.artist,
            genre = songEntity.genre,
            bpm = songEntity.bpm,
            from = songEntity.from,
            isNew = songEntity.isNew,
            // 注意: BasicInfo 原本可能还有 releaseDate 或其他字段，
            // 如果 SongEntity 中没有，这里会缺失或需要默认值。
            // title 在 Song 级别，不在 BasicInfo 中。
            releaseDate = "" // 假设 releaseDate 在 SongEntity 中没有，提供默认值
        )

        // 2. 将 List<ChartEntity> 转换回 List<Chart> (model)
        // 并从中提取 ds 和 level 列表
        // 注意：原始 Song.charts 的顺序很重要，它与 ds 和 level 列表的索引对应。
        // 我们需要根据 ChartEntity.difficulty 来尝试恢复这个顺序。

        // 定义标准难度顺序以便排序
        val difficultyOrder = mapOf(
            DifficultyType.BASIC to 0,
            DifficultyType.ADVANCED to 1,
            DifficultyType.EXPERT to 2,
            DifficultyType.MASTER to 3,
            DifficultyType.REMASTER to 4,
            DifficultyType.UTAGE to 5, // 假设宴会場谱面放在最后或有特定顺序
            DifficultyType.UTAGE2P to 6
        )

        // 根据难度排序 ChartEntities 来尝试恢复原始顺序
        val sortedChartEntities = chartEntities.sortedBy { difficultyOrder[it.difficulty] ?: Int.MAX_VALUE }

        val chartsModelList = mutableListOf<Chart>()
        val dsList = mutableListOf<Double>()
        val levelList = mutableListOf<String>()
        // 假设 cids 在实体中没有直接对应，如果需要，要找到恢复它的方法
        val cidsList = mutableListOf<Int>() // 默认为空，因为实体中没有直接来源

        sortedChartEntities.forEach { chartEntity ->
            val notesArray = intArrayOf(
                chartEntity.notesTap,
                chartEntity.notesHold,
                chartEntity.notesSlide,
                chartEntity.notesTouch,
                chartEntity.notesBreak
            )
            chartsModelList.add(
                Chart(
                    notes = notesArray.toList(), // 转换为 List<Int>
                    charter = chartEntity.charter
                )
            )
            dsList.add(chartEntity.ds)
            levelList.add(chartEntity.level)
            // cidsList.add(...) // 如果可以从 chartEntity 推断出 cid
        }

        // 3. 创建 Song 对象
        return Song(
            id = songEntity.id,
            title = songEntity.title,
            type = songEntity.type, // 来自 SongEntity
            ds = dsList,
            level = levelList,
            cids = cidsList, // 可能为空或需要其他逻辑恢复
            charts = chartsModelList,
            basicInfo = basicInfo
        )
    }

}