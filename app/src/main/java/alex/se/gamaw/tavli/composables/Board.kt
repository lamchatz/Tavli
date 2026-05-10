package alex.se.gamaw.tavli.composables

import alex.se.gamaw.tavli.connection.LocalConnection
import alex.se.gamaw.tavli.data.BarIndex
import alex.se.gamaw.tavli.data.Board
import alex.se.gamaw.tavli.data.Piece
import alex.se.gamaw.tavli.data.calculateBoardLayout
import alex.se.gamaw.tavli.gamemode.PortesMode
import alex.se.gamaw.tavli.viewmodel.BoardViewModel
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
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel

val boardBrush = Brush.verticalGradient(
    0.0f to Color(0xFF5D3A1A),
    0.5f to Color(0xFF8B5A2B),
    1.0f to Color(0xFF5D3A1A)
)

val lightTriangle = Color(0xFFD9A066)
val darkTriangle = Color(0xFF5C2E1A)
val barColor = Color(0xFF4E2A17)

@Composable
fun GameScreen(boardViewModel: BoardViewModel = viewModel()) {

    boardViewModel.setGameMode(PortesMode())
    boardViewModel.setConnection(LocalConnection())
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(boardBrush),
        contentAlignment = Alignment.Center
    ) {
        Board(
            boardViewModel = boardViewModel
        )
    }
}


@Composable
fun Board(
    boardViewModel: BoardViewModel
) {
    val piecesByPosition = boardViewModel.piecesByPosition.collectAsStateWithLifecycle()
    val highlights = boardViewModel.allowedMoves.collectAsStateWithLifecycle()
    val selectedPoint = boardViewModel.selectedPoint.collectAsStateWithLifecycle()
    val dice = boardViewModel.dice.collectAsStateWithLifecycle()

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
                        boardViewModel.processInput(
                            findClickedIndex(
                                offset,
                                size = size,
                                layout,
                                piecesByPosition.value
                            )
                        )
                    }
                }
        ) {
            // 2. Draw Triangles
            layout.pointLayouts.forEach { (index, point) ->
                val color = if (point.isLight) lightTriangle else darkTriangle
                drawPath(path = point.path, color = color)

                if (highlights.value.contains(index)) {
                    drawPath(
                        path = point.path,
                        color = Color.Yellow.copy(alpha = 0.4f),
                        style = Fill
                    )
                    drawPath(
                        path = point.path,
                        color = Color.Yellow,
                        style = Stroke(width = 4.dp.toPx())
                    )
                }

                if (selectedPoint.value == index) {
                    drawPath(
                        path = point.path,
                        color = Color.Magenta.copy(alpha = 0.4f),
                        style = Fill
                    )
                    drawPath(
                        path = point.path,
                        color = Color.Magenta,
                        style = Stroke(width = 4.dp.toPx())
                    )
                }
            }
// 3. Draw Bar
            drawRect(
                color = barColor,
                topLeft = Offset(layout.sideWidth, 0f),
                size = Size(layout.barWidth, height)
            )

// 3b. Draw Bar Pieces
            val whiteBarPieces = piecesByPosition.value[BarIndex.WHITE.value] ?: emptyList()
            val blackBarPieces = piecesByPosition.value[BarIndex.BLACK.value] ?: emptyList()

            whiteBarPieces.forEachIndexed { index, piece ->
                drawCircle(
                    color = piece.color,
                    radius = layout.pieceRadius,
                    center = Offset(layout.barCenterX, layout.barWhiteY + (index * layout.spacing))
                )
            }

            blackBarPieces.forEachIndexed { index, piece ->
                drawCircle(
                    color = piece.color,
                    radius = layout.pieceRadius,
                    center = Offset(layout.barCenterX, layout.barBlackY - (index * layout.spacing))
                )
            }

            // 4. Draw Pieces (Using the pre-grouped map)
            piecesByPosition.value.forEach { (index, stack) ->
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

            val diceValues = dice.value
            if (diceValues.isNotEmpty()) {
                val startX =
                    layout.leftSideCenterX - (layout.diceWidth / 2f) + (layout.dieSize / 2f)

                diceValues.forEachIndexed { index, value ->
                    drawDie(
                        die = value,
                        center = Offset(
                            x = startX + index * (layout.dieSize + layout.dieSpacing.toPx()),
                            y = layout.centerY
                        ),
                        dieSize = layout.dieSize
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

    val whiteBarPieces = piecesByPosition[BarIndex.WHITE.value] ?: emptyList()
    whiteBarPieces.forEachIndexed { index, _ ->
        val pieceCenter = Offset(
            layout.barCenterX,
            layout.barWhiteY + (index * layout.spacing)
        )
        if ((offset - pieceCenter).getDistance() <= layout.pieceRadius) return BarIndex.WHITE.value
    }

    val blackBarPieces = piecesByPosition[BarIndex.BLACK.value] ?: emptyList()
    blackBarPieces.forEachIndexed { index, _ ->
        val pieceCenter = Offset(
            layout.barCenterX,
            layout.barBlackY - (index * layout.spacing)
        )
        if ((offset - pieceCenter).getDistance() <= layout.pieceRadius) return BarIndex.BLACK.value
    }

    piecesByPosition.forEach { (index, stack) ->
        if (index == BarIndex.WHITE.value || index == BarIndex.BLACK.value) return@forEach

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
        !isTopZone && !isBottomZone -> null
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