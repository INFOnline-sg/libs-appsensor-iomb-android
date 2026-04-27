package de.infonline.lib.iomb.measurements.common.config

import androidx.annotation.Keep
import de.infonline.lib.iomb.events.IOLBaseEvent
import java.time.Instant

internal interface ConfigData<LocalT : LocalConfiguration, RemoteT : ConfigData.Remote> {
    val localConfig: LocalT
    val remoteConfig: RemoteT

    fun isEventAllowed(event: IOLBaseEvent): Boolean {
        val allowedEvents = remoteConfig.configuration?.activeEvents
        if (allowedEvents?.containsKey(event.identifier) == false) return false
        return allowedEvents?.getValue(event.identifier)?.any { it == event.state || it == WILDCARD } ?: false
    }

    fun isPIEvent(event: IOLBaseEvent) = getActiveEvent(event)?.pi ?: false

    fun isMeasuredRegular(event: IOLBaseEvent) = getActiveEvent(event)?.regular ?: false

    fun isMeasuredAudit(event: IOLBaseEvent) = getActiveEvent(event)?.audit ?: false

    private fun getActiveEvent(event: IOLBaseEvent): Remote.ActiveEvent? {
        if(remoteConfig.activeEvents == null) return null

        val state = if (remoteConfig.activeEvents!![event.identifier]?.containsKey(ConfigData.WILDCARD) == true) ConfigData.WILDCARD else event.state
        remoteConfig.activeEvents!![event.identifier]?.forEach { (s, activeEvent) ->
            if (s.lowercase() == state?.lowercase()) return activeEvent
        }
        return null
    }

    @Keep
    enum class ConfigType { LEGACY, ACSAM, IOMB }

    interface Remote {
        val configType: ConfigType
        val formatVersion: String?
            get() = null
        val configTTLSeconds: Long?
            get() = null
        val createdAt: Instant?
            get() = null
        val configuration: Configuration?
            get() = null
        val activeEvents: Map<String, Map<String, ActiveEvent>>?
            get() = null
        val cache: Cache?
            get() = null
        val config: Config?
            get() = null
        val configVersion: Long?
            get() = null
        val offlineMode: Boolean?
            get() = null
        val publicRsa: String?
            get() = null
        val secureMode: Boolean?
            get() = null
        val sendAutoEvents: SendAutoEvents?
            get() = null
        val sessionTimeout: Long?
            get() = null
        val viewtimeTimeout: Long?
            get() = null
        val specialParameters: SpecialParameters?
            get() = null

        fun getConfigVersion(): String {
            return (configVersion ?: configuration?.configVersion ?: "Invalid").toString()
        }

        fun getTTLSeconds(): Long {
            return (if (config?.ttl != null) config!!.ttl / 1000 else configTTLSeconds) ?: 86400L
        }

        fun getBatchSize(): Int {
            return cache?.maxBulkEvents ?: configuration?.minBatchSize ?: 10
        }

        fun shouldUpdate(): Boolean

        val isExpired: Boolean
            get() = if(expirationDate == null) false else Instant.now().isAfter(expirationDate)

        val expirationDate: Instant
            get() = createdAt?.plusSeconds(getTTLSeconds()) ?: Instant.MAX

        fun copyInstance(
            createdAt: Instant = this.createdAt ?: Instant.now(),
            configTTLSeconds: Long? = getTTLSeconds()
        ): Remote

        interface ActiveEvent {
            val audit: Boolean
            val pi: Boolean
            val regular: Boolean
        }

        interface SendAutoEvents {
            val regular: Boolean
            val audit: Boolean
        }

        interface Cache {
            val maxBulkEvents: Int
            val maxBulkEventsAuditMode: Int
            val ttl: Long?
        }

        interface Config {
            val ttl: Long
        }

        interface SpecialParameters {
            val comment: Boolean
        }

        interface Configuration {
            val configVersion: String
            val hashingType: HashingType
            val minBatchSize: Int
            val maxCacheAgeSeconds: Long
            val activeEvents: Map<String, List<String>>
            val tcf: TCF?

            interface TCF {
                val vendors: List<Int>
                val automaticProcess: Boolean
            }

            enum class HashingType(
                val typeValue: Int
            ) {
                MD5(0),
                MD5_SHA256(1),
                SHA256(2);

                companion object {
                    fun fromInt(type: Int): HashingType = values().firstOrNull { it.typeValue == type } ?: MD5
                }
            }
        }
    }

    companion object {
        const val WILDCARD = "*"
    }
}