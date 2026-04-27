package de.infonline.lib.iomb

import android.webkit.WebView
import de.infonline.lib.iomb.measurements.common.IOMbHybridBridgeJSInterface
import de.infonline.lib.iomb.measurements.common.ProofToken
import de.infonline.lib.iomb.measurements.iomb.IOMBMeasurement
import de.infonline.lib.iomb.util.IOLLog
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.disposables.Disposable
import io.reactivex.rxjava3.schedulers.Schedulers

class IOMBHybridBridge {

    private val hybridBridgeName = "IOMbHybridBridge"
    private lateinit var hybridBridge: IOMbHybridBridgeJSInterface
    private lateinit var proofToken: ProofToken
    private var lastEventDisposable: Disposable? = null


    fun configureWebViewForIOMbHybridMeasurement(webView: WebView) {

        proofToken = ProofToken(webView.context)

        (IOMB.getAllBlocking()
            .mapNotNull { it.value }
            .find { it is IOMBMeasurement } as? IOMBMeasurement)?.let { iombMeasurement ->

            hybridBridge = IOMbHybridBridgeJSInterface(
                webView,
                iombMeasurement.setup.offerIdentifier,
                iombMeasurement.setup.baseUrl,
                proofToken
            )

            lastEventDisposable = iombMeasurement.eventProcessor.lastEvent
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.computation())
                .subscribe({
                    hybridBridge.apply {
                        code = it.category
                        comment = it.comment
                    }
                }, {
                    IOLLog.tag(TAG, public = false)
                        .d("Error while subscribing to latest event")
                })

            webView.addJavascriptInterface(hybridBridge, hybridBridgeName)
        }
    }

    fun destroy() {
        lastEventDisposable?.dispose()
    }

    companion object {
        private const val TAG = "IOMbHybridBridgeHelper"
    }
}