package com.dishut_lampung.sitanihut.domain.usecase.report

import com.dishut_lampung.sitanihut.data.local.entity.SyncStatus
import com.dishut_lampung.sitanihut.domain.model.ReportDetail
import com.dishut_lampung.sitanihut.domain.model.ReportStatus
import com.dishut_lampung.sitanihut.domain.repository.ReportRepository
import com.dishut_lampung.sitanihut.util.Resource
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class ReviewReportUseCaseTest {
    private val repository: ReportRepository = mockk()
    private lateinit var reviewReportUseCase: ReviewReportUseCase

    @Before
    fun setUp() {
        reviewReportUseCase = ReviewReportUseCase(repository)
    }

    @Test
    fun `when role Penyuluh verifies 'menunggu' report, should return Success`() = runTest {
        val reportId = "1"
        val currentStatus = ReportStatus.PENDING
        val newStatus = ReportStatus.VERIFIED
        val userRole = "penyuluh"

        val mockReport = mockk<ReportDetail>(relaxed = true)
        every { mockReport.status } returns currentStatus

        coEvery { repository.getReportById(reportId) } returns flowOf(Resource.Success(mockReport))
        coEvery { repository.updateReportStatus(reportId, newStatus) } returns Resource.Success(Unit)

        val result = reviewReportUseCase(reportId, newStatus, userRole)
        assertTrue("Harus return Success", result is Resource.Success)
        coVerify { repository.updateReportStatus(reportId, newStatus) }
    }

    @Test
    fun `when role PJ approves 'diverifikasi' report, should return Success`() = runTest {
        val reportId = "1"
        val currentStatus = ReportStatus.VERIFIED
        val newStatus = ReportStatus.APPROVED
        val userRole = "penanggung jawab"

        val mockReport = mockk<ReportDetail>(relaxed = true)
        every { mockReport.status } returns currentStatus

        coEvery { repository.getReportById(reportId) } returns flowOf(Resource.Success(mockReport))
        coEvery { repository.updateReportStatus(reportId, newStatus) } returns Resource.Success(Unit)

        val result = reviewReportUseCase(reportId, newStatus, userRole)
        assertTrue("Harus return Success", result is Resource.Success)
        coVerify { repository.updateReportStatus(reportId, newStatus) }
    }

    @Test
    fun `when role Penyuluh rejects 'menunggu' report, should return Success`() = runTest {
        val reportId = "1"
        val currentStatus = ReportStatus.APPROVED
        val newStatus = ReportStatus.REJECTED
        val userRole = "penyuluh"

        val mockReport = mockk<ReportDetail>(relaxed = true)
        every { mockReport.status } returns currentStatus

        coEvery { repository.getReportById(reportId) } returns flowOf(Resource.Success(mockReport))
        coEvery { repository.updateReportStatus(reportId, newStatus) } returns Resource.Success(Unit)

        val result = reviewReportUseCase(reportId, newStatus, userRole)
        assertTrue(result is Resource.Success)
        coVerify { repository.updateReportStatus(reportId, newStatus) }
    }

    @Test
    fun `when role PJ tries to approve 'menunggu' report, should return Error`() = runTest {
        val reportId = "1"
        val currentStatus = ReportStatus.APPROVED
        val newStatus = ReportStatus.APPROVED
        val userRole = "penanggung jawab"

        val mockReport = mockk<ReportDetail>(relaxed = true)
        every { mockReport.status } returns currentStatus

        coEvery { repository.getReportById(reportId) } returns flowOf(Resource.Success(mockReport))
        val result = reviewReportUseCase(reportId, newStatus, userRole)

        assertTrue("Harus return Error", result is Resource.Error)
        assertEquals("Laporan harus diverifikasi Penyuluh terlebih dahulu", result.message)
        coVerify(exactly = 0) { repository.updateReportStatus(any(), any()) }
    }

    @Test
    fun `when role Petani tries to change status, should return Error`() = runTest {
        val reportId = "1"
        val newStatus = ReportStatus.VERIFIED
        val userRole = "petani"

        val result = reviewReportUseCase(reportId, newStatus, userRole)
        assertTrue(result is Resource.Error)
        assertEquals("Anda tidak memiliki akses", result.message)

        coVerify(exactly = 0) { repository.getReportById(any()) }
        coVerify(exactly = 0) { repository.updateReportStatus(any(), any()) }
    }

    @Test
    fun `when report not found, should return Error`() = runTest {
        val reportId = "99"
        val userRole = "penyuluh"
        val newStatus = ReportStatus.VERIFIED
        coEvery { repository.getReportById(reportId) } returns flowOf(Resource.Error("Data not found"))

        val result = reviewReportUseCase(reportId, newStatus, userRole)
        assertTrue(result is Resource.Error)
        coVerify(exactly = 0) { repository.updateReportStatus(any(), any()) }
    }
}