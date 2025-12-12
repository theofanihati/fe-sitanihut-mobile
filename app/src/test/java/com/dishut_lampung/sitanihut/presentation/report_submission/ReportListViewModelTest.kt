package com.dishut_lampung.sitanihut.presentation.report_submission

import androidx.paging.PagingData
import app.cash.turbine.test
import com.dishut_lampung.sitanihut.domain.model.ReportStatus
import com.dishut_lampung.sitanihut.domain.usecase.report.DeleteReportUseCase
import com.dishut_lampung.sitanihut.domain.usecase.report.GetReportsUseCase
import com.dishut_lampung.sitanihut.domain.usecase.report.SubmitReportUseCase
import com.dishut_lampung.sitanihut.presentation.report_submission.list.PengajuanLaporanEvent
import com.dishut_lampung.sitanihut.presentation.report_submission.list.ReportListViewModel
import com.dishut_lampung.sitanihut.util.MainCoroutineRule
import com.dishut_lampung.sitanihut.util.Resource
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class ReportListViewModelTest {

    @get:Rule
    val mainCoroutineRule = MainCoroutineRule()

    private val getReportsUseCase: GetReportsUseCase = mockk()
    private val deleteReportUseCase: DeleteReportUseCase = mockk()
    private val submitReportUseCase: SubmitReportUseCase = mockk()

    private lateinit var viewModel: ReportListViewModel

    @Before
    fun setUp() {
        every { getReportsUseCase(any(), any()) } returns flowOf(PagingData.from(emptyList()))
        coEvery { deleteReportUseCase(any()) } returns Resource.Success(Unit)
        coEvery { submitReportUseCase(any()) } returns Resource.Success(Unit)

        viewModel = ReportListViewModel(
            getReportsUseCase,
            deleteReportUseCase,
            submitReportUseCase
        )
    }

    @Test
    fun `init should call getReportsUseCase with default params`() = runTest {
        viewModel.reportPagingFlow.test {
            verify(exactly = 1) { getReportsUseCase("", null) }
            cancelAndIgnoreRemainingEvents()
        }

        assertNotNull(viewModel.reportPagingFlow)
    }

    @Test
    fun `onSearch should update query and reload data`() = runTest {
        val job = backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.reportPagingFlow.collect {}
        }
        viewModel.onEvent(PengajuanLaporanEvent.OnSearchQueryChange("Jagung"))
        advanceUntilIdle()

        assertEquals("Jagung", viewModel.uiState.value.searchQuery)
        verify { getReportsUseCase("Jagung", null) }
        job.cancel()
    }

    @Test
    fun `onApplyFilter should update status and reload data`() = runTest {
        val job = backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.reportPagingFlow.collect {}
        }
        viewModel.onEvent(PengajuanLaporanEvent.OnFilterChange(ReportStatus.VERIFIED))
        advanceUntilIdle()

        assertEquals(ReportStatus.VERIFIED, viewModel.uiState.value.selectedStatus)
        verify { getReportsUseCase("", ReportStatus.VERIFIED) }
        job.cancel()
    }

    @Test
    fun `onReportMoreOptionClick should set selectedReportId and open sheet`() = runTest {
        val reportId = "raport-id-222"
        viewModel.onEvent(PengajuanLaporanEvent.OnReportMoreOptionClick(reportId))

        val state = viewModel.uiState.value
        assertEquals(reportId, state.selectedReportId)
        assertTrue(state.isOptionSheetVisible)
    }

    @Test
    fun `onDeleteClick should close option sheet and show delete confirmation dialog`() = runTest {
        val reportId = "raport-id-222"

        viewModel.onEvent(PengajuanLaporanEvent.OnReportMoreOptionClick(reportId))

        viewModel.onEvent(PengajuanLaporanEvent.OnDeleteClick)

        val state = viewModel.uiState.value
        assertFalse("Sheet tertutup", state.isOptionSheetVisible)
        assertTrue("Dialog terbuka", state.isDeleteDialogVisible)
        assertEquals("ID tersimpan", reportId, state.selectedReportId)
    }

    @Test
    fun `onDeleteConfirm success should call deleteReport and show success message`() = runTest {
        val reportId = "id-to-delete"
        viewModel.onEvent(PengajuanLaporanEvent.OnReportMoreOptionClick(reportId))
        coEvery { deleteReportUseCase(reportId) } coAnswers {
            delay(100)
            Resource.Success(Unit)
        }

        viewModel.uiState.test {
            val initialState = awaitItem()

            viewModel.onEvent(PengajuanLaporanEvent.OnDeleteConfirm)

            val loadingState = awaitItem()
            assertTrue("Harus loading saat proses hapus", loadingState.isLoading)
            assertFalse("Dialog harus tertutup saat loading", loadingState.isDeleteDialogVisible)

            val successState = awaitItem()
            assertFalse("Loading selesai", successState.isLoading)
            assertEquals("Laporan berhasil dihapus", successState.successMessage)
            assertNull("ID harus di-reset setelah sukses", successState.selectedReportId)

            coVerify(exactly = 1) { deleteReportUseCase(reportId) }
        }
    }

    @Test
    fun `onSubmitClick success should call submitReport and show success message`() = runTest {
        val reportId = "id-to-submit"
        viewModel.onEvent(PengajuanLaporanEvent.OnReportMoreOptionClick(reportId))
        coEvery { submitReportUseCase(reportId) } coAnswers {
            delay(100)
            Resource.Success(Unit)
        }
        viewModel.uiState.test {
            val initialState = awaitItem()

            viewModel.onEvent(PengajuanLaporanEvent.OnSubmitClick)

            val loadingState = awaitItem()
            assertTrue(loadingState.isLoading)
            assertFalse(loadingState.isOptionSheetVisible)

            val successState = awaitItem()
            assertFalse(successState.isLoading)
            assertEquals("Laporan berhasil diajukan", successState.successMessage)

            coVerify(exactly = 1) { submitReportUseCase(reportId) }
        }
    }

    @Test
    fun `onConfirmDelete error should show error message`() = runTest {
        val reportId = "123"
        viewModel.onEvent(PengajuanLaporanEvent.OnReportMoreOptionClick(reportId))

        coEvery { deleteReportUseCase(reportId) } returns Resource.Error("Gagal Hapus")

        viewModel.onEvent(PengajuanLaporanEvent.OnDeleteConfirm)
        advanceUntilIdle()

        coVerify { deleteReportUseCase(reportId) }
        assertEquals("Gagal Hapus", viewModel.uiState.value.errorMessage)
    }

    @Test
    fun `onSubmitClick success should call usecase`() = runTest {
        val reportId = "456"
        viewModel.onEvent(PengajuanLaporanEvent.OnReportMoreOptionClick(reportId))
        coEvery { submitReportUseCase(reportId) } returns Resource.Success(Unit)

        viewModel.onEvent(PengajuanLaporanEvent.OnSubmitClick)
        advanceUntilIdle()

        coVerify { submitReportUseCase(reportId) }
        assertNotNull(viewModel.uiState.value.successMessage)
    }
}