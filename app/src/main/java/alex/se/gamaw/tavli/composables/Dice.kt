package alex.se.gamaw.tavli.composables

import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope


private val DieDots = mapOf(
    1 to floatArrayOf(0f, 0f),
    2 to floatArrayOf(-0.25f, -0.25f, 0.25f, 0.25f),
    3 to floatArrayOf(-0.25f, -0.25f, 0f, 0f, 0.25f, 0.25f),
    4 to floatArrayOf(-0.25f, -0.25f, 0.25f, -0.25f, -0.25f, 0.25f, 0.25f, 0.25f),
    5 to floatArrayOf(-0.25f, -0.25f, 0.25f, -0.25f, 0f, 0f, -0.25f, 0.25f, 0.25f, 0.25f),
    6 to floatArrayOf(-0.25f, -0.3f, 0.25f, -0.3f, -0.25f, 0f, 0.25f, 0f, -0.25f, 0.3f, 0.25f, 0.3f)
)


private val color = Color(0xFFF5F5F5)
fun DrawScope.drawDie(
    value: Int,
    center: Offset,
    dieSize: Float
) {
    val dotCoords = DieDots[value] ?: return
    val cornerRadius = dieSize * 0.15f
    val dotRadius = dieSize * 0.08f

    // Draw Die Background
    val halfSize = dieSize / 2
    val topLeft = Offset(center.x - halfSize, center.y - halfSize)
    drawRoundRect(
        color = color,
        topLeft = topLeft,
        size = Size(dieSize, dieSize),
        cornerRadius = CornerRadius(cornerRadius, cornerRadius)
    )

    // Draw Dots based on value
    for (i in dotCoords.indices step 2) {
        drawCircle(
            color = Color.Black,
            radius = dotRadius,
            center = Offset(
                center.x + dotCoords[i] * dieSize,
                center.y + dotCoords[i + 1] * dieSize
            )
        )
    }
}
