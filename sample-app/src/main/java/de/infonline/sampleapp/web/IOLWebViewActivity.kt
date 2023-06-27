package de.infonline.sampleapp.web

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.os.Bundle
import android.view.KeyEvent
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import de.infonline.lib.iomb.IOMBHybridBridge
import de.infonline.lib.iomb.IOMbOEWAHybridBridge
import de.infonline.sampleapp.R

class IOLWebViewActivity : AppCompatActivity() {

    private lateinit var webView: WebView
    private lateinit var iombHybridBridge: IOMBHybridBridge
    private lateinit var iombOewaHybridBridge: IOMbOEWAHybridBridge

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_hybridwebview)

        val extras = intent.extras
        var defaultUrl = ""
        if (extras != null) {
            defaultUrl = extras.getString("url")!!
        }

        val urlTextField = findViewById<EditText>(R.id.webview_url_text)
        urlTextField.setText(defaultUrl)
        urlTextField.setOnEditorActionListener { v: TextView, _: Int, _: KeyEvent? ->
            var url = v.text.toString()
            if (!url.startsWith("http")) url = "http://$url"
            webView.loadUrl(url)
            true
        }

        webView = findViewById(R.id.webview)

        @SuppressLint("SetJavaScriptEnabled")
        webView.settings.javaScriptEnabled = true
        webView.settings.domStorageEnabled = true

        webView.webViewClient = object : WebViewClient() {
            override fun onPageStarted(view: WebView, url: String, favicon: Bitmap?) {
                title = "Hybrid WebView: "
            }

            override fun onPageFinished(view: WebView, url: String) {
                title = "Hybrid WebView: " + view.title
            }
        }

        iombHybridBridge = IOMBHybridBridge()
        iombHybridBridge.configureWebViewForIOMbHybridMeasurement(webView)

        iombOewaHybridBridge = IOMbOEWAHybridBridge()
        iombOewaHybridBridge.configureWebViewForIOMbOEWAHybridMeasurement(webView)

        webView.loadUrl(defaultUrl)
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        return when {
            keyCode == KeyEvent.KEYCODE_BACK && webView.canGoBack() -> {
                webView.goBack()
                true
            }
            else -> super.onKeyDown(keyCode, event)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        iombHybridBridge.destroy()
        iombOewaHybridBridge.destroy()
    }
}