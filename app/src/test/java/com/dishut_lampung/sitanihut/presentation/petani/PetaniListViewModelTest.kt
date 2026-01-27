package com.dishut_lampung.sitanihut.presentation.petani

import app.cash.turbine.test
import com.dishut_lampung.sitanihut.data.local.UserPreferences
import com.dishut_lampung.sitanihut.domain.model.Petani
import com.dishut_lampung.sitanihut.domain.model.UserDetail
import com.dishut_lampung.sitanihut.domain.repository.PetaniRepository
import com.dishut_lampung.sitanihut.domain.usecase.petani.DeletePetaniUseCase
import com.dishut_lampung.sitanihut.domain.usecase.petani.GetPetaniListUseCase
import com.dishut_lampung.sitanihut.domain.usecase.petani.SyncPetaniDataUseCase
import com.dishut_lampung.sitanihut.presentation.petani.list.PetaniEvent
import com.dishut_lampung.sitanihut.presentation.petani.list.PetaniListViewModel
import com.dishut_lampung.sitanihut.presentation.user_management.list.UserEvent
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
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class PetaniListViewModelTest{

    @get:Rule
    val mainCoroutineRule = MainCoroutineRule()

    private var getPetaniListUseCase: GetPetaniListUseCase = mockk()
    private var deletePetaniUseCase: DeletePetaniUseCase = mockk()
    private val syncPetaniDataUseCase: SyncPetaniDataUseCase = mockk(relaxed = true)
    private var userPreferences: UserPreferences = mockk()
    private var connectivityObserver: ConnectivityObserver = mockk()
    private var repository: PetaniRepository = mockk(relaxed = true)
    private val pdfService: PdfService = mockk()
    private lateinit var viewModel: PetaniListViewModel

    private fun createViewModel() {
        viewModel = PetaniListViewModel(
            getPetaniListUseCase,
            syncPetaniDataUseCase,
            deletePetaniUseCase,
            userPreferences,
            connectivityObserver,
            pdfService
        )
    }
    @Test
    fun `init should load Petani list and observe connectivity`() = runTest {
        val dummyPetani = listOf(
            Petani(
                id = "1",
                name = "Tepani Canz",
                identityNumber = "1802045310040001",
                kphName = "KPH Tahura",
                kthName = "KTH Sukses",
            ),
            Petani(
                id = "2",
                name = "Mawar",
                identityNumber = "1802045310040001",
                kphName = "KPH Tahura",
                kthName = "KTH Sukses",
            ),
        )

        every { userPreferences.userRole } returns flowOf("penyuluh")
        every { getPetaniListUseCase("penyuluh", "") } returns flowOf(Resource.Success(dummyPetani))
        every { connectivityObserver.observe() } returns flowOf(ConnectivityObserver.Status.Available)

        viewModel = PetaniListViewModel(getPetaniListUseCase, syncPetaniDataUseCase, deletePetaniUseCase, userPreferences, connectivityObserver, pdfService)

        viewModel.uiState.test {
            val state = awaitItem()
            assertEquals(dummyPetani, state.petaniList)
            assertTrue(state.isOnline)

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `when connectivity is lost, isOnline should be false`() = runTest {
        every { userPreferences.userRole } returns flowOf("penyuluh")
        every { getPetaniListUseCase(any(), any()) } returns flowOf(Resource.Success(emptyList()))
        every { connectivityObserver.observe() } returns flowOf(ConnectivityObserver.Status.Lost)

        viewModel = PetaniListViewModel(getPetaniListUseCase, syncPetaniDataUseCase, deletePetaniUseCase, userPreferences, connectivityObserver, pdfService)
        viewModel.uiState.test {
            val state = awaitItem()
            assertFalse(state.isOnline)

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `search should filter data locally`() = runTest {
        val listData = listOf(
            Petani(
                id = "1",
                name = "Mawar",
                identityNumber = "1802045310040001",
                kphName = "KPH Tahura",
                kthName = "KTH Sukses",
            ),
            Petani(
                id = "2",
                name = "Melati",
                identityNumber = "1802045310040001",
                kphName = "KPH Tahura",
                kthName = "KTH Sukses",
            ),
        )

        every { userPreferences.userRole } returns flowOf("penyuluh")
        every { getPetaniListUseCase("penyuluh", "") } returns flowOf(Resource.Success(listData))
        every { connectivityObserver.observe() } returns flowOf(ConnectivityObserver.Status.Available)

        viewModel = PetaniListViewModel(getPetaniListUseCase, syncPetaniDataUseCase, deletePetaniUseCase, userPreferences, connectivityObserver, pdfService)
        viewModel.uiState.test {
            awaitItem()

            viewModel.onEvent(PetaniEvent.OnSearchQueryChange("Mawar"))

            val filteredState = awaitItem()
            assertEquals(1, filteredState.petaniList.size)
            assertEquals("Mawar", filteredState.petaniList.first().name)

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `OnRefresh should trigger fetch data again`() = runTest {
        every { userPreferences.userRole } returns flowOf("penyuluh")
        every { getPetaniListUseCase("penyuluh", "") } returns flowOf(Resource.Success(emptyList()))
        every { connectivityObserver.observe() } returns flowOf(ConnectivityObserver.Status.Available)

        viewModel = PetaniListViewModel(getPetaniListUseCase, syncPetaniDataUseCase, deletePetaniUseCase, userPreferences, connectivityObserver, pdfService)
        viewModel.onEvent(PetaniEvent.OnRefresh)

        coVerify(exactly = 1) { getPetaniListUseCase("penyuluh", "") }
        coVerify(exactly = 1) { syncPetaniDataUseCase() }
    }

    @Test
    fun `onEvent OnRefresh success should update data`() = runTest {
        val successMessage = "Data berhasil diperbarui"
        every { connectivityObserver.observe() } returns flowOf(ConnectivityObserver.Status.Available)
        coEvery { syncPetaniDataUseCase() } returns Resource.Success(Unit)
        every { userPreferences.userRole } returns flowOf("penyuluh")
        every { getPetaniListUseCase(any(), any()) } returns flowOf(Resource.Success(emptyList()))

        viewModel = PetaniListViewModel(getPetaniListUseCase, syncPetaniDataUseCase, deletePetaniUseCase, userPreferences, connectivityObserver, pdfService)
        val job = launch { viewModel.uiState.collect{} }

        advanceUntilIdle()

        viewModel.onEvent(PetaniEvent.OnRefresh)
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
        every { getPetaniListUseCase(any(), any()) } returns flowOf(Resource.Success(emptyList()))

        viewModel = PetaniListViewModel(getPetaniListUseCase, syncPetaniDataUseCase, deletePetaniUseCase, userPreferences, connectivityObserver, pdfService)
        val job = launch { viewModel.uiState.collect{} }
        advanceUntilIdle()

        viewModel.onEvent(PetaniEvent.OnRefresh)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isRefreshing)
        assertEquals("Tidak ada koneksi internet", state.errorMessage)

        coVerify(exactly = 0) { syncPetaniDataUseCase() }
        job.cancel()
    }

    @Test
    fun `onEvent OnRefresh should handle sync error`() = runTest {
        val errorMsg = "Gagal sinkronisasi"
        every { connectivityObserver.observe() } returns flowOf(ConnectivityObserver.Status.Available)
        coEvery { syncPetaniDataUseCase() } returns Resource.Error(errorMsg)
        every { userPreferences.userRole } returns flowOf("penyuluh")
        every { getPetaniListUseCase(any(), any()) } returns flowOf(Resource.Success(emptyList()))

        viewModel = PetaniListViewModel(getPetaniListUseCase, syncPetaniDataUseCase, deletePetaniUseCase, userPreferences, connectivityObserver, pdfService)
        val job = launch { viewModel.uiState.collect{} }

        advanceUntilIdle()

        viewModel.onEvent(PetaniEvent.OnRefresh)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isRefreshing)
        assertEquals(errorMsg, state.errorMessage)
        job.cancel()
    }

    @Test
    fun `BottomSheet should update state correctly`() = runTest {
        every { userPreferences.userRole } returns flowOf("penyuluh")
        every { getPetaniListUseCase(any(), any()) } returns flowOf(Resource.Success(emptyList()))
        every { connectivityObserver.observe() } returns flowOf(ConnectivityObserver.Status.Available)

        viewModel = PetaniListViewModel(getPetaniListUseCase, syncPetaniDataUseCase, deletePetaniUseCase, userPreferences, connectivityObserver, pdfService)
        viewModel.uiState.test {
            awaitItem()
            val selectedId = "123"
            viewModel.onEvent(PetaniEvent.OnMoreOptionClick(selectedId))
            val openSheetState = awaitItem()
            assertTrue(openSheetState.isBottomSheetVisible)
            assertEquals(selectedId, openSheetState.selectedPetaniId)

            viewModel.onEvent(PetaniEvent.OnBottomSheetDismiss)
            val closeSheetState = awaitItem()
            assertFalse(closeSheetState.isBottomSheetVisible)

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `Delete success should remove item and show success message`() = runTest {
        val itemToDelete = Petani("1", "Delete ini", "", "", "", "", "", "", 0.0, "", "", "", "")
        val initialList = listOf(itemToDelete)

        every { userPreferences.userRole } returns flowOf("penyuluh")
        every { getPetaniListUseCase(any(), any()) } returns flowOf(Resource.Success(initialList))
        every { connectivityObserver.observe() } returns flowOf(ConnectivityObserver.Status.Available)
        coEvery { deletePetaniUseCase("1") } returns Resource.Success(Unit)

        viewModel = PetaniListViewModel(getPetaniListUseCase, syncPetaniDataUseCase, deletePetaniUseCase, userPreferences, connectivityObserver, pdfService)
        viewModel.uiState.test {
            awaitItem()
            viewModel.onEvent(PetaniEvent.OnMoreOptionClick("1"))
            val sheetState = awaitItem()
            assertTrue(sheetState.isBottomSheetVisible)
            assertEquals("1", sheetState.selectedPetaniId)

            viewModel.onEvent(PetaniEvent.OnDeleteClick)
            val dialogState = awaitItem()
            assertFalse(dialogState.isBottomSheetVisible)
            assertTrue(dialogState.isDeleteDialogVisible)

            viewModel.onEvent(PetaniEvent.OnDeleteConfirm)

            var currentState = awaitItem()
            while (currentState.isLoading) {
                currentState = awaitItem()
            }

            assertFalse("Loading harus sudah false", currentState.isLoading)
            assertFalse("Dialog harus sudah tutup", currentState.isDeleteDialogVisible)
            assertTrue("List harus kosong setelah delete", currentState.petaniList.isEmpty())
            assertEquals("Pesan sukses harus muncul", "Data berhasil dihapus", currentState.successMessage)
            assertNull("Selected ID harus di-reset", currentState.selectedPetaniId)

            cancelAndIgnoreRemainingEvents()
        }

        coVerify { deletePetaniUseCase("1") }
    }

    @Test
    fun `Couldn't Delete if user role is Penanggung Jawab`() = runTest {
        every { userPreferences.userRole } returns flowOf("penanggung jawab")
        every { getPetaniListUseCase(any(), any()) } returns flowOf(Resource.Success(emptyList()))
        every { connectivityObserver.observe() } returns flowOf(ConnectivityObserver.Status.Available)

        viewModel = PetaniListViewModel(getPetaniListUseCase, syncPetaniDataUseCase, deletePetaniUseCase, userPreferences, connectivityObserver, pdfService)
        val job = launch { viewModel.uiState.collect {} }
        advanceUntilIdle()

        viewModel.onEvent(PetaniEvent.OnDeleteClick)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse("Dialog delete tidak boleh muncul untuk Penanggung Jawab", state.isDeleteDialogVisible)

        viewModel.onEvent(PetaniEvent.OnMoreOptionClick("1"))
        viewModel.onEvent(PetaniEvent.OnDeleteConfirm)
        advanceUntilIdle()

        val errorState = viewModel.uiState.value
        assertEquals("Anda tidak memiliki akses hapus", errorState.errorMessage)

        job.cancel()
    }

    @Test
    fun `Delete error should show error message`() = runTest {
        val itemToDelete = Petani("1", "Error", "", "", "", "", "", "", 0.0, "", "", "", "")
        every { userPreferences.userRole } returns flowOf("penyuluh")
        every { getPetaniListUseCase(any(), any()) } returns flowOf(Resource.Success(listOf(itemToDelete)))
        every { connectivityObserver.observe() } returns flowOf(ConnectivityObserver.Status.Available)

        val errorMsg = "Gagal hapus bro"
        coEvery { deletePetaniUseCase("1") } returns Resource.Error(errorMsg)

        viewModel = PetaniListViewModel(getPetaniListUseCase, syncPetaniDataUseCase, deletePetaniUseCase, userPreferences, connectivityObserver, pdfService)
        viewModel.uiState.test {
            awaitItem()
            viewModel.onEvent(PetaniEvent.OnMoreOptionClick("1"))
            awaitItem()
            viewModel.onEvent(PetaniEvent.OnDeleteClick)
            awaitItem()
            viewModel.onEvent(PetaniEvent.OnDeleteConfirm)

            var errorState = awaitItem()
            while (errorState.errorMessage == null) {
                errorState = awaitItem()
            }

            assertFalse(errorState.isLoading)
            assertEquals(errorMsg, errorState.errorMessage)
            assertEquals(1, errorState.petaniList.size)

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `Delete when offline should not call usecase and show error message`() = runTest {
        val itemToDelete = Petani("1", "Offline Delete", "", "", "", "", "", "", 0.0, "", "", "", "")
        every { userPreferences.userRole } returns flowOf("penyuluh")
        every { getPetaniListUseCase(any(), any()) } returns flowOf(Resource.Success(listOf(itemToDelete)))

        every { connectivityObserver.observe() } returns flowOf(ConnectivityObserver.Status.Lost)

        viewModel = PetaniListViewModel(getPetaniListUseCase, syncPetaniDataUseCase, deletePetaniUseCase, userPreferences, connectivityObserver, pdfService)
        viewModel.uiState.test {
            awaitItem()

            viewModel.onEvent(PetaniEvent.OnMoreOptionClick("1"))
            awaitItem()

            viewModel.onEvent(PetaniEvent.OnDeleteClick)
            awaitItem()

            viewModel.onEvent(PetaniEvent.OnDeleteConfirm)

            var errorState = awaitItem()
            while (errorState.errorMessage == null) {
                errorState = awaitItem()
            }

            assertEquals("Tidak ada koneksi internet", errorState.errorMessage)
            assertFalse(errorState.isDeleteDialogVisible)

            cancelAndIgnoreRemainingEvents()
        }

        coVerify(exactly = 0) { deletePetaniUseCase(any()) }
    }

    @Test
    fun `Dismiss should clear states`() = runTest {
        every { userPreferences.userRole } returns flowOf("penyuluh")
        every { getPetaniListUseCase(any(), any()) } returns flowOf(Resource.Success(emptyList()))
        every { connectivityObserver.observe() } returns flowOf(ConnectivityObserver.Status.Available)
        coEvery { deletePetaniUseCase("1") } returns Resource.Error("Error Hapus")

        viewModel = PetaniListViewModel(getPetaniListUseCase, syncPetaniDataUseCase, deletePetaniUseCase, userPreferences, connectivityObserver, pdfService)
        viewModel.uiState.test {
            awaitItem()
            viewModel.onEvent(PetaniEvent.OnMoreOptionClick("1"))
            awaitItem()
            viewModel.onEvent(PetaniEvent.OnDeleteClick)
            val dialogState = awaitItem()
            assertTrue(dialogState.isDeleteDialogVisible)
            assertEquals("1", dialogState.selectedPetaniId)

            viewModel.onEvent(PetaniEvent.OnDismissDeleteDialog)
            val dialogDismissState = awaitItem()
            assertFalse(dialogDismissState.isDeleteDialogVisible)
            assertNull("Selected ID harus null setelah dialog dismiss", dialogDismissState.selectedPetaniId)

            viewModel.onEvent(PetaniEvent.OnMoreOptionClick("1"))
            awaitItem()
            viewModel.onEvent(PetaniEvent.OnDeleteClick)
            awaitItem()
            viewModel.onEvent(PetaniEvent.OnDeleteConfirm)

            var errorState = awaitItem()
            while (errorState.errorMessage == null) {
                errorState = awaitItem()
            }
            assertEquals("Error Hapus", errorState.errorMessage)

            viewModel.onEvent(PetaniEvent.OnDismissError)
            val errorDismissState = awaitItem()
            assertNull("Error message harus hilang", errorDismissState.errorMessage)
            cancelAndIgnoreRemainingEvents()
        }
    }


    @Test
    fun `Export List success should call pdfService with all data`() = runTest {
        val dummyData = listOf(
            Petani(
                id = "1",
                name = "Tepani Canz",
                identityNumber = "1802045310040001",
                kphName = "KPH Tahura",
                kthName = "KTH Sukses",
            ),
            Petani(
                id = "2",
                name = "Mawar",
                identityNumber = "1802045310040001",
                kphName = "KPH Tahura",
                kthName = "KTH Sukses",
            ),
        )

        every { userPreferences.userRole } returns flowOf("penyuluh")
        every { getPetaniListUseCase(any(), any()) } returns flowOf(Resource.Success(dummyData))
        every { connectivityObserver.observe() } returns flowOf(ConnectivityObserver.Status.Available)

        coEvery {
            pdfService.generatePdf<Petani>(any(), any(), any(), any(), any())
        } returns Resource.Success("/storage/emulated/0/Download/Data_Pengguna.pdf")

        createViewModel()

        viewModel.uiState.test {
            awaitItem()
            viewModel.onEvent(PetaniEvent.OnExportList)

            var state = awaitItem()
            while (state.isLoading) {
                state = awaitItem()
            }

            assertEquals("/storage/emulated/0/Download/Data_Pengguna.pdf", state.successMessage)
            assertFalse(state.isBottomSheetVisible)

            coVerify {
                pdfService.generatePdf<Petani>(
                    fileName = any(),
                    reportTitle = "LAPORAN DATA PETANI",
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
        val targetPetani = Petani(
            id = targetId,
            name = "Mawar",
            identityNumber = "1802045310040002",
            kphName = "KPH Tahura",
            kthName = "KTH Sukses",
        )
        val dummyData = listOf(
            Petani(
                id = "1",
                name = "Tepani Canz",
                identityNumber = "1802045310040001",
                kphName = "KPH Tahura",
                kthName = "KTH Sukses",
            ),
            targetPetani,
        )

        every { userPreferences.userRole } returns flowOf("penyuluh")
        every { getPetaniListUseCase(any(), any()) } returns flowOf(Resource.Success(dummyData))
        every { connectivityObserver.observe() } returns flowOf(ConnectivityObserver.Status.Available)

        coEvery {
            pdfService.generatePdf<Petani>(any(), any(), any(), any(), any())
        } returns Resource.Success("File Saved")

        createViewModel()

        viewModel.uiState.test {
            awaitItem()
            viewModel.onEvent(PetaniEvent.OnExportDetail(targetId))

            var state = awaitItem()
            while (state.isLoading) {
                state = awaitItem()
            }

            assertEquals("File Saved", state.successMessage)

            coVerify {
                pdfService.generatePdf<Petani>(
                    fileName = any(),
                    reportTitle = "DETAIL DATA PETANI",
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
        val dummyData = listOf(
            Petani(
                id = "1",
                name = "Tepani Canz",
                identityNumber = "1802045310040001",
                kphName = "KPH Tahura",
                kthName = "KTH Sukses",
            )
        )
        every { userPreferences.userRole } returns flowOf("penyuluh")
        every { getPetaniListUseCase(any(), any()) } returns flowOf(Resource.Success(dummyData))
        every { connectivityObserver.observe() } returns flowOf(ConnectivityObserver.Status.Available)

        val errorMsg = "Permission Denied"
        coEvery {
            pdfService.generatePdf<UserDetail>(any(), any(), any(), any(), any())
        } returns Resource.Error(errorMsg)

        createViewModel()

        viewModel.uiState.test {
            awaitItem()

            viewModel.onEvent(PetaniEvent.OnExportList)

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
        every { getPetaniListUseCase(any(), any()) } returns flowOf(Resource.Success(emptyList()))
        every { connectivityObserver.observe() } returns flowOf(ConnectivityObserver.Status.Available)

        createViewModel()

        viewModel.uiState.test {
            awaitItem()

            viewModel.onEvent(PetaniEvent.OnExportList)
            var state = awaitItem()
            if(state.isLoading) state = awaitItem()

            assertEquals("Tidak ada data untuk diekspor", state.errorMessage)

            coVerify(exactly = 0) {
                pdfService.generatePdf<UserDetail>(any(), any(), any(), any(), any())
            }

            cancelAndIgnoreRemainingEvents()
        }
    }
}