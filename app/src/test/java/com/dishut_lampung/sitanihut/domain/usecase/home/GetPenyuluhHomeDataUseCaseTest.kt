package com.dishut_lampung.sitanihut.domain.usecase.home

import app.cash.turbine.test
import com.dishut_lampung.sitanihut.domain.model.Report
import com.dishut_lampung.sitanihut.domain.model.ReportStatus
import com.dishut_lampung.sitanihut.domain.repository.HomeRepository
import com.dishut_lampung.sitanihut.util.Resource
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.verify
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class GetPenyuluhHomeDataUseCaseTest {

    @MockK
    private lateinit var repository: HomeRepository
    private lateinit var useCase: GetPenyuluhHomeDataUseCase

    @Before
    fun setUp() {
        MockKAnnotations.init(this)
        useCase = GetPenyuluhHomeDataUseCase(repository)
    }

    @Test
    fun `invoke should call repository with 'menunggu' status and return data`() = runTest {
        val dummyReports = listOf(
            Report(
                id = "1",
                period = 2024,
                monthPeriod = "Agustus",
                submissionDate = "20-08-2024",
                totalTransaction = 50000.0,
                status = ReportStatus.PENDING
            )
        )
        val expectedFlow = Resource.Success(dummyReports)
        every { repository.getReportsByStatus("menunggu") } returns flowOf(expectedFlow)

        useCase().test {
            val result = awaitItem()
            assertEquals(expectedFlow, result)
            assertEquals(1, result.data?.size)

            awaitComplete()
        }
        verify(exactly = 1) { repository.getReportsByStatus("menunggu") }
    }

    @Test
    fun `invoke should propagate error from repository`() = runTest {
        val errorMsg = "Network Error"
        val errorFlow = Resource.Error<List<Report>>(errorMsg)

        every { repository.getReportsByStatus("menunggu") } returns flowOf(errorFlow)
        useCase().test {
            val result = awaitItem()
            assert(result is Resource.Error)
            assertEquals(errorMsg, result.message)

            awaitComplete()
        }
    }
}