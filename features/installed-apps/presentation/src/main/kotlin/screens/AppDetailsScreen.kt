package screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.amp.appliverytest.features.installedapps.presentation.R
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import components.AppDetailCard
import entities.InstalledApp
import org.koin.androidx.compose.koinViewModel
import viewmodels.appsDetails.AppDetailsEffect
import viewmodels.appsDetails.AppDetailsIntent
import viewmodels.appsDetails.AppDetailsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppDetailsScreen(
    packageName: String,
    onNavigateBack: () -> Unit,
    viewModel: AppDetailsViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    
    LaunchedEffect(packageName) {
        viewModel.handleIntent(AppDetailsIntent.LoadAppDetails(packageName))
    }
    
    LaunchedEffect(viewModel) {
        viewModel.effects.collect { effect ->
            when (effect) {
                is AppDetailsEffect.ShowError -> {
                    snackbarHostState.showSnackbar(effect.message)
                }
                is AppDetailsEffect.NavigateBack -> {
                    onNavigateBack()
                }
            }
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(state.app?.name ?: stringResource(R.string.app_details_title)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Volver"
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
                        onRetry = { 
                            viewModel.handleIntent(AppDetailsIntent.LoadAppDetails(packageName)) 
                        },
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                
                state.app != null -> {
                    AppDetailsContent(
                        app = state.app!!,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        }
    }
}

@Composable
private fun AppDetailsContent(
    app: InstalledApp,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        AppDetailCard(
            app = app,
            modifier = Modifier.fillMaxWidth()
        )
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