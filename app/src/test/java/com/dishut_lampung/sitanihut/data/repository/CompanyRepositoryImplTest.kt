package com.dishut_lampung.sitanihut.data.repository

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.test.core.app.ApplicationProvider
import com.dishut_lampung.sitanihut.R
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkAll
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.io.OutputStream

@RunWith(RobolectricTestRunner::class)
class CompanyRepositoryImplTest {

    private lateinit var context: Context
    private lateinit var repository: CompanyRepositoryImpl
    private val mockBitmap: Bitmap = mockk(relaxed = true)

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        repository = CompanyRepositoryImpl(context)

       mockkStatic(BitmapFactory::class)
        every { BitmapFactory.decodeResource(any(), any()) } returns mockBitmap
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    @Config(sdk = [29, 33])
    fun `saveStructureImageToGallery on Android Q+ should succeed using MediaStore`() = runTest {
        every { mockBitmap.compress(any(), any(), any()) } returns true
        val result = repository.saveStructureImageToGallery()
        assertTrue(result.isSuccess)
        verify { BitmapFactory.decodeResource(any(), R.drawable.susunan_organisasi) }
        verify { mockBitmap.compress(Bitmap.CompressFormat.PNG, 100, any<OutputStream>()) }
    }

    @Test
    @Config(sdk = [28])
    fun `saveStructureImageToGallery on Android Legacy should succeed using FileOutputStream`() = runTest {
        every { mockBitmap.compress(any(), any(), any()) } returns true
        val result = repository.saveStructureImageToGallery()
        assertTrue(result.isSuccess)
        verify { mockBitmap.compress(Bitmap.CompressFormat.PNG, 100, any<OutputStream>()) }
    }

    @Test
    fun `saveStructureImageToGallery should return Failure when decoding fails`() = runTest {
        every { BitmapFactory.decodeResource(any(), any()) } throws RuntimeException("Decode Error")

        val result = repository.saveStructureImageToGallery()
        assertTrue(result.isFailure)
    }
}