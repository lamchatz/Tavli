package alex.se.gamaw.tavli.gamemode

import alex.se.gamaw.tavli.data.Die
import alex.se.gamaw.tavli.data.Move
import alex.se.gamaw.tavli.data.MoveResult
import alex.se.gamaw.tavli.data.Piece
import androidx.compose.ui.graphics.Color

interface GameMode {
    fun initialBoard(): List<Piece>

    fun isLegalMove(boardState: Map<Int, List<Piece>>, from: Int, target: Int, color: Color, movePool: List<Int>): Boolean
    fun getLegalMoves(boardState: Map<Int, List<Piece>>, selectedPiece: Piece, movePool: List<Int>): Set<Int>
    fun hasLegalMove(boardState: Map<Int, List<Piece>>, color: Color, movePool: List<Int>): Boolean
    fun resolveMove(move: Move): MoveResult
}