package com.dishut_lampung.sitanihut.domain.usecase.report


import com.dishut_lampung.sitanihut.domain.repository.ReportRepository
import com.dishut_lampung.sitanihut.util.Resource
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class DeleteReportUseCaseTest {

    private val repository: ReportRepository = mockk()
    private lateinit var deleteReportUseCase: DeleteReportUseCase

    @Before
    fun setUp() {
        deleteReportUseCase = DeleteReportUseCase(repository)
    }

    @Test
    fun `invoke should call deleteReport from repository and return Success`() = runTest {
        val reportId = "123"
        val expectedResult = Resource.Success(Unit)

        coEvery { repository.deleteReport(reportId) } returns expectedResult

        val result = deleteReportUseCase(reportId)

        assertTrue(result is Resource.Success)
        assertEquals(expectedResult, result)

        coVerify(exactly = 1) { repository.deleteReport(reportId) }
    }

    @Test
    fun `invoke should return Error when repository fails`() = runTest {
        val reportId = "123"
        val errorMessage = "Delete failed"
        val expectedResult = Resource.Error<Unit>(errorMessage)

        coEvery { repository.deleteReport(reportId) } returns expectedResult

        val result = deleteReportUseCase(reportId)

        assertTrue(result is Resource.Error)
        assertEquals(errorMessage, result.message)

        coVerify(exactly = 1) { repository.deleteReport(reportId) }
    }
}