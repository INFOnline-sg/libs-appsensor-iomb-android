package de.infonline.lib.iomb

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.net.http.SslError
import android.os.Build
import android.os.Message
import android.util.AttributeSet
import android.view.KeyEvent
import android.webkit.*
import de.infonline.lib.iomb.events.internal.IOLWebViewEventPrivate
import de.infonline.lib.iomb.util.IOLLog

/**
 * IOLWebView enables tracking in hybrid apps.
 *
 *
 * If your app displays web sites that have the INFOnline web tracking enabled, you can use this webview to pass information
 * about the device to the web tracking service in order identify your users across your app and the web sites.
 *
 *
 * In order to work properly with the tracked websites, IOLWebView
 *
 *  * has JavaScript enabled (If you disable JavaScript the tracking logic will not work).
 *  * has DOM-Storage enabled (If you disable DOM-Storage the tracking logic will not work)
 *
 */
class IOLWebView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = android.R.attr.webViewStyle
) : WebView(context, attrs, defStyle) {

    private lateinit var webViewClient: IOLWebViewClientBase
    private lateinit var ioMbHybridBridge: IOMBHybridBridge

    init {

        //fix error in layout preview
        if (!isInEditMode) {
            @SuppressLint("SetJavaScriptEnabled")
            settings.javaScriptEnabled = true
            settings.domStorageEnabled = true

            ioMbHybridBridge = IOMBHybridBridge().apply {
                configureWebViewForIOMbHybridMeasurement(this@IOLWebView)
            }

            webViewClient = when {
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.N -> {
                    IOLLog.tag(TAG, public = true).d("IOLWebView using IOLWebViewClientV24")
                    IOLWebViewClientV24(context)
                }
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP -> {
                    IOLLog.tag(TAG, public = true).d("IOLWebView using IOLWebViewClientV21")
                    IOLWebViewClientV21(context)
                }
                else -> {
                    IOLLog.tag(TAG, public = true).d("IOLWebView using IOLWebViewClientBase")
                    IOLWebViewClientBase(context)
                }
            }
            super.setWebViewClient(webViewClient)
            val initEvent =
                IOLWebViewEventPrivate(IOLWebViewEventPrivate.IOLWebViewEventPrivateType.Init)
            IOMB.getAllBlocking().mapNotNull { it.value }.forEach {
                it.logEvent(initEvent)
            }
            IOLLog.tag(TAG, public = true).d("IOLWebView initialized.")
        }
    }

    override fun setWebViewClient(userWebViewClient: WebViewClient) {
        webViewClient?.userWebViewClient = userWebViewClient
    }

    override fun destroy() {
        super.destroy()
        ioMbHybridBridge.destroy()
    }

    // TODO it's unclear which arguments from `WebViewClient` can be null, currently more than necessary is annotated with ?
    internal open class IOLWebViewClientBase(
        context: Context?
    ) : WebViewClient() {

        internal var userWebViewClient: WebViewClient? = null

        override fun onPageFinished(view: WebView, url: String) {
            userWebViewClient?.onPageFinished(view, url)
        }

        override fun doUpdateVisitedHistory(view: WebView, url: String, isReload: Boolean) {
            userWebViewClient?.doUpdateVisitedHistory(view, url, isReload)
        }

        override fun onFormResubmission(view: WebView, dontResend: Message?, resend: Message?) {
            userWebViewClient?.onFormResubmission(view, dontResend, resend)
        }

        override fun onLoadResource(view: WebView, url: String) {
            userWebViewClient?.onLoadResource(view, url)
        }

        override fun onPageStarted(view: WebView, url: String, favicon: Bitmap?) {
            userWebViewClient?.onPageStarted(view, url, favicon)
        }

        override fun onReceivedError(
            view: WebView,
            errorCode: Int,
            description: String?,
            failingUrl: String?
        ) {
            userWebViewClient?.onReceivedError(view, errorCode, description, failingUrl)
        }

        override fun onReceivedHttpAuthRequest(
            view: WebView,
            handler: HttpAuthHandler?,
            host: String?,
            realm: String?
        ) {
            userWebViewClient?.onReceivedHttpAuthRequest(view, handler, host, realm)
        }

        override fun onReceivedSslError(
            view: WebView,
            handler: SslErrorHandler?,
            error: SslError?
        ) {
            userWebViewClient?.onReceivedSslError(view, handler, error)
        }

        override fun onScaleChanged(view: WebView, oldScale: Float, newScale: Float) {
            userWebViewClient?.onScaleChanged(view, oldScale, newScale)
        }

        override fun onTooManyRedirects(view: WebView, cancelMsg: Message?, continueMsg: Message?) {
            userWebViewClient?.onTooManyRedirects(view, cancelMsg, continueMsg)
        }

        override fun onUnhandledKeyEvent(view: WebView, event: KeyEvent) {
            userWebViewClient?.onUnhandledKeyEvent(view, event)
        }

        override fun shouldOverrideKeyEvent(view: WebView, event: KeyEvent): Boolean {
            return userWebViewClient?.shouldOverrideKeyEvent(view, event)
                ?: super.shouldOverrideKeyEvent(view, event)
        }

        override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {
            return userWebViewClient?.shouldOverrideUrlLoading(view, url)
                ?: super.shouldOverrideUrlLoading(view, url)
        }

        // v12
        @SuppressLint("NewApi")
        override fun onReceivedLoginRequest(
            view: WebView,
            realm: String?,
            account: String?,
            args: String?
        ) {
            userWebViewClient?.onReceivedLoginRequest(view, realm, account, args)
        }

        // v11
        @SuppressLint("NewApi")
        override fun shouldInterceptRequest(view: WebView, url: String): WebResourceResponse? {
            return userWebViewClient?.shouldInterceptRequest(view, url)
                ?: super.shouldInterceptRequest(view, url)
        }
    }

    companion object {
        private const val TAG = "IOLWebView"
    }
}