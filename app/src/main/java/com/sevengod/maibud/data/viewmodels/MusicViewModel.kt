package com.sevengod.maibud.data.viewmodels

import android.content.Context
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.map
import androidx.lifecycle.viewModelScope
import com.sevengod.maibud.data.model.Song
import com.sevengod.maibud.utils.DSUtils
import com.sevengod.maibud.utils.SongUtil
import com.sevengod.maibud.data.entities.RecordEntity
import com.sevengod.maibud.data.model.Record
import com.sevengod.maibud.repository.SongDataRepository
import com.sevengod.maibud.utils.DBUtils
import kotlinx.coroutines.launch

// 定义数据初始化状态
sealed interface DataInitState {
    object Idle : DataInitState
    object Loading : DataInitState
    object Success : DataInitState
    data class Error(val message: String) : DataInitState
}

class MusicViewModel(
    private val context: Context
) : ViewModel() {

    private val TAG = "MusicViewModel"

    var dataInitState by mutableStateOf<DataInitState>(DataInitState.Idle)
        private set
    //当前数据,会触发重组
    var localSongData by mutableStateOf<List<Song>?>(null)
        private set

    var localRecordData by mutableStateOf<List<Record>?>(null)
        private set

    var rating: Int by mutableIntStateOf(0)
        private set



    init {
        // ViewModel创建时自动初始化数据
        initializeData()
    }

    /**
     * 初始化应用数据
     */
    fun initializeData() {
        viewModelScope.launch {
            try {
                dataInitState = DataInitState.Loading
                Log.d(TAG, "开始初始化应用数据...")

                //额外测试操作,删除上一次的日期,现在是不需要了
//                DSUtils.removeData(context,"last_song_update_date")

                // 初始化歌曲数据
                SongUtil.getSongData(context)
                SongUtil.getPlayerRecordData(context)

                // 加载本地歌曲数据到内存中
                loadLocalSongData()
                loadLocalRecordData()


                dataInitState = DataInitState.Success
                Log.d(TAG, "应用数据初始化完成")

            } catch (e: Exception) {
                Log.e(TAG, "应用数据初始化失败", e)
                dataInitState = DataInitState.Error(e.message ?: "数据初始化失败")
            }
        }
    }

    /**
     * 加载本地歌曲数据
     */
    private suspend fun loadLocalSongData() {
        try {
            localSongData = SongUtil.getLocalSongData(context)
            Log.d(TAG, "本地歌曲数据加载完成: ${localSongData?.size ?: 0} 首")
        } catch (e: Exception) {
            Log.e(TAG, "加载本地歌曲数据失败", e)
        }
    }

    /**
     * 强制刷新数据
     */
    fun forceRefreshData() {
        viewModelScope.launch {
            try {
                dataInitState = DataInitState.Loading
                Log.d(TAG, "强制刷新数据...")

                // 强制刷新歌曲数据
                SongUtil.forceRefreshSongData(context)
                SongUtil.forceRefreshPlayerRecordData(context)

                // 重新加载本地数据
                loadLocalSongData()

                dataInitState = DataInitState.Success
                Log.d(TAG, "数据刷新完成")

            } catch (e: Exception) {
                Log.e(TAG, "数据刷新失败", e)
                dataInitState = DataInitState.Error(e.message ?: "数据刷新失败")
            }
        }
    }

    /**
     * 获取歌曲数据
     */
    fun getSongData(): List<Song>? {
        return localSongData
    }

    /**
     * 根据ID查找歌曲
     */
    fun findSongById(id: Int): Song? {
        return localSongData?.find { it.id == id }
    }

    /**
     * 根据标题搜索歌曲
     */
    fun searchSongsByTitle(title: String): List<Song> {
        return localSongData?.filter {
            it.title.contains(title, ignoreCase = true)
        } ?: emptyList()
    }

    /**
     * 清除数据初始化状态
     */
    fun goInitState() {
        dataInitState = DataInitState.Idle
    }

    private suspend fun loadLocalRecordData() {
        try {
            localRecordData = SongUtil.getLocalPlayerRecordData(context)?.records
            Log.d(TAG, "本地游玩记录数据加载完成: ${localSongData?.size ?: 0} 首")
        } catch (e: Exception) {
            Log.e(TAG, "加载本地游玩记录数据失败", e)
        }
    }

    /**
     * 同步本地数据到数据库
     */
    fun syncDataToDatabase() {
        viewModelScope.launch {
            try {
                Log.d(TAG, "开始同步数据到数据库...")
                val success = DBUtils.syncLocalDataToDatabase(context)
                if (success) {
                    Log.d(TAG, "数据同步成功")
                } else {
                    Log.e(TAG, "数据同步失败")
                }
            } catch (e: Exception) {
                Log.e(TAG, "同步数据时发生错误", e)
            }
        }
    }

    /**
     * 获取数据库中的记录数量
     */
    suspend fun getDatabaseRecordCount(): Int {
        return try {
            DBUtils.getRecordCount(context)
        } catch (e: Exception) {
            Log.e(TAG, "获取数据库记录数量失败", e)
            0
        }
    }

    /**
     * 根据歌曲ID获取记录
     */
    suspend fun getRecordsBySongId(songId: Int): List<RecordEntity> {
        return try {
            DBUtils.getRecordsBySongId(context, songId)
        } catch (e: Exception) {
            Log.e(TAG, "获取歌曲记录失败", e)
            emptyList()
        }
    }
    fun getUserRating(){
        viewModelScope.launch {
            rating = SongUtil.getLocalPlayerRecordData(context)?.rating ?: 0
        }
    }
    fun searchSongs(name: String? = null, minDs: Double? = null, maxDs: Double? = null) {
        viewModelScope.launch {
            try {
                val results = SongDataRepository.searchSongs(context,minDs, maxDs,name)
                localSongData = SongUtil.mapSongWithChartsEntitiesToSongs(results)
            } catch (e: Exception) {
                // 处理错误
            }
        }
    }
}

/**
 * MusicViewModel 工厂类
 */
class MusicViewModelFactory(
    private val context: Context
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MusicViewModel::class.java)) {
            return MusicViewModel(context) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}