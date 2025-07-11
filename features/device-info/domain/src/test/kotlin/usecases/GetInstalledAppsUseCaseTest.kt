package usecases

import com.google.common.truth.Truth.assertThat
import entities.apps.InstalledApp
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import org.koin.test.KoinTest
import org.koin.test.inject
import repositories.InstalledAppsRepository

class GetInstalledAppsUseCaseTest : KoinTest {
    
    private val mockRepository: InstalledAppsRepository = mockk()
    private val getInstalledAppsUseCase: GetInstalledAppsUseCase by inject()
    
    @Before
    fun setup() {
        startKoin {
            modules(module {
                single<InstalledAppsRepository> { mockRepository }
                single { GetInstalledAppsUseCase(get()) }
            })
        }
    }
    
    @After
    fun tearDown() {
        stopKoin()
    }
    
    @Test
    fun `should return list of installed apps when repository call is successful`() = runTest {
        val expectedApps = listOf(
            InstalledApp(
                packageName = "com.google.chrome",
                name = "Chrome",
                version = "118.0.5993.88",
                versionCode = 599308800L,
                installTime = System.currentTimeMillis() - 86400000L, // 1 day ago
                updateTime = System.currentTimeMillis() - 3600000L, // 1 hour ago
                size = 150_000_000L
            ),
            InstalledApp(
                packageName = "com.spotify.music",
                name = "Spotify",
                version = "8.8.50.488",
                versionCode = 88050488L,
                installTime = System.currentTimeMillis() - 2592000000L, // 30 days ago
                updateTime = System.currentTimeMillis() - 604800000L, // 7 days ago
                size = 80_000_000L
            )
        )
        
        coEvery { mockRepository.getInstalledApps() } returns expectedApps

        val result = getInstalledAppsUseCase()

        assertThat(result).containsExactlyElementsIn(expectedApps)
        assertThat(result).hasSize(2)
        coVerify(exactly = 1) { mockRepository.getInstalledApps() }
    }
    
    @Test
    fun `should return empty list when no apps are installed`() = runTest {
        coEvery { mockRepository.getInstalledApps() } returns emptyList()

        val result = getInstalledAppsUseCase()

        assertThat(result).isEmpty()
        coVerify(exactly = 1) { mockRepository.getInstalledApps() }
    }
    
    @Test
    fun `should propagate exception when repository fails`() = runTest {
        val exception = SecurityException("Permission denied")
        coEvery { mockRepository.getInstalledApps() } throws exception

        try {
            getInstalledAppsUseCase()
            assertThat(false).isTrue()
        } catch (e: Exception) {
            assertThat(e).isEqualTo(exception)
        }
        
        coVerify(exactly = 1) { mockRepository.getInstalledApps() }
    }
}