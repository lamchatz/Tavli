package alex.se.gamaw.tavli.composables

import alex.se.gamaw.tavli.data.Piece
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp

data class PointLayout(
    val path: Path,
    val centerX: Float,
    val isTop: Boolean
)


@Composable
fun Board(
    pieces: List<Piece>,
    onPieceClick: (Int) -> Unit = {}
) {
    val boardColor = Color(0xFF8B5A2B)
    val lightTriangle = Color(0xFFD9A066)
    val darkTriangle = Color(0xFF5C2E1A)

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxHeight(0.75f)
            .padding(top = 64.dp, start = 16.dp, end = 16.dp, bottom = 32.dp)
            .background(boardColor)
    ) {
        val width = constraints.maxWidth.toFloat()
        val height = constraints.maxHeight.toFloat()
        val map = mutableMapOf<Int, PointLayout>()


        val barWidth = width * 0.05f
        val sideWidth = (width - barWidth) / 2f
        val pointWidth = sideWidth / 6f
        val pointHeight = height / 2f * 0.7f

        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    detectTapGestures { offset ->

                        val pieceRadius = pointWidth * 0.4f
                        val spacing = pieceRadius * 2.1f

                        // FIRST: check if a piece was clicked
                        map.forEach { (index, layout) ->

                            val stack = pieces.filter { it.position == index }

                            stack.forEachIndexed { stackIndex, piece ->

                                val y = if (layout.isTop) {
                                    pieceRadius + spacing * stackIndex
                                } else {
                                    size.height - pieceRadius - spacing * stackIndex
                                }

                                val center = Offset(layout.centerX, y)

                                val distance = (offset - center).getDistance()

                                if (distance <= pieceRadius) {
                                    onPieceClick(index)
                                    return@detectTapGestures
                                }
                            }
                        }

                        // SECOND: fallback → click on column
                        val barWidth = size.width * 0.05f
                        val sideWidth = (size.width - barWidth) / 2f
                        val pointWidth = sideWidth / 6f

                        val x = offset.x
                        val y = offset.y

                        val isTop = y < size.height / 2f

                        val index = when {
                            x < sideWidth -> {
                                val local = (x / pointWidth).toInt()
                                val reversed = (5 - local).coerceIn(0, 5)
                                if (isTop) 6 + reversed else 17 - reversed
                            }

                            x > sideWidth + barWidth -> {
                                val local = ((x - sideWidth - barWidth) / pointWidth).toInt()
                                val reversed = (5 - local).coerceIn(0, 5)
                                if (isTop) reversed else 23 - reversed
                            }

                            else -> null
                        }

                        index?.let { onPieceClick(it) }
                    }
                }) {


            // DRAW TRIANGLES + STORE LAYOUT
            for (i in 0 until 12) {

                val isLeftSide = i < 6
                val localIndex = i % 6

                val x = if (isLeftSide) {
                    localIndex * pointWidth
                } else {
                    sideWidth + barWidth + localIndex * pointWidth
                }

                val isLight = i % 2 == 0

                // TOP
                val topPath = Path().apply {
                    moveTo(x, 0f)
                    lineTo(x + pointWidth, 0f)
                    lineTo(x + pointWidth / 2f, pointHeight)
                    close()
                }

                val topIndex = 11 - i
                map[topIndex] = PointLayout(
                    path = topPath,
                    centerX = x + pointWidth / 2f,
                    isTop = true
                )

                drawPath(
                    path = topPath,
                    color = if (isLight) lightTriangle else darkTriangle
                )


// BOTTOM
                val bottomIndex = 12 + i

                val bottomPath = Path().apply {
                    moveTo(x, height)
                    lineTo(x + pointWidth, height)
                    lineTo(x + pointWidth / 2f, height - pointHeight)
                    close()
                }

                map[bottomIndex] = PointLayout(
                    path = bottomPath,
                    centerX = x + pointWidth / 2f,
                    isTop = false
                )

                drawPath(
                    path = bottomPath,
                    color = if (!isLight) lightTriangle else darkTriangle
                )
            }

            // DRAW BAR
            drawRect(
                color = Color(0xFF4E2A17),
                topLeft = Offset(sideWidth, 0f),
                size = Size(barWidth, height)
            )

            // DRAW PIECES (STACKED)
            val pieceRadius = pointWidth * 0.4f
            val spacing = pieceRadius * 2.1f

            map.forEach { (index, layout) ->

                val stack = pieces.filter { it.position == index }

                stack.forEachIndexed { stackIndex, piece ->

                    val y = if (layout.isTop) {
                        // start from TOP (base) and go DOWN
                        pieceRadius + spacing * stackIndex
                    } else {
                        // start from BOTTOM (base) and go UP
                        height - pieceRadius - spacing * stackIndex
                    }

                    drawCircle(
                        color = piece.color,
                        radius = pieceRadius,
                        center = Offset(layout.centerX, y)
                    )
                }
            }
        }
    }
}