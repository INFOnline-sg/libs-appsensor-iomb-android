package de.infonline.lib.iomb.measurements.common

import android.annotation.SuppressLint
import android.content.Context
import javax.inject.Inject
import javax.inject.Singleton

@SuppressLint("HardwareIds")
@Singleton
class SecureSettingsRepo @Inject constructor(
        private val context: Context
) {

    val androidId: String by lazy {
        android.provider.Settings.Secure.getString(context.contentResolver, android.provider.Settings.Secure.ANDROID_ID)
    }

}