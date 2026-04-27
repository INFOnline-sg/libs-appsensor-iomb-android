package de.infonline.lib.iomb

import android.annotation.SuppressLint
import android.annotation.TargetApi
import android.content.Context
import android.webkit.ClientCertRequest
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebView
import de.infonline.lib.iomb.IOLWebView.IOLWebViewClientBase

internal class IOLWebViewClientV24(
        context: Context?
) : IOLWebViewClientBase(context) {

    // v21
    @SuppressLint("NewApi")
    override fun onReceivedClientCertRequest(view: WebView, request: ClientCertRequest) {
        if (userWebViewClient != null) {
            userWebViewClient!!.onReceivedClientCertRequest(view, request)
        }
    }

    // v21
    @SuppressLint("NewApi")
    override fun shouldInterceptRequest(view: WebView, request: WebResourceRequest): WebResourceResponse? {
        return userWebViewClient?.shouldInterceptRequest(view, request) ?: super.shouldInterceptRequest(view, request)
    }

    @TargetApi(24)
    override fun shouldOverrideUrlLoading(webView: WebView, webResourceRequest: WebResourceRequest): Boolean {
        return userWebViewClient != null && userWebViewClient!!.shouldOverrideUrlLoading(webView, webResourceRequest)
    }
}