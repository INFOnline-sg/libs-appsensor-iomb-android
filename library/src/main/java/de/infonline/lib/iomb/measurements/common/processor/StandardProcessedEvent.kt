package de.infonline.lib.iomb.measurements.common.processor

import androidx.annotation.Keep
import com.squareup.moshi.JsonClass
import java.time.Instant

@Keep
@JsonClass(generateAdapter = true)
data class StandardProcessedEvent(
        override val createdAt: Instant,
        override val persist: Boolean = false,
        override val event: Map<String, Any>
) : EventProcessor.ProcessedEvent