package de.infonline.lib.iomb.measurements.common

import androidx.annotation.Keep
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import de.infonline.lib.iomb.measurements.common.network.NetworkMonitor

@Keep
@JsonClass(generateAdapter = true)
internal data class ClientInfoLegacyMapping(
    @Json(name = "uuids") val uuids: DeviceIdentifiers? = null,
    @Json(name = "screen") val screen: Screen,
    @Json(name = "language") val language: String,
    @Json(name = "country") val country: String,
    @Json(name = "carrier") val carrier: String? = null,
    @Json(name = "network") val network: Int? = null,
    @Json(name = "osIdentifier") val osIdentifier: String = "android",
    @Json(name = "osVersion") val osVersion: String,
    @Json(name = "platform") val platform: String,
    @Json(name = "deviceName") val deviceName: String? = null
) {

    @Keep
    @JsonClass(generateAdapter = true)
    internal data class DeviceIdentifiers(
            @Json(name = "installationId") val installationId: String?,
            @Json(name = "installationIdSHA256") val installationIdSHA256: String?,
            @Json(name = "advertisingIdentifier") val advertisingIdentifier: String?,
            @Json(name = "advertisingIdentifierSHA256") val advertisingIdentifierSHA256: String?,
            @Json(name = "androidId") val androidId: String?,
            @Json(name = "androidIdSHA256") val androidIdSHA256: String?
    )

    @Keep
    @JsonClass(generateAdapter = true)
    internal data class Screen(
            @Json(name = "resolution") val resolution: String,
            @Json(name = "dpi") val dpi: Int,
            @Json(name = "size") val size: Int
    )
}

internal fun ClientInfoBuilder.InfoInternal.toLegacyMapping() = ClientInfoLegacyMapping(
        uuids = this.uuids?.toLegacyMapping(),
        screen = this.screen.toLegacyMapping(),
        language = this.locale.language,
        country = this.locale.country,
        carrier = this.carrier.carriers.firstOrNull()?.name ?: "",
        network = when (this.network) {
            NetworkMonitor.NetworkType.NO_PERMISSION, NetworkMonitor.NetworkType.NO_NETWORK -> null
            else -> this.network.typeValue
        },
        osIdentifier = this.osIdentifier,
        osVersion = this.osVersion,
        platform = this.platform,
        deviceName = this.deviceName
)

internal fun ClientInfoBuilder.InfoInternal.DeviceIdentifiers.toLegacyMapping() =
    ClientInfoLegacyMapping.DeviceIdentifiers(
        installationId = this.installationId,
        installationIdSHA256 = this.installationIdSHA256,
        advertisingIdentifier = this.advertisingIdentifier,
        advertisingIdentifierSHA256 = this.advertisingIdentifierSHA256,
        androidId = this.androidId,
        androidIdSHA256 = this.androidIdSHA256
    )

internal fun ClientInfoBuilder.InfoInternal.Screen.toLegacyMapping() =
    ClientInfoLegacyMapping.Screen(
        resolution = this.resolution,
        dpi = this.dpi,
        size = this.size
    )