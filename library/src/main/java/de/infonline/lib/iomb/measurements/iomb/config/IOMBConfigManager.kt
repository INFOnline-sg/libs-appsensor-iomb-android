package de.infonline.lib.iomb.measurements.iomb.config

import de.infonline.lib.iomb.measurements.Measurement
import de.infonline.lib.iomb.measurements.common.config.ConfigData
import de.infonline.lib.iomb.measurements.common.config.ConfigManager
import de.infonline.lib.iomb.measurements.common.config.LocalConfiguration
import de.infonline.lib.iomb.measurements.common.dispatch.EventDispatcher
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Single
import javax.inject.Inject

internal class IOMBConfigManager @Inject internal constructor(
    setup: Measurement.Setup
) : ConfigManager<IOMBConfigData, EventDispatcher.Response> {

    val tag: String = setup.logTag("IOMBConfigManager")

    override fun configuration(): Observable<out IOMBConfigData> {
        return Observable.just(IOMBConfigData())
    }

    override fun tryUpdateRemoteConfig(): Single<out ConfigData.Remote?> {
        return Single.just(IOMBConfigData.Remote())
    }

    override fun checkRemoteConfig(response: EventDispatcher.Response): Single<out ConfigData.Remote> {
        return Single.just(IOMBConfigData.Remote())
    }

    override fun updateLocalConfig(action: (LocalConfiguration) -> LocalConfiguration): Single<out IOMBConfigData> {
        return Single.just(IOMBConfigData())
    }

    override fun release() {
    }
}