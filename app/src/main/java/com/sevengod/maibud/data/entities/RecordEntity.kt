package com.sevengod.maibud.data.entities

import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName
import com.sevengod.maibud.R
import kotlinx.parcelize.Parcelize

@Entity(tableName = "record")
@Parcelize
data class RecordEntity(
    // 主键（自增长，默认值 0）
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    // 完成率
    @ColumnInfo(name = "achievements")
    val achievements: Double,

    // 定数
    @ColumnInfo(name = "ds")
    val ds: Double,

    // DX分数
    @ColumnInfo(name = "dx_score")
    val dxScore: Int,

    // 全连状态
    @ColumnInfo(name = "fc")
    val fc: String,

    // 同步状态
    @ColumnInfo(name = "fs")
    val fs: String,

    // 等级
    @ColumnInfo(name = "level")
    val level: String,

    // 等级索引
    @SerializedName("level_index")
    @ColumnInfo(name = "level_index")
    val levelIndex: Int,

    // 等级标签
    @SerializedName("level_label")
    @ColumnInfo(name = "level_label")
    val levelLabel: String,

    // Rating值
    @ColumnInfo(name = "ra")
    val ra: Int,

    // 评级
    @ColumnInfo(name = "rate")
    val rate: String,

    // 歌曲ID
    @SerializedName("song_id")
    @ColumnInfo(name = "song_id", index = true)
    val songId: Int,

    // 歌曲标题
    @ColumnInfo(name = "title")
    val title: String,

    // 标准 or DX
    @ColumnInfo(name = "type")
    val type: String
) : Parcelable {
    fun getFcIcon() = when (fc) {
        "fc" -> R.drawable.fc
        "fcp" -> R.drawable.fcp
        "ap" -> R.drawable.ap
        "app" -> R.drawable.app
        else -> R.drawable.blank
    }

    fun getFsIcon() = when (fs) {
        "fs" -> R.drawable.fs
        "fsp" -> R.drawable.fsp
        "fsd" -> R.drawable.fsd
        "fsdp" -> R.drawable.fsdp
        else -> R.drawable.blank
    }

    fun getRateIcon() = when (rate){
        "d" -> R.drawable.d
        "c" -> R.drawable.c
        "b" -> R.drawable.b
        "bb" -> R.drawable.bb
        "bbb" -> R.drawable.bbb
        "a" -> R.drawable.a
        "aa" -> R.drawable.aa
        "aaa" -> R.drawable.aaa
        "s" -> R.drawable.s
        "sp" -> R.drawable.sp
        "ss" -> R.drawable.ss
        "ssp" -> R.drawable.ssp
        "sss" -> R.drawable.sss
        "sssp" -> R.drawable.sssp
        else -> R.drawable.d
    }
}