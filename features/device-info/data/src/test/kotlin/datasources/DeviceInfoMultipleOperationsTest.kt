package datasources

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import datasources.battery.BatteryDataSource
import datasources.device.DeviceBasicInfoDataSource
import datasources.network.NetworkDataSource
import datasources.storage.StorageDataSource
import entities.info.BatteryHealth
import entities.info.BatteryInfo
import entities.info.ConnectionType
import entities.info.NetworkInfo
import entities.info.StorageInfo
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

class DeviceInfoMultipleOperationsTest {
    
    private val deviceBasicInfoDataSource: DeviceBasicInfoDataSource = mockk()
    private val batteryDataSource: BatteryDataSource = mockk()
    private val networkDataSource: NetworkDataSource = mockk()
    private val storageDataSource: StorageDataSource = mockk()
    
    private lateinit var dataSource: DeviceInfoDataSource
    
    @Before
    fun setup() {
        dataSource = DeviceInfoDataSourceImpl(
            deviceBasicInfoDataSource,
            batteryDataSource,
            networkDataSource,
            storageDataSource
        )
    }
    
    @Test
    fun `should handle multiple concurrent calls correctly`() = runTest {
        // Given
        val deviceModel = "Pixel 7"
        val manufacturer = "Google"
        val batteryInfo = BatteryInfo(90, true, 23.5f, BatteryHealth.GOOD)
        val networkInfo = NetworkInfo(ConnectionType.WIFI, true, 95)
        val storageInfo = StorageInfo(256_000_000_000L, 128_000_000_000L, 128_000_000_000L)
        
        coEvery { deviceBasicInfoDataSource.getDeviceModel() } returns deviceModel
        coEvery { deviceBasicInfoDataSource.getManufacturer() } returns manufacturer
        coEvery { batteryDataSource.getBatteryInfo() } returns batteryInfo
        coEvery { networkDataSource.getNetworkInfo() } returns networkInfo
        coEvery { storageDataSource.getStorageInfo() } returns storageInfo
        
        // When
        val resultModel = dataSource.getDeviceModel()
        val resultManufacturer = dataSource.getManufacturer()
        val resultBattery = dataSource.getBatteryInfo()
        val resultNetwork = dataSource.getNetworkInfo()
        val resultStorage = dataSource.getStorageInfo()
        
        // Then
        assertThat(resultModel).isEqualTo(deviceModel)
        assertThat(resultManufacturer).isEqualTo(manufacturer)
        assertThat(resultBattery).isEqualTo(batteryInfo)
        assertThat(resultNetwork).isEqualTo(networkInfo)
        assertThat(resultStorage).isEqualTo(storageInfo)
        
        coVerify(exactly = 1) { deviceBasicInfoDataSource.getDeviceModel() }
        coVerify(exactly = 1) { deviceBasicInfoDataSource.getManufacturer() }
        coVerify(exactly = 1) { batteryDataSource.getBatteryInfo() }
        coVerify(exactly = 1) { networkDataSource.getNetworkInfo() }
        coVerify(exactly = 1) { storageDataSource.getStorageInfo() }
    }
    
    @Test
    fun `should handle flow operations independently`() = runTest {
        // Given
        val batteryInfo1 = BatteryInfo(80, true, 25.0f, BatteryHealth.GOOD)
        val batteryInfo2 = BatteryInfo(75, true, 26.0f, BatteryHealth.GOOD)
        val networkInfo1 = NetworkInfo(ConnectionType.WIFI, true, 90)
        val networkInfo2 = NetworkInfo(ConnectionType.CELLULAR, true, 85)

        every { batteryDataSource.observeBatteryChanges() } returns flowOf(batteryInfo1, batteryInfo2)
        every { networkDataSource.observeNetworkChanges() } returns flowOf(networkInfo1, networkInfo2)

        // When & Then - Battery flow
        dataSource.observeBatteryChanges().test {
            assertThat(awaitItem()).isEqualTo(batteryInfo1)
            assertThat(awaitItem()).isEqualTo(batteryInfo2)
            awaitComplete()
        }

        // When & Then - Network flow
        dataSource.observeNetworkChanges().test {
            assertThat(awaitItem()).isEqualTo(networkInfo1)
            assertThat(awaitItem()).isEqualTo(networkInfo2)
            awaitComplete()
        }

        verify(exactly = 1) { batteryDataSource.observeBatteryChanges() }
        verify(exactly = 1) { networkDataSource.observeNetworkChanges() }
    }
}