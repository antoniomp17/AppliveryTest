package usecases

import com.google.common.truth.Truth.assertThat
import entities.info.BatteryHealth
import entities.info.BatteryInfo
import entities.info.ConnectionType
import entities.info.DeviceInfo
import entities.info.NetworkInfo
import entities.info.StorageInfo
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import org.koin.test.KoinTest
import org.koin.test.inject
import repositories.DeviceInfoRepository

class GetDeviceInfoUseCaseTest : KoinTest {
    
    private val mockRepository: DeviceInfoRepository = mockk()
    private val getDeviceInfoUseCase: GetDeviceInfoUseCase by inject()
    
    @Before
    fun setup() {
        startKoin {
            modules(module {
                single<DeviceInfoRepository> { mockRepository }
                single { GetDeviceInfoUseCase(get()) }
            })
        }
    }
    
    @After
    fun tearDown() {
        stopKoin()
    }
    
    @Test
    fun `should return device info when repository call is successful`() = runTest {
        val expectedDeviceInfo = DeviceInfo(
            deviceModel = "Pixel 7",
            manufacturer = "Google",
            osVersion = "Android 14",
            availableStorage = StorageInfo(
                totalSpace = 128_000_000_000L,
                freeSpace = 64_000_000_000L,
                usedSpace = 64_000_000_000L
            ),
            batteryInfo = BatteryInfo(
                level = 85,
                isCharging = true,
                temperature = 25.5f,
                health = BatteryHealth.GOOD
            ),
            networkInfo = NetworkInfo(
                connectionType = ConnectionType.WIFI,
                isConnected = true,
                signalStrength = 90
            )
        )
        
        coEvery { mockRepository.getDeviceInfo() } returns expectedDeviceInfo

        val result = getDeviceInfoUseCase()

        assertThat(result).isEqualTo(expectedDeviceInfo)
        coVerify(exactly = 1) { mockRepository.getDeviceInfo() }
    }
    
    @Test
    fun `should propagate exception when repository throws error`() = runTest {
        val exception = RuntimeException("Network error")
        coEvery { mockRepository.getDeviceInfo() } throws exception

        try {
            getDeviceInfoUseCase()
            assertThat(false).isTrue()
        } catch (e: Exception) {
            assertThat(e).isEqualTo(exception)
        }
        
        coVerify(exactly = 1) { mockRepository.getDeviceInfo() }
    }
}