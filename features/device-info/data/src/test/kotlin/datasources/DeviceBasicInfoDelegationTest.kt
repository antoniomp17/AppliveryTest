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

class DeviceBasicInfoDelegationTest {
    
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
    fun `should delegate getDeviceModel to deviceBasicInfoDataSource`() = runTest {
        // Given
        val expectedModel = "Pixel 7"
        coEvery { deviceBasicInfoDataSource.getDeviceModel() } returns expectedModel
        
        // When
        val result = dataSource.getDeviceModel()
        
        // Then
        assertThat(result).isEqualTo(expectedModel)
        coVerify(exactly = 1) { deviceBasicInfoDataSource.getDeviceModel() }
    }
    
    @Test
    fun `should delegate getManufacturer to deviceBasicInfoDataSource`() = runTest {
        // Given
        val expectedManufacturer = "Google"
        coEvery { deviceBasicInfoDataSource.getManufacturer() } returns expectedManufacturer
        
        // When
        val result = dataSource.getManufacturer()
        
        // Then
        assertThat(result).isEqualTo(expectedManufacturer)
        coVerify(exactly = 1) { deviceBasicInfoDataSource.getManufacturer() }
    }
    
    @Test
    fun `should delegate getOsVersion to deviceBasicInfoDataSource`() = runTest {
        // Given
        val expectedVersion = "Android 14 (API 34)"
        coEvery { deviceBasicInfoDataSource.getOsVersion() } returns expectedVersion
        
        // When
        val result = dataSource.getOsVersion()
        
        // Then
        assertThat(result).isEqualTo(expectedVersion)
        coVerify(exactly = 1) { deviceBasicInfoDataSource.getOsVersion() }
    }
}