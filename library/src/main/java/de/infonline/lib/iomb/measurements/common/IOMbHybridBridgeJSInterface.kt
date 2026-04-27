package de.infonline.lib.iomb.measurements.common

import android.webkit.JavascriptInterface
import android.webkit.WebView
import de.infonline.lib.iomb.util.IOLLog
import org.json.JSONObject

internal class IOMbHybridBridgeJSInterface(
    private val webView: WebView,
    private val site: String,
    private val baseUrl: String,
    private val proofToken: ProofToken
) {

    var code: String? = null
    var comment: String? = null

    @JavascriptInterface
    fun getNativeAppData(globalObject: String, command: String, hybridToken: String?) {
        val domainServiceName = baseUrl.replace("https://", "")
        val auditToken = proofToken.lookupToken()

        val initData = JSONObject().apply {
            put("st", site)
            put("cp", code)
            put("co", comment)
            put("domainServiceName", domainServiceName)
            put("hybridToken", hybridToken)
            put("auditToken", auditToken)
        }.toString()

        IOLLog.tag("IOMbHybridBridge", public = true)
            .v(
                "Initialize WebSensor with || st = %s || cp = %s || co = %s || domainServiceName = %s || hybridToken = %s || auditToken = %s",
                site,
                code,
                comment,
                domainServiceName,
                hybridToken,
                auditToken,
            )
        val evalScript = "javascript:$globalObject('$command', '$initData')"
        webView.post { webView.evaluateJavascript(evalScript, null) }
    }
}