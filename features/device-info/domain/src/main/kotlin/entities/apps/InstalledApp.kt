package entities.apps

data class InstalledApp(
    val packageName: String,
    val name: String,
    val version: String,
    val versionCode: Long,
    val icon: ByteArray? = null,
    val installTime: Long,
    val updateTime: Long,
    val size: Long
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        
        other as InstalledApp
        return packageName == other.packageName
    }
    
    override fun hashCode(): Int {
        return packageName.hashCode()
    }
}