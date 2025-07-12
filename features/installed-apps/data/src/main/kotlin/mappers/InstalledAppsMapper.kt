package mappers

import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.os.Build
import entities.InstalledApp
import java.io.ByteArrayOutputStream
import androidx.core.graphics.createBitmap
import java.io.File

class InstalledAppsMapper {
    
    fun mapToInstalledApp(packageInfo: PackageInfo, packageManager: PackageManager): InstalledApp? {
        return try {
            val applicationInfo = packageInfo.applicationInfo ?: return null
            val packageName = packageInfo.packageName
            
            val appName = applicationInfo.loadLabel(packageManager).toString()
            val version = packageInfo.versionName ?: "Unknown"
            val versionCode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                packageInfo.longVersionCode
            } else {
                @Suppress("DEPRECATION")
                packageInfo.versionCode.toLong()
            }
            
            val installTime = packageInfo.firstInstallTime
            val updateTime = packageInfo.lastUpdateTime
            
            // Get app icon as byte array
            val iconBytes = try {
                val drawable = applicationInfo.loadIcon(packageManager)
                drawableToByteArray(drawable)
            } catch (e: Exception) {
                null
            }
            
            // Get app size (approximate)
            val appSize = try {
                applicationInfo.sourceDir?.let { sourceDir ->
                    File(sourceDir).length()
                } ?: 0L
            } catch (e: Exception) {
                0L
            }
            
            InstalledApp(
                packageName = packageName,
                name = appName,
                version = version,
                versionCode = versionCode,
                icon = iconBytes,
                installTime = installTime,
                updateTime = updateTime,
                size = appSize
            )
        } catch (e: Exception) {
            null
        }
    }
    
    private fun drawableToByteArray(drawable: Drawable): ByteArray? {
        return try {
            val bitmap = if (drawable is BitmapDrawable) {
                drawable.bitmap
            } else {
                val bitmap = createBitmap(drawable.intrinsicWidth, drawable.intrinsicHeight)
                val canvas = Canvas(bitmap)
                drawable.setBounds(0, 0, canvas.width, canvas.height)
                drawable.draw(canvas)
                bitmap
            }
            
            val stream = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
            stream.toByteArray()
        } catch (e: Exception) {
            null
        }
    }
}