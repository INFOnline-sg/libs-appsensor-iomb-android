package de.infonline.lib.iomb.measurements

import android.content.Context
import androidx.annotation.Keep
import com.squareup.moshi.Json
import de.infonline.lib.iomb.events.IOLBaseEvent
import de.infonline.lib.iomb.measurements.common.config.LocalConfiguration
import de.infonline.lib.iomb.measurements.iomb.IOMBSetup
import de.infonline.lib.iomb.util.IOLLog.tag
import de.infonline.lib.iomb.util.serialization.MyPolymorphicJsonAdapterFactory
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Observable
import java.io.File

interface Measurement {

    /**
     * The setup that this **[Measurement]** instance was created with.
     * This does not change.
     */
    val setup: Setup

    /**
     *  An Observable for the current **[LocalConfiguration]**.
     *  Changes after calling **[updateConfig]**
     */
    val localConfig: Observable<LocalConfiguration>

    /**
     * Current **[RemoteConfigurationInfo]**
     * The library automatically checks for updates when necessary.
     */
    val remoteConfigInfo: Observable<RemoteConfigurationInfo>

    /**
     * The multiIdentifier used in **[android.webkit.WebView]** instances to identify this device
     */
    val multiIdentifier: Observable<MultiIdentifier>

    /**
     * Returns if the automatic TCF processing is enabled (false is you are not using SZM)
     */
    fun isAutomaticProcessEnabled(): Boolean = false

    /**
     * Returns the currently used consent string (null if you are not using SZM)
     */
    fun getConsent(): String? = null

    /**
     * Set the custom consent (always ignored if you are not using SZM)
     */
    fun setCustomConsent(consent: String?) {
        tag("Consent").i("TFC2.0 Consent Data will be ignored as it is not relevant for this measurement system.")
    }

    /**
     * Allows you to make changes to the local configuration.
     * i.e. Change the **[PrivacySetting]**
     * You are passed the current configuration and can then make changes to it.
     * You'll likely want to cast the configuration object, to the same type you passed when creating the **[Measurement]**
     *
     * **[Measurement.Type.SZM]** -> **[de.infonline.lib.measurements.szm.SZMConfig]**
     * **[Measurement.Type.OEWA]** -> **[de.infonline.lib.measurements.oewa.OEWAConfig]**
     * **[Measurement.Type.ACSAM]** -> **[de.infonline.lib.measurements.acsam.ACSAMConfig]**
     *
     * Note: If you change the identifiers, the update will be ignored. To change identifiers you have to delete and recreate.
     */
    fun updateConfig(update: (LocalConfiguration) -> LocalConfiguration)

    /**
     * Submits an event, e.g. **[de.infonline.lib.IOLViewEvent]** for processing.
     * The event will be asyncronously processed, possibly cached, and then dispatched.
     */
    fun logEvent(event: IOLBaseEvent)

    /**
     * Asks to dispatch the currently queued events. Otherwise dispatch attempts are triggered by network or cache changes.
     * If you set **[forced]**, then any minimum event count is ignored, and dispatch is attempted, if there is at least 1 event.
     */
    fun dispatch(forced: Boolean)

    /**
     * If this measurement is released, you shouldn no longer use it.
     * Any operations will, e.g. **[logEvent]** will be ignored.
     * Becomes true after calling **[release]**
     */
    val isReleased: Boolean

    /**
     * Stops this **[Measurement]** instance. After the **[Completable]** completes, **[isReleased]** will be true.
     * Releasing stops queue processing.
     * Cached events and configs will be restored if you call **[de.infonline.lib.IOL.createBlocking]** with the same arguments.
     */
    fun release(): Completable

    @Keep
    enum class Type constructor(
        val value: String
    ) {
        @Json(name = "szm") SZM("szm"),
        @Json(name = "oewa") OEWA("oewa"),
        @Json(name = "acsam") ACSAM("acsam"),
        @Json(name = "iomb") IOMB("iomb");

        val defaultIdentifier = "default"
        val defaultKey = "${this.value}.$defaultIdentifier"
    }


    interface Setup {
        val offerIdentifier: String
        val hybridIdentifier: String?
        val type: Type
        val identifier: String
        val eventServerUrl: String
        val configServerUrl: String?
        val customerData: String?

        fun logTag(tag: String) = tag

        val measurementKey: String
            get() = "${type.value}.$identifier"

        // TODO test
        fun getDataDir(context: Context): File {
            return File(File(context.filesDir, BASE_LIB_DIR), measurementKey)
        }

        companion object {
            internal val MOSHI_FACTORY: MyPolymorphicJsonAdapterFactory<Setup> =
                MyPolymorphicJsonAdapterFactory.of(Setup::class.java, "type")
                    .withSubtype(IOMBSetup::class.java, Type.IOMB.value)
                    .skipLabelSerialization()
            internal const val BASE_LIB_DIR = "infonline"
        }
    }

}