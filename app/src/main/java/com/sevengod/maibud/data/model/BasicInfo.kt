package com.sevengod.maibud.data.model

import com.google.gson.annotations.SerializedName

data class BasicInfo(
    val title: String,
    val artist: String,
    val genre: String,
    val bpm: Int,
    @SerializedName("release_date")
    val releaseDate: String,
    val from: String,
    @SerializedName("is_new")
    val isNew: Boolean
)
