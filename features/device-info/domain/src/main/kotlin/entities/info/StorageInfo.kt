package entities.info

data class StorageInfo(
    val totalSpace: Long,
    val freeSpace: Long,
    val usedSpace: Long
) {
    val usedPercentage: Float
        get() = (usedSpace.toFloat() / totalSpace.toFloat()) * 100
}