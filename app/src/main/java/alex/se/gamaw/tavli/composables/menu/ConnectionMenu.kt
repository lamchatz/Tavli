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
fun ConnectionMenu(selectOnline: () -> Unit,
                   selectLan: () -> Unit,
                   selectBluetooth: () -> Unit,
                   selectLocal: () -> Unit) {
    Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
        Button(onClick = selectOnline) {
            Text("Online")
        }

        Button(onClick = selectLan) {
            Text("LAN")
        }

        Button(onClick = selectBluetooth) {
            Text("Bluetooth")
        }

        Button(onClick = selectLocal) {
            Text("Local")
        }
    }
}