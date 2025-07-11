package usecases

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import entities.MonitoringEvent
import entities.info.BatteryHealth
import entities.info.BatteryInfo
import entities.info.ConnectionType
import entities.info.NetworkInfo
import entities.info.StorageInfo
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import org.koin.test.KoinTest
import org.koin.test.inject
import repositories.DeviceInfoRepository

class ObserveMonitoringEventsUseCaseTest : KoinTest {
    
    private val mockRepository: DeviceInfoRepository = mockk()
    private val observeMonitoringEventsUseCase: ObserveMonitoringEventsUseCase by inject()
    
    @Before
    fun setup() {
        startKoin {
            modules(module {
                single<DeviceInfoRepository> { mockRepository }
                single { ObserveMonitoringEventsUseCase(get()) }
            })
        }
    }
    
    @After
    fun tearDown() {
        stopKoin()
    }
    
    @Test
    fun `should emit battery events when repository emits them`() = runTest {
        // Given
        val batteryEvent = MonitoringEvent.BatteryChanged(
            batteryInfo = BatteryInfo(
                level = 50,
                isCharging = false,
                temperature = 30.0f,
                health = BatteryHealth.GOOD
            )
        )
        
        every { mockRepository.observeMonitoringEvents() } returns flowOf(batteryEvent)

        observeMonitoringEventsUseCase().test {
            val emission = awaitItem()
            assertThat(emission).isEqualTo(batteryEvent)
            assertThat(emission).isInstanceOf(MonitoringEvent.BatteryChanged::class.java)
            awaitComplete()
        }
        
        verify(exactly = 1) { mockRepository.observeMonitoringEvents() }
    }
    
    @Test
    fun `should emit network events when repository emits them`() = runTest {
        // Given
        val networkEvent = MonitoringEvent.NetworkChanged(
            networkInfo = NetworkInfo(
                connectionType = ConnectionType.CELLULAR,
                isConnected = true,
                signalStrength = 75
            )
        )
        
        every { mockRepository.observeMonitoringEvents() } returns flowOf(networkEvent)

        observeMonitoringEventsUseCase().test {
            val emission = awaitItem()
            assertThat(emission).isEqualTo(networkEvent)
            assertThat(emission).isInstanceOf(MonitoringEvent.NetworkChanged::class.java)
            awaitComplete()
        }
        
        verify(exactly = 1) { mockRepository.observeMonitoringEvents() }
    }
    
    @Test
    fun `should emit multiple events in sequence`() = runTest {
        // Given
        val batteryEvent = MonitoringEvent.BatteryChanged(
            batteryInfo = BatteryInfo(75, true, 25.0f, BatteryHealth.GOOD)
        )
        val networkEvent = MonitoringEvent.NetworkChanged(
            networkInfo = NetworkInfo(ConnectionType.WIFI, true, 95)
        )
        val storageEvent = MonitoringEvent.StorageChanged(
            storageInfo = StorageInfo(128_000_000_000L, 32_000_000_000L, 96_000_000_000L)
        )
        
        every { mockRepository.observeMonitoringEvents() } returns flowOf(
            batteryEvent,
            networkEvent,
            storageEvent
        )

        observeMonitoringEventsUseCase().test {
            assertThat(awaitItem()).isEqualTo(batteryEvent)
            assertThat(awaitItem()).isEqualTo(networkEvent)
            assertThat(awaitItem()).isEqualTo(storageEvent)
            awaitComplete()
        }
        
        verify(exactly = 1) { mockRepository.observeMonitoringEvents() }
    }
}