package com.sevengod.maibud.data.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Query
import com.sevengod.maibud.data.entities.SongWithChartsEntity

@Dao
interface SongWithChartsDao {
    @Query(
        """
            SELECT * 
            FROM song_data 
            WHERE (
                -- 歌曲名字匹配（允许为空）
                (
                    :searchText IS NULL 
                    OR :searchText = '' 
                    OR title LIKE '%' || :searchText || '%'
                )
                -- 定数区间匹配（两个参数都允许为空，如果一个为空则只匹配另一个边界，如果都为空则不限制定数）
                AND (
                    (:minDs IS NULL AND :maxDs IS NULL) -- 如果最小和最大定数都为NULL，则不应用定数过滤
                    OR EXISTS (
                        SELECT 1
                        FROM chart 
                        WHERE 
                            chart.song_id = song_data.id 
                            AND (
                                (:minDs IS NULL OR chart.ds >= :minDs) -- 如果minDs为NULL，则忽略下限
                                AND
                                (:maxDs IS NULL OR chart.ds <= :maxDs) -- 如果maxDs为NULL，则忽略上限
                            )
                            -- 确保至少有一个边界被设置，否则如果 minDs 和 maxDs 都为 null，
                            -- 内部的 AND 条件会变成 (TRUE AND TRUE)，
                            -- 而我们已经通过外层的 (:minDs IS NULL AND :maxDs IS NULL) 处理了这种情况。
                            -- 如果需要更严格的“必须至少有一个chart在区间内”，
                            -- 并且 minDs/maxDs 至少一个非空，那么这里的逻辑可以保持。
                    )
                )
                -- 版本匹配（允许为空，如果为空则不限制版本）
                AND (
                    :version IS NULL 
                    OR :version = '' 
                    OR song_data.`from` = :version -- 假设 song_data 表中版本列名为 'version'
                )
            )
        """
    )
    fun searchSongsWithCharts(
        searchText: String? = null,
        minDs: Double? = null, // 新增:最小定数
        maxDs: Double? = null,  // 新增:最大定数
        version: String? = null //新增:版本
    ): List<SongWithChartsEntity>

}