package de.infonline.lib.iomb.util

import java.math.BigDecimal
import java.time.Instant
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TimeStamper @Inject constructor() {

    val currentUTCInSeconds: Double
        get() = BigDecimal(nowUTC.toEpochMilli() / 1000.0).setScale(3, BigDecimal.ROUND_FLOOR).toDouble()

    val localTimeZone: TimeZone
        get() = TimeZone.getDefault()

    val nowUTC: Instant
        get() = Instant.now()

}