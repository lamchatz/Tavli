package alex.se.gamaw.tavli.composables

import alex.se.gamaw.tavli.data.Piece
import alex.se.gamaw.tavli.data.PieceColor
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.unit.dp



@Composable
fun TavliScreen() {
    val points = 24

    var pieces by remember {
        mutableStateOf(
            List(30) { index ->
                Piece(
                    id = index,
                    color = if (index < 15) PieceColor.WHITE else PieceColor.BLACK,
                    pointIndex = index % points
                )
            }
        )
    }

    var selectedPieceId by remember { mutableStateOf<Int?>(null) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF6D3F1F)),
        contentAlignment = Alignment.Center
    ) {
        Board(
            pieces = pieces,
            selectedPieceId = selectedPieceId,
            onPieceClick = { selectedPieceId = it },
            onPieceMove = { pieceId, newPoint ->
                pieces = pieces.map {
                    if (it.id == pieceId) it.copy(pointIndex = newPoint)
                    else it
                }
                selectedPieceId = null
            }
        )
    }
}



@Composable
fun Board(
    pieces: List<Piece>,
    selectedPieceId: Int?,
    onPieceClick: (Int) -> Unit,
    onPieceMove: (Int, Int) -> Unit
) {
    val boardColor = Color(0xFF8B5A2B)
    val lightTriangle = Color(0xFFD9A066)
    val darkTriangle = Color(0xFF5C2E1A)

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxHeight(0.75f)
            .padding(16.dp)
            .background(boardColor)
    ) {
        val width = constraints.maxWidth.toFloat()
        val height = constraints.maxHeight.toFloat()

        val pointWidth = width / 12f
        val pointHeight = height / 2f * 0.7f

        // ---------------------------
        // DRAW TRIANGLES
        // ---------------------------

        Canvas(modifier = Modifier.fillMaxSize()) {

            val barWidth = width * 0.05f
            val sideWidth = (width - barWidth) / 2f
            val pointWidth = sideWidth / 6f

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
                drawPath(
                    path = Path().apply {
                        moveTo(x, 0f)
                        lineTo(x + pointWidth, 0f)
                        lineTo(x + pointWidth / 2f, pointHeight)
                        close()
                    },
                    color = if (isLight) lightTriangle else darkTriangle
                )

                // BOTTOM
                drawPath(
                    path = Path().apply {
                        moveTo(x, height)
                        lineTo(x + pointWidth, height)
                        lineTo(x + pointWidth / 2f, height - pointHeight)
                        close()
                    },
                    color = if (!isLight) lightTriangle else darkTriangle
                )
            }

            drawRect(
                color = Color(0xFF4E2A17),
                topLeft = Offset(sideWidth, 0f),
                size = androidx.compose.ui.geometry.Size(barWidth, height)
            )
        }

//
//        // ---------------------------
//        // HIGHLIGHTS (for now: ALL points)
//        // ---------------------------
//        if (selectedPieceId != null) {
//            Canvas(modifier = Modifier.fillMaxSize()) {
//                for (i in 0 until 24) {
//                    val col = i % 12
//                    val rowTop = i < 12
//
//                    val x = col * pointWidth
//                    val y = if (rowTop) 0f else height / 2f
//
//                    drawRect(
//                        color = Color.Yellow.copy(alpha = 0.2f),
//                        topLeft = Offset(x, y),
//                        size = androidx.compose.ui.geometry.Size(pointWidth, height / 2f)
//                    )
//                }
//            }
//        }
//
//        // ---------------------------
//        // PIECES
//        // ---------------------------
//        pieces.forEach { piece ->
//            PieceView(
//                piece = piece,
//                boardWidth = width,
//                boardHeight = height,
//                onClick = { onPieceClick(piece.id) },
//                onDrop = { newPoint -> onPieceMove(piece.id, newPoint) }
//            )
//        }
    }
}
