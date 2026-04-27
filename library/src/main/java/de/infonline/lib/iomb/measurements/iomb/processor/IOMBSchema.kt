package de.infonline.lib.iomb.measurements.iomb.processor

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
internal data class IOMBSchema(
    @Json(name = "di") val deviceInformation: DeviceInformation,
    @Json(name = "si") val siteInformation: SiteInformation,
    @Json(name = "sv") val schemaVersion: String = "1.0.0",
    @Json(name = "ti") val technicalInformation: TechnicalInformation
) {
    @JsonClass(generateAdapter = true)
    data class DeviceInformation(
        // "Detected platform operating system"
        @Json(name = "pn") val osIdentifier: String = "android",
        // "Detected platform operating system", e.g. 8.1.0
        @Json(name = "pv") val osVersion: String,
        // "Well formed token identifier for auditing purposes", e.g. "iotest5ee391701234"
        @Json(name = "to") val deviceName: String? // Device name for ProofToken
    )

    @JsonClass(generateAdapter = true)
    data class SiteInformation(
            // For which country should the request being measured, "examples": ["de", "at"]
            @Json(name = "cn") val country: String = "de",
            // "An extra field for the customer to send any other text based data to the measurement endpoint"
            @Json(name = "co") val comment: String?,
            // "Identifier for one or multiple web section(s)", "examples": ["foo", "bar"]
            @Json(name = "cp") val contentCode: String? = "Leercode_nichtzuordnungsfaehig",
            // Distribution Channel
            @Json(name = "dc") val distributionChannel: String = "app",
            // "The event which has occured on the client device and should be transmitted as part of the measured data",
            // "examples": ["view.appeared"]
            @Json(name = "ev") val event: String?,
            // "The origin of the information or which sensor type measured the usage." Const for App.
            @Json(name = "pt") val pixelType: String = "ap",
            // "The site identifier",  "examples": ["infonlin"]
            @Json(name = "st") val site: String = "dummy"
    )

    @JsonClass(generateAdapter = true)
    data class TechnicalInformation(
        @Json(name = "cs") var checksumMD5: String? = null,
        @Json(name = "dm") val debugModus: Boolean = false,
        @Json(name = "it") val integrationType: String = "sa",
        @Json(name = "vr") val sensorSDKVersion: String
    )
}