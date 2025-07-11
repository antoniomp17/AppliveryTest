package datasources.device

import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class DeviceBasicInfoDataSourceTest {
    
    private lateinit var dataSource: DeviceBasicInfoDataSource
    
    @Before
    fun setup() {
        dataSource = DeviceBasicInfoDataSourceImpl()
    }
    
    @Test
    fun `should return device model from Build`() = runTest {
        val result = dataSource.getDeviceModel()

        assertThat(result).isNotEmpty()
        assertThat(result).contains("robolectric")
    }
    
    @Test
    fun `should format OS version correctly`() = runTest {
        val result = dataSource.getOsVersion()

        assertThat(result).contains("Android")
        assertThat(result).contains("API")
        assertThat(result).matches("Android .* \\(API \\d+\\)")
    }
    
    @Test
    fun `should return consistent values on multiple calls`() = runTest {
        val firstModel = dataSource.getDeviceModel()
        val firstManufacturer = dataSource.getManufacturer()
        val firstOsVersion = dataSource.getOsVersion()
        
        val secondModel = dataSource.getDeviceModel()
        val secondManufacturer = dataSource.getManufacturer()
        val secondOsVersion = dataSource.getOsVersion()
        
        assertThat(secondModel).isEqualTo(firstModel)
        assertThat(secondManufacturer).isEqualTo(firstManufacturer)
        assertThat(secondOsVersion).isEqualTo(firstOsVersion)
    }
    
    @Test
    fun `should not return null or empty values`() = runTest {
        val model = dataSource.getDeviceModel()
        val manufacturer = dataSource.getManufacturer()
        val osVersion = dataSource.getOsVersion()
        
        assertThat(model).isNotNull()
        assertThat(model).isNotEmpty()
        assertThat(manufacturer).isNotNull()
        assertThat(manufacturer).isNotEmpty()
        assertThat(osVersion).isNotNull()
        assertThat(osVersion).isNotEmpty()
    }
    
    @Test
    fun `should return valid Android version format`() = runTest {
        val osVersion = dataSource.getOsVersion()
        
        val pattern = "Android \\d+(\\.\\d+)* \\(API \\d+\\)"
        assertThat(osVersion).matches(pattern)
        
        assertThat(osVersion).startsWith("Android")
        assertThat(osVersion).contains("(API")
        assertThat(osVersion).endsWith(")")
    }
    
    @Test
    fun `should return reasonable device model values`() = runTest {
        val model = dataSource.getDeviceModel()
        
        assertThat(model.length).isAtLeast(1)
        assertThat(model.length).isAtMost(100)
        assertThat(model).doesNotContain("\n")
        assertThat(model.trim()).isEqualTo(model)
    }
    
    @Test
    fun `should return reasonable manufacturer values`() = runTest {
        val manufacturer = dataSource.getManufacturer()
        
        assertThat(manufacturer.length).isAtLeast(1)
        assertThat(manufacturer.length).isAtMost(50)
        assertThat(manufacturer).doesNotContain("\n")
        assertThat(manufacturer.trim()).isEqualTo(manufacturer)
    }
    
    @Test
    fun `should extract API level from OS version`() = runTest {
        val osVersion = dataSource.getOsVersion()
        
        val apiMatch = "\\(API (\\d+)\\)".toRegex().find(osVersion)
        assertThat(apiMatch).isNotNull()
        
        val apiLevel = apiMatch!!.groupValues[1].toInt()
        assertThat(apiLevel).isAtLeast(21)
        assertThat(apiLevel).isAtMost(50)
    }
    
    @Test
    fun `should handle Build access without exceptions`() = runTest {
        try {
            val model = dataSource.getDeviceModel()
            val manufacturer = dataSource.getManufacturer()
            val osVersion = dataSource.getOsVersion()
            
            assertThat(model).isNotEmpty()
            assertThat(manufacturer).isNotEmpty()
            assertThat(osVersion).isNotEmpty()
        } catch (e: Exception) {
            assertThat(false).isTrue()
        }
    }
}