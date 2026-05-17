package alex.se.gamaw.tavli.gamemode

import alex.se.gamaw.tavli.data.Die
import alex.se.gamaw.tavli.data.Piece
import androidx.compose.ui.graphics.Color

interface GameMode {
    fun initialBoard(): List<Piece>
    fun getLegalMoves(boardState: Map<Int, List<Piece>>, selectedPiece: Piece, movePool: List<Int>): Set<Int>
    fun hasLegalMove(boardState: Map<Int, List<Piece>>, color: Color, movePool: List<Int>): Boolean

    fun resolveMove(boardState: Map<Int, List<Piece>>, from: Int, to: Int, movePool: List<Int>): Map<Int, List<Piece>>
}