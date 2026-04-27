package de.infonline.lib.iomb.measurements.common

import android.content.Context
import android.content.pm.PackageInfo
import androidx.annotation.Keep
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import de.infonline.lib.iomb.util.IOLLog
import javax.inject.Inject

@Suppress("DEPRECATION")
class ApplicationInfoBuilder @Inject constructor(
        private val context: Context
) {

    private val packageInfo by lazy {
        try {
            context.packageManager.getPackageInfo(context.packageName, 0)
        } catch (e: Exception) {
            IOLLog.tag(TAG).e(e, "getPackageInfo failed.")
            PackageInfo().apply {
                packageName = context.packageName
                versionName = "0.0.0"
                versionCode = -1
            }
        }
    }

    fun build(): Info {
        return Info(
                packageName = packageInfo.packageName,
                versionName = packageInfo.versionName,
                versionCode = packageInfo.versionCode.toLong()
        )
    }

    @Keep
    @JsonClass(generateAdapter = true)
    data class Info(
            @Json(name = "package") val packageName: String,
            val versionName: String?,
            val versionCode: Long
    )

    companion object {
        private const val TAG = "ApplicationInfoBuilder"
    }
}