package datasources.storage

import android.os.Environment
import android.os.StatFs
import entities.info.StorageInfo

class StorageDataSourceImpl : StorageDataSource {
    override suspend fun getStorageInfo(): StorageInfo {
        val statFs = StatFs(Environment.getDataDirectory().path)
        val totalSpace = statFs.totalBytes
        val freeSpace = statFs.availableBytes
        val usedSpace = totalSpace - freeSpace

        return StorageInfo(
            totalSpace = totalSpace,
            freeSpace = freeSpace,
            usedSpace = usedSpace
        )
    }
}