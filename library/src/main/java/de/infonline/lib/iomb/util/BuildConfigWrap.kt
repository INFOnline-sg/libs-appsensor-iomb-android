package de.infonline.lib.iomb.util

import de.infonline.lib.iomb.BuildConfig

internal object BuildConfigWrap {

    val isDebugBuild: Boolean = de.infonline.lib.iomb.BuildConfig.DEBUG || de.infonline.lib.iomb.BuildConfig.FLAVOR == "dev"

    val isDebugConfigEnabled: Boolean = de.infonline.lib.iomb.BuildConfig.DEBUG_CONFIG_ENABLED

    val debugConfigExpiryCode: String? = de.infonline.lib.iomb.BuildConfig.DEBUG_CONFIG_EXPIRY_RESPONSE_CODE

    val libraryVersionName: String = de.infonline.lib.iomb.BuildConfig.VERSION_NAME

    val libraryVersionNameFull: String = "${de.infonline.lib.iomb.BuildConfig.VERSION_NAME}-${de.infonline.lib.iomb.BuildConfig.GIT_COUNT}-${de.infonline.lib.iomb.BuildConfig.GIT_HASH}"

    @JvmStatic val oewaConfigApiUrl: String = de.infonline.lib.iomb.BuildConfig.OEWA_CONFIG_FILE_URL
    @JvmStatic val oewaEventApiUrl: String = de.infonline.lib.iomb.BuildConfig.OEWA_EVENT_API_URL

    @JvmStatic val szmConfigApiUrl: String = de.infonline.lib.iomb.BuildConfig.SZM_CONFIG_FILE_URL
    @JvmStatic val szmEventApiUrl: String = de.infonline.lib.iomb.BuildConfig.SZM_EVENT_API_URL
}