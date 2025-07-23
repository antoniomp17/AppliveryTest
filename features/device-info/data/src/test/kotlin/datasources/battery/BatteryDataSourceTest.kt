package datasources.battery

import android.content.Context
import android.content.Intent
import android.os.BatteryManager
import androidx.core.content.ContextCompat
import org.junit.Test

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import entities.info.BatteryHealth
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import mappers.BatteryMapper
import org.junit.Before
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class BatteryDataSourceTest {

    private val context: Context = mockk()
    private val batteryMapper: BatteryMapper = mockk()

    private lateinit var dataSource: BatteryDataSource

    @Before
    fun setup() {
        dataSource = BatteryDataSourceImpl(context, batteryMapper)
    }

    @Test
    fun `should return battery info with correct values`() = runTest {
        val mockIntent = Intent(Intent.ACTION_BATTERY_CHANGED).apply {
            putExtra(BatteryManager.EXTRA_LEVEL, 85)
            putExtra(BatteryManager.EXTRA_SCALE, 100)
            putExtra(BatteryManager.EXTRA_STATUS, BatteryManager.BATTERY_STATUS_CHARGING)
            putExtra(BatteryManager.EXTRA_TEMPERATURE, 250)  // 25.0f
            putExtra(BatteryManager.EXTRA_HEALTH, BatteryManager.BATTERY_HEALTH_GOOD)
        }
        every { context.registerReceiver(null, any()) } returns mockIntent
        every { batteryMapper.mapBatteryHealth(BatteryManager.BATTERY_HEALTH_GOOD) } returns BatteryHealth.GOOD


        val result = dataSource.getBatteryInfo()

        assertThat(result.level).isEqualTo(85)
        assertThat(result.isCharging).isTrue()
        assertThat(result.temperature).isEqualTo(25.0f)
        assertThat(result.health).isEqualTo(BatteryHealth.GOOD)
    }

    @Test
    fun `should return fallback values when battery manager fails`() = runTest {

        val mockIntent = Intent(Intent.ACTION_BATTERY_CHANGED).apply {
            putExtra(BatteryManager.EXTRA_LEVEL, -1)
            putExtra(BatteryManager.EXTRA_SCALE, -1)
            putExtra(BatteryManager.EXTRA_STATUS, -1)
            putExtra(BatteryManager.EXTRA_TEMPERATURE, -1)
            putExtra(BatteryManager.EXTRA_HEALTH, -1)
        }
        every { context.registerReceiver(null, any()) } returns mockIntent
        every { batteryMapper.mapBatteryHealth(any()) } returns BatteryHealth.UNKNOWN

        val result = dataSource.getBatteryInfo()

        assertThat(result.level).isEqualTo(-1)
        assertThat(result.isCharging).isFalse()
        assertThat(result.health).isEqualTo(BatteryHealth.UNKNOWN)
    }

    @Test
    fun `should initialize flow without exceptions`() = runTest {
        every {
            ContextCompat.registerReceiver(
                context,
                any(),
                any(),
                ContextCompat.RECEIVER_NOT_EXPORTED
            ) } returns null

        every { context.unregisterReceiver(any()) } returns Unit

        dataSource.observeBatteryChanges().test {
            cancelAndIgnoreRemainingEvents()
        }
    }
}