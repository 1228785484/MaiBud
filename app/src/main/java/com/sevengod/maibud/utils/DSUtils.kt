package com.sevengod.maibud.utils

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

object DSUtils {
    // 创建 DataStore 扩展属性
    private val Context.dataStore by preferencesDataStore(name = "save")

    // 存储数据
    suspend fun storeData(context: Context, key: String, value: String) {
        val prefKey = stringPreferencesKey(key)
        context.dataStore.edit { preferences ->
            preferences[prefKey] = value
        }
    }

    // 获取数据
    suspend fun getData(context: Context, key: String): String? {
        val prefKey = stringPreferencesKey(key)
        return context.dataStore.data.map { preferences ->
            preferences[prefKey]
        }.first()
    }

    // 可选：删除特定键的数据
    suspend fun removeData(context: Context, key: String) {
        val prefKey = stringPreferencesKey(key)
        context.dataStore.edit { preferences ->
            preferences.remove(prefKey)
        }
    }

    // 可选：清除所有数据
    suspend fun clearAll(context: Context) {
        context.dataStore.edit { preferences ->
            preferences.clear()
        }
    }

    // 可选：检查键是否存在
    suspend fun containsKey(context: Context, key: String): Boolean {
        val prefKey = stringPreferencesKey(key)
        return context.dataStore.data.map { preferences ->
            preferences.contains(prefKey)
        }.first()
    }
}