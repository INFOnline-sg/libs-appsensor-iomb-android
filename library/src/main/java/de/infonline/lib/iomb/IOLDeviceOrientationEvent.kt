package de.infonline.lib.iomb

import android.content.Context
import android.content.res.Configuration
import de.infonline.lib.iomb.events.IOLBaseEvent
import de.infonline.lib.iomb.util.extensions.mutate


class IOLDeviceOrientationEvent @JvmOverloads constructor(
    type: IOLDeviceOrientationEventType,
    category: String? = null,
    comment: String? = null,
    customParams: Map<String, Any>? = null
) : IOLEvent(
        identifier = ID,
        state = type.state,
        category = category,
        comment = comment,
        customParams = customParams
) {
    enum class IOLDeviceOrientationEventType(override val state: String) : IOLBaseEvent.State {
        Changed("changed");
    }

    enum class DeviceOrientationType(val state: Int) {
        Unknown(0),
        Portrait(1),
        Landscape(2),
        Square(3);
    }

    override fun buildParameters(context: Context): Map<String, Any> {
        val basicevent = super.buildParameters(context)

        val orientationType = when (context.applicationContext.resources.configuration.orientation) {
            Configuration.ORIENTATION_LANDSCAPE -> DeviceOrientationType.Landscape
            Configuration.ORIENTATION_PORTRAIT -> DeviceOrientationType.Portrait
            Configuration.ORIENTATION_SQUARE -> DeviceOrientationType.Square
            else -> DeviceOrientationType.Unknown
        }
        return basicevent.mutate {
            this["orientation"] = orientationType.state.toString()
        }
    }

    companion object {
        const val ID = "deviceOrientation"

        @Deprecated(
                message = "Use ID property",
                replaceWith = ReplaceWith(expression = "IOLDeviceOrientationEvent.ID"),
                level = DeprecationLevel.WARNING
        )
        const val eventIdentifier = ID
    }
}