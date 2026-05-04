package alex.se.gamaw.tavli.composables

import alex.se.gamaw.tavli.data.Board
import alex.se.gamaw.tavli.data.Piece
import alex.se.gamaw.tavli.data.calculateBoardLayout
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp

val boardBrush = Brush.verticalGradient(
    0.0f to Color(0xFF5D3A1A),
    0.5f to Color(0xFF8B5A2B),
    1.0f to Color(0xFF5D3A1A)
)

val lightTriangle = Color(0xFFD9A066)
val darkTriangle = Color(0xFF5C2E1A)
val barColor = Color(0xFF4E2A17)


@Composable
fun GameScreen() {

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(boardBrush),
        contentAlignment = Alignment.Center
    ) {
        Board(
            listOf(
                Piece(0, Color.White),
                Piece(1, Color.Gray),
                Piece(2, Color.Green),
                Piece(2, Color.Green),
                Piece(3, Color.Red),
                Piece(4, Color.Blue),
                Piece(5, Color.Magenta),
                Piece(6, Color.DarkGray),
                Piece(11, Color.Black),
                Piece(12, Color.Red),
                Piece(18, Color.White),
            ), onPieceClick = { i ->
                println("lala")
                println(i)
            })
    }
}


@Composable
fun Board(
    pieces: List<Piece>,
    onPieceClick: (Int) -> Unit = {}
) {
    val piecesByPosition = remember(pieces) {
        pieces.groupBy { it.position }
    }

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxHeight(0.75f)
            .border(
                width = 8.dp,
                brush = Brush.verticalGradient(
                    colors = listOf(Color(0xFF5D4037), Color(0xFF3E2723))
                ),
                shape = RoundedCornerShape(4.dp)
            )
            .padding(8.dp)
            .shadow(16.dp, RoundedCornerShape(4.dp))
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


    val x = offset.x
    val y = offset.y
    val pointHeight = size.height * 0.35f
    val isTopZone = y < pointHeight
    val isBottomZone = y > (size.height - pointHeight)

    val barWidth = size.width * 0.05f
    val sideWidth = (size.width - barWidth) / 2f
    val pointWidth = sideWidth / 6f

    return when {
        !isTopZone && !isBottomZone -> null // Tapped the middle "dead zone"
        x < sideWidth -> {
            val column = (x / pointWidth).toInt().coerceIn(0, 5)
            val reversed = 5 - column
            if (isTopZone) 6 + reversed else 17 - reversed
        }

        x > sideWidth + barWidth -> {
            val column = ((x - sideWidth - barWidth) / pointWidth).toInt().coerceIn(0, 5)
            val reversed = 5 - column
            if (isTopZone) reversed else 23 - reversed
        }

        else -> null
    }
}