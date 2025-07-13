package screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import components.AppListItem
import components.AppSearchBar
import entities.InstalledApp
import org.koin.androidx.compose.koinViewModel
import viewmodels.installedApps.InstalledAppsEffect
import viewmodels.installedApps.InstalledAppsIntent
import viewmodels.installedApps.InstalledAppsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InstalledAppsScreen(
    onNavigateToAppDetails: (String) -> Unit,
    onNavigateBack: (() -> Unit)? = null,  // Parámetro añadido
    viewModel: InstalledAppsViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(viewModel) {
        viewModel.effects.collect { effect ->
            when (effect) {
                is InstalledAppsEffect.ShowError -> {
                    snackbarHostState.showSnackbar(effect.message)
                }
                is InstalledAppsEffect.ShowRefreshSuccess -> {
                    snackbarHostState.showSnackbar("Aplicaciones actualizadas")
                }
                is InstalledAppsEffect.NavigateToAppDetails -> {
                    onNavigateToAppDetails(effect.packageName)
                }
                is InstalledAppsEffect.ShowToast -> {
                    snackbarHostState.showSnackbar(effect.message)
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text("Aplicaciones Instaladas (${state.totalAppsCount})")
                },
                navigationIcon = {
                    // Mostrar botón de volver solo si se proporciona callback
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
                        onClick = { viewModel.handleIntent(InstalledAppsIntent.RefreshApps) }
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
                        onRetry = { viewModel.handleIntent(InstalledAppsIntent.LoadApps) },
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                
                else -> {
                    InstalledAppsContent(
                        apps = state.filteredApps,
                        searchQuery = state.searchQuery,
                        isRefreshing = state.isRefreshing,
                        onSearch = { query -> 
                            viewModel.handleIntent(InstalledAppsIntent.SearchApps(query)) 
                        },
                        onClearSearch = { 
                            viewModel.handleIntent(InstalledAppsIntent.ClearSearch) 
                        },
                        onAppClick = { app -> 
                            viewModel.handleIntent(InstalledAppsIntent.SelectApp(app)) 
                        },
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        }
    }
}

@Composable
private fun InstalledAppsContent(
    apps: List<InstalledApp>,
    searchQuery: String,
    isRefreshing: Boolean,
    onSearch: (String) -> Unit,
    onClearSearch: () -> Unit,
    onAppClick: (InstalledApp) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(16.dp)
    ) {
        AppSearchBar(
            query = searchQuery,
            onQueryChange = onSearch,
            onClearClick = onClearSearch,
            modifier = Modifier.fillMaxWidth()
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        if (apps.isEmpty()) {
            EmptyAppsState(
                searchQuery = searchQuery,
                modifier = Modifier.fillMaxWidth()
            )
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(
                    items = apps,
                    key = { it.packageName }
                ) { app ->
                    AppListItem(
                        app = app,
                        onClick = { onAppClick(app) },
                        isRefreshing = isRefreshing
                    )
                }
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

@Composable
private fun EmptyAppsState(
    searchQuery: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = if (searchQuery.isBlank()) {
                "No se encontraron aplicaciones instaladas"
            } else {
                "No hay aplicaciones que coincidan con \"$searchQuery\""
            },
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}