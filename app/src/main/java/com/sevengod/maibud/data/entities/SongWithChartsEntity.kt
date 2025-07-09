package com.sevengod.maibud.data.entities

import android.os.Parcelable
import androidx.room.Embedded
import androidx.room.Relation
import kotlinx.parcelize.Parcelize

@Parcelize
class SongWithChartsEntity (
    @Embedded
    val song: SongEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "song_id"
    )
    val charts:List<ChartEntity>

): Parcelable

