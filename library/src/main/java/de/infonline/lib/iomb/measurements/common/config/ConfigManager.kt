package de.infonline.lib.iomb.measurements.common.config

import de.infonline.lib.iomb.measurements.common.dispatch.EventDispatcher
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Single

internal interface ConfigManager<
        ConfT : ConfigData<*, *>,
        ResponseT : EventDispatcher.Response
        > {

    fun configuration(): Observable<out ConfT>

    fun tryUpdateRemoteConfig(): Single<out ConfigData.Remote?>

    fun checkRemoteConfig(response: ResponseT): Single<out ConfigData.Remote>

    fun updateLocalConfig(action: (LocalConfiguration) -> LocalConfiguration): Single<out ConfT>

    fun release()

    enum class Status {
        OK,
        TTL_EXPIRED,
        USE_FALLBACK,
        FORCE_UPDATE
    }

}