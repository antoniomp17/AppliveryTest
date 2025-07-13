package components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import android.graphics.BitmapFactory
import entities.InstalledApp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppListItem(
    app: InstalledApp,
    onClick: () -> Unit,
    isRefreshing: Boolean = false,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // App Icon
            AppIcon(
                iconBytes = app.icon,
                modifier = Modifier.size(48.dp)
            )
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = app.name,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                
                Text(
                    text = app.packageName,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                Text(
                    text = "v${app.version} • ${formatBytes(app.size)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            if (isRefreshing) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    strokeWidth = 2.dp
                )
            }
        }
    }
}

@Composable
fun AppIcon(
    iconBytes: ByteArray?,
    modifier: Modifier = Modifier
) {
    if (iconBytes != null) {
        val bitmap = BitmapFactory.decodeByteArray(iconBytes, 0, iconBytes.size)
        if (bitmap != null) {
            Image(
                bitmap = bitmap.asImageBitmap(),
                contentDescription = "App Icon",
                modifier = modifier.clip(RoundedCornerShape(8.dp))
            )
            return
        }
    }
    
    // Default icon placeholder
    Surface(
        modifier = modifier.clip(RoundedCornerShape(8.dp)),
        color = MaterialTheme.colorScheme.surfaceVariant
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.fillMaxSize()
        ) {
            Text(
                text = "APP",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
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