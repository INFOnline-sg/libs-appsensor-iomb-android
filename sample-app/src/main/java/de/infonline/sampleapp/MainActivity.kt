package de.infonline.sampleapp

import android.content.Intent
import android.os.Bundle
import android.text.method.ScrollingMovementMethod
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.OnApplyWindowInsetsListener
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import de.infonline.lib.iomb.IOLViewEvent
import de.infonline.sampleapp.web.IOLWebViewActivity
import kotlin.math.max

class MainActivity : AppCompatActivity() {

    private lateinit var logView: TextView
    private var logCounter: Int = 0
    private val textBuffer = mutableListOf<String>()

    private val logListener = { _: Int, tag: String, message: String?, _: Throwable? ->
        logView.post {
            if (textBuffer.size >= 100) {
                textBuffer.removeAt(0)
            }
            textBuffer.add("#${++logCounter} $tag: $message")

            logView.text = textBuffer.joinToString("\n")

            try {
                val scrollAmount = logView.layout.getLineTop(logView.lineCount) - logView.height
                logView.scrollTo(0, max(scrollAmount, 0))
            } catch (exception: Exception) {
                // no-op
            }
        }
        Unit
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        ViewCompat.setOnApplyWindowInsetsListener(
            findViewById(android.R.id.content)
        ) { v: View?, windowInsets: WindowInsetsCompat? ->
            val insets = windowInsets!!.getInsets(WindowInsetsCompat.Type.systemBars())
            v!!.setPadding(insets.left, insets.top, insets.right, 0
            )
            windowInsets
        }

        logView = findViewById<TextView>(R.id.logging_output).apply {
            movementMethod = ScrollingMovementMethod()
            setHorizontallyScrolling(true)
        }


        findViewById<Button>(R.id.webview_action_hybrid).setOnClickListener {
            val intent = Intent(this@MainActivity, IOLWebViewActivity::class.java)
            intent.putExtra("url", "https://www.infonline.de/iomb-hybrid-test.html")

            startActivity(intent)
        }

        findViewById<Button>(R.id.webview_action_oewa_hybrid).setOnClickListener {
            val intent = Intent(this@MainActivity, IOLWebViewActivity::class.java)
            intent.putExtra("url", "https://www.infonline.de/oewa-iomb-hybrid-test.html")

            startActivity(intent)
        }

        findViewById<Button>(R.id.events_submit).setOnClickListener {
            App.IOMB_SESSION.logEvent(IOLViewEvent(type = IOLViewEvent.IOLViewEventType.Refreshed, category = "MainScreen"))
            App.IOMB_AT_SESSION.logEvent(IOLViewEvent(type = IOLViewEvent.IOLViewEventType.Refreshed, category = "MainScreen"))
        }
    }

    override fun onResume() {
        super.onResume()
        App.IOMB_SESSION.logEvent(IOLViewEvent(type = IOLViewEvent.IOLViewEventType.Appeared, category = "MainScreen"))
        App.IOMB_AT_SESSION.logEvent(IOLViewEvent(type = IOLViewEvent.IOLViewEventType.Appeared, category = "MainScreen"))

        App.logListeners.add(logListener)
    }

    override fun onPause() {
        App.logListeners.remove(logListener)
        super.onPause()
    }
}
