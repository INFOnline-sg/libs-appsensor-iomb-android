package de.infonline.lib.iomb.measurements.common

import android.content.ContentResolver
import android.content.Context
import android.os.Build
import android.provider.Settings
import de.infonline.lib.iomb.util.BuildVersionWrap
import de.infonline.lib.iomb.util.IOLLog
import java.util.regex.Pattern
import javax.inject.Inject
import javax.inject.Singleton


@Singleton
internal class ProofToken @Inject constructor(
        private val context: Context

) {
    private val secureSettingsResolver: (String) -> String? = { key ->
        try {
            SecureSettingsWrap.getString(context.contentResolver, key)
        } catch (e: Exception) {
            IOLLog.tag(TAG).w(e, "Failed to get $key from secure settings.")
            null
        }
    }

    private var cachedToken: String? = null

    // https://stackoverflow.com/a/56024910/1251958
    private fun findToken(): String? {
        if(cachedToken == null) {
            var bluetoothName = secureSettingsResolver(KEY_BLUETOOTH_NAME)

            if (bluetoothName == null && BuildVersionWrap.SDK_VERSION >= Build.VERSION_CODES.Q) {
                bluetoothName = secureSettingsResolver(KEY_DEVICE_NAME)

                // The key may change in future Android versions, let have an alternative to the hardcoded key
                if (bluetoothName == null
                    && Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1
                    && KEY_DEVICE_NAME != Settings.Global.DEVICE_NAME
                ) {
                    bluetoothName = secureSettingsResolver(KEY_DEVICE_NAME)
                }
            }

            cachedToken = bluetoothName
        }
        return cachedToken
    }

    private var lastPotentialToken: String? = null

    fun lookupToken(): String? {
        val potentialToken = findToken() ?: ""
        return if (validateToken(potentialToken)) {
            if (lastPotentialToken != potentialToken) {
                IOLLog.tag(TAG).d("ProofToken: %s", potentialToken)
                lastPotentialToken = potentialToken
            }
            potentialToken
        } else {
            if (lastPotentialToken != potentialToken) {
                IOLLog.tag(TAG).d("Device name not a prooftoken: %s", potentialToken)
                lastPotentialToken = potentialToken
            }
            null
        }
    }

    fun clearCachedToken() {
        cachedToken = null
        lastPotentialToken = null
    }

    companion object {
        const val TAG = "ProofToken"
        const val KEY_DEVICE_NAME = "device_name"
        const val KEY_BLUETOOTH_NAME = "bluetooth_name"

        fun validateToken(token: String): Boolean = TOKEN_REGEX.matches(token)

        // iotest5ee391701234
        private val TOKEN_REGEX: Regex = Pattern.compile("^(iotest[0-9a-fA-F]{8,}[0-9]{4})\$").toRegex()
    }
}

internal object SecureSettingsWrap {
    fun getString(contentResolver: ContentResolver, key: String): String? = Settings.Secure.getString(contentResolver, key)
}