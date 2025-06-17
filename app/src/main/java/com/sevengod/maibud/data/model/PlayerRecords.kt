package com.sevengod.maibud.data.model

import com.google.gson.annotations.SerializedName

data class PlayerRecord(
    @SerializedName("additional_rating")
    val additionalRating: Int,

    val nickname: String,
    val plate: String,
    val rating: Int,

    val records: List<Record>,

    val username: String
)
