package alex.se.gamaw.tavli.data.enums

import androidx.compose.ui.graphics.Color

enum class BarIndex(val value: Int) {
    WHITE(24), BLACK(-1);

    companion object {
        fun get(color: Color): Int {
            return if (color == Color.White) WHITE.value else BLACK.value
        }

        fun isBarIndexPoint(target: Int): Boolean = WHITE.value == target || BLACK.value == target
    }
}