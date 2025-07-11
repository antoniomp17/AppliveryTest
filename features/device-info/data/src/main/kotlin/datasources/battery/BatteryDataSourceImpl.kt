package datasources.battery

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.os.Build
import androidx.annotation.RequiresApi
import entities.info.BatteryHealth
import entities.info.BatteryInfo
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import mappers.BatteryMapper
import receivers.BatteryBroadcastReceiver

class BatteryDataSourceImpl(
    private val context: Context,
    private val batteryManager: BatteryManager,
    private val batteryMapper: BatteryMapper
) : BatteryDataSource {

    override suspend fun getBatteryInfo(): BatteryInfo {
        val batteryIntent = context.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
            ?: return getFallbackBatteryInfo()

        val level = batteryIntent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
        val scale = batteryIntent.getIntExtra(BatteryManager.EXTRA_SCALE, -1)
        val batteryLevel = if (level != -1 && scale != -1) (level * 100 / scale) else -1

        val status = batteryIntent.getIntExtra(BatteryManager.EXTRA_STATUS, -1)
        val isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING ||
                status == BatteryManager.BATTERY_STATUS_FULL

        val temperature = batteryIntent.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, -1) / 10.0f

        val health = batteryIntent.getIntExtra(BatteryManager.EXTRA_HEALTH, -1)
        val batteryHealth = batteryMapper.mapBatteryHealth(health)

        return BatteryInfo(
            level = batteryLevel,
            isCharging = isCharging,
            temperature = temperature,
            health = batteryHealth
        )
    }

    private fun getFallbackBatteryInfo(): BatteryInfo {
        return BatteryInfo(
            level = -1,
            isCharging = false,
            temperature = -1f,
            health = BatteryHealth.UNKNOWN
        )
    }

    override fun observeBatteryChanges(): Flow<BatteryInfo> = callbackFlow {
        val receiver = BatteryBroadcastReceiver(batteryMapper) { trySend(it) }
        val filter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
        context.registerReceiver(receiver, filter)

        awaitClose { context.unregisterReceiver(receiver) }
    }.distinctUntilChanged()
}