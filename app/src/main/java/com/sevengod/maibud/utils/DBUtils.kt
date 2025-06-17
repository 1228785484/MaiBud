package com.sevengod.maibud.utils

import android.content.Context
import android.util.Log
import androidx.room.Room
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