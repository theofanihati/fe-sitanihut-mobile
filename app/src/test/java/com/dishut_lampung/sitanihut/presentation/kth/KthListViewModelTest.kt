package com.dishut_lampung.sitanihut.presentation.kth

import app.cash.turbine.test
import com.dishut_lampung.sitanihut.data.local.UserPreferences
import com.dishut_lampung.sitanihut.domain.model.Kth
import com.dishut_lampung.sitanihut.domain.usecase.kth.DeleteKthUseCase
import com.dishut_lampung.sitanihut.domain.usecase.kth.GetKthListUseCase
import com.dishut_lampung.sitanihut.domain.usecase.kth.SyncKthDataUseCase
import com.dishut_lampung.sitanihut.presentation.kth.list.KthEvent
import com.dishut_lampung.sitanihut.presentation.kth.list.KthListViewModel
import com.dishut_lampung.sitanihut.presentation.petani.list.PetaniEvent
import com.dishut_lampung.sitanihut.presentation.petani.list.PetaniListViewModel
import com.dishut_lampung.sitanihut.util.ConnectivityObserver
import com.dishut_lampung.sitanihut.util.MainCoroutineRule
import com.dishut_lampung.sitanihut.util.PdfService
import com.dishut_lampung.sitanihut.util.Resource
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class KthListViewModelTest {

    @get:Rule
    val mainCoroutineRule = MainCoroutineRule()

    private var getKthListUseCase: GetKthListUseCase = mockk(relaxed = true)
    private var deleteKthUseCase: DeleteKthUseCase = mockk()
    private var syncKthDataUseCase: SyncKthDataUseCase = mockk(relaxed = true)
    private var userPreferences: UserPreferences = mockk()
    private var connectivityObserver: ConnectivityObserver = mockk()
    private var pdfService: PdfService = mockk()
    private lateinit var viewModel: KthListViewModel

    @Before
    fun setUp() {
    }

    private fun createViewModel() {
        viewModel = KthListViewModel(
            getKthListUseCase,
            syncKthDataUseCase,
            deleteKthUseCase,
            userPreferences,
            connectivityObserver,
            pdfService
        )
    }

    @Test
    fun `init should load KTH list and observe connectivity`() = runTest {
        val dummyKth = listOf(Kth("1", "KTH Mawar", "Desa A", "Kec A","Kab A", "Koor A", "089785983784", "KPH A"))

        every { userPreferences.userRole } returns flowOf("penyuluh")
        every { getKthListUseCase("penyuluh", "") } returns flowOf(Resource.Success(dummyKth))
        every { connectivityObserver.observe() } returns flowOf(ConnectivityObserver.Status.Available)

        createViewModel()

        viewModel.uiState.test {
            val state = awaitItem()
            assertEquals(dummyKth, state.kthList)
            assertTrue(state.isOnline)

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `when connectivity is lost, isOnline should be false`() = runTest {
        every { userPreferences.userRole } returns flowOf("penyuluh")
        every { getKthListUseCase(any(), any()) } returns flowOf(Resource.Success(emptyList()))
        every { connectivityObserver.observe() } returns flowOf(ConnectivityObserver.Status.Lost)

        createViewModel()
        viewModel.uiState.test {
            val state = awaitItem()
            assertFalse(state.isOnline)

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `search should filter data locally`() = runTest {
        val listData = listOf(
            Kth("1", "KTH Mawar", "Desa A", "Kec A","Kab A", "Koor A", "089785983784", "KPH A"),
            Kth("2", "KTH Melati", "Desa B", "Kec A","Kab B", "Koor A", "088877278837",  "KPH B")
        )

        every { userPreferences.userRole } returns flowOf("penyuluh")
        every { getKthListUseCase("penyuluh", "") } returns flowOf(Resource.Success(listData))
        every { connectivityObserver.observe() } returns flowOf(ConnectivityObserver.Status.Available)

        createViewModel()
        viewModel.uiState.test {
            awaitItem()

            viewModel.onEvent(KthEvent.OnSearchQueryChange("Mawar"))

            val filteredState = awaitItem()
            assertEquals(1, filteredState.kthList.size)
            assertEquals("KTH Mawar", filteredState.kthList.first().name)

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `OnRefresh should trigger fetch data again`() = runTest {
        every { userPreferences.userRole } returns flowOf("penyuluh")
        every { getKthListUseCase("penyuluh", "") } returns flowOf(Resource.Success(emptyList()))
        every { connectivityObserver.observe() } returns flowOf(ConnectivityObserver.Status.Available)

        createViewModel()
        viewModel.onEvent(KthEvent.OnRefresh)

        coVerify(exactly = 1) { syncKthDataUseCase() }
        coVerify(exactly = 1) { getKthListUseCase("penyuluh", "") }
    }

    @Test
    fun `onEvent OnRefresh success should update data`() = runTest {
        val successMessage = "Data berhasil diperbarui"
        every { connectivityObserver.observe() } returns flowOf(ConnectivityObserver.Status.Available)
        coEvery { syncKthDataUseCase() } returns Resource.Success(Unit)
        every { userPreferences.userRole } returns flowOf("penyuluh")
        every { getKthListUseCase(any(), any()) } returns flowOf(Resource.Success(emptyList()))

        createViewModel()
        val job = launch { viewModel.uiState.collect{} }

        advanceUntilIdle()

        viewModel.onEvent(KthEvent.OnRefresh)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isRefreshing)
        assertEquals(successMessage, state.successMessage)
        job.cancel()
    }

    @Test
    fun `onEvent OnRefresh should show error when offline`() = runTest {
        every { connectivityObserver.observe() } returns flowOf(ConnectivityObserver.Status.Lost)
        every { userPreferences.userRole } returns flowOf("penyuluh")
        every { getKthListUseCase(any(), any()) } returns flowOf(Resource.Success(emptyList()))

        createViewModel()
        val job = launch { viewModel.uiState.collect{} }
        advanceUntilIdle()

        viewModel.onEvent(KthEvent.OnRefresh)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isRefreshing)
        assertEquals("Tidak ada koneksi internet", state.errorMessage)

        coVerify(exactly = 0) { syncKthDataUseCase() }
        job.cancel()
    }

    @Test
    fun `onEvent OnRefresh should handle sync error`() = runTest {
        val errorMsg = "Gagal sinkronisasi"
        every { connectivityObserver.observe() } returns flowOf(ConnectivityObserver.Status.Available)
        coEvery { syncKthDataUseCase() } returns Resource.Error(errorMsg)
        every { userPreferences.userRole } returns flowOf("penyuluh")
        every { getKthListUseCase(any(), any()) } returns flowOf(Resource.Success(emptyList()))

        createViewModel()
        val job = launch { viewModel.uiState.collect{} }

        advanceUntilIdle()

        viewModel.onEvent(KthEvent.OnRefresh)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isRefreshing)
        assertEquals(errorMsg, state.errorMessage)
        job.cancel()
    }

    @Test
    fun `BottomSheet should update state correctly`() = runTest {
        every { userPreferences.userRole } returns flowOf("penyuluh")
        every { getKthListUseCase(any(), any()) } returns flowOf(Resource.Success(emptyList()))
        every { connectivityObserver.observe() } returns flowOf(ConnectivityObserver.Status.Available)

        createViewModel()
        viewModel.uiState.test {
            awaitItem()
            val selectedId = "123"
            viewModel.onEvent(KthEvent.OnMoreOptionClick(selectedId))
            val openSheetState = awaitItem()
            assertTrue(openSheetState.isBottomSheetVisible)
            assertEquals(selectedId, openSheetState.selectedKthId)

            viewModel.onEvent(KthEvent.OnBottomSheetDismiss)
            val closeSheetState = awaitItem()
            assertFalse(closeSheetState.isBottomSheetVisible)

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `Delete success should remove item and show success message`() = runTest {
        val itemToDelete = Kth("1", "Delete ini", "", "", "", "", "", "")
        val initialList = listOf(itemToDelete)

        every { userPreferences.userRole } returns flowOf("penyuluh")
        every { getKthListUseCase(any(), any()) } returns flowOf(Resource.Success(initialList))
        every { connectivityObserver.observe() } returns flowOf(ConnectivityObserver.Status.Available)
        coEvery { deleteKthUseCase("1") } returns Resource.Success(Unit)

        createViewModel()
        viewModel.uiState.test {
            awaitItem()
            viewModel.onEvent(KthEvent.OnMoreOptionClick("1"))
            val sheetState = awaitItem()
            assertTrue(sheetState.isBottomSheetVisible)
            assertEquals("1", sheetState.selectedKthId)

            viewModel.onEvent(KthEvent.OnDeleteClick)
            val dialogState = awaitItem()
            assertFalse(dialogState.isBottomSheetVisible)
            assertTrue(dialogState.isDeleteDialogVisible)

            viewModel.onEvent(KthEvent.OnDeleteConfirm)

            var currentState = awaitItem()
            while (currentState.isLoading) {
                currentState = awaitItem()
            }

            assertFalse("Loading harus sudah false", currentState.isLoading)
            assertFalse("Dialog harus sudah tutup", currentState.isDeleteDialogVisible)
            assertTrue("List harus kosong setelah delete", currentState.kthList.isEmpty())
            assertEquals("Pesan sukses harus muncul", "Data berhasil dihapus", currentState.successMessage)
            assertNull("Selected ID harus di-reset", currentState.selectedKthId)

            cancelAndIgnoreRemainingEvents()
        }

        coVerify { deleteKthUseCase("1") }
    }

    @Test
    fun `Couldn't Delete if user role is Penanggung Jawab`() = runTest {
        every { userPreferences.userRole } returns flowOf("penanggung jawab")
        every { getKthListUseCase(any(), any()) } returns flowOf(Resource.Success(emptyList()))
        every { connectivityObserver.observe() } returns flowOf(ConnectivityObserver.Status.Available)

        createViewModel()
        val job = launch { viewModel.uiState.collect {} }
        advanceUntilIdle()

        viewModel.onEvent(KthEvent.OnDeleteClick)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse("Dialog delete tidak boleh muncul untuk Penanggung Jawab", state.isDeleteDialogVisible)

        viewModel.onEvent(KthEvent.OnMoreOptionClick("1"))
        viewModel.onEvent(KthEvent.OnDeleteConfirm)
        advanceUntilIdle()

        val errorState = viewModel.uiState.value
        assertEquals("Anda tidak memiliki akses hapus", errorState.errorMessage)

        job.cancel()
    }

    @Test
    fun `Delete error should show error message`() = runTest {
        val itemToDelete = Kth("1", "Err", "", "", "", "", "", "")
        every { userPreferences.userRole } returns flowOf("penyuluh")
        every { getKthListUseCase(any(), any()) } returns flowOf(Resource.Success(listOf(itemToDelete)))
        every { connectivityObserver.observe() } returns flowOf(ConnectivityObserver.Status.Available)

        val errorMsg = "Gagal hapus bro"
        coEvery { deleteKthUseCase("1") } returns Resource.Error(errorMsg)

        createViewModel()
        viewModel.uiState.test {
            awaitItem()
            viewModel.onEvent(KthEvent.OnMoreOptionClick("1"))
            awaitItem()
            viewModel.onEvent(KthEvent.OnDeleteClick)
            awaitItem()
            viewModel.onEvent(KthEvent.OnDeleteConfirm)

            var errorState = awaitItem()
            while (errorState.errorMessage == null) {
                errorState = awaitItem()
            }

            assertFalse(errorState.isLoading)
            assertEquals(errorMsg, errorState.errorMessage)
            assertEquals(1, errorState.kthList.size)

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `Delete when offline should not call usecase and show error message`() = runTest {
        val itemToDelete = Kth("1", "Offline Delete", "", "", "", "", "", "")
        every { userPreferences.userRole } returns flowOf("penyuluh")
        every { getKthListUseCase(any(), any()) } returns flowOf(Resource.Success(listOf(itemToDelete)))

        every { connectivityObserver.observe() } returns flowOf(ConnectivityObserver.Status.Lost)

        createViewModel()
        viewModel.uiState.test {
            awaitItem()

            viewModel.onEvent(KthEvent.OnMoreOptionClick("1"))
            awaitItem()

            viewModel.onEvent(KthEvent.OnDeleteClick)
            awaitItem()

            viewModel.onEvent(KthEvent.OnDeleteConfirm)

            var errorState = awaitItem()
            while (errorState.errorMessage == null) {
                errorState = awaitItem()
            }

            assertEquals("Tidak ada koneksi internet", errorState.errorMessage)
            assertFalse(errorState.isDeleteDialogVisible)

            cancelAndIgnoreRemainingEvents()
        }

        coVerify(exactly = 0) { deleteKthUseCase(any()) }
    }

    @Test
    fun `Dismiss should clear states`() = runTest {
        every { userPreferences.userRole } returns flowOf("penyuluh")
        every { getKthListUseCase(any(), any()) } returns flowOf(Resource.Success(emptyList()))
        every { connectivityObserver.observe() } returns flowOf(ConnectivityObserver.Status.Available)
        coEvery { deleteKthUseCase("1") } returns Resource.Error("Error Hapus")

        createViewModel()
        viewModel.uiState.test {
            awaitItem()
            viewModel.onEvent(KthEvent.OnMoreOptionClick("1"))
            awaitItem()
            viewModel.onEvent(KthEvent.OnDeleteClick)
            val dialogState = awaitItem()
            assertTrue(dialogState.isDeleteDialogVisible)
            assertEquals("1", dialogState.selectedKthId)

            viewModel.onEvent(KthEvent.OnDismissDeleteDialog)
            val dialogDismissState = awaitItem()
            assertFalse(dialogDismissState.isDeleteDialogVisible)
            assertNull("Selected ID harus null setelah dialog dismiss", dialogDismissState.selectedKthId)

            viewModel.onEvent(KthEvent.OnMoreOptionClick("1"))
            awaitItem()
            viewModel.onEvent(KthEvent.OnDeleteClick)
            awaitItem()
            viewModel.onEvent(KthEvent.OnDeleteConfirm)

            var errorState = awaitItem()
            while (errorState.errorMessage == null) {
                errorState = awaitItem()
            }
            assertEquals("Error Hapus", errorState.errorMessage)

            viewModel.onEvent(KthEvent.OnDismissError)
            val errorDismissState = awaitItem()
            assertNull("Error message harus hilang", errorDismissState.errorMessage)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `Export List success should call pdfService with all data`() = runTest {
        val dummyData = listOf(
            Kth("1", "KTH A", "Desa A", "Kec A", "Kab A", "LatLong", "081", "KPH 1"),
            Kth("2", "KTH B", "Desa B", "Kec B", "Kab B", "LatLong", "082", "KPH 2")
        )

        every { userPreferences.userRole } returns flowOf("penyuluh")
        every { getKthListUseCase(any(), any()) } returns flowOf(Resource.Success(dummyData))
        every { connectivityObserver.observe() } returns flowOf(ConnectivityObserver.Status.Available)

        coEvery {
            pdfService.generatePdf<Kth>(any(), any(), any(), any(), any())
        } returns Resource.Success("/storage/emulated/0/Download/Data_Petani.pdf")

        createViewModel()

        viewModel.uiState.test {
            awaitItem()
            viewModel.onEvent(KthEvent.OnExportList)

            var state = awaitItem()
            while (state.isLoading) {
                state = awaitItem()
            }

            assertEquals("/storage/emulated/0/Download/Data_Petani.pdf", state.successMessage)
            assertFalse(state.isBottomSheetVisible)

            coVerify {
                pdfService.generatePdf(
                    fileName = any(),
                    reportTitle = "LAPORAN DATA KTH",
                    headers = any(),
                    data = dummyData,
                    rowMapper = any()
                )
            }

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `Export Detail success should call pdfService with filtered data`() = runTest {
        val targetId = "2"
        val targetKth = Kth(targetId, "KTH B", "Desa B", "Kec B", "Kab B", "LatLong", "082", "KPH 2")
        val dummyData = listOf(
            Kth("1", "KTH A", "Desa A", "Kec A", "Kab A", "LatLong", "081", "KPH 1"),
            targetKth
        )

        every { userPreferences.userRole } returns flowOf("penyuluh")
        every { getKthListUseCase(any(), any()) } returns flowOf(Resource.Success(dummyData))
        every { connectivityObserver.observe() } returns flowOf(ConnectivityObserver.Status.Available)

        coEvery {
            pdfService.generatePdf<Kth>(any(), any(), any(), any(), any())
        } returns Resource.Success("File Saved")

        createViewModel()

        viewModel.uiState.test {
            awaitItem()
            viewModel.onEvent(KthEvent.OnExportDetail(targetId))

            var state = awaitItem()
            while (state.isLoading) {
                state = awaitItem()
            }

            assertEquals("File Saved", state.successMessage)

            coVerify {
                pdfService.generatePdf<Kth>(
                    fileName = any(),
                    reportTitle = "DETAIL DATA KTH",
                    headers = any(),
                    data = match { list ->
                        list.size == 1 && list.first().id == targetId
                    },
                    rowMapper = any()
                )
            }

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `Export should show error message when PdfService fails`() = runTest {
        val dummyData = listOf(Kth("1", "KTH A", "", "", "", "", "", ""))
        every { userPreferences.userRole } returns flowOf("penyuluh")
        every { getKthListUseCase(any(), any()) } returns flowOf(Resource.Success(dummyData))
        every { connectivityObserver.observe() } returns flowOf(ConnectivityObserver.Status.Available)

        val errorMsg = "Permission Denied"
        coEvery {
            pdfService.generatePdf<Kth>(any(), any(), any(), any(), any())
        } returns Resource.Error(errorMsg)

        createViewModel()

        viewModel.uiState.test {
            awaitItem()

            viewModel.onEvent(KthEvent.OnExportList)

            var state = awaitItem()
            while (state.isLoading) {
                state = awaitItem()
            }

            assertEquals(errorMsg, state.errorMessage)
            assertNull(state.successMessage)

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `Export should show error when data is empty`() = runTest {
        every { userPreferences.userRole } returns flowOf("penyuluh")
        every { getKthListUseCase(any(), any()) } returns flowOf(Resource.Success(emptyList()))
        every { connectivityObserver.observe() } returns flowOf(ConnectivityObserver.Status.Available)

        createViewModel()

        viewModel.uiState.test {
            awaitItem()

            viewModel.onEvent(KthEvent.OnExportList)
            var state = awaitItem()
            if(state.isLoading) state = awaitItem()

            assertEquals("Tidak ada data untuk diekspor", state.errorMessage)

            coVerify(exactly = 0) {
                pdfService.generatePdf<Kth>(any(), any(), any(), any(), any())
            }

            cancelAndIgnoreRemainingEvents()
        }
    }
}