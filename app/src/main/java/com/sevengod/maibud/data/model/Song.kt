package com.sevengod.maibud.data.model

import com.google.gson.annotations.SerializedName

data class Song(
    val id: Int,
    val title: String,
    val type: String,
    val ds: List<Double>,
    val level: List<String>,
    val cids: List<Int>,
    val charts: List<Chart>,
    @SerializedName("basic_info")
    val basicInfo: BasicInfo
)
