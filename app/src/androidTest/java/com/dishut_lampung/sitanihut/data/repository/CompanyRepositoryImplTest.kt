package com.dishut_lampung.sitanihut.data.repository

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.SmallTest
import com.google.common.truth.Truth.assertThat
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
@SmallTest
class CompanyRepositoryTest {
    @get:Rule
    var hiltRule = HiltAndroidRule(this)

    private lateinit var repository: CompanyRepositoryImpl
    private lateinit var context: Context

    @Before
    fun setUp() {
        hiltRule.inject()
        context = ApplicationProvider.getApplicationContext()
        repository = CompanyRepositoryImpl(context)
    }

    @Test
    fun saveStructureImageToGallery_shouldReturnSuccess() = runTest {
        val result = repository.saveStructureImageToGallery()
        assertThat(result.isSuccess).isTrue()
        assertThat(result.getOrNull()).isEqualTo("Gambar berhasil disimpan di Galeri")
    }

    // make sure Resource valid
    @Test
    fun ensureDrawableResourceExists_andCanBeDecoded() {
        val bitmap = android.graphics.BitmapFactory.decodeResource(
            context.resources,
            com.dishut_lampung.sitanihut.R.drawable.susunan_organisasi
        )

        assertThat(bitmap).isNotNull()
    }
}