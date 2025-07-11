package datasources

import com.google.common.truth.Truth.assertThat
import datasources.battery.BatteryDataSource
import datasources.device.DeviceBasicInfoDataSource
import datasources.network.NetworkDataSource
import datasources.storage.StorageDataSource
import entities.info.StorageInfo
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

class StorageDataDelegationTest {
    
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
    fun `should delegate getStorageInfo to storageDataSource`() = runTest {
        // Given
        val expectedStorageInfo = StorageInfo(128_000_000_000L, 64_000_000_000L, 64_000_000_000L)
        coEvery { storageDataSource.getStorageInfo() } returns expectedStorageInfo
        
        // When
        val result = dataSource.getStorageInfo()
        
        // Then
        assertThat(result).isEqualTo(expectedStorageInfo)
        coVerify(exactly = 1) { storageDataSource.getStorageInfo() }
    }
}