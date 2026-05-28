package alex.se.gamaw.tavli.data

data class TurnData(
    val dice: List<Die>,
    val player: Player,
    val movePool: List<Int>,
    val showRight: Boolean = false
)