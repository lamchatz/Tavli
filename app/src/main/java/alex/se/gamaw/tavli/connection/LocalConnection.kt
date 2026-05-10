package alex.se.gamaw.tavli.connection

import alex.se.gamaw.tavli.data.Die
import java.security.SecureRandom

class LocalConnection : Connection {
    val random = SecureRandom()
    override fun getDice(): List<Die> {
        return listOf(Die(random.nextInt(6) + 1), Die(random.nextInt(6) + 1))
    }
}