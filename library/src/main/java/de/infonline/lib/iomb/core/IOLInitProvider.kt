package de.infonline.lib.iomb.core

import android.content.ContentProvider
import android.content.ContentValues
import android.content.Context
import android.content.pm.ProviderInfo
import android.database.Cursor
import android.net.Uri
import de.infonline.lib.iomb.IOMB
import de.infonline.lib.iomb.util.IOLLog

class IOLInitProvider : ContentProvider() {

    override fun attachInfo(context: Context?, info: ProviderInfo?) {
        if (info == null) {
            throw IllegalStateException("ProviderInfo is null")
        }
        if (DEFAULT_AUTHORITY == info.authority) {
            throw IllegalStateException("Unexpected: ${info.authority}. Did you declare 'applicationId' in build.gradle?")
        }
        super.attachInfo(context, info)
    }

    override fun onCreate(): Boolean {
        IOMB.init(requireNotNull(context))
        IOLLog.tag(TAG, public = true).d("... initialization done.")
        return true
    }

    override fun insert(uri: Uri, values: ContentValues?): Uri? = null

    override fun query(
            uri: Uri,
            projection: Array<String>?,
            selection: String?,
            selectionArgs: Array<String>?,
            sortOrder: String?
    ): Cursor? = null

    override fun update(uri: Uri, values: ContentValues?, selection: String?, selectionArgs: Array<String>?): Int = 0

    override fun delete(uri: Uri, selection: String?, selectionArgs: Array<String>?): Int = 0

    override fun getType(uri: Uri): String? = null

    companion object {
        val DEFAULT_AUTHORITY = "de.infonline.lib.iomb.core.${IOLInitProvider::class.java.simpleName}"
        private const val TAG = "IOLInitProvider"
    }

}