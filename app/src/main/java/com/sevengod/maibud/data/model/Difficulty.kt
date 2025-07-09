package com.sevengod.maibud.data.model
import androidx.compose.ui.graphics.Color

enum class Difficulty(val index: Int, val displayName: String, val color: Color) {
    BASIC(0, "Basic", Color(0xFF34D399)),      // #34d399 绿色
    ADVANCED(1, "Advanced", Color(0xFFFBBF24)), // #fbbf24 黄色
    EXPERT(2, "Expert", Color(0xFFEF4444)),     // #ef4444 红色
    MASTER(3, "Master", Color(0xFF8B5CF6)),     // #8b5cf6 紫色
    REMASTER(4, "Re:MASTER", Color(0xFFBC6DE0))  // #ec4899 粉色
}