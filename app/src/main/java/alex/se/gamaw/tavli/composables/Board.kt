package alex.se.gamaw.tavli.composables

import alex.se.gamaw.tavli.data.Board
import alex.se.gamaw.tavli.data.Piece
import alex.se.gamaw.tavli.data.calculateBoardLayout
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp

@Composable
fun Board(
    pieces: List<Piece>,
    onPieceClick: (Int) -> Unit = {}
) {
    val boardColor = Color(0xFF8B5A2B)
    val lightTriangle = Color(0xFFD9A066)
    val darkTriangle = Color(0xFF5C2E1A)
    val barColor = Color(0xFF4E2A17)

    val piecesByPosition = remember(pieces) {
        pieces.groupBy { it.position }
    }

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxHeight()
            .padding(16.dp)
            .background(boardColor)
    ) {
        val width = constraints.maxWidth.toFloat()
        val height = constraints.maxHeight.toFloat()

        // 1. Calculate Layout Constants
        val layout = remember(width, height) {
            calculateBoardLayout(width, height)
        }

        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(width, height) {
                    detectTapGestures { offset ->
                        val clickedIndex =
                            findClickedIndex(offset, size = size, layout, piecesByPosition)
                        clickedIndex?.let { onPieceClick(it) }
                    }
                }
        ) {
            // 2. Draw Triangles
            layout.pointLayouts.forEach { (index, point) ->
                val color = if (point.isLight) lightTriangle else darkTriangle
                drawPath(path = point.path, color = color)
            }

            // 3. Draw Bar
            drawRect(
                color = barColor,
                topLeft = Offset(layout.sideWidth, 0f),
                size = Size(layout.barWidth, height)
            )

            // 4. Draw Pieces (Using the pre-grouped map)
            piecesByPosition.forEach { (index, stack) ->
                val point = layout.pointLayouts[index] ?: return@forEach

                stack.forEachIndexed { stackIndex, piece ->
                    val y = if (point.isTop) {
                        layout.pieceRadius + layout.spacing * stackIndex
                    } else {
                        height - layout.pieceRadius - layout.spacing * stackIndex
                    }

                    drawCircle(
                        color = piece.color,
                        radius = layout.pieceRadius,
                        center = Offset(point.centerX, y)
                    )
                }
            }
        }
    }
}


fun findClickedIndex(
    offset: Offset,
    size: IntSize,
    layout: Board,
    piecesByPosition: Map<Int, List<Piece>>
): Int? {
    val x = offset.x
    val y = offset.y
    val isTop = y < size.height / 2f

    piecesByPosition.forEach { (index, stack) ->
        val point = layout.pointLayouts[index] ?: return@forEach
        stack.forEachIndexed { stackIndex, _ ->
            val pieceY = if (point.isTop) {
                layout.pieceRadius + layout.spacing * stackIndex
            } else {
                size.height - layout.pieceRadius - layout.spacing * stackIndex
            }

            val center = Offset(point.centerX, pieceY)
            if ((offset - center).getDistance() <= layout.pieceRadius) return index
        }
    }

    val barWidth = size.width * 0.05f
    val sideWidth = (size.width - barWidth) / 2f
    val pointWidth = sideWidth / 6f

    return when {
        // Left side of the board
        x < sideWidth -> {
            val column = (x / pointWidth).toInt().coerceIn(0, 5)
            val reversed = 5 - column
            if (isTop) 6 + reversed else 17 - reversed
        }
        // Right side of the board
        x > sideWidth + barWidth -> {
            val column = ((x - sideWidth - barWidth) / pointWidth).toInt().coerceIn(0, 5)
            val reversed = 5 - column
            if (isTop) reversed else 23 - reversed
        }
        // Tapped the bar
        else -> null
    }
}