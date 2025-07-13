package repositories

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import datasources.battery.BatteryDataSource
import datasources.device.DeviceBasicInfoDataSource
import datasources.network.NetworkDataSource
import datasources.storage.StorageDataSource
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
    
    private val deviceBasicInfoDataSource: DeviceBasicInfoDataSource = mockk()
    private val batteryDataSource: BatteryDataSource = mockk()
    private val networkDataSource: NetworkDataSource = mockk()
    private val storageDataSource: StorageDataSource = mockk()
    
    private lateinit var repository: DeviceInfoRepository
    
    @Before
    fun setup() {
        repository = DeviceInfoRepositoryImpl(
            deviceBasicInfoDataSource,
            batteryDataSource,
            networkDataSource,
            storageDataSource
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
        
        coEvery { deviceBasicInfoDataSource.getDeviceModel() } returns deviceModel
        coEvery { deviceBasicInfoDataSource.getManufacturer() } returns manufacturer
        coEvery { deviceBasicInfoDataSource.getOsVersion() } returns osVersion
        coEvery { batteryDataSource.getBatteryInfo() } returns batteryInfo
        coEvery { networkDataSource.getNetworkInfo() } returns networkInfo
        coEvery { storageDataSource.getStorageInfo() } returns storageInfo

        val result = repository.getDeviceInfo()

        assertThat(result.deviceModel).isEqualTo(deviceModel)
        assertThat(result.manufacturer).isEqualTo(manufacturer)
        assertThat(result.osVersion).isEqualTo(osVersion)
        assertThat(result.batteryInfo).isEqualTo(batteryInfo)
        assertThat(result.networkInfo).isEqualTo(networkInfo)
        assertThat(result.availableStorage).isEqualTo(storageInfo)
        
        coVerify(exactly = 1) { deviceBasicInfoDataSource.getDeviceModel() }
        coVerify(exactly = 1) { deviceBasicInfoDataSource.getManufacturer() }
        coVerify(exactly = 1) { deviceBasicInfoDataSource.getOsVersion() }
        coVerify(exactly = 1) { batteryDataSource.getBatteryInfo() }
        coVerify(exactly = 1) { networkDataSource.getNetworkInfo() }
        coVerify(exactly = 1) { storageDataSource.getStorageInfo() }
    }
    
    @Test
    fun `should throw DeviceInfoException when datasource fails`() = runTest {
        val exception = RuntimeException("Device info error")
        coEvery { deviceBasicInfoDataSource.getDeviceModel() } throws exception
        coEvery { deviceBasicInfoDataSource.getManufacturer() } returns "Google"
        coEvery { deviceBasicInfoDataSource.getOsVersion() } returns "Android 14"
        coEvery { batteryDataSource.getBatteryInfo() } returns BatteryInfo(85, true, 25.0f, BatteryHealth.GOOD)
        coEvery { networkDataSource.getNetworkInfo() } returns NetworkInfo(ConnectionType.WIFI, true, 90)
        coEvery { storageDataSource.getStorageInfo() } returns StorageInfo(128_000_000_000L, 64_000_000_000L, 64_000_000_000L)

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
        coEvery { networkDataSource.getNetworkInfo() } throws SecurityException("Permission denied")

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

        every { batteryDataSource.observeBatteryChanges() } returns flowOf(batteryInfo)
        every { networkDataSource.observeNetworkChanges() } returns flowOf(networkInfo)
        coEvery { storageDataSource.getStorageInfo() } returns StorageInfo(128_000_000_000L, 64_000_000_000L, 64_000_000_000L)

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