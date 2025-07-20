package viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import usecases.GetLocationInfoUseCase
import usecases.ObserveLocationInfoUseCase

class LocationInfoViewModel(
    private val getLocationInfoUseCase: GetLocationInfoUseCase,
    private val observeLocationInfoUseCase: ObserveLocationInfoUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(LocationInfoState())
    val state: StateFlow<LocationInfoState> = _state.asStateFlow()

    private val _effects = MutableSharedFlow<LocationInfoEffect>()
    val effects: SharedFlow<LocationInfoEffect> = _effects.asSharedFlow()

    init {
        checkPermissionsAndLoad()
    }

    fun handleIntent(intent: LocationInfoIntent) {
        when (intent) {
            is LocationInfoIntent.LoadLocationInfo -> loadLocationInfo()
            is LocationInfoIntent.ClearError -> clearError()
            is LocationInfoIntent.RetryLoading -> retryLoading()
            is LocationInfoIntent.RefreshLocationInfo -> refreshLocationInfo()
            is LocationInfoIntent.RequestLocationPermission -> requestLocationPermission()
            is LocationInfoIntent.OnPermissionResult -> onPermissionResult(intent.granted)
        }
    }

    private fun checkPermissionsAndLoad() {
        // This will be handled by the UI layer checking permissions
        // For now, just try to load location info
        handleIntent(LocationInfoIntent.LoadLocationInfo)
    }

    private fun requestLocationPermission() {
        viewModelScope.launch {
            _effects.emit(LocationInfoEffect.RequestLocationPermission)
        }
    }

    private fun onPermissionResult(granted: Boolean) {
        _state.value = _state.value.copy(hasLocationPermission = granted)

        if (granted) {
            handleIntent(LocationInfoIntent.LoadLocationInfo)
            observeLocationInfo()
        } else {
            _state.value = _state.value.copy(
                error = "Permisos de ubicación denegados"
            )
        }
    }

    fun updatePermissionState(hasPermission: Boolean, shouldShowRationale: Boolean = false) {
        _state.value = _state.value.copy(
            hasLocationPermission = hasPermission,
            shouldShowPermissionRationale = shouldShowRationale
        )

        if (hasPermission) {
            handleIntent(LocationInfoIntent.LoadLocationInfo)
            observeLocationInfo()
        }
    }

    private fun loadLocationInfo() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null)

            try {
                val locationInfo = getLocationInfoUseCase()
                _state.value = _state.value.copy(
                    isLoading = false,
                    locationInfo = locationInfo,
                    error = null,
                    lastUpdated = System.currentTimeMillis()
                )
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    isLoading = false,
                    error = e.message ?: "Error desconocido"
                )
                _effects.emit(
                    LocationInfoEffect.ShowError(
                        e.message ?: "Error al cargar información"
                    )
                )
            }
        }
    }

    private fun refreshLocationInfo() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isRefreshing = true, error = null)

            try {
                val locationInfo = getLocationInfoUseCase()
                _state.value = _state.value.copy(
                    isRefreshing = false,
                    locationInfo = locationInfo,
                    error = null,
                    lastUpdated = System.currentTimeMillis()
                )
                _effects.emit(LocationInfoEffect.ShowRefreshSuccess)
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    isRefreshing = false,
                    error = e.message ?: "Error desconocido"
                )
                _effects.emit(LocationInfoEffect.ShowError(e.message ?: "Error al actualizar información"))
            }
        }
    }

    private fun clearError() {
        _state.value = _state.value.copy(error = null)
    }

    private fun retryLoading() {
        handleIntent(LocationInfoIntent.LoadLocationInfo)
    }

    private fun observeLocationInfo() {
        observeLocationInfoUseCase()
            .onEach { locationInfo ->
                val currentState = _state.value
                // Only update if location actually changed to reduce UI recompositions
                if (currentState.locationInfo != locationInfo) {
                    _state.value = currentState.copy(
                        locationInfo = locationInfo,
                        lastUpdated = System.currentTimeMillis()
                    )
                }
            }
            .catch { e ->
                _effects.emit(LocationInfoEffect.ShowError("Error en monitoreo: ${e.message}"))
            }
            .launchIn(viewModelScope)
    }
}