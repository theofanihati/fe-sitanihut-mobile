package com.dishut_lampung.sitanihut.presentation.user_management

import app.cash.turbine.test
import com.dishut_lampung.sitanihut.data.local.UserPreferences
import com.dishut_lampung.sitanihut.domain.model.UserDetail
import com.dishut_lampung.sitanihut.domain.usecase.user_management.DeleteUserUseCase
import com.dishut_lampung.sitanihut.domain.usecase.user_management.GetUserListUseCase
import com.dishut_lampung.sitanihut.domain.usecase.user_management.SyncUserDataUseCase
import com.dishut_lampung.sitanihut.presentation.user_management.list.UserEvent
import com.dishut_lampung.sitanihut.presentation.user_management.list.UserListViewModel
import com.dishut_lampung.sitanihut.util.ConnectivityObserver
import com.dishut_lampung.sitanihut.util.MainCoroutineRule
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
class UserListViewModelTest {

    @get:Rule
    val mainCoroutineRule = MainCoroutineRule()

    private val getUserListUseCase: GetUserListUseCase = mockk(relaxed = true)
    private val syncUserDataUseCase: SyncUserDataUseCase = mockk(relaxed = true)
    private val deleteUserUseCase: DeleteUserUseCase = mockk(relaxed = true)
    private val userPreferences: UserPreferences = mockk(relaxed = true)
    private val connectivityObserver: ConnectivityObserver = mockk(relaxed = true)
    private lateinit var viewModel: UserListViewModel

    @Test
    fun `init should load User list and observe connectivity`() = runTest {
        val dummyUser = listOf(
            UserDetail(
                id = "1",
                name = "Tepani Canz",
                kphName = "KPH Tahura",
                kthName = "KTH Sukses",
                role = "penyuluh",
                gender = "Laki-laki",
            ),
        )

        every { userPreferences.userRole } returns flowOf("penyuluh")
        every { getUserListUseCase("penyuluh", "") } returns flowOf(Resource.Success(dummyUser))
        every { connectivityObserver.observe() } returns flowOf(ConnectivityObserver.Status.Available)

        viewModel = UserListViewModel(getUserListUseCase, syncUserDataUseCase, deleteUserUseCase, userPreferences, connectivityObserver)

        viewModel.uiState.test {
            val state = awaitItem()
            assertEquals(dummyUser, state.userList)
            assertTrue(state.isOnline)

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `when connectivity is lost, isOnline should be false`() = runTest {
        every { userPreferences.userRole } returns flowOf("penyuluh")
        every { getUserListUseCase(any(), any()) } returns flowOf(Resource.Success(emptyList()))
        every { connectivityObserver.observe() } returns flowOf(ConnectivityObserver.Status.Lost)

        viewModel = UserListViewModel(getUserListUseCase, syncUserDataUseCase, deleteUserUseCase, userPreferences, connectivityObserver)
        viewModel.uiState.test {
            val state = awaitItem()
            assertFalse(state.isOnline)

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `search should filter data locally`() = runTest {
        val listData = listOf(
            UserDetail(
                id = "1",
                name = "Tepani Canz",
                kphName = "KPH Tahura",
                kthName = "KTH Sukses",
                role = "penyuluh",
                gender = "Laki-laki",
            ),
            UserDetail(
                id = "2",
                name = "Hati",
                kphName = "KPH Tahura",
                kthName = "KTH Sukses",
                role = "penyuluh",
                gender = "Laki-laki",
            ),
        )

        every { userPreferences.userRole } returns flowOf("penyuluh")
        every { getUserListUseCase("penyuluh", "") } returns flowOf(Resource.Success(listData))
        every { connectivityObserver.observe() } returns flowOf(ConnectivityObserver.Status.Available)

        viewModel = UserListViewModel(getUserListUseCase, syncUserDataUseCase, deleteUserUseCase, userPreferences, connectivityObserver)
        viewModel.uiState.test {
            awaitItem()

            viewModel.onEvent(UserEvent.OnSearchQueryChange("Hati"))

            val filteredState = awaitItem()
            assertEquals(1, filteredState.userList.size)
            assertEquals("Hati", filteredState.userList.first().name)

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `OnRefresh should trigger fetch data again`() = runTest {
        every { userPreferences.userRole } returns flowOf("penyuluh")
        every { getUserListUseCase("penyuluh", "") } returns flowOf(Resource.Success(emptyList()))
        every { connectivityObserver.observe() } returns flowOf(ConnectivityObserver.Status.Available)

        viewModel = UserListViewModel(getUserListUseCase, syncUserDataUseCase, deleteUserUseCase, userPreferences, connectivityObserver)
        viewModel.onEvent(UserEvent.OnRefresh)

        coVerify(exactly = 1) { getUserListUseCase("penyuluh", "") }
        coVerify(exactly = 1) { syncUserDataUseCase() }
    }

    @Test
    fun `onEvent OnRefresh success should update data`() = runTest {
        val successMessage = "Data berhasil diperbarui"
        every { connectivityObserver.observe() } returns flowOf(ConnectivityObserver.Status.Available)
        coEvery { syncUserDataUseCase() } returns Resource.Success(Unit)
        every { userPreferences.userRole } returns flowOf("penyuluh")
        every { getUserListUseCase(any(), any()) } returns flowOf(Resource.Success(emptyList()))

        viewModel = UserListViewModel(getUserListUseCase, syncUserDataUseCase, deleteUserUseCase, userPreferences, connectivityObserver)
        val job = launch { viewModel.uiState.collect{} }

        advanceUntilIdle()

        viewModel.onEvent(UserEvent.OnRefresh)
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
        every { getUserListUseCase(any(), any()) } returns flowOf(Resource.Success(emptyList()))

        viewModel = UserListViewModel(getUserListUseCase, syncUserDataUseCase, deleteUserUseCase, userPreferences, connectivityObserver)
        val job = launch { viewModel.uiState.collect{} }
        advanceUntilIdle()

        viewModel.onEvent(UserEvent.OnRefresh)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isRefreshing)
        assertEquals("Tidak ada koneksi internet", state.errorMessage)

        coVerify(exactly = 0) { syncUserDataUseCase() }
        job.cancel()
    }

    @Test
    fun `onEvent OnRefresh should handle sync error`() = runTest {
        val errorMsg = "Gagal sinkronisasi"
        every { connectivityObserver.observe() } returns flowOf(ConnectivityObserver.Status.Available)
        coEvery { syncUserDataUseCase() } returns Resource.Error(errorMsg)
        every { userPreferences.userRole } returns flowOf("penyuluh")
        every { getUserListUseCase(any(), any()) } returns flowOf(Resource.Success(emptyList()))

        viewModel = UserListViewModel(getUserListUseCase, syncUserDataUseCase, deleteUserUseCase, userPreferences, connectivityObserver)
        val job = launch { viewModel.uiState.collect{} }

        advanceUntilIdle()

        viewModel.onEvent(UserEvent.OnRefresh)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isRefreshing)
        assertEquals(errorMsg, state.errorMessage)
        job.cancel()
    }

    @Test
    fun `BottomSheet should update state correctly`() = runTest {
        every { userPreferences.userRole } returns flowOf("penyuluh")
        every { getUserListUseCase(any(), any()) } returns flowOf(Resource.Success(emptyList()))
        every { connectivityObserver.observe() } returns flowOf(ConnectivityObserver.Status.Available)

        viewModel = UserListViewModel(getUserListUseCase, syncUserDataUseCase, deleteUserUseCase, userPreferences, connectivityObserver)
        viewModel.uiState.test {
            awaitItem()
            val selectedId = "123"
            viewModel.onEvent(UserEvent.OnMoreOptionClick(selectedId))
            val openSheetState = awaitItem()
            assertTrue(openSheetState.isBottomSheetVisible)
            assertEquals(selectedId, openSheetState.selectedUserId)

            viewModel.onEvent(UserEvent.OnBottomSheetDismiss)
            val closeSheetState = awaitItem()
            assertFalse(closeSheetState.isBottomSheetVisible)

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `Delete success should remove item and show success message`() = runTest {
        val itemToDelete = UserDetail(
            id = "1",
            name = "Delete ini",
            kphName = "",
            kthName = "",
            role = "",
            gender = "",
        )
        val initialList = listOf(itemToDelete)

        every { userPreferences.userRole } returns flowOf("penyuluh")
        every { getUserListUseCase(any(), any()) } returns flowOf(Resource.Success(initialList))
        every { connectivityObserver.observe() } returns flowOf(ConnectivityObserver.Status.Available)
        coEvery { deleteUserUseCase("1") } returns Resource.Success(Unit)

        viewModel = UserListViewModel(getUserListUseCase, syncUserDataUseCase, deleteUserUseCase, userPreferences, connectivityObserver)
        viewModel.uiState.test {
            awaitItem()
            viewModel.onEvent(UserEvent.OnMoreOptionClick("1"))
            val sheetState = awaitItem()
            assertTrue(sheetState.isBottomSheetVisible)
            assertEquals("1", sheetState.selectedUserId)

            viewModel.onEvent(UserEvent.OnDeleteClick)
            val dialogState = awaitItem()
            assertFalse(dialogState.isBottomSheetVisible)
            assertTrue(dialogState.isDeleteDialogVisible)

            viewModel.onEvent(UserEvent.OnDeleteConfirm)

            var currentState = awaitItem()
            while (currentState.isLoading) {
                currentState = awaitItem()
            }

            assertFalse("Loading harus sudah false", currentState.isLoading)
            assertFalse("Dialog harus sudah tutup", currentState.isDeleteDialogVisible)
            assertTrue("List harus kosong setelah delete", currentState.userList.isEmpty())
            assertEquals("Pesan sukses harus muncul", "Data berhasil dihapus", currentState.successMessage)
            assertNull("Selected ID harus di-reset", currentState.selectedUserId)

            cancelAndIgnoreRemainingEvents()
        }

        coVerify { deleteUserUseCase("1") }
    }

    @Test
    fun `Couldn't Delete if user role is Penanggung Jawab`() = runTest {
        every { userPreferences.userRole } returns flowOf("penanggung jawab")
        every { getUserListUseCase(any(), any()) } returns flowOf(Resource.Success(emptyList()))
        every { connectivityObserver.observe() } returns flowOf(ConnectivityObserver.Status.Available)

        viewModel = UserListViewModel(getUserListUseCase, syncUserDataUseCase, deleteUserUseCase, userPreferences, connectivityObserver)
        val job = launch { viewModel.uiState.collect {} }
        advanceUntilIdle()

        viewModel.onEvent(UserEvent.OnDeleteClick)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse("Dialog delete tidak boleh muncul untuk Penanggung Jawab", state.isDeleteDialogVisible)

        viewModel.onEvent(UserEvent.OnMoreOptionClick("1"))
        viewModel.onEvent(UserEvent.OnDeleteConfirm)
        advanceUntilIdle()

        val errorState = viewModel.uiState.value
        assertEquals("Anda tidak memiliki akses hapus", errorState.errorMessage)

        job.cancel()
    }

    @Test
    fun `Delete error should show error message`() = runTest {
        val itemToDelete = UserDetail(
            id = "1",
            name = "Error",
            kphName = "",
            kthName = "",
            role = "",
            gender = "",
        )
        every { userPreferences.userRole } returns flowOf("penyuluh")
        every { getUserListUseCase(any(), any()) } returns flowOf(Resource.Success(listOf(itemToDelete)))
        every { connectivityObserver.observe() } returns flowOf(ConnectivityObserver.Status.Available)

        val errorMsg = "Gagal hapus bro"
        coEvery { deleteUserUseCase("1") } returns Resource.Error(errorMsg)

        viewModel = UserListViewModel(getUserListUseCase, syncUserDataUseCase, deleteUserUseCase, userPreferences, connectivityObserver)
        viewModel.uiState.test {
            awaitItem()
            viewModel.onEvent(UserEvent.OnMoreOptionClick("1"))
            awaitItem()
            viewModel.onEvent(UserEvent.OnDeleteClick)
            awaitItem()
            viewModel.onEvent(UserEvent.OnDeleteConfirm)

            var errorState = awaitItem()
            while (errorState.errorMessage == null) {
                errorState = awaitItem()
            }

            assertFalse(errorState.isLoading)
            assertEquals(errorMsg, errorState.errorMessage)
            assertEquals(1, errorState.userList.size)

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `Delete when offline should not call usecase and show error message`() = runTest {
        val itemToDelete = UserDetail(
            id = "1",
            name = "Offline Delete",
            kphName = "",
            kthName = "",
            role = "",
            gender = "",
        )
        every { userPreferences.userRole } returns flowOf("penyuluh")
        every { getUserListUseCase(any(), any()) } returns flowOf(Resource.Success(listOf(itemToDelete)))

        every { connectivityObserver.observe() } returns flowOf(ConnectivityObserver.Status.Lost)

        viewModel = UserListViewModel(getUserListUseCase, syncUserDataUseCase, deleteUserUseCase, userPreferences, connectivityObserver)
        viewModel.uiState.test {
            awaitItem()

            viewModel.onEvent(UserEvent.OnMoreOptionClick("1"))
            awaitItem()

            viewModel.onEvent(UserEvent.OnDeleteClick)
            awaitItem()

            viewModel.onEvent(UserEvent.OnDeleteConfirm)

            var errorState = awaitItem()
            while (errorState.errorMessage == null) {
                errorState = awaitItem()
            }

            assertEquals("Tidak ada koneksi internet", errorState.errorMessage)
            assertFalse(errorState.isDeleteDialogVisible)

            cancelAndIgnoreRemainingEvents()
        }

        coVerify(exactly = 0) { deleteUserUseCase(any()) }
    }

    @Test
    fun `Dismiss should clear states`() = runTest {
        every { userPreferences.userRole } returns flowOf("penyuluh")
        every { getUserListUseCase(any(), any()) } returns flowOf(Resource.Success(emptyList()))
        every { connectivityObserver.observe() } returns flowOf(ConnectivityObserver.Status.Available)
        coEvery { deleteUserUseCase("1") } returns Resource.Error("Error Hapus")

        viewModel = UserListViewModel(getUserListUseCase, syncUserDataUseCase, deleteUserUseCase, userPreferences, connectivityObserver)
        viewModel.uiState.test {
            awaitItem()
            viewModel.onEvent(UserEvent.OnMoreOptionClick("1"))
            awaitItem()
            viewModel.onEvent(UserEvent.OnDeleteClick)
            val dialogState = awaitItem()
            assertTrue(dialogState.isDeleteDialogVisible)
            assertEquals("1", dialogState.selectedUserId)

            viewModel.onEvent(UserEvent.OnDismissDeleteDialog)
            val dialogDismissState = awaitItem()
            assertFalse(dialogDismissState.isDeleteDialogVisible)
            assertNull("Selected ID harus null setelah dialog dismiss", dialogDismissState.selectedUserId)

            viewModel.onEvent(UserEvent.OnMoreOptionClick("1"))
            awaitItem()
            viewModel.onEvent(UserEvent.OnDeleteClick)
            awaitItem()
            viewModel.onEvent(UserEvent.OnDeleteConfirm)

            var errorState = awaitItem()
            while (errorState.errorMessage == null) {
                errorState = awaitItem()
            }
            assertEquals("Error Hapus", errorState.errorMessage)

            viewModel.onEvent(UserEvent.OnDismissError)
            val errorDismissState = awaitItem()
            assertNull("Error message harus hilang", errorDismissState.errorMessage)
            cancelAndIgnoreRemainingEvents()
        }
    }
}