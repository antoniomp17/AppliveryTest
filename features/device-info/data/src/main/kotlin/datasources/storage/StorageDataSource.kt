package datasources.storage

import entities.info.StorageInfo

interface StorageDataSource {
    suspend fun getStorageInfo(): StorageInfo
}