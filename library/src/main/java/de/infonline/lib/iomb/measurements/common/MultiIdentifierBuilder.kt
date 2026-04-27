package de.infonline.lib.iomb.measurements.common

import androidx.annotation.Keep
import com.squareup.moshi.JsonClass
import com.squareup.moshi.Moshi
import de.infonline.lib.iomb.measurements.MultiIdentifier
import de.infonline.lib.iomb.measurements.common.config.ConfigData
import de.infonline.lib.iomb.util.IOLLog
import de.infonline.lib.iomb.util.PerMeasurement
import io.reactivex.rxjava3.core.Scheduler
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.kotlin.Singles
import javax.inject.Inject

@PerMeasurement
internal class MultiIdentifierBuilder @Inject constructor(
    private val moshi: Moshi,
    private val libraryInfoBuilder: LibraryInfoBuilder,
    private val clientInfoBuilder: ClientInfoBuilder,
    private val scheduler: Scheduler
) {

    private val jsonAdapter by lazy { moshi.adapter(InternalMapper::class.java) }

    // TODO test
    fun build(configData: ConfigData<*, *>): Single<Identifier> = Singles
            .zip(
                    clientInfoBuilder.build(configData),
                    libraryInfoBuilder.build(configData)
            )
            .subscribeOn(scheduler)
            .map { (clientInfo, libraryInfo) ->
                val identifierData = InternalMapper(
                        library = libraryInfo,
                        client = clientInfo.toLegacyMapping()
                )
                Identifier(jsonAdapter.toJson(identifierData))
            }
            .onErrorReturn {
                IOLLog.tag(TAG).e(it, "Failed to generate MultiIdentifier for %s", configData)
                Identifier()
            }
            .doOnSuccess { IOLLog.tag(TAG).i("Generated MultiIdentifier: %s", it) }

    @Keep
    @JsonClass(generateAdapter = true)
    data class InternalMapper(
        val library: LibraryInfoBuilder.Info,
        val client: ClientInfoLegacyMapping
    )

    data class Identifier(
            val rawJson: String = "{}"
    ) : MultiIdentifier {
        override val javaScriptString: String by lazy { rawJson.replace("\"".toRegex(), "\\\\\"") }
    }


    companion object {
        private const val TAG = "MultiIdentifierBuilder"
    }
}