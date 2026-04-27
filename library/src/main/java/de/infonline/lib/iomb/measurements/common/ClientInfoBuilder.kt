package de.infonline.lib.iomb.measurements.common

import android.content.Context
import android.content.res.Configuration
import de.infonline.lib.iomb.measurements.Measurement
import de.infonline.lib.iomb.measurements.common.config.ConfigData
import de.infonline.lib.iomb.measurements.common.network.CarrierInfo
import de.infonline.lib.iomb.measurements.common.network.NetworkMonitor
import de.infonline.lib.iomb.util.HashHelper.toSHA256
import de.infonline.lib.iomb.util.IOLLog
import de.infonline.lib.iomb.util.PerMeasurement
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.kotlin.Singles
import java.security.MessageDigest
import java.util.*
import javax.inject.Inject
import kotlin.math.pow
import kotlin.math.round
import kotlin.math.sqrt


@PerMeasurement
internal class ClientInfoBuilder @Inject constructor(
    private val setup: Measurement.Setup,
    private val context: Context,
    private val secureSettingsRepo: SecureSettingsRepo,
    private val networkMonitor: NetworkMonitor,
    private val carrierInfo: CarrierInfo,
//    private val installIdRepo: InstallIdRepo,
//    private val advertisingIdRepo: AdvertisingIdRepo,
    private val platformInfos: PlatformInfos,
    private val proofToken: ProofToken
) {
    private val tag = setup.logTag("ClientInfoBuilder")

    // This does not produce common MD5 results, but has to stay as we shouldn't change the identifiers in legacy
    private fun String.toMD5IDEncode(): String? = try {
        val messageDigest = MessageDigest.getInstance("MD5").let {
            it.update(this@toMD5IDEncode.toByteArray())
            it.digest()
        }

        val hexString = StringBuffer()
        for (i in messageDigest.indices) {
            hexString.append(Integer.toHexString(0xFF and messageDigest[i].toInt()))
        }
        hexString.toString()
    } catch (e: Exception) {
        IOLLog.tag(tag).e(e, "Failed to MD5 encode ID: %s", this)
        null
    }

    private fun String.toSHA256IDEncode(): String? = try {
        val salt = StringBuilder(this).reverse().toString()
        (this + salt.toSHA256()).toSHA256()
    } catch (e: Exception) {
        IOLLog.tag(tag).e(e, "Failed to SHA256 encode ID %s", this)
        null
    }

    private fun getDeviceIdentifiers(configData: ConfigData<*, *>): InfoInternal.DeviceIdentifiers? {
        return null
        // TODO Move this check up one level
/*        if ((setup as? LegacySetup)?.privacySetting == PrivacySetting.PAGE_IMPRESSIONS_ONLY) {
            return null
        } else if(configData.remoteConfig.configType == ConfigData.ConfigType.ACSAM || configData.remoteConfig.configType == ConfigData.ConfigType.IOMB){
            return null
        }

        val hashingType = configData.remoteConfig.configuration?.hashingType

        val installationIdMD5 = if (hashingType == HashingType.MD5 || hashingType == HashingType.MD5_SHA256) {
            installIdRepo.installationId?.toMD5IDEncode()
        } else null

        val installationIdSHA256 = if (hashingType == HashingType.MD5_SHA256 || hashingType == HashingType.SHA256) {
            installIdRepo.installationId?.toSHA256IDEncode()
        } else null

        val advertisingId = advertisingIdRepo.advertisingIdentifier.blockingGet()
        val advertisingIdMD5 = if (hashingType == HashingType.MD5 || hashingType == HashingType.MD5_SHA256) {
            advertisingId?.toMD5IDEncode()
        } else null


        val advertisingIdSHA256 = if (hashingType == HashingType.MD5_SHA256 || hashingType == HashingType.SHA256) {
            advertisingId?.toSHA256IDEncode()
        } else null

        val androidId = if (advertisingId == null || configData.localConfig.deviceIDsEnabled) {
            if (configData.localConfig.deviceIDsEnabled) {
                IOLLog.tag(tag).d("Device ids are enabled and will also be logged.")
            } else {
                IOLLog.tag(tag).d("Logging device ids because Google Play Services are not available on device.")
            }

            secureSettingsRepo.androidId
        } else null

        val androidIdMD5 = if (hashingType == HashingType.MD5 || hashingType == HashingType.MD5_SHA256) {
            androidId?.toMD5IDEncode()
        } else null

        val androidIdSHA256 = if (hashingType == HashingType.MD5_SHA256 || hashingType == HashingType.SHA256) {
            androidId?.toSHA256IDEncode()
        } else null

        return InfoInternal.DeviceIdentifiers(
            installationId = installationIdMD5,
            installationIdSHA256 = installationIdSHA256,
            advertisingIdentifier = advertisingIdMD5,
            advertisingIdentifierSHA256 = advertisingIdSHA256,
            androidId = androidIdMD5,
            androidIdSHA256 = androidIdSHA256
        )*/
    }

    private fun getScreenInfo(): InfoInternal.Screen {
        val resources = context.resources
        val displayMetrics = resources.displayMetrics

        val clientScreenResolution = String.format(
                Locale.ROOT, "%sx%s",
                displayMetrics.widthPixels, displayMetrics.heightPixels
        )
        val clientScreenDPI = displayMetrics.densityDpi

        val clientScreenSize = run {
            val configuration = resources.configuration
            configuration.screenLayout and Configuration.SCREENLAYOUT_SIZE_MASK
        }

        val diagonalInches = run {
            val widthInches = displayMetrics.widthPixels / displayMetrics.xdpi
            val heightInches = displayMetrics.heightPixels / displayMetrics.ydpi
            sqrt(widthInches.toDouble().pow(2.0) + heightInches.toDouble().pow(2.0))
        }.round(2)

        return InfoInternal.Screen(
            resolution = clientScreenResolution,
            dpi = clientScreenDPI,
            size = clientScreenSize,
            screenInches = diagonalInches
        )
    }

    private fun Double.round(decimals: Int): Double {
        var multiplier = 1.0
        repeat(decimals) { multiplier *= 10 }
        return round(this * multiplier) / multiplier
    }

    fun build(configData: ConfigData<*, *>): Single<InfoInternal> = Singles
            .zip(
                    networkMonitor.networkType,
                    carrierInfo.info
            )
            .map { (networkType, carrierInfo) ->
                val locale = Locale.getDefault()

                val uuids = when (setup.type) {
                    Measurement.Type.ACSAM,
                    Measurement.Type.IOMB -> null
                    else -> getDeviceIdentifiers(configData)
                }

                val deviceName = when (setup.type) {
                    Measurement.Type.ACSAM,
                    Measurement.Type.IOMB -> proofToken.lookupToken()
                    else -> null
                }

                InfoInternal(
                        uuids = uuids,
                        screen = getScreenInfo(),
                        locale = locale,
                        carrier = carrierInfo,
                        network = networkType,
                        osVersion = platformInfos.osVersion,
                        platform = platformInfos.fingerPrint,
                        deviceName = deviceName
                )
            }

    data class InfoInternal(
        val uuids: DeviceIdentifiers? = null,
        val screen: Screen,
        val locale: Locale,
        val carrier: CarrierInfo.Info,
        val network: NetworkMonitor.NetworkType,
        val osIdentifier: String = "android",
        val osVersion: String,
        val platform: String,
        val deviceName: String? = null
    ) {

        data class DeviceIdentifiers(
                val installationId: String?,
                val installationIdSHA256: String?,
                val advertisingIdentifier: String?,
                val advertisingIdentifierSHA256: String?,
                val androidId: String?,
                val androidIdSHA256: String?
        )

        data class Screen(
                val resolution: String,
                val dpi: Int,
                val size: Int,
                val screenInches: Double
        )
    }
}