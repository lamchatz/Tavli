package alex.se.gamaw.tavli.data

data class Move(
    val boardState: Map<Int, List<Piece>>,
    val from: Int,
    val to: Int,
    val movePool: List<Int>
) {
    constructor(moveResult: MoveResult, from: Int, to: Int): this(moveResult.boardState, from, to, moveResult.moves)
}
