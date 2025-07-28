package com.sevengod.maibud.repository

import android.content.Context
import android.util.Log
import com.sevengod.maibud.data.entities.RecordEntity
import com.sevengod.maibud.data.model.PlayerRecord
import com.sevengod.maibud.data.model.Record
import com.sevengod.maibud.instances.DataBaseInstance
import com.sevengod.maibud.utils.SongUtil

object RecordRepository {
    /**
     * 保存Record列表到数据库
     */
    private const val TAG = "RecordRepository"

    suspend fun saveRecordsToDB(context: Context, records: List<Record>) {
        try {
            val database = DataBaseInstance.getInstance(context)
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
            val database = DataBaseInstance.getInstance(context)
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
            val database = DataBaseInstance.getInstance(context)
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
            val database = DataBaseInstance.getInstance(context)
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
            val database = DataBaseInstance.getInstance(context)
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
}