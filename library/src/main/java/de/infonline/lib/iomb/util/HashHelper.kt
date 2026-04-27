package de.infonline.lib.iomb.util

import java.security.MessageDigest
import java.util.*

internal object HashHelper {

    fun String.toSHA256() = hashString("SHA-256", this)

    fun String.toSHA1() = hashString("SHA-1", this)

    fun String.toMD5() = hashString("MD5", this)

    private fun hashString(type: String, input: String) = MessageDigest
            .getInstance(type)
            .digest(input.toByteArray())
            .joinToString(separator = "") { String.format("%02X", it) }
            .toLowerCase(Locale.ROOT)
}