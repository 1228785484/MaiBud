package com.sevengod.maibud.data.entities

import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize

@Parcelize
@Entity(tableName = "song_data")
data class SongEntity(
    // 主键（歌曲id）
    @PrimaryKey
    val id: Int,

    // 标题
    val title: String,

    // 作曲家
    val artist: String,

    // 歌曲流派
    val genre: String,

    // bpm
    val bpm: Int,

    // 添加版本
    val from: String,

    // 标准 or DX
    val type: String,

    // 是否为新版本歌曲
    @ColumnInfo(name = "is_new")
    val isNew: Boolean,

    // 双人协谱标记
    val buddy: String?,
): Parcelable{

}
