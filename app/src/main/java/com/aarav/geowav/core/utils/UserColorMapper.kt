package com.aarav.geowav.core.utils

import androidx.compose.ui.graphics.Color

val userColors = listOf(
    Color(0xFF1E88E5), // Blue
    Color(0xFF43A047), // Green
    Color(0xFFFB8C00), // Orange
    Color(0xFF8E24AA), // Purple
    Color(0xFF00897B), // Teal
    Color(0xFF5E35B1), // Deep Purple
    Color(0xFF6D4C41), // Brown
    Color(0xFF3949AB), // Indigo
    Color(0xFF00ACC1), // Cyan
    Color(0xFF7CB342)  // Light Green
)
object UserColorMapper {
    fun getUserColor(userId: String): Color {
        val index = kotlin.math.abs(userId.hashCode()) % userColors.size
        return userColors[index]
    }
}