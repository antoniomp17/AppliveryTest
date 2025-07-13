package viewmodels.appsDetails

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import usecases.GetAppDetailsUseCase

class AppDetailsViewModel(
    private val getAppDetailsUseCase: GetAppDetailsUseCase
) : ViewModel() {
    
    private val _state = MutableStateFlow(AppDetailsState())
    val state: StateFlow<AppDetailsState> = _state.asStateFlow()
    
    private val _effects = MutableSharedFlow<AppDetailsEffect>()
    val effects: SharedFlow<AppDetailsEffect> = _effects.asSharedFlow()
    
    fun handleIntent(intent: AppDetailsIntent) {
        when (intent) {
            is AppDetailsIntent.LoadAppDetails -> loadAppDetails(intent.packageName)
            is AppDetailsIntent.ClearError -> clearError()
        }
    }
    
    private fun loadAppDetails(packageName: String) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null)
            
            try {
                val app = getAppDetailsUseCase(packageName)
                if (app != null) {
                    _state.value = _state.value.copy(
                        isLoading = false,
                        app = app,
                        error = null
                    )
                } else {
                    _state.value = _state.value.copy(
                        isLoading = false,
                        error = "Aplicación no encontrada"
                    )
                    _effects.emit(AppDetailsEffect.ShowError("Aplicación no encontrada"))
                }
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    isLoading = false,
                    error = e.message ?: "Error desconocido"
                )
                _effects.emit(AppDetailsEffect.ShowError(e.message ?: "Error al cargar detalles"))
            }
        }
    }
    
    private fun clearError() {
        _state.value = _state.value.copy(error = null)
    }
}