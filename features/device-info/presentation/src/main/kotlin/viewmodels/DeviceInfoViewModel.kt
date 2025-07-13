package viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import entities.MonitoringEvent
import entities.info.BatteryInfo
import entities.info.NetworkInfo
import entities.info.StorageInfo
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
import usecases.GetDeviceInfoUseCase
import usecases.ObserveMonitoringEventsUseCase

class DeviceInfoViewModel(
    private val getDeviceInfoUseCase: GetDeviceInfoUseCase,
    private val observeMonitoringEventsUseCase: ObserveMonitoringEventsUseCase
) : ViewModel() {
    
    private val _state = MutableStateFlow(DeviceInfoState())
    val state: StateFlow<DeviceInfoState> = _state.asStateFlow()
    
    private val _effects = MutableSharedFlow<DeviceInfoEffect>()
    val effects: SharedFlow<DeviceInfoEffect> = _effects.asSharedFlow()
    
    init {
        handleIntent(DeviceInfoIntent.LoadDeviceInfo)
        observeMonitoringEvents()
    }
    
    fun handleIntent(intent: DeviceInfoIntent) {
        when (intent) {
            is DeviceInfoIntent.LoadDeviceInfo -> loadDeviceInfo()
            is DeviceInfoIntent.RefreshDeviceInfo -> refreshDeviceInfo()
            is DeviceInfoIntent.ClearError -> clearError()
            is DeviceInfoIntent.RetryLoading -> retryLoading()
        }
    }
    
    private fun loadDeviceInfo() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null)
            
            try {
                val deviceInfo = getDeviceInfoUseCase()
                _state.value = _state.value.copy(
                    isLoading = false,
                    deviceInfo = deviceInfo,
                    error = null,
                    lastUpdated = System.currentTimeMillis()
                )
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    isLoading = false,
                    error = e.message ?: "Error desconocido"
                )
                _effects.emit(DeviceInfoEffect.ShowError(e.message ?: "Error al cargar información"))
            }
        }
    }
    
    private fun refreshDeviceInfo() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isRefreshing = true, error = null)
            
            try {
                val deviceInfo = getDeviceInfoUseCase()
                _state.value = _state.value.copy(
                    isRefreshing = false,
                    deviceInfo = deviceInfo,
                    error = null,
                    lastUpdated = System.currentTimeMillis()
                )
                _effects.emit(DeviceInfoEffect.ShowRefreshSuccess)
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    isRefreshing = false,
                    error = e.message ?: "Error desconocido"
                )
                _effects.emit(DeviceInfoEffect.ShowError(e.message ?: "Error al actualizar información"))
            }
        }
    }
    
    private fun retryLoading() {
        handleIntent(DeviceInfoIntent.LoadDeviceInfo)
    }
    
    private fun clearError() {
        _state.value = _state.value.copy(error = null)
    }
    
    private fun observeMonitoringEvents() {
        observeMonitoringEventsUseCase()
            .onEach { event ->
                when (event) {
                    is MonitoringEvent.BatteryChanged -> updateBatteryInfo(event.batteryInfo)
                    is MonitoringEvent.NetworkChanged -> updateNetworkInfo(event.networkInfo)
                    is MonitoringEvent.StorageChanged -> updateStorageInfo(event.storageInfo)
                }
            }
            .catch { e ->
                _effects.emit(DeviceInfoEffect.ShowError("Error en monitoreo: ${e.message}"))
            }
            .launchIn(viewModelScope)
    }
    
    private fun updateBatteryInfo(batteryInfo: BatteryInfo) {
        val currentDeviceInfo = _state.value.deviceInfo ?: return
        val updatedDeviceInfo = currentDeviceInfo.copy(batteryInfo = batteryInfo)
        _state.value = _state.value.copy(
            deviceInfo = updatedDeviceInfo,
            lastUpdated = System.currentTimeMillis()
        )
    }
    
    private fun updateNetworkInfo(networkInfo: NetworkInfo) {
        val currentDeviceInfo = _state.value.deviceInfo ?: return
        val updatedDeviceInfo = currentDeviceInfo.copy(networkInfo = networkInfo)
        _state.value = _state.value.copy(
            deviceInfo = updatedDeviceInfo,
            lastUpdated = System.currentTimeMillis()
        )
    }
    
    private fun updateStorageInfo(storageInfo: StorageInfo) {
        val currentDeviceInfo = _state.value.deviceInfo ?: return
        val updatedDeviceInfo = currentDeviceInfo.copy(availableStorage = storageInfo)
        _state.value = _state.value.copy(
            deviceInfo = updatedDeviceInfo,
            lastUpdated = System.currentTimeMillis()
        )
    }
}