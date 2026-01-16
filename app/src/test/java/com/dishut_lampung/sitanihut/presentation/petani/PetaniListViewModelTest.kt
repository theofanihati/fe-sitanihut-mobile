package com.dishut_lampung.sitanihut.presentation.petani

import androidx.work.WorkManager
import app.cash.turbine.test
import com.dishut_lampung.sitanihut.data.local.UserPreferences
import com.dishut_lampung.sitanihut.domain.model.Petani
import com.dishut_lampung.sitanihut.domain.usecase.petani.DeletePetaniUseCase
import com.dishut_lampung.sitanihut.domain.usecase.petani.GetPetaniListUseCase
import com.dishut_lampung.sitanihut.domain.usecase.petani.SyncPetaniDataUseCase
import com.dishut_lampung.sitanihut.presentation.petani.list.PetaniEvent
import com.dishut_lampung.sitanihut.presentation.petani.list.PetaniListViewModel
import com.dishut_lampung.sitanihut.util.ConnectivityObserver
import com.dishut_lampung.sitanihut.util.MainCoroutineRule
import com.dishut_lampung.sitanihut.util.Resource
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
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
    private lateinit var viewModel: PetaniListViewModel

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

        viewModel = PetaniListViewModel(getPetaniListUseCase, syncPetaniDataUseCase, deletePetaniUseCase, userPreferences, connectivityObserver)

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

        viewModel = PetaniListViewModel(getPetaniListUseCase, syncPetaniDataUseCase, deletePetaniUseCase, userPreferences, connectivityObserver)
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

        viewModel = PetaniListViewModel(getPetaniListUseCase, syncPetaniDataUseCase, deletePetaniUseCase, userPreferences, connectivityObserver)
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

        viewModel = PetaniListViewModel(getPetaniListUseCase, syncPetaniDataUseCase, deletePetaniUseCase, userPreferences, connectivityObserver)
        viewModel.onEvent(PetaniEvent.OnRefresh)

        coVerify(atLeast = 2) { getPetaniListUseCase("penyuluh", "") }
    }

    @Test
    fun `BottomSheet should update state correctly`() = runTest {
        every { userPreferences.userRole } returns flowOf("penyuluh")
        every { getPetaniListUseCase(any(), any()) } returns flowOf(Resource.Success(emptyList()))
        every { connectivityObserver.observe() } returns flowOf(ConnectivityObserver.Status.Available)

        viewModel = PetaniListViewModel(getPetaniListUseCase, syncPetaniDataUseCase, deletePetaniUseCase, userPreferences, connectivityObserver)
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

        viewModel = PetaniListViewModel(getPetaniListUseCase, syncPetaniDataUseCase, deletePetaniUseCase, userPreferences, connectivityObserver)
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
    fun `Delete error should show error message`() = runTest {
        val itemToDelete = Petani("1", "Error", "", "", "", "", "", "", 0.0, "", "", "", "")
        every { userPreferences.userRole } returns flowOf("penyuluh")
        every { getPetaniListUseCase(any(), any()) } returns flowOf(Resource.Success(listOf(itemToDelete)))
        every { connectivityObserver.observe() } returns flowOf(ConnectivityObserver.Status.Available)

        val errorMsg = "Gagal hapus bro"
        coEvery { deletePetaniUseCase("1") } returns Resource.Error(errorMsg)

        viewModel = PetaniListViewModel(getPetaniListUseCase, syncPetaniDataUseCase, deletePetaniUseCase, userPreferences, connectivityObserver)

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

        viewModel = PetaniListViewModel(getPetaniListUseCase, syncPetaniDataUseCase, deletePetaniUseCase, userPreferences, connectivityObserver)

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

        viewModel = PetaniListViewModel(getPetaniListUseCase, syncPetaniDataUseCase, deletePetaniUseCase, userPreferences, connectivityObserver)

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
}