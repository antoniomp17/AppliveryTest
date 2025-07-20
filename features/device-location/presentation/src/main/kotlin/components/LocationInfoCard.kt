package components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import com.amp.appliverytest.features.devicelocation.presentation.R
import androidx.compose.ui.unit.dp
import entities.LocationInfo
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LocationInfoCard(
    locationInfo: LocationInfo,
    isRefreshing: Boolean,
    modifier: Modifier = Modifier
) {
    // Get string resources first (outside of remember blocks)
    val degreesFormat = stringResource(R.string.degrees_format)
    val metersFormat = stringResource(R.string.meters_format)
    val speedFormat = stringResource(R.string.speed_format)
    val bearingFormat = stringResource(R.string.bearing_format)
    
    // Memoize expensive formatting operations to reduce main thread work
    val formattedLatitude = remember(locationInfo.latitude, degreesFormat) { 
        String.format(Locale.getDefault(), degreesFormat, locationInfo.latitude) 
    }
    val formattedLongitude = remember(locationInfo.longitude, degreesFormat) { 
        String.format(Locale.getDefault(), degreesFormat, locationInfo.longitude) 
    }
    val formattedAltitude = remember(locationInfo.altitude, metersFormat) { 
        String.format(Locale.getDefault(), metersFormat, locationInfo.altitude) 
    }
    val formattedAccuracy = remember(locationInfo.accuracy, metersFormat) { 
        String.format(Locale.getDefault(), metersFormat, locationInfo.accuracy) 
    }
    val formattedSpeed = remember(locationInfo.speed, speedFormat) { 
        String.format(Locale.getDefault(), speedFormat, locationInfo.speed) 
    }
    val formattedBearing = remember(locationInfo.bearing, bearingFormat) { 
        String.format(Locale.getDefault(), bearingFormat, locationInfo.bearing) 
    }
    val formattedTimestamp = remember(locationInfo.timestamp) { 
        formatTimestamp(locationInfo.timestamp) 
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = stringResource(R.string.location_icon_description),
                        tint = if (isLocationAvailable(locationInfo)) Color.Green else Color.Red
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = stringResource(R.string.location_info_title),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
                if (isRefreshing) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        strokeWidth = 2.dp
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            if (isLocationAvailable(locationInfo)) {
                InfoRow(label = stringResource(R.string.latitude_label), value = formattedLatitude)
                InfoRow(label = stringResource(R.string.longitude_label), value = formattedLongitude)
                InfoRow(label = stringResource(R.string.altitude_label), value = formattedAltitude)
                InfoRow(label = stringResource(R.string.accuracy_label), value = formattedAccuracy)
                
                if (locationInfo.speed > 0) {
                    InfoRow(label = stringResource(R.string.speed_label), value = formattedSpeed)
                }
                
                if (locationInfo.bearing > 0) {
                    InfoRow(label = stringResource(R.string.bearing_label), value = formattedBearing)
                }
                
                InfoRow(
                    label = stringResource(R.string.provider_label), 
                    value = locationInfo.provider.ifEmpty { stringResource(R.string.unknown_provider) }
                )
                
                if (locationInfo.timestamp > 0) {
                    InfoRow(
                        label = stringResource(R.string.last_update_label), 
                        value = formattedTimestamp
                    )
                }
            } else {
                Text(
                    text = stringResource(R.string.location_not_available),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun InfoRow(
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
            fontWeight = FontWeight.Medium
        )
    }
}

private fun isLocationAvailable(locationInfo: LocationInfo): Boolean {
    return locationInfo.latitude != 0.0 && locationInfo.longitude != 0.0
}

private fun formatTimestamp(timestamp: Long): String {
    val formatter = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
    return formatter.format(Date(timestamp))
}
