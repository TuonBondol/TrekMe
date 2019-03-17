package com.peterlaurence.trekme.core.fileprovider

import android.content.ContentProvider
import android.content.ContentValues
import android.content.Context
import android.content.pm.ProviderInfo
import android.database.Cursor
import android.net.Uri
import android.os.ParcelFileDescriptor
import java.io.File

/**
 * This [ContentProvider] exposes files of TrekMe to other apps with read-only rights.
 * It expects files to be requested with a specific [Uri]. This why external code should get the uri
 * of a file using [TrekmeFilesProvider.generateUri] method.
 */
class TrekmeFilesProvider : ContentProvider() {

    override fun onCreate(): Boolean {
        return true
    }

    /**
     * The file uri is supposed to be right next after the authority (it if was formed using
     * [generateUri] method).
     */
    @Throws(SecurityException::class)
    override fun openFile(uri: Uri, mode: String): ParcelFileDescriptor? {
        val file = File(uri.path?.substringAfter(authority))
        return if (file.exists()) {
            ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY)
        } else {
            null
        }
    }

    /**
     * This is how we get the authority, which is defined in the manifest of the application.
     */
    override fun attachInfo(context: Context?, info: ProviderInfo?) {
        super.attachInfo(context, info)

        info?.authority?.let {
            authority = it
        }
    }

    override fun update(uri: Uri, contentvalues: ContentValues?, s: String?,
                        `as`: Array<String>?): Int {
        return 0
    }

    override fun delete(uri: Uri, s: String, `as`: Array<String>?): Int {
        return 0
    }

    override fun insert(uri: Uri, contentvalues: ContentValues?): Uri? {
        return null
    }

    override fun getType(uri: Uri): String? {
        return null
    }

    override fun query(uri: Uri, projection: Array<String>?, s: String?, as1: Array<String>?,
                       s1: String?): Cursor? {
        return null
    }

    companion object MyCompanion {
        // The authority is the symbolic name for the provider class
        lateinit var authority: String

        fun generateUri(file: File): Uri? {
            return if (this::authority.isInitialized) {
                Uri.parse("content://$authority$file")
            } else {
                null
            }
        }
    }
}