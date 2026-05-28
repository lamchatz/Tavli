package alex.se.gamaw.tavli.composables.menu

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

@Composable
fun GameModeMenu(selectPortes: () -> Unit,
                 selectPlakoto: () -> Unit,
                 selectFevga: () -> Unit) {
    Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
        Button(onClick = selectPortes) {
            Text("Πόρτες")
        }

        Button(onClick = selectPlakoto) {
            Text("Πλακωτό")
        }

        Button(onClick = selectFevga) {
            Text("Φεύγα")
        }
    }
}