package de.infonline.lib.iomb.measurements.common.config

import de.infonline.lib.iomb.measurements.Measurement

interface LocalConfiguration {
    val type: Measurement.Type

    companion object {
        const val MAX_DATA_LENGTH = 255
    }
}