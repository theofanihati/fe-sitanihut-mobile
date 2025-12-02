package com.dishut_lampung.sitanihut.domain.usecase.pengajuan_laporan

import androidx.paging.PagingData
import com.dishut_lampung.sitanihut.domain.model.Report
import com.dishut_lampung.sitanihut.domain.model.ReportStatus
import com.dishut_lampung.sitanihut.domain.repository.ReportRepository
import com.dishut_lampung.sitanihut.domain.usecase.report.GetReportsUseCase
import com.dishut_lampung.sitanihut.util.Resource
import com.google.common.base.Verify.verify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
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
}