package alex.se.gamaw.tavli.gamemode

import alex.se.gamaw.tavli.data.Piece
import androidx.compose.ui.graphics.Color

interface GameMode {
    fun initialBoard(): List<Piece>
    fun getLegalMoves(selectedPiece: Piece, boardState: Map<Int, List<Piece>>): Set<Int>
    fun hasLegalMove(boardState: Map<Int, List<Piece>>, color: Color, dices: List<Int>): Boolean
}