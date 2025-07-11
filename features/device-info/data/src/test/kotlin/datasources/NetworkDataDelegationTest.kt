package datasources

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import datasources.battery.BatteryDataSource
import datasources.device.DeviceBasicInfoDataSource
import datasources.network.NetworkDataSource
import datasources.storage.StorageDataSource
import entities.info.ConnectionType
import entities.info.NetworkInfo
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

class NetworkDataDelegationTest {
    
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
    fun `should delegate getNetworkInfo to networkDataSource`() = runTest {
        // Given
        val expectedNetworkInfo = NetworkInfo(ConnectionType.WIFI, true, 90)
        coEvery { networkDataSource.getNetworkInfo() } returns expectedNetworkInfo
        
        // When
        val result = dataSource.getNetworkInfo()
        
        // Then
        assertThat(result).isEqualTo(expectedNetworkInfo)
        coVerify(exactly = 1) { networkDataSource.getNetworkInfo() }
    }
    
    @Test
    fun `should delegate observeNetworkChanges to networkDataSource`() = runTest {
        // Given
        val networkInfo = NetworkInfo(ConnectionType.CELLULAR, true, 85)
        val networkFlow = flowOf(networkInfo)
        every { networkDataSource.observeNetworkChanges() } returns networkFlow
        
        // When & Then
        dataSource.observeNetworkChanges().test {
            val emission = awaitItem()
            assertThat(emission).isEqualTo(networkInfo)
            awaitComplete()
        }
        
        verify(exactly = 1) { networkDataSource.observeNetworkChanges() }
    }
}