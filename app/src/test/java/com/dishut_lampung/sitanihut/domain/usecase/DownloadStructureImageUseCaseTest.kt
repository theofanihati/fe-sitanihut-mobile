package com.dishut_lampung.sitanihut.domain.usecase

import com.dishut_lampung.sitanihut.domain.repository.CompanyRepository
import com.dishut_lampung.sitanihut.domain.usecase.information.DownloadStructureImageUseCase
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class DownloadStructureImageUseCaseTest {
    private lateinit var useCase: DownloadStructureImageUseCase
    private val mockCompanyRepository = mockk<CompanyRepository>(relaxed = true)

    @Before
    fun setUp() {
        useCase = DownloadStructureImageUseCase(mockCompanyRepository)
    }

    @Test
    fun `invoke should return success when repository save operation is successful`() = runTest {

        val dummyPath = "/storage/emulated/0/Pictures/struktur.jpg"
        val expectedResult = Result.success(dummyPath)
        coEvery { mockCompanyRepository.saveStructureImageToGallery() } returns expectedResult

        val result = useCase()
        assertTrue(result.isSuccess)
        assertEquals(dummyPath, result.getOrNull())

        coVerify(exactly = 1) { mockCompanyRepository.saveStructureImageToGallery() }
    }

    @Test
    fun `invoke should return failure when repository save operation fails`() = runTest {
        val dummyException = Exception("Gagal menyimpan gambar")
        val expectedResult = Result.failure<String>(dummyException)
        coEvery { mockCompanyRepository.saveStructureImageToGallery() } returns expectedResult

        val result = useCase()
        assertTrue(result.isFailure)
        assertEquals(dummyException, result.exceptionOrNull())

        coVerify(exactly = 1) { mockCompanyRepository.saveStructureImageToGallery() }
    }
}