package de.infonline.lib.iomb.events

import android.content.Context

interface IOLBaseEvent {

    val identifier: String
    val category: String?
    val state: String?
    val comment: String?

    fun buildParameters(context: Context): Map<String, Any>

    interface State {
        val state: String
    }
}