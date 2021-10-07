package de.infonline.sampleapp.web

import android.graphics.Bitmap
import android.os.Bundle
import android.view.KeyEvent
import android.view.Window
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import de.infonline.lib.iomb.IOLWebView
import de.infonline.sampleapp.R

class IOLWebViewActivity : AppCompatActivity() {

    private lateinit var iolWebView: IOLWebView

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        window.requestFeature(Window.FEATURE_PROGRESS)
        setContentView(R.layout.activity_hybridwebview)

        val urlTextField = findViewById<EditText>(R.id.webview_url_text)
        urlTextField.setText(TEST_URL)
        urlTextField.setOnEditorActionListener { v: TextView, actionId: Int, event: KeyEvent? ->
            var url = v.text.toString()
            if (!url.startsWith("http")) url = "http://$url"
            iolWebView.loadUrl(url)
            true
        }

        iolWebView = findViewById(R.id.webview)
        iolWebView.webViewClient = object : WebViewClient() {
            override fun onPageStarted(view: WebView, url: String, favicon: Bitmap?) {
                title = "Hybrid WebView: "
            }

            override fun onPageFinished(view: WebView, url: String) {
                title = "Hybrid WebView: " + view.title

                //Nothing extraordinary to do here,
                //IOLWebView (in layout activity_hybridwebview.xml.xml) calls the JavaScript method 'setMultiIdentifier' automatically
            }
        }
        iolWebView.webChromeClient = object : WebChromeClient() {
            override fun onProgressChanged(view: WebView, progress: Int) {
                setProgress(progress * 100)
            }
        }
        iolWebView.loadUrl(TEST_URL)
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        return when {
            keyCode == KeyEvent.KEYCODE_BACK && iolWebView.canGoBack() -> {
                iolWebView.goBack()
                true
            }
            else -> super.onKeyDown(keyCode, event)
        }
    }

    companion object {
        private const val TEST_URL = "https://www.infonline.de/iomb-hybrid-test.html"
    }
}