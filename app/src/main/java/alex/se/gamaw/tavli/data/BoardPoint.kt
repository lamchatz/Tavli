package alex.se.gamaw.tavli.data

data class BoardPoint(
    val index: Int, // 0 to 23
    val checkerCount: Int,
    val isWhite: Boolean,
    val isTopRow: Boolean
)