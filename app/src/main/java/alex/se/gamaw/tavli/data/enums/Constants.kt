package alex.se.gamaw.tavli.data.enums

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

const val TOTAL_PIECES = 15

val HOME_BASES = mapOf( Color.White to (5 downTo 0), Color.Black to 18..23)
val boardBrush = Brush.verticalGradient(
    0.0f to Color(0xFF5D3A1A),
    0.5f to Color(0xFF8B5A2B),
    1.0f to Color(0xFF5D3A1A)
)

val lightTriangle = Color(0xFFD9A066)
val darkTriangle = Color(0xFF5C2E1A)
val barColor = Color(0xFF4E2A17)
val offTrayColor = Color(0xFF2E1C16) // Slightly darker tray background
