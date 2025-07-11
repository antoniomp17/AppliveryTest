package receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import entities.info.BatteryInfo
import mappers.BatteryMapper

class BatteryBroadcastReceiver(
    private val batteryMapper: BatteryMapper,
    private val onBatteryChanged: (BatteryInfo) -> Unit
) : BroadcastReceiver() {
    
    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent?.action == Intent.ACTION_BATTERY_CHANGED) {
            val batteryInfo = batteryMapper.mapBatteryInfoFromIntent(intent)
            onBatteryChanged(batteryInfo)
        }
    }
}