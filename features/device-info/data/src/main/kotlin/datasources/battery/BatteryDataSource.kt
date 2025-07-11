package datasources.battery

import entities.info.BatteryInfo
import kotlinx.coroutines.flow.Flow

interface BatteryDataSource {
    suspend fun getBatteryInfo(): BatteryInfo
    fun observeBatteryChanges(): Flow<BatteryInfo>
}