package de.infonline.lib.iomb.measurements.common

import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PlatformInfos @Inject constructor() {

    val osVersion: String by lazy { android.os.Build.VERSION.RELEASE }

    val fingerPrint: String by lazy {
        String.format(
                Locale.US, "%s,%s,%s,%s,%s,%s",
                android.os.Build.MANUFACTURER,
                android.os.Build.MODEL,
                android.os.Build.DEVICE,
                android.os.Build.BRAND,
                android.os.Build.HARDWARE,
                android.os.Build.PRODUCT
        )
    }
}