package alex.se.gamaw.tavli.data

import androidx.compose.ui.graphics.Color

data class Player(
    val name: String,
    val color: Color
) {
    companion object {
        // Safe, clear placeholder token
        val Uninitialized = Player( "Loading...", Color.White)
    }
}
