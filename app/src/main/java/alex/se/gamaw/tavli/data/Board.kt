package alex.se.gamaw.tavli.data

import androidx.compose.ui.graphics.Path

data class Board(
    val pointLayouts: Map<Int, Point>,
    val sideWidth: Float,
    val barWidth: Float,
    val pointWidth: Float,
    val pieceRadius: Float,
    val spacing: Float,
    val barCenterX: Float,
    val barWhiteY: Float,
    val barBlackY: Float
)


fun calculateBoardLayout(width: Float, height: Float): Board {
    val barWidth = width * 0.05f
    val sideWidth = (width - barWidth) / 2f
    val pointWidth = sideWidth / 6f
    val pointHeight = height * 0.35f
    val pieceRadius = pointWidth * 0.4f

    val layouts = mutableMapOf<Int, Point>()

    for (i in 0 until 12) {
        val isLeftSide = i < 6
        val localIndex = i % 6
        val x =
            if (isLeftSide) localIndex * pointWidth else sideWidth + barWidth + localIndex * pointWidth
        val centerX = x + pointWidth / 2f

        val topIndex = 11 - i
        layouts[topIndex] = Point(
            path = Path().apply {
                moveTo(x, 0f); lineTo(x + pointWidth, 0f); lineTo(centerX, pointHeight); close()
            },
            centerX = centerX, isTop = true, isLight = i % 2 == 0
        )

        val bottomIndex = 12 + i
        layouts[bottomIndex] = Point(
            path = Path().apply {
                moveTo(x, height); lineTo(x + pointWidth, height); lineTo(
                centerX,
                height - pointHeight
            ); close()
            },
            centerX = centerX, isTop = false, isLight = i % 2 != 0
        )
    }

    val barCenterX = sideWidth + (barWidth / 2f)
    val barWhiteY = height * 0.25f
    val barBlackY = height * 0.75f

    return Board(
        layouts, sideWidth, barWidth, pointWidth, pieceRadius, pieceRadius * 2.1f,
        barCenterX, barWhiteY, barBlackY
    )
}

