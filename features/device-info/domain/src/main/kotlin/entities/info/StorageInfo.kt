package entities.info

data class StorageInfo(
    val totalSpace: Long,
    val freeSpace: Long,
    val usedSpace: Long
) {
    val usedPercentage: Float
        get() = if (totalSpace > 0) {
            (usedSpace.toFloat() / totalSpace.toFloat()) * 100
        } else {
            0f
        }
}