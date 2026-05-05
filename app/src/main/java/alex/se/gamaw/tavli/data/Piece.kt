package alex.se.gamaw.tavli.data

import androidx.compose.ui.graphics.Color


data class Piece(
    val id: Int,
    val position: Int, // 0–23
    val color: Color
)

