package datasources

import com.google.common.truth.Truth.assertThat
import datasources.battery.BatteryDataSource
import datasources.device.DeviceBasicInfoDataSource
import datasources.network.NetworkDataSource
import datasources.storage.StorageDataSource
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

class DeviceInfoErrorHandlingTest {
    
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
    fun `should propagate exceptions from deviceBasicInfoDataSource`() = runTest {
        // Given
        val exception = RuntimeException("Device model error")
        coEvery { deviceBasicInfoDataSource.getDeviceModel() } throws exception
        
        // When & Then
        try {
            dataSource.getDeviceModel()
            assertThat(false).isTrue()
        } catch (e: Exception) {
            assertThat(e).isEqualTo(exception)
        }
        
        coVerify(exactly = 1) { deviceBasicInfoDataSource.getDeviceModel() }
    }
    
    @Test
    fun `should propagate exceptions from batteryDataSource`() = runTest {
        // Given
        val exception = SecurityException("Battery access denied")
        coEvery { batteryDataSource.getBatteryInfo() } throws exception
        
        // When & Then
        try {
            dataSource.getBatteryInfo()
            assertThat(false).isTrue()
        } catch (e: Exception) {
            assertThat(e).isEqualTo(exception)
        }
        
        coVerify(exactly = 1) { batteryDataSource.getBatteryInfo() }
    }
    
    @Test
    fun `should propagate exceptions from networkDataSource`() = runTest {
        // Given
        val exception = SecurityException("Network access denied")
        coEvery { networkDataSource.getNetworkInfo() } throws exception
        
        // When & Then
        try {
            dataSource.getNetworkInfo()
            assertThat(false).isTrue()
        } catch (e: Exception) {
            assertThat(e).isEqualTo(exception)
        }
        
        coVerify(exactly = 1) { networkDataSource.getNetworkInfo() }
    }
    
    @Test
    fun `should propagate exceptions from storageDataSource`() = runTest {
        // Given
        val exception = RuntimeException("Storage access error")
        coEvery { storageDataSource.getStorageInfo() } throws exception
        
        // When & Then
        try {
            dataSource.getStorageInfo()
            assertThat(false).isTrue()
        } catch (e: Exception) {
            assertThat(e).isEqualTo(exception)
        }
        
        coVerify(exactly = 1) { storageDataSource.getStorageInfo() }
    }
}