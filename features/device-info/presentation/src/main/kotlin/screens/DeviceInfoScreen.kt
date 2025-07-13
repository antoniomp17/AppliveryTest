package screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import components.BatteryInfoCard
import components.DeviceBasicInfoCard
import components.NetworkInfoCard
import components.StorageInfoCard
import entities.info.DeviceInfo
import org.koin.androidx.compose.koinViewModel
import viewmodels.DeviceInfoEffect
import viewmodels.DeviceInfoIntent
import viewmodels.DeviceInfoViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeviceInfoScreen(
    onNavigateBack: (() -> Unit)? = null,
    viewModel: DeviceInfoViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(viewModel) {
        viewModel.effects.collect { effect ->
            when (effect) {
                is DeviceInfoEffect.ShowError -> {
                    snackbarHostState.showSnackbar(effect.message)
                }
                is DeviceInfoEffect.ShowRefreshSuccess -> {
                    snackbarHostState.showSnackbar("Información actualizada")
                }
                is DeviceInfoEffect.ShowToast -> {
                    snackbarHostState.showSnackbar(effect.message)
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Información del Dispositivo") },
                navigationIcon = {
                    onNavigateBack?.let { callback ->
                        IconButton(onClick = callback) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Volver"
                            )
                        }
                    }
                },
                actions = {
                    IconButton(
                        onClick = { viewModel.handleIntent(DeviceInfoIntent.RefreshDeviceInfo) }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Actualizar"
                        )
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when {
                state.isLoading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                
                state.error != null -> {
                    ErrorState(
                        error = state.error!!,
                        onRetry = { viewModel.handleIntent(DeviceInfoIntent.RetryLoading) },
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                
                state.deviceInfo != null -> {
                    DeviceInfoContent(
                        deviceInfo = state.deviceInfo!!,
                        isRefreshing = state.isRefreshing,
                        lastUpdated = state.lastUpdated,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        }
    }
}

@Composable
private fun DeviceInfoContent(
    deviceInfo: DeviceInfo,
    isRefreshing: Boolean,
    lastUpdated: Long,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            DeviceBasicInfoCard(
                deviceModel = deviceInfo.deviceModel,
                manufacturer = deviceInfo.manufacturer,
                osVersion = deviceInfo.osVersion
            )
        }
        
        item {
            BatteryInfoCard(
                batteryInfo = deviceInfo.batteryInfo,
                isRefreshing = isRefreshing
            )
        }
        
        item {
            NetworkInfoCard(
                networkInfo = deviceInfo.networkInfo,
                isRefreshing = isRefreshing
            )
        }
        
        item {
            StorageInfoCard(
                storageInfo = deviceInfo.availableStorage,
                isRefreshing = isRefreshing
            )
        }
        
        if (lastUpdated > 0) {
            item {
                Text(
                    text = "Última actualización: ${formatTimestamp(lastUpdated)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        }
    }
}

@Composable
private fun ErrorState(
    error: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = error,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.error
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Button(onClick = onRetry) {
            Text("Reintentar")
        }
    }
}

private fun formatTimestamp(timestamp: Long): String {
    return SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date(timestamp))
}