package alex.se.gamaw.tavli.data

data class GameState(
    val piecesByPosition: Map<Int, List<Piece>> = emptyMap(),
    val selectedPoint: Int? = null,
    val allowedMoves: Set<Int> = emptySet(),
    val currentPlayer: Player? = null,
    val dice: List<Die> = listOf(Die(4), Die(3)),
    val movePool: List<Int> = emptyList(),
    val showRight: Boolean = false
)
