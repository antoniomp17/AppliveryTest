package usecases

import entities.MonitoringEvent
import kotlinx.coroutines.flow.Flow
import repositories.DeviceInfoRepository

class ObserveMonitoringEventsUseCase(
    private val deviceInfoRepository: DeviceInfoRepository
) {
    operator fun invoke(): Flow<MonitoringEvent> {
        return deviceInfoRepository.observeMonitoringEvents()
    }
}