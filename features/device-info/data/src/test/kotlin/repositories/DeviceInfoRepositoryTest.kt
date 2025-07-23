package repositories

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import datasources.DeviceInfoDataSource
import entities.MonitoringEvent
import entities.info.BatteryHealth
import entities.info.BatteryInfo
import entities.info.ConnectionType
import entities.info.NetworkInfo
import entities.info.StorageInfo
import exceptions.DeviceInfoException
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

class DeviceInfoRepositoryTest {
    
    private val deviceInfoDataSource: DeviceInfoDataSource = mockk()
    
    private lateinit var repository: DeviceInfoRepository
    
    @Before
    fun setup() {
        repository = DeviceInfoRepositoryImpl(
            deviceInfoDataSource
        )
    }
    
    @Test
    fun `should return complete device info when all datasources succeed`() = runTest {
        val deviceModel = "Pixel 7"
        val manufacturer = "Google"
        val osVersion = "Android 14"
        val batteryInfo = BatteryInfo(85, true, 25.0f, BatteryHealth.GOOD)
        val networkInfo = NetworkInfo(ConnectionType.WIFI, true, 90)
        val storageInfo = StorageInfo(128_000_000_000L, 64_000_000_000L, 64_000_000_000L)
        
        coEvery { deviceInfoDataSource.getDeviceModel() } returns deviceModel
        coEvery { deviceInfoDataSource.getManufacturer() } returns manufacturer
        coEvery { deviceInfoDataSource.getOsVersion() } returns osVersion
        coEvery { deviceInfoDataSource.getBatteryInfo() } returns batteryInfo
        coEvery { deviceInfoDataSource.getNetworkInfo() } returns networkInfo
        coEvery { deviceInfoDataSource.getStorageInfo() } returns storageInfo

        val result = repository.getDeviceInfo()

        assertThat(result.deviceModel).isEqualTo(deviceModel)
        assertThat(result.manufacturer).isEqualTo(manufacturer)
        assertThat(result.osVersion).isEqualTo(osVersion)
        assertThat(result.batteryInfo).isEqualTo(batteryInfo)
        assertThat(result.networkInfo).isEqualTo(networkInfo)
        assertThat(result.availableStorage).isEqualTo(storageInfo)
        
        coVerify(exactly = 1) { deviceInfoDataSource.getDeviceModel() }
        coVerify(exactly = 1) { deviceInfoDataSource.getManufacturer() }
        coVerify(exactly = 1) { deviceInfoDataSource.getOsVersion() }
        coVerify(exactly = 1) { deviceInfoDataSource.getBatteryInfo() }
        coVerify(exactly = 1) { deviceInfoDataSource.getNetworkInfo() }
        coVerify(exactly = 1) { deviceInfoDataSource.getStorageInfo() }
    }
    
    @Test
    fun `should throw DeviceInfoException when datasource fails`() = runTest {
        val exception = RuntimeException("Device info error")
        coEvery { deviceInfoDataSource.getDeviceModel() } throws exception
        coEvery { deviceInfoDataSource.getManufacturer() } returns "Google"
        coEvery { deviceInfoDataSource.getOsVersion() } returns "Android 14"
        coEvery { deviceInfoDataSource.getBatteryInfo() } returns BatteryInfo(85, true, 25.0f, BatteryHealth.GOOD)
        coEvery { deviceInfoDataSource.getNetworkInfo() } returns NetworkInfo(ConnectionType.WIFI, true, 90)
        coEvery { deviceInfoDataSource.getStorageInfo() } returns StorageInfo(128_000_000_000L, 64_000_000_000L, 64_000_000_000L)

        try {
            repository.getDeviceInfo()
            assertThat(false).isTrue()
        } catch (e: DeviceInfoException) {
            assertThat(e.message).contains("Error getting device info")
            assertThat(e.cause).isEqualTo(exception)
        }
    }
    
    @Test
    fun `should return fallback network info when network datasource fails`() = runTest {
        coEvery { deviceInfoDataSource.getNetworkInfo() } throws SecurityException("Permission denied")

        val result = repository.getNetworkInfo()

        assertThat(result.connectionType).isEqualTo(ConnectionType.NONE)
        assertThat(result.isConnected).isFalse()
        assertThat(result.signalStrength).isNull()
    }

    @Test
    fun `should emit monitoring events from all sources`() = runTest {
        // Given
        val batteryInfo = BatteryInfo(50, false, 30.0f, BatteryHealth.GOOD)
        val networkInfo = NetworkInfo(ConnectionType.CELLULAR, true, 85)

        every { deviceInfoDataSource.observeBatteryChanges() } returns flowOf(batteryInfo)
        every { deviceInfoDataSource.observeNetworkChanges() } returns flowOf(networkInfo)
        coEvery { deviceInfoDataSource.getStorageInfo() } returns StorageInfo(128_000_000_000L, 64_000_000_000L, 64_000_000_000L)

        // When & Then
        repository.observeMonitoringEvents().test {

            val events = mutableListOf<MonitoringEvent>()

            repeat(2) {
                events.add(awaitItem())
            }

            val batteryEvents = events.filterIsInstance<MonitoringEvent.BatteryChanged>()
            val networkEvents = events.filterIsInstance<MonitoringEvent.NetworkChanged>()

            assertThat(batteryEvents).hasSize(1)
            assertThat(batteryEvents.first().batteryInfo).isEqualTo(batteryInfo)

            assertThat(networkEvents).hasSize(1)
            assertThat(networkEvents.first().networkInfo).isEqualTo(networkInfo)

            cancelAndIgnoreRemainingEvents()
        }
    }
}