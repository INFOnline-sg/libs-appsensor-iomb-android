package de.infonline.lib.iomb.measurements.common.dispatch

import de.infonline.lib.iomb.measurements.common.config.ConfigData
import de.infonline.lib.iomb.measurements.common.config.ConfigManager
import de.infonline.lib.iomb.measurements.common.processor.EventProcessor
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Single

internal interface EventDispatcher<
        ConfigDataT : ConfigData<*, *>,
        RequestT : EventDispatcher.Request,
        ResponseT : EventDispatcher.Response> {

    fun dispatch(request: RequestT, config: ConfigDataT): Single<out ResponseT>

    fun release(): Completable

    interface Request {
        val events: List<EventProcessor.ProcessedEvent>
    }

    interface Response {
        val configStatusCode: ConfigManager.Status
    }

}