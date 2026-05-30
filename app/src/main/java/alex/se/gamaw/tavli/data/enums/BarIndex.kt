package alex.se.gamaw.tavli.data.enums

import androidx.compose.ui.graphics.Color

enum class BarIndex(val value: Int) {
    WHITE(25), BLACK(-2);

    companion object {
        fun get(color: Color): Int {
            return if (color == Color.White) WHITE.value else BLACK.value
        }
    }
}