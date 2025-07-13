package viewmodels.installedApps

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import entities.InstalledApp
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import usecases.GetInstalledAppsUseCase

class InstalledAppsViewModel(
    private val getInstalledAppsUseCase: GetInstalledAppsUseCase
) : ViewModel() {
    
    private val _state = MutableStateFlow(InstalledAppsState())
    val state: StateFlow<InstalledAppsState> = _state.asStateFlow()
    
    private val _effects = MutableSharedFlow<InstalledAppsEffect>()
    val effects: SharedFlow<InstalledAppsEffect> = _effects.asSharedFlow()
    
    init {
        handleIntent(InstalledAppsIntent.LoadApps)
    }
    
    fun handleIntent(intent: InstalledAppsIntent) {
        when (intent) {
            is InstalledAppsIntent.LoadApps -> loadApps()
            is InstalledAppsIntent.RefreshApps -> refreshApps()
            is InstalledAppsIntent.SearchApps -> searchApps(intent.query)
            is InstalledAppsIntent.SelectApp -> selectApp(intent.app)
            is InstalledAppsIntent.ClearSelection -> clearSelection()
            is InstalledAppsIntent.ClearError -> clearError()
            is InstalledAppsIntent.ClearSearch -> clearSearch()
        }
    }
    
    private fun loadApps() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null)
            
            try {
                val apps = getInstalledAppsUseCase()
                _state.value = _state.value.copy(
                    isLoading = false,
                    apps = apps,
                    filteredApps = apps,
                    totalAppsCount = apps.size,
                    error = null
                )
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    isLoading = false,
                    error = e.message ?: "Error desconocido"
                )
                _effects.emit(InstalledAppsEffect.ShowError(e.message ?: "Error al cargar aplicaciones"))
            }
        }
    }
    
    private fun refreshApps() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isRefreshing = true, error = null)
            
            try {
                val apps = getInstalledAppsUseCase()
                _state.value = _state.value.copy(
                    isRefreshing = false,
                    apps = apps,
                    filteredApps = filterApps(apps, _state.value.searchQuery),
                    totalAppsCount = apps.size,
                    error = null
                )
                _effects.emit(InstalledAppsEffect.ShowRefreshSuccess)
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    isRefreshing = false,
                    error = e.message ?: "Error desconocido"
                )
                _effects.emit(InstalledAppsEffect.ShowError(e.message ?: "Error al actualizar aplicaciones"))
            }
        }
    }
    
    private fun searchApps(query: String) {
        val currentState = _state.value
        val filteredApps = filterApps(currentState.apps, query)
        
        _state.value = currentState.copy(
            searchQuery = query,
            filteredApps = filteredApps
        )
    }
    
    private fun selectApp(app: InstalledApp) {
        viewModelScope.launch {
            _state.value = _state.value.copy(selectedApp = app)
            _effects.emit(InstalledAppsEffect.NavigateToAppDetails(app.packageName))
        }
    }
    
    private fun clearSelection() {
        _state.value = _state.value.copy(selectedApp = null)
    }
    
    private fun clearError() {
        _state.value = _state.value.copy(error = null)
    }
    
    private fun clearSearch() {
        val currentState = _state.value
        _state.value = currentState.copy(
            searchQuery = "",
            filteredApps = currentState.apps
        )
    }
    
    private fun filterApps(apps: List<InstalledApp>, query: String): List<InstalledApp> {
        return if (query.isBlank()) {
            apps
        } else {
            apps.filter { app ->
                app.name.contains(query, ignoreCase = true) ||
                app.packageName.contains(query, ignoreCase = true)
            }
        }
    }
}