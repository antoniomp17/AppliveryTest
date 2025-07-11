package datasources.storage

import android.os.Environment
import android.os.StatFs
import entities.info.StorageInfo

class StorageDataSourceImpl : StorageDataSource {

    override suspend fun getStorageInfo(): StorageInfo {
        return try {
            val statFs = StatFs(Environment.getDataDirectory().path)
            val totalSpace = statFs.totalBytes
            val freeSpace = statFs.availableBytes
            val usedSpace = totalSpace - freeSpace

            if (totalSpace <= 0 || freeSpace < 0) {
                getFallbackStorageInfo()
            } else {
                StorageInfo(
                    totalSpace = totalSpace,
                    freeSpace = freeSpace,
                    usedSpace = usedSpace
                )
            }
        } catch (e: Exception) {
            getFallbackStorageInfo()
        }
    }

    private fun getFallbackStorageInfo(): StorageInfo {
        val totalSpace = 64_000_000_000L // 64 GB
        val freeSpace = 32_000_000_000L   // 32 GB
        val usedSpace = 32_000_000_000L   // 32 GB

        return StorageInfo(
            totalSpace = totalSpace,
            freeSpace = freeSpace,
            usedSpace = usedSpace
        )
    }
}