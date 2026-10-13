package com.example.gastrack.util

import android.content.ContentValues
import android.content.Context
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import java.io.File
import java.io.FileOutputStream

/**
 * Saves files into the device's public Downloads folder (Android/Downloads visible to the user),
 * not the app's private sandbox. API 29+ uses MediaStore (no permission needed); API 26-28 falls
 * back to writing the public Downloads directory directly, which requires WRITE_EXTERNAL_STORAGE
 * granted at runtime.
 */
object FileSaver {

    fun saveText(context: Context, fileName: String, content: String): String {
        return saveBytes(context, fileName, content.toByteArray())
    }

    fun saveBytes(context: Context, fileName: String, bytes: ByteArray): String {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val resolver = context.contentResolver
            val values = ContentValues().apply {
                put(MediaStore.Downloads.DISPLAY_NAME, fileName)
                put(MediaStore.Downloads.MIME_TYPE, "text/csv")
                put(MediaStore.Downloads.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
            }
            val uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values)
                ?: throw IllegalStateException("Could not create file in Downloads")
            resolver.openOutputStream(uri)?.use { it.write(bytes) }
                ?: throw IllegalStateException("Could not open output stream")
            "Downloads/$fileName"
        } else {
            @Suppress("DEPRECATION")
            val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            if (!downloadsDir.exists()) downloadsDir.mkdirs()
            val file = File(downloadsDir, fileName)
            FileOutputStream(file).use { it.write(bytes) }
            file.absolutePath
        }
    }

    /** True if writing to public storage needs a runtime permission request first (API 26-28 only). */
    fun needsLegacyPermission(): Boolean = Build.VERSION.SDK_INT < Build.VERSION_CODES.Q
}