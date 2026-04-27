package de.infonline.lib.iomb

import android.annotation.SuppressLint
import android.content.Context
import android.view.InputEvent
import android.webkit.ClientCertRequest
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebView
import de.infonline.lib.iomb.IOLWebView.IOLWebViewClientBase
import java.lang.reflect.InvocationTargetException

internal class IOLWebViewClientV21(
        context: Context?
) : IOLWebViewClientBase(context) {
    // v21
    @SuppressLint("NewApi")
    override fun onReceivedClientCertRequest(view: WebView, request: ClientCertRequest) {
        userWebViewClient?.onReceivedClientCertRequest(view, request)
    }

    // FIXME unused?
    // v21
    @SuppressLint("NewApi")
    fun onUnhandledInputEvent(view: WebView?, event: InputEvent?) {
        if (userWebViewClient != null) {
            try {
                val onUnhandledInputEvent = userWebViewClient!!.javaClass.getMethod("onUnhandledInputEvent", WebView::class.java, InputEvent::class.java)
                onUnhandledInputEvent.invoke(userWebViewClient, view, event)
            } catch (e: NoSuchMethodException) {
                e.printStackTrace()
            } catch (e: InvocationTargetException) {
                e.printStackTrace()
            } catch (e: IllegalAccessException) {
                e.printStackTrace()
            }
        }
    }

    // v21
    @SuppressLint("NewApi")
    override fun shouldInterceptRequest(view: WebView, request: WebResourceRequest): WebResourceResponse? {
        return userWebViewClient?.shouldInterceptRequest(view, request) ?: super.shouldInterceptRequest(view, request)
    }
}