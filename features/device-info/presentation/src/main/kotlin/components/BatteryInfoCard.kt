package components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Battery5Bar
import androidx.compose.material3.*
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import entities.info.BatteryInfo

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BatteryInfoCard(
    batteryInfo: BatteryInfo,
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
                    imageVector = Icons.Default.Battery5Bar,
                    contentDescription = null,
                    tint = getBatteryColor(batteryInfo.level)
                )
                
                Spacer(modifier = Modifier.width(8.dp))
                
                Text(
                    text = "Batería",
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
            
            // Battery Level Progress
            LinearProgressIndicator(
                progress = { batteryInfo.level / 100f },
                modifier = Modifier.fillMaxWidth(),
                color = getBatteryColor(batteryInfo.level),
                trackColor = ProgressIndicatorDefaults.linearTrackColor,
                strokeCap = ProgressIndicatorDefaults.LinearStrokeCap,
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            InfoRow(label = "Nivel", value = "${batteryInfo.level}%")
            InfoRow(label = "Estado", value = if (batteryInfo.isCharging) "Cargando" else "Desconectado")
            InfoRow(label = "Temperatura", value = "${batteryInfo.temperature}°C")
            InfoRow(label = "Salud", value = batteryInfo.health.name)
        }
    }
}

private fun getBatteryColor(level: Int): Color {
    return when {
        level > 50 -> Color.Green
        level > 20 -> Color(0xFFFFA500) // Orange
        else -> Color.Red
    }
}