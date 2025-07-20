package screens

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.amp.appliverytest.features.devicelocation.presentation.R
import components.LocationInfoCard
import components.LocationMapCard
import components.LocationPermissionCard
import entities.LocationInfo
import org.koin.androidx.compose.koinViewModel
import viewmodels.LocationInfoEffect
import viewmodels.LocationInfoIntent
import viewmodels.LocationInfoViewModel
import java.text.SimpleDateFormat
import java.util.*
import androidx.core.net.toUri

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LocationInfoScreen(
    onNavigateBack: (() -> Unit)? = null,
    viewModel: LocationInfoViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current

    val locationUpdatedMessage = stringResource(R.string.location_updated)
    
    // Permission launcher
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val fineLocationGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] ?: false
        val coarseLocationGranted = permissions[Manifest.permission.ACCESS_COARSE_LOCATION] ?: false
        val granted = fineLocationGranted || coarseLocationGranted
        
        viewModel.handleIntent(LocationInfoIntent.OnPermissionResult(granted))
    }
    
    // Check permissions on composition
    LaunchedEffect(Unit) {
        val fineLocationPermission = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        
        val coarseLocationPermission = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        
        val hasPermission = fineLocationPermission || coarseLocationPermission
        viewModel.updatePermissionState(hasPermission)
    }
    
    LaunchedEffect(viewModel) {
        viewModel.effects.collect { effect ->
            when (effect) {
                is LocationInfoEffect.ShowError -> {
                    snackbarHostState.showSnackbar(effect.message)
                }
                is LocationInfoEffect.ShowRefreshSuccess -> {
                    snackbarHostState.showSnackbar(locationUpdatedMessage)
                }
                is LocationInfoEffect.ShowToast -> {
                    snackbarHostState.showSnackbar(effect.message)
                }
                is LocationInfoEffect.RequestLocationPermission -> {
                    permissionLauncher.launch(
                        arrayOf(
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION
                        )
                    )
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.location_info_title)) },
                navigationIcon = {
                    onNavigateBack?.let { callback ->
                        IconButton(onClick = callback) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = stringResource(R.string.back)
                            )
                        }
                    }
                },
                actions = {
                    if (state.hasLocationPermission) {
                        IconButton(
                            onClick = { 
                                viewModel.handleIntent(LocationInfoIntent.RefreshLocationInfo) 
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = stringResource(R.string.refresh)
                            )
                        }
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
                !state.hasLocationPermission -> {
                    LocationPermissionCard(
                        onRequestPermission = {
                            viewModel.handleIntent(LocationInfoIntent.RequestLocationPermission)
                        },
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(16.dp)
                    )
                }
                
                state.isLoading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                
                state.error != null -> {
                    ErrorState(
                        error = state.error!!,
                        onRetry = { 
                            viewModel.handleIntent(LocationInfoIntent.RetryLoading) 
                        },
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                
                state.locationInfo != null -> {
                    LocationInfoContent(
                        locationInfo = state.locationInfo!!,
                        isRefreshing = state.isRefreshing,
                        lastUpdated = state.lastUpdated,
                        onOpenMap = { locationInfo ->
                            openLocationInMaps(context, locationInfo)
                        },
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        }
    }
}

@Composable
private fun LocationInfoContent(
    locationInfo: LocationInfo,
    isRefreshing: Boolean,
    lastUpdated: Long,
    onOpenMap: (LocationInfo) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            LocationInfoCard(
                locationInfo = locationInfo,
                isRefreshing = isRefreshing
            )
        }
        
        item {
            LocationMapCard(
                locationInfo = locationInfo,
                onOpenMap = { onOpenMap(locationInfo) }
            )
        }
        
        if (lastUpdated > 0) {
            item {
                Text(
                    text = stringResource(R.string.last_updated, formatTimestamp(lastUpdated)),
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
            Text(stringResource(R.string.retry))
        }
    }
}

private fun openLocationInMaps(context: android.content.Context, locationInfo: LocationInfo) {
    if (locationInfo.latitude != 0.0 || locationInfo.longitude != 0.0) {
        val uri =
            "geo:${locationInfo.latitude},${locationInfo.longitude}?q=${locationInfo.latitude},${locationInfo.longitude}".toUri()
        val intent = Intent(Intent.ACTION_VIEW, uri)
        intent.setPackage("com.google.android.apps.maps")
        
        if (intent.resolveActivity(context.packageManager) != null) {
            context.startActivity(intent)
        } else {
            // Fallback to web browser
            val webUri =
                "https://maps.google.com/?q=${locationInfo.latitude},${locationInfo.longitude}".toUri()
            val webIntent = Intent(Intent.ACTION_VIEW, webUri)
            context.startActivity(webIntent)
        }
    }
}

private fun formatTimestamp(timestamp: Long): String {
    val formatter = SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.US)
    return formatter.format(Date(timestamp))
}
