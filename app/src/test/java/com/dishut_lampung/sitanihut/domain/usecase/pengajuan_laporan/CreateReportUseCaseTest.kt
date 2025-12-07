package com.dishut_lampung.sitanihut.domain.usecase.pengajuan_laporan

import com.dishut_lampung.sitanihut.domain.model.CreateReportInput
import com.dishut_lampung.sitanihut.domain.repository.ReportRepository
import com.dishut_lampung.sitanihut.domain.usecase.report.CreateReportUseCase
import com.dishut_lampung.sitanihut.util.Resource
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class CreateReportUseCaseTest {

    private val repository: ReportRepository = mockk()
    private lateinit var createReportUseCase: CreateReportUseCase

    @Before
    fun setUp() {
        createReportUseCase = CreateReportUseCase(repository)
    }

    @Test
    fun `invoke should call repository createReport`() = runTest {
        val input = mockk<CreateReportInput>(relaxed = true)
        coEvery { repository.createReport(input) } returns Resource.Success(Unit)

        val result = createReportUseCase(input)

        assertTrue(result is Resource.Success)
        coVerify(exactly = 1) { repository.createReport(input) }
    }
}