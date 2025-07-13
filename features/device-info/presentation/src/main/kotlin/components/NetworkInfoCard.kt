package components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import entities.info.NetworkInfo

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NetworkInfoCard(
    networkInfo: NetworkInfo,
    isRefreshing: Boolean,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Wifi,
                    contentDescription = null,
                    tint = if (networkInfo.isConnected) Color.Green else Color.Red
                )
                
                Spacer(modifier = Modifier.width(8.dp))
                
                Text(
                    text = "Red",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                
                if (isRefreshing) {
                    Spacer(modifier = Modifier.width(8.dp))
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        strokeWidth = 2.dp
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            InfoRow(label = "Estado", value = if (networkInfo.isConnected) "Conectado" else "Desconectado")
            InfoRow(label = "Tipo", value = networkInfo.connectionType.name)
            
            networkInfo.signalStrength?.let { strength ->
                InfoRow(label = "Señal", value = "$strength%")
            }
        }
    }
}