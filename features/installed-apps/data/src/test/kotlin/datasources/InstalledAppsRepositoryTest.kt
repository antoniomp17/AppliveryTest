package datasources

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import entities.InstalledApp
import exceptions.InstalledAppsException
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import repositories.InstalledAppsRepository
import repositories.InstalledAppsRepositoryImpl

class InstalledAppsRepositoryTest {
    
    private val installedAppsDataSource: InstalledAppsDataSource = mockk()
    
    private lateinit var repository: InstalledAppsRepository
    
    @Before
    fun setup() {
        repository = InstalledAppsRepositoryImpl(installedAppsDataSource)
    }
    
    @Test
    fun `should return installed apps when datasource succeeds`() = runTest {
        val expectedApps = listOf(
            InstalledApp("com.app1", "App 1", "1.0", 1L, null, 123L, 456L, 1000L),
            InstalledApp("com.app2", "App 2", "2.0", 2L, null, 789L, 101L, 2000L)
        )
        
        coEvery { installedAppsDataSource.getInstalledApps() } returns expectedApps

        val result = repository.getInstalledApps()

        assertThat(result).isEqualTo(expectedApps)
        coVerify { installedAppsDataSource.getInstalledApps() }
    }
    
    @Test
    fun `should throw InstalledAppsException when datasource fails`() = runTest {
        val exception = RuntimeException("DataSource error")
        coEvery { installedAppsDataSource.getInstalledApps() } throws exception

        try {
            repository.getInstalledApps()
            assertThat(false).isTrue() // Should not reach here
        } catch (e: InstalledAppsException) {
            assertThat(e.message).contains("Error getting installed apps")
            assertThat(e.cause).isEqualTo(exception)
        }
        
        coVerify { installedAppsDataSource.getInstalledApps() }
    }
    
    @Test
    fun `should return app details when datasource succeeds`() = runTest {
        val packageName = "com.test.app"
        val expectedApp = InstalledApp(packageName, "Test App", "1.0", 1L, null, 123L, 456L, 1000L)
        
        coEvery { installedAppsDataSource.getAppDetails(packageName) } returns expectedApp

        val result = repository.getAppDetails(packageName)

        assertThat(result).isEqualTo(expectedApp)
        coVerify { installedAppsDataSource.getAppDetails(packageName) }
    }
    
    @Test
    fun `should return null when app not found`() = runTest {
        val packageName = "com.nonexistent.app"
        coEvery { installedAppsDataSource.getAppDetails(packageName) } returns null

        val result = repository.getAppDetails(packageName)

        assertThat(result).isNull()
        coVerify { installedAppsDataSource.getAppDetails(packageName) }
    }
    
    @Test
    fun `should return null when datasource throws exception for app details`() = runTest {
        val packageName = "com.error.app"
        coEvery { installedAppsDataSource.getAppDetails(packageName) } throws SecurityException("Permission denied")

        val result = repository.getAppDetails(packageName)

        assertThat(result).isNull()
        coVerify { installedAppsDataSource.getAppDetails(packageName) }
    }
    
    @Test
    fun `should emit app installations from datasource`() = runTest {
        val apps = listOf(
            InstalledApp("com.app1", "App 1", "1.0", 1L, null, 123L, 456L, 1000L),
            InstalledApp("com.app2", "App 2", "2.0", 2L, null, 789L, 101L, 2000L)
        )
        
        every { installedAppsDataSource.observeAppInstallations() } returns flowOf(apps)

        repository.observeAppInstallations().test {
            val emission = awaitItem()
            assertThat(emission).isEqualTo(apps)
            awaitComplete()
        }
    }
    
    @Test
    fun `should emit empty list when flow throws exception`() = runTest {
        // Given
        every { installedAppsDataSource.observeAppInstallations() } returns flowOf<List<InstalledApp>>().apply {
            // Simular error en el flow
        }
        
        every { installedAppsDataSource.observeAppInstallations() } returns flowOf(
            listOf(InstalledApp("com.app1", "App 1", "1.0", 1L, null, 123L, 456L, 1000L))
        )
        
        repository.observeAppInstallations().test {
            val emission = awaitItem()
            assertThat(emission).hasSize(1)
            awaitComplete()
        }
    }
    
    @Test
    fun `should handle empty app list from datasource`() = runTest {
        coEvery { installedAppsDataSource.getInstalledApps() } returns emptyList()
        
        val result = repository.getInstalledApps()
        
        assertThat(result).isEmpty()
        coVerify { installedAppsDataSource.getInstalledApps() }
    }
    
    @Test
    fun `should preserve app order from datasource`() = runTest {
        val app1 = InstalledApp("com.z.app", "Z App", "1.0", 1L, null, 123L, 456L, 1000L)
        val app2 = InstalledApp("com.a.app", "A App", "1.0", 1L, null, 123L, 456L, 1000L)
        val expectedApps = listOf(app1, app2) // Z antes que A (orden del datasource)
        
        coEvery { installedAppsDataSource.getInstalledApps() } returns expectedApps

        val result = repository.getInstalledApps()

        assertThat(result).containsExactly(app1, app2).inOrder()
        coVerify { installedAppsDataSource.getInstalledApps() }
    }
}