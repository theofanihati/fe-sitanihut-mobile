package com.dishut_lampung.sitanihut.util

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import android.webkit.MimeTypeMap
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream

fun copyUriToInternalStorage(context: Context, uri: Uri): String? {
    return try {
        val attachmentDir = File(context.filesDir, "attachments")
        if (!attachmentDir.exists()) {
            attachmentDir.mkdirs()
        }

        val fileName = "lampiran_${System.currentTimeMillis()}_${getFileName(context, uri)}"
        val destinationFile = File(attachmentDir, fileName)

        val inputStream: InputStream? = context.contentResolver.openInputStream(uri)
        val outputStream = FileOutputStream(destinationFile)

        inputStream?.use { input ->
            outputStream.use { output ->
                input.copyTo(output)
            }
        }

        destinationFile.absolutePath
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}

private fun getFileName(context: Context, uri: Uri): String {
    var result: String? = null
    if (uri.scheme == "content") {
        val cursor = context.contentResolver.query(uri, null, null, null, null)
        cursor?.use {
            if (it.moveToFirst()) {
                val index = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (index != -1) result = it.getString(index)
            }
        }
    }
    return result ?: "unknown_file"
}

fun getMimeType(file: File): String {
    val extension = MimeTypeMap.getFileExtensionFromUrl(file.path)
    return MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension) ?: "application/octet-stream"
}