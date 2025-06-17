package com.sevengod.maibud.utils

import android.content.Context
import android.util.Log
import androidx.datastore.preferences.core.stringPreferencesKey
import com.google.gson.Gson
import com.sevengod.maibud.data.model.Song
import com.sevengod.maibud.repository.SongDataRepository
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn

object SongUtil {
    const val LAST_SONG_UPDATE_DATE = "last_song_update_date"
    const val SONG_DATA = "song_data"
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
}