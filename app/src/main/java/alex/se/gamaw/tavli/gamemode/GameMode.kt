package alex.se.gamaw.tavli.gamemode

import alex.se.gamaw.tavli.data.Die
import alex.se.gamaw.tavli.data.Piece
import androidx.compose.ui.graphics.Color

interface GameMode {
    fun initialBoard(): List<Piece>
    fun getLegalMoves(selectedPiece: Piece, boardState: Map<Int, List<Piece>>, dice: List<Die>): Set<Int>
    fun hasLegalMove(boardState: Map<Int, List<Piece>>, color: Color, dice: List<Die>): Boolean

    fun resolveMove(boardState: Map<Int, List<Piece>>, from: Int, to: Int, dice: List<Die>): Map<Int, List<Piece>>
}