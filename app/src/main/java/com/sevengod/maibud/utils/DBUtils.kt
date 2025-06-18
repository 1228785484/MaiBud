package com.sevengod.maibud.utils

import android.content.Context
import android.util.Log
import androidx.room.Room
import com.sevengod.maibud.data.entities.RecordEntity
import com.sevengod.maibud.data.model.Record
import com.sevengod.maibud.data.model.PlayerRecord
import com.sevengod.maibud.instances.DataBaseInstance

object DBUtils {
    @Volatile
    private var INSTANCE: DataBaseInstance? = null
    
    private const val DATABASE_NAME = "maibud_app_database"
    private const val TAG = "DBUtils"

    /**
     * 获取数据库实例（单例模式）
     */
    fun getDatabase(context: Context): DataBaseInstance {
        return INSTANCE ?: synchronized(this) {
            val instance = Room.databaseBuilder(
                context.applicationContext,
                DataBaseInstance::class.java,
                DATABASE_NAME
            )
            .fallbackToDestructiveMigration() // 简单处理：在结构变化时重建数据库
            .build()
            INSTANCE = instance
            Log.d(TAG, "数据库实例创建成功")
            instance
        }
    }

    /**
     * 保存Record列表到数据库
     */
    suspend fun saveRecordsToDB(context: Context, records: List<Record>) {
        try {
            val database = getDatabase(context)
            val recordDao = database.recordDao()
            
            // 转换Record为RecordEntity
            val recordEntities = records.map { record ->
                RecordEntity(
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
            }
            
            // 批量插入到数据库
            recordDao.insertRecords(recordEntities)
            Log.d(TAG, "成功保存 ${recordEntities.size} 条记录到数据库")
            
        } catch (e: Exception) {
            Log.e(TAG, "保存记录到数据库失败", e)
            throw e
        }
    }

    /**
     * 保存PlayerRecord到数据库
     */
    suspend fun savePlayerRecordToDB(context: Context, playerRecord: PlayerRecord) {
        try {
            saveRecordsToDB(context, playerRecord.records)
            Log.d(TAG, "成功保存玩家记录到数据库: ${playerRecord.nickname}")
        } catch (e: Exception) {
            Log.e(TAG, "保存玩家记录到数据库失败", e)
            throw e
        }
    }

    /**
     * 从本地DSUtils获取PlayerRecord数据并保存到数据库
     */
    suspend fun saveLocalPlayerRecordToDB(context: Context) {
        try {
            val playerRecord = SongUtil.getLocalPlayerRecordData(context)
            if (playerRecord != null) {
                savePlayerRecordToDB(context, playerRecord)
                Log.d(TAG, "从本地数据成功保存 ${playerRecord.records.size} 条记录到数据库")
            } else {
                Log.w(TAG, "本地没有玩家记录数据")
            }
        } catch (e: Exception) {
            Log.e(TAG, "从本地保存玩家记录到数据库失败", e)
            throw e
        }
    }

    /**
     * 清空所有记录数据
     */
    suspend fun clearAllRecords(context: Context) {
        try {
            val database = getDatabase(context)
            val recordDao = database.recordDao()
            recordDao.deleteAllRecords()
            Log.d(TAG, "所有记录数据已清空")
        } catch (e: Exception) {
            Log.e(TAG, "清空记录数据失败", e)
            throw e
        }
    }

    /**
     * 获取数据库中的记录数量
     */
    suspend fun getRecordCount(context: Context): Int {
        return try {
            val database = getDatabase(context)
            val recordDao = database.recordDao()
            val count = recordDao.getRecordCount()
            Log.d(TAG, "数据库中共有 $count 条记录")
            count
        } catch (e: Exception) {
            Log.e(TAG, "获取记录数量失败", e)
            0
        }
    }

    /**
     * 根据歌曲ID获取记录
     */
    suspend fun getRecordsBySongId(context: Context, songId: Int): List<RecordEntity> {
        return try {
            val database = getDatabase(context)
            val recordDao = database.recordDao()
            val records = recordDao.getRecordsBySongIdSync(songId)
            Log.d(TAG, "获取歌曲ID $songId 的 ${records.size} 条记录")
            records
        } catch (e: Exception) {
            Log.e(TAG, "获取歌曲记录失败", e)
            emptyList()
        }
    }

    /**
     * 获取所有记录（同步方式，适用于一次性获取）
     */
    suspend fun getAllRecordsSync(context: Context): List<RecordEntity> {
        return try {
            val database = getDatabase(context)
            val recordDao = database.recordDao()
            val records = recordDao.getAllRecordsSync()
            Log.d(TAG, "同步获取所有记录，共 ${records.size} 条")
            records
        } catch (e: Exception) {
            Log.e(TAG, "同步获取所有记录失败", e)
            emptyList()
        }
    }

    /**
     * 强制从本地DSUtils重新同步到数据库
     */
    suspend fun syncLocalDataToDatabase(context: Context): Boolean {
        return try {
            Log.d(TAG, "开始同步本地数据到数据库...")
            
            // 清空现有数据
            clearAllRecords(context)
            
            // 重新保存
            saveLocalPlayerRecordToDB(context)
            
            val count = getRecordCount(context)
            Log.d(TAG, "同步完成，数据库中现有 $count 条记录")
            true
        } catch (e: Exception) {
            Log.e(TAG, "同步本地数据到数据库失败", e)
            false
        }
    }

    /**
     * 关闭数据库连接
     */
    fun closeDatabase() {
        INSTANCE?.let { database ->
            try {
                if (database.isOpen) {
                    database.close()
                    Log.d(TAG, "数据库连接已关闭")
                }
                INSTANCE = null
            } catch (e: Exception) {
                Log.e(TAG, "关闭数据库时发生错误", e)
            }
        }
    }
    
    /**
     * 检查数据库是否已初始化且可用
     */
    fun isDatabaseInitialized(): Boolean {
        return INSTANCE?.isOpen == true
    }
    
    /**
     * 获取数据库版本信息
     */
    fun getDatabaseVersion(context: Context): Int {
        return try {
            val database = getDatabase(context)
            database.openHelper.readableDatabase.version
        } catch (e: Exception) {
            Log.e(TAG, "获取数据库版本失败", e)
            -1
        }
    }
    
    /**
     * 获取数据库路径
     */
    fun getDatabasePath(context: Context): String {
        return context.getDatabasePath(DATABASE_NAME).absolutePath
    }
    
    /**
     * 清空所有数据表（谨慎使用）
     */
    suspend fun clearAllTables(context: Context) {
        try {
            val database = getDatabase(context)
            database.clearAllTables()
            Log.d(TAG, "所有数据表已清空")
        } catch (e: Exception) {
            Log.e(TAG, "清空数据表时发生错误", e)
            throw e
        }
    }
    
    /**
     * 检查数据库健康状态
     */
    fun checkDatabaseHealth(context: Context): Boolean {
        return try {
            val database = getDatabase(context)
            // 尝试执行一个简单的查询来检查数据库是否正常工作
            database.query("SELECT COUNT(*) FROM sqlite_master", null)
            true
        } catch (e: Exception) {
            Log.e(TAG, "数据库健康检查失败", e)
            false
        }
    }
}