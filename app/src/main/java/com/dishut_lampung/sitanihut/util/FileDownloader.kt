package com.dishut_lampung.sitanihut.util

import android.os.Environment
import okhttp3.ResponseBody
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import javax.inject.Inject

class FileDownloader @Inject constructor() {

    fun saveFile(body: ResponseBody, fileName: String): Resource<String> {
        return try {
            val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            val file = File(downloadsDir, "$fileName.pdf")

            var inputStream: InputStream? = null
            var outputStream: FileOutputStream? = null

            try {
                val fileReader = ByteArray(4096)
                var fileSizeDownloaded: Long = 0
                val fileSize = body.contentLength()

                inputStream = body.byteStream()
                outputStream = FileOutputStream(file)

                while (true) {
                    val read = inputStream.read(fileReader)
                    if (read == -1) {
                        break
                    }
                    outputStream.write(fileReader, 0, read)
                    fileSizeDownloaded += read
                }

                outputStream.flush()
                Resource.Success("File tersimpan: ${file.absolutePath}")
            } catch (e: Exception) {
                Resource.Error("Gagal menyimpan file: ${e.message}")
            } finally {
                inputStream?.close()
                outputStream?.close()
            }
        } catch (e: Exception) {
            Resource.Error("Error: ${e.message}")
        }
    }
}