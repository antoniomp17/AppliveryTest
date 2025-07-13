package components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material3.*
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import entities.InstalledApp
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppDetailCard(
    app: InstalledApp,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(24.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                AppIcon(
                    iconBytes = app.icon,
                    modifier = Modifier.size(64.dp)
                )
                
                Spacer(modifier = Modifier.width(16.dp))
                
                Column {
                    Text(
                        text = app.name,
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    
                    Text(
                        text = app.packageName,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))

            HorizontalDivider(Modifier, DividerDefaults.Thickness, DividerDefaults.color)

            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = "Información de la Aplicación",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            DetailRow(label = "Versión", value = app.version)
            DetailRow(label = "Código de versión", value = app.versionCode.toString())
            DetailRow(label = "Tamaño", value = formatBytes(app.size))
            DetailRow(
                label = "Instalado", 
                value = formatDate(app.installTime)
            )
            DetailRow(
                label = "Última actualización", 
                value = formatDate(app.updateTime)
            )
        }
    }
}

@Composable
private fun DetailRow(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

private fun formatDate(timestamp: Long): String {
    val date = Date(timestamp)
    val formatter = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
    return formatter.format(date)
}

private fun formatBytes(bytes: Long): String {
    return when {
        bytes >= 1_000_000_000 -> "${(bytes / 1_000_000_000.0).format(1)} GB"
        bytes >= 1_000_000 -> "${(bytes / 1_000_000.0).format(1)} MB"
        bytes >= 1_000 -> "${(bytes / 1_000.0).format(1)} KB"
        else -> "$bytes B"
    }
}

private fun Double.format(digits: Int) = "%.${digits}f".format(this)