package de.infonline.lib.iomb.util.extensions

import de.infonline.lib.iomb.util.IOLLog
import java.io.File
import java.io.FileNotFoundException
import java.io.IOException

@Suppress("functionName")
internal fun File(vararg crumbs: String): File {
    var compacter = File(crumbs[0])
    for (i in 1 until crumbs.size) {
        compacter = File(compacter, crumbs[i])
    }
    return compacter
}

@Suppress("functionName")
internal fun File(parent: File, vararg crumbs: String): File {
    var compacter = parent
    for (element in crumbs) {
        compacter = File(compacter, element)
    }
    return compacter
}

internal fun File.tryMkDirs(): File {
    if (exists()) {
        if (isDirectory) {
            IOLLog.tag(TAG).v("Directory already exists, not creating: %s", this)
            return this
        } else {
            val ex = IllegalStateException("Directory exists, but is not a directory: $this")
            IOLLog.tag(TAG).w(ex)
            throw ex
        }
    }

    if (mkdirs()) {
        IOLLog.tag(TAG).v("Directory created: %s", this)
        return this
    } else {
        val ex = IllegalStateException("Couldn't create Directory: $this")
        IOLLog.tag(TAG).w(ex)
        throw ex
    }
}

internal fun File.tryMkFile(): File {
    if (exists()) {
        if (isFile) {
            IOLLog.tag(TAG).v("File already exists, not creating: %s", this)
            return this
        } else {
            val ex = IllegalStateException("Path exists but is not a file: $this")
            IOLLog.tag(TAG).w(ex)
            throw ex
        }
    }

    if (!parentFile!!.exists()) parentFile!!.tryMkDirs()

    if (createNewFile()) {
        IOLLog.tag(TAG).v("File created: %s", this)
        return this
    } else {
        val ex = IllegalStateException("Couldn't create file: $this")
        IOLLog.tag(TAG).w(ex)
        throw ex
    }
}

@Throws(IOException::class)
internal fun File.deleteAll() {
    if (isDirectory) {
        listFiles()?.forEach { it.deleteAll() }
    }
    if (delete()) {
        IOLLog.tag(TAG).v("File.release(): Deleted %s", this)
    } else if (!exists()) {
        IOLLog.tag(TAG).w("File.release(): File didn't exist: %s", this)
    } else {
        throw FileNotFoundException("Failed to delete file: $this")
    }
}


@Throws(IOException::class)
internal fun File.listFilesThrowing(): List<File> {
    return this.listFiles()?.toList() ?: throw IOException("listFiles() returned NULL on $path")
}

@Throws(IOException::class)
internal fun File.tryDelete() {
    if (exists() && !delete()) {
        IOLLog.tag(TAG).w("File.tryDelete(): Failed to delete %s", this)
    }
}

private const val TAG = "FileExtensions"