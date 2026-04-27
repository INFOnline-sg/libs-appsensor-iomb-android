package de.infonline.lib.iomb.util.extensions

import java.io.PrintWriter
import java.io.StringWriter

internal fun Throwable.toStackTraceString(): String? =
        StringWriter(256).use { stringWriter ->
            PrintWriter(stringWriter, false).use { printWriter ->
                this.printStackTrace(printWriter)
                printWriter.flush()
            }
            stringWriter.flush()
        }.toString()