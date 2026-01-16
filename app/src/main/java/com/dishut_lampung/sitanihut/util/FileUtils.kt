package com.dishut_lampung.sitanihut.util

import android.app.DownloadManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Environment
import android.provider.OpenableColumns
import android.webkit.MimeTypeMap
import android.widget.Toast
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import com.dishut_lampung.sitanihut.domain.model.ReportAttachment
import com.dishut_lampung.sitanihut.presentation.shared.components.animations.MessageType
import java.util.Locale

fun copyUriToInternalStorage(context: Context, uri: Uri): String? {
    return try {
        val attachmentDir = File(context.filesDir, "attachments")
        if (!attachmentDir.exists()) {
            attachmentDir.mkdirs()
        }

        var originalName = getFileName(context, uri)
        if (!originalName.contains(".")) {
            val mimeType = context.contentResolver.getType(uri)
            val extension = MimeTypeMap.getSingleton().getExtensionFromMimeType(mimeType)
            if (extension != null) {
                originalName += ".$extension"
            }
        }

        val fileName = "lampiran_${System.currentTimeMillis()}_$originalName"
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

fun openFileOrUrl(
    context: Context,
    attachment: ReportAttachment,
    onMessage: (String, MessageType) -> Unit
) {
    try {
        if (attachment.isLocal) {
            val file = File(attachment.filePath)
            if (!file.exists()) {
                onMessage("File tidak ditemukan di HP", MessageType.Error)
                return
            }

            val intent = Intent(Intent.ACTION_VIEW)
            val uri = FileProvider.getUriForFile(context, "${context.packageName}.provider", file)
            val mimeType = getMimeType(file)
            android.util.Log.d("DEBUG_FILE", "File: ${file.name}, Ext: ${file.extension}, Mime: $mimeType")

            intent.setDataAndType(uri, mimeType)
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)

            context.startActivity(intent)
        } else {
            val fileName = attachment.filePath.substringAfterLast("/")
            downloadFile(context, attachment.filePath, fileName, onMessage)
        }

    } catch (e: Exception) {
        e.printStackTrace()
        onMessage("Gagal membuka file: ${e.localizedMessage}", MessageType.Error)
    }
}

fun downloadFile(
    context: Context,
    url: String,
    fileName: String?,
    onMessage: (String, MessageType) -> Unit
) {
    try {
        val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        val uri = Uri.parse(url)
        val finalFileName = fileName ?: url.substringAfterLast("/")

        val request = DownloadManager.Request(uri)
            .setTitle(finalFileName)
            .setDescription("Mengunduh lampiran laporan...")
            .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            .setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, finalFileName)
            .setAllowedOverMetered(true)
            .setAllowedOverRoaming(true)

        downloadManager.enqueue(request)
        onMessage("Unduhan dimulai...", MessageType.Success)

    } catch (e: Exception) {
        e.printStackTrace()
        onMessage("Gagal memulai unduhan", MessageType.Error)
    }
}

fun getMimeType(file: File): String {
    val extension = file.extension.lowercase(Locale.ROOT)
    var type = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension)
    if (type == null) {
        type = when (extension) {
            "jpg", "jpeg" -> "image/jpeg"
            "png" -> "image/png"
            "pdf" -> "application/pdf"
            "doc", "docx" -> "application/msword"
            "xls", "xlsx" -> "application/vnd.ms-excel"
            else -> "*/*"
        }
    }
    return type
}