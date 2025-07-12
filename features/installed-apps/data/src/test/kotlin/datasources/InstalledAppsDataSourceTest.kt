package datasources

import android.content.pm.ApplicationInfo
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import entities.InstalledApp
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import mappers.InstalledAppsMapper
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment

@RunWith(RobolectricTestRunner::class)
class InstalledAppsDataSourceTest {

    private val packageManager: PackageManager = mockk()
    private val installedAppsMapper: InstalledAppsMapper = mockk()
    
    private lateinit var dataSource: InstalledAppsDataSource
    
    @Before
    fun setup() {
        dataSource = InstalledAppsDataSourceImpl(packageManager, installedAppsMapper)
    }
    
    @Test
    fun `should return list of installed apps when package manager succeeds`() = runTest {
        val mockPackageInfo1 = createMockPackageInfo("com.test.app1", "Test App 1")
        val mockPackageInfo2 = createMockPackageInfo("com.test.app2", "Test App 2")
        val packageInfoList = listOf(mockPackageInfo1, mockPackageInfo2)
        
        val expectedApp1 =
            InstalledApp("com.test.app1", "Test App 1", "1.0", 1L, null, 123L, 456L, 1000L)
        val expectedApp2 = InstalledApp("com.test.app2", "Test App 2", "2.0", 2L, null, 789L, 101L, 2000L)
        
        every { packageManager.getInstalledPackages(any<Int>()) } returns packageInfoList
        every { installedAppsMapper.mapToInstalledApp(mockPackageInfo1, packageManager) } returns expectedApp1
        every { installedAppsMapper.mapToInstalledApp(mockPackageInfo2, packageManager) } returns expectedApp2

        val result = dataSource.getInstalledApps()

        assertThat(result).hasSize(2)
        assertThat(result).containsExactly(expectedApp1, expectedApp2)
        verify { packageManager.getInstalledPackages(any<Int>()) }
        verify { installedAppsMapper.mapToInstalledApp(mockPackageInfo1, packageManager) }
        verify { installedAppsMapper.mapToInstalledApp(mockPackageInfo2, packageManager) }
    }
    
    @Test
    fun `should filter out system apps`() = runTest {
        val userApp = createMockPackageInfo("com.user.app", "User App", isSystemApp = false)
        val systemApp = createMockPackageInfo("com.android.system", "System App", isSystemApp = true)
        val packageInfoList = listOf(userApp, systemApp)
        
        val expectedUserApp = InstalledApp("com.user.app", "User App", "1.0", 1L, null, 123L, 456L, 1000L)
        
        every { packageManager.getInstalledPackages(any<Int>()) } returns packageInfoList
        every { installedAppsMapper.mapToInstalledApp(userApp, packageManager) } returns expectedUserApp

        val result = dataSource.getInstalledApps()

        assertThat(result).hasSize(1)
        assertThat(result).containsExactly(expectedUserApp)
        verify { installedAppsMapper.mapToInstalledApp(userApp, packageManager) }
        verify(exactly = 0) { installedAppsMapper.mapToInstalledApp(systemApp, packageManager) }
    }
    
    @Test
    fun `should return empty list when package manager throws exception`() = runTest {
        every { packageManager.getInstalledPackages(any<Int>()) } throws SecurityException("Permission denied")

        val result = dataSource.getInstalledApps()

        assertThat(result).isEmpty()
        verify { packageManager.getInstalledPackages(any<Int>()) }
    }
    
    @Test
    fun `should filter out null mapped apps`() = runTest {
        val validPackageInfo = createMockPackageInfo("com.valid.app", "Valid App")
        val invalidPackageInfo = createMockPackageInfo("com.invalid.app", "Invalid App")
        val packageInfoList = listOf(validPackageInfo, invalidPackageInfo)
        
        val expectedValidApp = InstalledApp("com.valid.app", "Valid App", "1.0", 1L, null, 123L, 456L, 1000L)
        
        every { packageManager.getInstalledPackages(any<Int>()) } returns packageInfoList
        every { installedAppsMapper.mapToInstalledApp(validPackageInfo, packageManager) } returns expectedValidApp
        every { installedAppsMapper.mapToInstalledApp(invalidPackageInfo, packageManager) } returns null
        
        val result = dataSource.getInstalledApps()
        
        assertThat(result).hasSize(1)
        assertThat(result).containsExactly(expectedValidApp)
    }
    
    @Test
    fun `should return app details for valid package name`() = runTest {
        val packageName = "com.test.app"
        val mockPackageInfo = createMockPackageInfo(packageName, "Test App")
        val expectedApp = InstalledApp(packageName, "Test App", "1.0", 1L, null, 123L, 456L, 1000L)
        
        every { packageManager.getPackageInfo(packageName, any<Int>()) } returns mockPackageInfo
        every { installedAppsMapper.mapToInstalledApp(mockPackageInfo, packageManager) } returns expectedApp
        
        val result = dataSource.getAppDetails(packageName)
        
        assertThat(result).isEqualTo(expectedApp)
        verify { packageManager.getPackageInfo(packageName, any<Int>()) }
        verify { installedAppsMapper.mapToInstalledApp(mockPackageInfo, packageManager) }
    }
    
    @Test
    fun `should return null for invalid package name`() = runTest {
        val packageName = "com.invalid.app"
        every { packageManager.getPackageInfo(packageName, any<Int>()) } throws PackageManager.NameNotFoundException()
        
        val result = dataSource.getAppDetails(packageName)
        
        assertThat(result).isNull()
        verify { packageManager.getPackageInfo(packageName, any<Int>()) }
    }
    
    @Test
    fun `should emit installed apps in flow`() = runTest {
        val mockPackageInfo = createMockPackageInfo("com.test.app", "Test App")
        val expectedApp = InstalledApp("com.test.app", "Test App", "1.0", 1L, null, 123L, 456L, 1000L)
        
        every { packageManager.getInstalledPackages(any<Int>()) } returns listOf(mockPackageInfo)
        every { installedAppsMapper.mapToInstalledApp(mockPackageInfo, packageManager) } returns expectedApp
        
        dataSource.observeAppInstallations().test {
            val emission = awaitItem()
            assertThat(emission).hasSize(1)
            assertThat(emission).containsExactly(expectedApp)
            awaitComplete()
        }
    }

    private fun createMockPackageInfo(
        packageName: String,
        appName: String,
        isSystemApp: Boolean = false
    ): PackageInfo {
        val packageInfo = mockk<PackageInfo>(relaxed = true)
        val applicationInfo = mockk<ApplicationInfo>(relaxed = true)

        packageInfo.packageName = packageName
        packageInfo.versionName = "1.0"
        packageInfo.versionCode = 1
        packageInfo.firstInstallTime = 123L
        packageInfo.lastUpdateTime = 456L
        packageInfo.applicationInfo = applicationInfo

        every { applicationInfo.loadLabel(packageManager) } returns appName
        every { applicationInfo.loadIcon(packageManager) } returns mockk(relaxed = true)

        applicationInfo.flags = if (isSystemApp) ApplicationInfo.FLAG_SYSTEM else 0
        applicationInfo.sourceDir = "/data/app/$packageName/base.apk"

        return packageInfo
    }
}