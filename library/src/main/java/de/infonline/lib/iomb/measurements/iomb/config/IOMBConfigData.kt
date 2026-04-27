package de.infonline.lib.iomb.measurements.iomb.config

import androidx.annotation.Keep
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import de.infonline.lib.iomb.events.IOLBaseEvent
import de.infonline.lib.iomb.events.internal.IOLApplicationEventPrivate
import de.infonline.lib.iomb.measurements.common.config.ConfigData
import de.infonline.lib.iomb.measurements.common.config.ConfigData.Companion.WILDCARD
import de.infonline.lib.iomb.measurements.iomb.IOMBConfig
import java.time.Instant

internal data class IOMBConfigData(
    override val localConfig: IOMBConfig = IOMBConfig(),
    override val remoteConfig: Remote = Remote()
) : ConfigData<IOMBConfig, IOMBConfigData.Remote> {

    @Keep
    @JsonClass(generateAdapter = true)
    data class Remote(
        override val configType: ConfigData.ConfigType = ConfigData.ConfigType.IOMB,

        @Json(name = "cache") override val cache: Cache? = Cache(),
        @Json(name = "formatVersion") override val formatVersion: String? = "1.0.0",
        @Json(name = "offlineMode") override val offlineMode: Boolean = false,
        @Json(name = "secureMode") override val secureMode: Boolean = false,
        @Json(name = "sendAutoEvents") override val sendAutoEvents: SendAutoEvents? = SendAutoEvents(),
        @Json(name = "specialParameters") override val specialParameters: SpecialParameters? = SpecialParameters(),
        @Json(name = "activeEvents") override val activeEvents: Map<String, Map<String, ActiveEvent>> = IOMBEvents.data

    ) : ConfigData.Remote {

        override fun copyInstance(
            createdAt: Instant,
            configTTLSeconds: Long?
        ): Remote = this

        override fun shouldUpdate(): Boolean {
            return false
        }

        @Keep
        @JsonClass(generateAdapter = true)
        data class Cache(
            @Json(name = "maxBulkEvents") override val maxBulkEvents: Int = 1,
            @Json(name = "maxBulkEventsAuditMode") override val maxBulkEventsAuditMode: Int = 1,
            @Json(name = "ttl") override val ttl: Long? = null,
        ) : ConfigData.Remote.Cache

        @Keep
        @JsonClass(generateAdapter = true)
        data class SendAutoEvents(
            @Json(name = "audit") override val audit: Boolean = true,
            @Json(name = "regular") override val regular: Boolean = false
        ) : ConfigData.Remote.SendAutoEvents

        @Keep
        @JsonClass(generateAdapter = true)
        data class SpecialParameters(
            @Json(name = "comment") override val comment: Boolean = true
        ) : ConfigData.Remote.SpecialParameters

        @Keep
        @JsonClass(generateAdapter = true)
        data class ActiveEvent(
            @Json(name = "audit") override val audit: Boolean = true,
            @Json(name = "pi") override val pi: Boolean = false,
            @Json(name = "regular") override val regular: Boolean = true
        ) : ConfigData.Remote.ActiveEvent
    }
}