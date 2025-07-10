package usecases

import entities.info.DeviceInfo
import repositories.DeviceInfoRepository

class GetDeviceInfoUseCase(
    private val deviceInfoRepository: DeviceInfoRepository
) {
    suspend operator fun invoke(): DeviceInfo {
        return deviceInfoRepository.getDeviceInfo()
    }
}