package com.dishut_lampung.sitanihut.data.repository

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import com.dishut_lampung.sitanihut.R
import com.dishut_lampung.sitanihut.domain.repository.CompanyRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream
import javax.inject.Inject

class CompanyRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : CompanyRepository {
    override suspend fun saveStructureImageToGallery(): Result<String> {
        return try {
            val bitmap = BitmapFactory.decodeResource(
                context.resources,
                R.drawable.susunan_organisasi
            )

            val filename = "Struktur_Organisasi_Dishut_Lampung_${System.currentTimeMillis()}.png"
            var outputStream: OutputStream? = null

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
               val contentValues = ContentValues().apply {
                    put(MediaStore.MediaColumns.DISPLAY_NAME, filename)
                    put(MediaStore.MediaColumns.MIME_TYPE, "image/png")
                    put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/Sitanihut")
                }

                val imageUri = context.contentResolver.insert(
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                    contentValues
                )

                imageUri?.let { uri ->
                    outputStream = context.contentResolver.openOutputStream(uri)
                } ?: throw Exception("Gagal membuat path MediaStore")

            } else {
                val imagesDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
                val appDir = File(imagesDir, "Sitanihut")
                if (!appDir.exists()) appDir.mkdirs()

                val imageFile = File(appDir, filename)
                outputStream = FileOutputStream(imageFile)
            }

            outputStream?.use { stream ->
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
            } ?: throw Exception("OutputStream null, gagal menyimpan.")

            // bitmap.recycle() // in case ntar perlu, tulis aja dulu wkwkwk
            Result.success("Gambar berhasil disimpan di Galeri")
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}