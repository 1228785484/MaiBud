package com.sevengod.maibud.data.model

import com.google.gson.annotations.SerializedName

data class Record(
    val achievements: Double,
    val ds: Double,
    val dxScore: Int,
    val fc: String,
    val fs: String,
    val level: String,

    @SerializedName("level_index")
    val levelIndex: Int,

    @SerializedName("level_label")
    val levelLabel: String,

    val ra: Int,
    val rate: String,

    @SerializedName("song_id")
    val songId: Int,

    val title: String,
    val type: String
)
