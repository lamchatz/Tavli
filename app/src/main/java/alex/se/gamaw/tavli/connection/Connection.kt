package alex.se.gamaw.tavli.connection

import alex.se.gamaw.tavli.data.Die

interface Connection {
    fun getDice(): List<Die>
}