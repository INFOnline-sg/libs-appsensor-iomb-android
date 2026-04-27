package de.infonline.lib.iomb.util

import java.time.Instant
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.*
import java.util.concurrent.TimeUnit

internal class TimestampFormatter {

    fun toHumanReadable(epochTime: Long): String {
        return try {
            val instant: Instant = Instant.ofEpochSecond(TimeUnit.MILLISECONDS.toSeconds(epochTime))
            val zoneId: ZoneId = ZoneId.of(TimeZone.getDefault().id)
            val zonedDateTime: ZonedDateTime = ZonedDateTime.ofInstant(instant, zoneId)
            dateTimeFormatter.format(zonedDateTime)
        } catch (e: Exception) {
            ""
        }
    }

    companion object {
        const val DATE_FORMAT = "dd.MM.yyyy HH:mm:ss Z"

        val dateTimeFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern(DATE_FORMAT)
    }
}