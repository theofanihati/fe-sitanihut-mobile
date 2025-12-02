package com.dishut_lampung.sitanihut.domain.usecase.pengajuan_laporan

import com.dishut_lampung.sitanihut.domain.repository.ReportRepository
import com.dishut_lampung.sitanihut.domain.usecase.report.SubmitReportUseCase
import com.dishut_lampung.sitanihut.util.Resource
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class SubmitReportUseCaseTest {

    private val repository: ReportRepository = mockk()
    private lateinit var submitReportUseCase: SubmitReportUseCase

    @Before
    fun setUp() {
        submitReportUseCase = SubmitReportUseCase(repository)
    }

    @Test
    fun `sukses submit`() = runTest {
        val reportId = "123"
        val expectedResult = Resource.Success(Unit)

        coEvery { repository.submitReport(reportId) } returns expectedResult

        val result = submitReportUseCase(reportId)

        assertTrue(result is Resource.Success)

        coVerify(exactly = 1) { repository.submitReport(reportId) }
    }

    @Test
    fun `gagal submit`() = runTest {
        val reportId = "123"
        val expectedResult = Resource.Error<Unit>("Network Error")

        coEvery { repository.submitReport(reportId) } returns expectedResult

        val result = submitReportUseCase(reportId)

        assertTrue(result is Resource.Error)

        coVerify(exactly = 1) { repository.submitReport(reportId) }
    }
}