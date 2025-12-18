package com.dishut_lampung.sitanihut.domain.usecase.report

import com.dishut_lampung.sitanihut.domain.model.ReportDetail
import com.dishut_lampung.sitanihut.domain.repository.ReportRepository
import com.dishut_lampung.sitanihut.util.Resource
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class GetReportDetailUseCaseTest {

    private val repository: ReportRepository = mockk()
    private lateinit var getReportDetailUseCase: GetReportDetailUseCase

    @Before
    fun setUp() {
        getReportDetailUseCase = GetReportDetailUseCase(repository)
    }

    @Test
    fun `invoke should return report data when repository success`() = runTest {
        val reportId = "123"
        val expectedReport = mockk<ReportDetail>()
        every { repository.getReportById(reportId) } returns flowOf(Resource.Success(expectedReport))

        val result = getReportDetailUseCase(reportId).first()

        assertTrue(result is Resource.Success)
        assertEquals(expectedReport, result.data)
        verify(exactly = 1) { repository.getReportById(reportId) }
    }

    @Test
    fun `invoke should return error when repository fails`() = runTest {
        val reportId = "123"
        val errorMessage = "Network Error"
        every { repository.getReportById(reportId) } returns flowOf(Resource.Error(errorMessage))

        val result = getReportDetailUseCase(reportId).first()

        assertTrue(result is Resource.Error)
        assertEquals(errorMessage, result.message)
    }
}