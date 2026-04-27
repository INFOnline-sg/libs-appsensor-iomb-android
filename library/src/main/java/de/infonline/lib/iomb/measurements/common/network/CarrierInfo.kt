package de.infonline.lib.iomb.measurements.common.network

import android.Manifest
import android.annotation.SuppressLint
import android.annotation.TargetApi
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.telephony.SubscriptionManager
import android.telephony.TelephonyManager
import androidx.core.content.ContextCompat
import dagger.Reusable
import de.infonline.lib.iomb.core.IOLibCoreScheduler
import de.infonline.lib.iomb.util.BuildVersionWrap
import de.infonline.lib.iomb.util.IOLLog
import io.reactivex.rxjava3.core.Scheduler
import io.reactivex.rxjava3.core.Single
import javax.inject.Inject

@Reusable
internal class CarrierInfo @Inject constructor(
        private val context: Context,
        @IOLibCoreScheduler private val coreScheduler: Scheduler
) {

    private val telephonyManager by lazy {
        (context.getSystemService(Context.TELEPHONY_SERVICE) as? TelephonyManager).also {
            if (it == null) IOLLog.tag(TAG).w("TelephonyManager was unavailable.")
        }
    }

    val info: Single<Info> = Single
            .fromCallable {
                val carriers = mutableListOf<Info.Carrier>()

                carriers.addAll(getMultiSimInfos())

                if (carriers.isEmpty()) {
                    getSimOperator()?.let { carriers.add(it) }
                }

                if (carriers.isEmpty()) {
                    getNetworkOperator()?.let { carriers.add(it) }
                }

                return@fromCallable Info(carriers = carriers)
            }
            .subscribeOn(coreScheduler)
            .onErrorReturn {
                IOLLog.tag(TAG).e(it, "Failed to determine carrier name.")
                Info()
            }

    @SuppressLint("MissingPermission")
    @TargetApi(Build.VERSION_CODES.LOLLIPOP_MR1)
    private fun getMultiSimInfos(): List<Info.Carrier> = when {
        BuildVersionWrap.SDK_VERSION < Build.VERSION_CODES.LOLLIPOP_MR1 -> {
            emptyList()
        }
        hasPermission(Manifest.permission.READ_PHONE_STATE) -> {
            try {
                context.getSystemService(Context.TELEPHONY_SUBSCRIPTION_SERVICE)?.let { sm ->
                    sm as SubscriptionManager

                    sm.activeSubscriptionInfoList.filterNot { it.carrierName.isNullOrBlank() }.map {
                        Info.Carrier(name = it.carrierName.toString())
                    }
                } ?: emptyList()
            } catch (e: Exception) {
                IOLLog.tag(TAG).w("Failed to determine multisim infos.")
                emptyList<Info.Carrier>()
            }
        }
        else -> {
            emptyList()
        }
    }

    private fun getSimOperator(): Info.Carrier? = try {
        telephonyManager?.let {
            if (it.simOperatorName.isNullOrBlank()) null
            else Info.Carrier(name = it.simOperatorName)
        }
    } catch (e: SecurityException) {
        // Some devices need READ_PHONE_STATE Permission to call getSimOperatorName()
        IOLLog.tag(TAG).w(e, "Error while reading carrier via simOperatorName.")
        null
    }

    private fun getNetworkOperator(): Info.Carrier? = try {
        telephonyManager?.let {
            if (it.networkOperatorName.isNullOrBlank()) null
            else Info.Carrier(name = it.networkOperatorName)
        }
    } catch (e: SecurityException) {
        // Because some devices need READ_PHONE_STATE Permission to call getSimOperatorName() we assume the same for getNetworkOperatorName()
        IOLLog.tag(TAG).w(e, "Error while reading carrier via networkOperatorName.")
        null
    }

    private fun hasPermission(permission: String): Boolean =
            ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED

    data class Info(val carriers: List<Carrier> = emptyList()) {
        data class Carrier(val name: String)
    }

    companion object {
        private const val TAG = "CarrierInfo"
    }
}