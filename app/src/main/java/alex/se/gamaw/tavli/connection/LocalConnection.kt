package alex.se.gamaw.tavli.connection

import kotlin.random.Random
import kotlin.random.nextInt

class LocalConnection : Connection {
    val random = Random(System.currentTimeMillis())
    override fun getDice(): List<Int> {
        return listOf(random.nextInt(1,6), random.nextInt(1,6))
    }
}