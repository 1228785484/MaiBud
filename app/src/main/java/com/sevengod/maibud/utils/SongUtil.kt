package com.sevengod.maibud.utils

import android.content.Context
import android.util.Log
import androidx.datastore.preferences.core.stringPreferencesKey
import com.google.gson.Gson
import com.sevengod.maibud.data.model.Song
import com.sevengod.maibud.data.model.PlayerRecord
import com.sevengod.maibud.repository.SongDataRepository
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn

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
                DBUtils.savePlayerRecordToDB(context, playerRecords)
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
}