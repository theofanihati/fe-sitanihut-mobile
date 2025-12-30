package com.dishut_lampung.sitanihut.domain.usecase.report

import androidx.paging.PagingData
import com.dishut_lampung.sitanihut.domain.model.Report
import com.dishut_lampung.sitanihut.domain.model.ReportStatus
import com.dishut_lampung.sitanihut.domain.repository.ReportRepository
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test

class GetReportsUseCaseTest {
    private val repository: ReportRepository = mockk()

    private lateinit var getReportsUseCase: GetReportsUseCase

    @Before
    fun setUp() {
        getReportsUseCase = GetReportsUseCase(repository)
    }

    @Test
    fun `invoke should return PagingData from repository`() = runTest {
        val dummyPagingData = PagingData.from(emptyList<Report>())
        every { repository.getReports(any(), any()) } returns flowOf(dummyPagingData)

        val result = getReportsUseCase("", null).first()
        assertNotNull(result)

        verify(exactly = 1) { repository.getReports("", null) }
    }

    @Test
    fun `invoke without arguments should call repository with default empty params and null status`() = runTest {
        val dummyPagingData = PagingData.from(emptyList<Report>())
        every { repository.getReports("", null) } returns flowOf(dummyPagingData)

        val result = getReportsUseCase().first()

        assertNotNull(result)
        verify(exactly = 1) { repository.getReports(params = "", status = null) }
    }

    @Test
    fun `invoke with specific params should pass arguments to repository`() = runTest {
        val dummyPagingData = PagingData.from(emptyList<Report>())
        val specificQuery = "Hutan Lindung"
        val specificStatus = ReportStatus.VERIFIED

        every { repository.getReports(any(), any()) } returns flowOf(dummyPagingData)

        val result = getReportsUseCase(params = specificQuery, status = specificStatus).first()
        assertNotNull(result)
        verify(exactly = 1) { repository.getReports(params = specificQuery, status = specificStatus) }
    }
}