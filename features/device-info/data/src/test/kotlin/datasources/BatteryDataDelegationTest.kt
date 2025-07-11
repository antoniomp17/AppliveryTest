package datasources

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import datasources.battery.BatteryDataSource
import datasources.device.DeviceBasicInfoDataSource
import datasources.network.NetworkDataSource
import datasources.storage.StorageDataSource
import entities.info.BatteryHealth
import entities.info.BatteryInfo
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

class BatteryDataDelegationTest {
    
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
    fun `should delegate getBatteryInfo to batteryDataSource`() = runTest {
        // Given
        val expectedBatteryInfo = BatteryInfo(85, true, 25.0f, BatteryHealth.GOOD)
        coEvery { batteryDataSource.getBatteryInfo() } returns expectedBatteryInfo
        
        // When
        val result = dataSource.getBatteryInfo()
        
        // Then
        assertThat(result).isEqualTo(expectedBatteryInfo)
        coVerify(exactly = 1) { batteryDataSource.getBatteryInfo() }
    }
    
    @Test
    fun `should delegate observeBatteryChanges to batteryDataSource`() = runTest {
        // Given
        val batteryInfo = BatteryInfo(75, false, 30.0f, BatteryHealth.GOOD)
        val batteryFlow = flowOf(batteryInfo)
        every { batteryDataSource.observeBatteryChanges() } returns batteryFlow
        
        // When & Then
        dataSource.observeBatteryChanges().test {
            val emission = awaitItem()
            assertThat(emission).isEqualTo(batteryInfo)
            awaitComplete()
        }
        
        verify(exactly = 1) { batteryDataSource.observeBatteryChanges() }
    }
}