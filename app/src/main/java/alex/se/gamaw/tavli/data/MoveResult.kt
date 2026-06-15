package alex.se.gamaw.tavli.data

data class MoveResult(val boardState: Map<Int, List<Piece>>, val moves: List<Int>) {
    companion object {
        fun noMove(boardState: Map<Int, List<Piece>>): MoveResult = MoveResult(boardState, emptyList())
        fun noMove(move: Move): MoveResult = MoveResult(move.boardState, emptyList())
    }
}