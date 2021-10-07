package de.infonline.sampleapp

import android.content.Intent
import android.os.Bundle
import android.text.method.ScrollingMovementMethod
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import de.infonline.lib.iomb.IOLViewEvent
import de.infonline.sampleapp.web.IOLWebViewActivity
import kotlin.math.max
import kotlin.math.min


class MainActivity : AppCompatActivity() {

    lateinit var logView: TextView
    var logCounter: Int = 0
    val textBuffer = mutableListOf<String>()

    val logListener = { priority: Int, tag: String, message: String?, throwable: Throwable? ->
        logView.post {
            if (textBuffer.size >= 100) {
                textBuffer.removeAt(0)
            }
            val shortTag = tag.substring(4, min(tag.length, 20))
            val shortMessage = message?.substring(0, min(message.length, 100))
            textBuffer.add("#${++logCounter} $shortTag: $shortMessage")

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

        logView = findViewById<TextView>(R.id.logging_output).apply {
            movementMethod = ScrollingMovementMethod()
            setHorizontallyScrolling(true)
        }

        findViewById<Button>(R.id.webview_action_hybrid).setOnClickListener {
            startActivity(Intent(this@MainActivity, IOLWebViewActivity::class.java))
        }

        findViewById<Button>(R.id.events_submit).setOnClickListener {
            App.IOMB_SESSION.logEvent(IOLViewEvent(type = IOLViewEvent.IOLViewEventType.Refreshed, category = "MainScreen"))
        }
    }

    override fun onResume() {
        super.onResume()
        App.IOMB_SESSION.logEvent(IOLViewEvent(type = IOLViewEvent.IOLViewEventType.Appeared, category = "MainScreen"))

        App.logListeners.add(logListener)
    }

    override fun onPause() {
        App.logListeners.remove(logListener)
        super.onPause()
    }
}
