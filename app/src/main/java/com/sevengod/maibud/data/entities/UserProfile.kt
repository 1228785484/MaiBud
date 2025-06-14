package com.sevengod.maibud.data.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "user_profile",
    indices = [
    Index(value = ["username"], unique = true),
    Index(value = ["jwt_token"], unique = true)
]
)
data class UserProfile (
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "user_id")
    val userId: Int = 0, // 自增主键，默认值为0

    @ColumnInfo(name = "username")
    val username: String,

    @ColumnInfo(name = "jwt_token")
    val jwtToken: String, // 可以是 var 如果 token 需要更新

    @ColumnInfo(name = "nickname")
    val nickname: String? = null // 可空类型对应数据库中的 NULL
)