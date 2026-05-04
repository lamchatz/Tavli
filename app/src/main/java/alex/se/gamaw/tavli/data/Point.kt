package alex.se.gamaw.tavli.data

import androidx.compose.ui.graphics.Path

data class Point(
    val path: Path,
    val centerX: Float,
    val isTop: Boolean,
    val isLight: Boolean
)
