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

    private companion object {
        private const val FALLBACK_TOTAL_SPACE_BYTES = 64_000_000_000L
        private const val FALLBACK_FREE_SPACE_BYTES = 32_000_000_000L
        private const val FALLBACK_USED_SPACE_BYTES = 32_000_000_000L
    }

    private fun getFallbackStorageInfo(): StorageInfo {
        val totalSpace = FALLBACK_TOTAL_SPACE_BYTES
        val freeSpace = FALLBACK_FREE_SPACE_BYTES
        val usedSpace = FALLBACK_USED_SPACE_BYTES

        return StorageInfo(
            totalSpace = totalSpace,
            freeSpace = freeSpace,
            usedSpace = usedSpace
        )
    }
}