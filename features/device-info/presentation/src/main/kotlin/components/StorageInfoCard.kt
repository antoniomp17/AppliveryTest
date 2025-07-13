package components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material3.*
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import entities.info.StorageInfo

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StorageInfoCard(
    storageInfo: StorageInfo,
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
                    imageVector = Icons.Default.Storage,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                
                Spacer(modifier = Modifier.width(8.dp))
                
                Text(
                    text = "Almacenamiento",
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
            
            LinearProgressIndicator(
                progress = { storageInfo.usedPercentage / 100f },
                modifier = Modifier.fillMaxWidth(),
                color = ProgressIndicatorDefaults.linearColor,
                trackColor = ProgressIndicatorDefaults.linearTrackColor,
                strokeCap = ProgressIndicatorDefaults.LinearStrokeCap
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            InfoRow(label = "Total", value = formatBytes(storageInfo.totalSpace))
            InfoRow(label = "Usado", value = formatBytes(storageInfo.usedSpace))
            InfoRow(label = "Libre", value = formatBytes(storageInfo.freeSpace))
            InfoRow(label = "Porcentaje usado", value = "${storageInfo.usedPercentage.toInt()}%")
        }
    }
}

private fun formatBytes(bytes: Long): String {
    return when {
        bytes >= 1_000_000_000 -> "${(bytes / 1_000_000_000.0).format(1)} GB"
        bytes >= 1_000_000 -> "${(bytes / 1_000_000.0).format(1)} MB"
        else -> "${(bytes / 1_000.0).format(1)} KB"
    }
}

private fun Double.format(digits: Int) = "%.${digits}f".format(this)