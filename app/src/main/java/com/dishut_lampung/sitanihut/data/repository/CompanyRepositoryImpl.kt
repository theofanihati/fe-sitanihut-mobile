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
        return return Result.failure(Exception("heheh ntar yah"))
    }
}