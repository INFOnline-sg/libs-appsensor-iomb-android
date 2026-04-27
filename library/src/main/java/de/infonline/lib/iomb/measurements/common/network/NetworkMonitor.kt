package de.infonline.lib.iomb.measurements.common.network

import android.Manifest
import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import androidx.core.content.ContextCompat
import com.squareup.moshi.FromJson
import com.squareup.moshi.ToJson
import de.infonline.lib.iomb.core.IOLibCoreScheduler
import de.infonline.lib.iomb.util.IOLLog
import de.infonline.lib.iomb.util.rx.filterEqual
import de.infonline.lib.iomb.util.rx.latest
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Scheduler
import io.reactivex.rxjava3.core.Single
import javax.inject.Inject
import javax.inject.Singleton


@Suppress("DEPRECATION")
@Singleton
internal class NetworkMonitor @Inject constructor(
        private val context: Context,
        @IOLibCoreScheduler private val coreScheduler: Scheduler
) {

    private val connectivityManager by lazy { context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager }

    val networkState: Observable<State> = Observable
            .create<State> { emitter ->
                val receiver = object : BroadcastReceiver() {
                    override fun onReceive(context: Context, intent: Intent) {
                        emitter.onNext(buildState())
                    }
                }

                IOLLog.tag(TAG).v("Registering CONNECTIVITY_ACTION receiver.")
                context.registerReceiver(receiver, IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION))
                emitter.setCancellable {
                    context.unregisterReceiver(receiver)
                    emitter.onComplete()
                }

                emitter.onNext(buildState())
            }
            .subscribeOn(coreScheduler)
            .filterEqual()
            .doOnSubscribe { IOLLog.tag(TAG).v("Starting network state monitor.") }
            .doOnNext { IOLLog.tag(TAG).v("New network state: %s", it) }
            .doOnError { IOLLog.tag(TAG).e(it, "Network state updated failed.") }
            .doFinally { IOLLog.tag(TAG).d("Stopping network state monitor, last subscriber disposed.") }
            .onErrorReturnItem(State(isOnline = true, networkType = NetworkType.NO_PERMISSION))
            .replay(1)
            .refCount()

    val isOnline: Single<Boolean>
        get() = networkState.map { it.isOnline }.latest()

    val networkType: Single<NetworkType>
        get() = networkState.map { it.networkType }.latest()

    private fun buildState(): State = State(
            isOnline = connectivityManager.isOnline(),
            networkType = connectivityManager.getNetworkType()
    )

    @SuppressLint("MissingPermission")
    private fun ConnectivityManager.getNetworkType(): NetworkType = if (!hasNnetworkStatePermission) {
        NetworkType.NO_PERMISSION
    } else {
        val netInfo = this.activeNetworkInfo
        if (netInfo == null || !netInfo.isConnected) {
            //in flightmode (netinfo == null)
            NetworkType.NO_NETWORK
        } else {
            NetworkType.fromInt(netInfo.type)
        }
    }

    @SuppressLint("MissingPermission")
    private fun ConnectivityManager.isOnline(): Boolean = if (!hasNnetworkStatePermission) {
        IOLLog.tag(TAG).v("ACCESS_NETWORK_STATE permission are unavailable, assuming isOnline=true")
        true
    } else {
        val netInfo = this.activeNetworkInfo
        netInfo != null && netInfo.isConnected
    }

    private val hasNnetworkStatePermission: Boolean
        get() = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_NETWORK_STATE) == PackageManager.PERMISSION_GRANTED

    data class NetworkType(
            val typeValue: Int
    ) {

        companion object {
            val NO_PERMISSION = NetworkType(Int.MIN_VALUE)
            val NO_NETWORK = NetworkType(0)
            val GSM = NetworkType(1)
            val WIFI = NetworkType(2)

            fun fromInt(connectivityType: Int): NetworkType = when (connectivityType) {
                -1 -> NO_NETWORK
                Integer.MIN_VALUE -> NO_PERMISSION
                ConnectivityManager.TYPE_MOBILE -> GSM
                ConnectivityManager.TYPE_WIFI -> WIFI
                else -> NetworkType(connectivityType)
            }
        }

    }

    data class State(
            val isOnline: Boolean,
            val networkType: NetworkType
    )

    internal class NetworkTypeAdapter {
        @ToJson
        fun toJson(networkType: NetworkType): Int = networkType.typeValue

        @FromJson
        fun toJson(typeValue: Int): NetworkType = NetworkType.fromInt(typeValue)
    }


    companion object {
        private const val TAG = "NetworkMonitor"
    }
}