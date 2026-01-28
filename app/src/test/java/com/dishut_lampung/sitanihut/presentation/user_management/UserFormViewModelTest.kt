package com.dishut_lampung.sitanihut.presentation.user_management

import androidx.lifecycle.SavedStateHandle
import com.dishut_lampung.sitanihut.data.local.UserPreferences
import com.dishut_lampung.sitanihut.domain.model.Kph
import com.dishut_lampung.sitanihut.domain.model.Kth
import com.dishut_lampung.sitanihut.domain.model.Role
import com.dishut_lampung.sitanihut.domain.model.UserDetail
import com.dishut_lampung.sitanihut.domain.usecase.kph.GetKphListUseCase
import com.dishut_lampung.sitanihut.domain.usecase.kth.GetKthListUseCase
import com.dishut_lampung.sitanihut.domain.usecase.profile.GetMyProfileUseCase
import com.dishut_lampung.sitanihut.domain.usecase.role.GetRolesUseCase
import com.dishut_lampung.sitanihut.domain.usecase.user_management.CreateUserUseCase
import com.dishut_lampung.sitanihut.domain.usecase.user_management.GetUserDetailUseCase
import com.dishut_lampung.sitanihut.domain.usecase.user_management.UpdateUserUseCase
import com.dishut_lampung.sitanihut.domain.usecase.user_management.ValidateUserManagementInputUseCase
import com.dishut_lampung.sitanihut.domain.validator.ListValidationResult
import com.dishut_lampung.sitanihut.presentation.petani.form.PetaniFormEvent
import com.dishut_lampung.sitanihut.presentation.shared.components.animations.MessageType
import com.dishut_lampung.sitanihut.presentation.user_management.form.UserFormEvent
import com.dishut_lampung.sitanihut.presentation.user_management.form.UserFormViewModel
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
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@ExperimentalCoroutinesApi
class UserFormViewModelTest {
    @get:Rule
    val mainCoroutineRule = MainCoroutineRule()

    private lateinit var viewModel: UserFormViewModel

    private val createUserUseCase: CreateUserUseCase = mockk()
    private val getUserDetailUseCase: GetUserDetailUseCase  = mockk()
    private val updateUserUseCase: UpdateUserUseCase = mockk()
    private val validateUserManagementInputUseCase: ValidateUserManagementInputUseCase = mockk()
    private val getKphListUseCase: GetKphListUseCase = mockk()
    private val getKthListUseCase: GetKthListUseCase = mockk()
    private val getRolesUseCase: GetRolesUseCase = mockk()
    private val userPreferences: UserPreferences = mockk()
    private val connectivityObserver: ConnectivityObserver = mockk()
    private val savedStateHandle: SavedStateHandle = mockk(relaxed = true)
    private val getMyProfileUseCase: GetMyProfileUseCase = mockk(relaxed = true)

    private val dummyKphList = listOf(Kph("1", "KPH Batutegi"), Kph("2", "KPH Liwa"))
    private val dummyKthList = listOf(
        Kth(id = "10", name = "KTH A", kphId = "1", kphName = "KPH Batutegi", desa = "", kecamatan = "", kabupaten = "", coordinator = "", whatsappNumber = ""),
        Kth(id = "11", name = "KTH B", kphId = "2", kphName = "KPH Liwa", desa = "", kecamatan = "", kabupaten = "", coordinator = "", whatsappNumber = "")
    )
    private val dummyRoles = listOf(
        Role("role-1", "penanggung jawab"),
        Role("role-2", "petani"),
        Role("role-3", "penyuluh")
    )
    private val dummyUserDetail = UserDetail(
        id = "user123", name = "User Lama", email = "lama@test.com",
        roleId = "role-2", role = "petani", gender = "wanita",
        kphId = "1", kphName = "KPH Batutegi", identityNumber = "1234567890123456",
        kthId = "10", kthName = "KTH A", landArea = 2.5
    )

    @Before
    fun setUp() {
        every { savedStateHandle.get<String>("id") } returns null
        every { connectivityObserver.observe() } returns flowOf(ConnectivityObserver.Status.Available)
        every { userPreferences.userRole } returns flowOf("penyuluh")
        every { userPreferences.userId } returns flowOf("user-123")
        every { getRolesUseCase() } returns flowOf(Resource.Success(dummyRoles))
        every { getKphListUseCase() } returns flowOf(dummyKphList)
        every { getKthListUseCase(any(), any()) } returns flowOf(Resource.Success(dummyKthList))
        every { validateUserManagementInputUseCase.execute(any()) } returns ListValidationResult(true)

        viewModel = UserFormViewModel(
            createUserUseCase,
            getUserDetailUseCase,
            updateUserUseCase,
            validateUserManagementInputUseCase,
            getKphListUseCase,
            getKthListUseCase,
            getRolesUseCase,
            getMyProfileUseCase,
            userPreferences,
            connectivityObserver,
            savedStateHandle,
        )
    }

    @Test
    fun `init loads roles and sets default role to petani`() = runTest {
        advanceUntilIdle()
        val state = viewModel.uiState.value

        assertEquals("petani", state.roleName)
        assertEquals("role-2", state.roleId)
        assertEquals(2, state.kphOptions.size)
    }

    @Test
    fun `init with id loads existing user data correctly`() = runTest {
        val editStateHandle = mockk<SavedStateHandle>(relaxed = true)
        every { editStateHandle.get<String>("id") } returns "user123"

        every { getUserDetailUseCase("user123") } returns flowOf(Resource.Success(dummyUserDetail))
        val viewModelTest = UserFormViewModel(
            createUserUseCase,
            getUserDetailUseCase,
            updateUserUseCase,
            validateUserManagementInputUseCase,
            getKphListUseCase,
            getKthListUseCase,
            getRolesUseCase,
            getMyProfileUseCase,
            userPreferences,
            connectivityObserver,
            editStateHandle
        )
        advanceUntilIdle()

        val state = viewModelTest.uiState.value

        assertTrue(state.isEditMode)
        assertEquals("User Lama", state.name)
        assertEquals("lama@test.com", state.email)
        assertEquals("2.5", state.landArea)
        assertEquals("", state.password)
        assertEquals("KTH A", state.selectedKthName)
    }

    @Test
    fun `load existing user failure shows error state`() = runTest {
        val editStateHandle = mockk<SavedStateHandle>(relaxed = true)
        every { editStateHandle.get<String>("id") } returns "user-error"

        val errorMsg = "Gagal mengambil data user"
        every { getUserDetailUseCase("user-error") } returns flowOf(Resource.Error(errorMsg))
        val viewModelTest = UserFormViewModel(
            createUserUseCase,
            getUserDetailUseCase,
            updateUserUseCase,
            validateUserManagementInputUseCase,
            getKphListUseCase,
            getKthListUseCase,
            getRolesUseCase,
            getMyProfileUseCase,
            userPreferences,
            connectivityObserver,
            editStateHandle
        )

        advanceUntilIdle()

        assertFalse(viewModelTest.uiState.value.isLoading)
        assertEquals(errorMsg, viewModelTest.uiState.value.error)
    }

    @Test
    fun `submit existing user calls update usecase with correct keys`() = runTest {
        val editStateHandle = mockk<SavedStateHandle>(relaxed = true)
        every { editStateHandle.get<String>("id") } returns "user123"
        every { getUserDetailUseCase("user123") } returns flowOf(Resource.Success(dummyUserDetail))
        coEvery { updateUserUseCase(any(), any()) } returns Resource.Success(Unit)

        val viewModelTest = UserFormViewModel(
            createUserUseCase,
            getUserDetailUseCase,
            updateUserUseCase,
            validateUserManagementInputUseCase,
            getKphListUseCase,
            getKthListUseCase,
            getRolesUseCase,
            getMyProfileUseCase,
            userPreferences,
            connectivityObserver,
            editStateHandle
        )
        advanceUntilIdle()

        viewModelTest.onEvent(UserFormEvent.OnNameChange("User Baru"))
        viewModelTest.onEvent(UserFormEvent.OnPasswordChange("PasswordBaru123"))
        viewModelTest.onEvent(UserFormEvent.OnSubmit)
        advanceUntilIdle()

        coVerify {
            updateUserUseCase(
                eq("user123"),
                match { map ->
                    map["nama_user"] == "User Baru" &&
                            map["password"] == "PasswordBaru123"
                }
            )
        }
        coVerify(exactly = 0) { createUserUseCase(any()) }
        assertEquals("Berhasil disimpan!", viewModelTest.uiState.value.successMessage)
    }

    @Test
    fun `submit existing user DOES NOT send password if empty`() = runTest {
        val editStateHandle = mockk<SavedStateHandle>(relaxed = true)
        every { editStateHandle.get<String>("id") } returns "user123"
        every { getUserDetailUseCase("user123") } returns flowOf(Resource.Success(dummyUserDetail))
        coEvery { updateUserUseCase(any(), any()) } returns Resource.Success(Unit)
        val viewModelTest = UserFormViewModel(
            createUserUseCase,
            getUserDetailUseCase,
            updateUserUseCase,
            validateUserManagementInputUseCase,
            getKphListUseCase,
            getKthListUseCase,
            getRolesUseCase,
            getMyProfileUseCase,
            userPreferences,
            connectivityObserver,
            editStateHandle
        )
        advanceUntilIdle()

        viewModelTest.onEvent(UserFormEvent.OnNameChange("User Baru Saja"))
        viewModelTest.onEvent(UserFormEvent.OnSubmit)
        advanceUntilIdle()

        coVerify {
            updateUserUseCase(
                eq("user123"),
                match { map ->
                    map["nama_user"] == "User Baru Saja" &&
                            !map.containsKey("password")
                }
            )
        }
    }

    @Test
    fun `submit existing user DOES NOT call update usecase WHEN no data changed`() = runTest {
        val editStateHandle = mockk<SavedStateHandle>(relaxed = true)
        val dummyUserDetail = UserDetail(
            id = "user123",
            name = "User Lama",
            email = "lama@test.com",
            address = "",
            whatsAppNumber = "",
            lastEducation = "",
            sideJob = "",
            landArea = 2.5,
            role = "petani",
            kphId = "kph-1",
            kthId = "kth-1",
            roleId = "role-2",  gender = "wanita",
            kphName = "KPH Batutegi", identityNumber = "1234567890123456",
            kthName = "KTH A"
        )
        every { editStateHandle.get<String>("id") } returns "user123"
        every { getUserDetailUseCase("user123") } returns flowOf(Resource.Success(dummyUserDetail))
        val viewModelTest = UserFormViewModel(
            createUserUseCase,
            getUserDetailUseCase,
            updateUserUseCase,
            validateUserManagementInputUseCase,
            getKphListUseCase,
            getKthListUseCase,
            getRolesUseCase,
            getMyProfileUseCase,
            userPreferences,
            connectivityObserver,
            editStateHandle
        )
        advanceUntilIdle()

        viewModelTest.onEvent(UserFormEvent.OnSubmit)
        advanceUntilIdle()

        coVerify(exactly = 0) { updateUserUseCase(any(), any()) }
        assertEquals("Tidak ada perubahan data", viewModelTest.uiState.value.successMessage)
    }

    @Test
    fun `kph selection resets kth and filters kth options`() = runTest {
        advanceUntilIdle()

        val selectedKph = dummyKphList[0]
        viewModel.onEvent(UserFormEvent.OnKphSelected(selectedKph))
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals("1", state.selectedKphId)
        assertEquals("KPH Batutegi", state.selectedKphName)

        assertEquals(1, state.kthOptions.size)
        assertEquals("KTH A", state.kthOptions[0].name)
    }

    @Test
    fun `kph search text filters options locally`() = runTest {
        advanceUntilIdle()
        viewModel.onEvent(UserFormEvent.OnKphSearchTextChange("Liwa"))
        val state = viewModel.uiState.value

        assertEquals("Liwa", state.selectedKphName)
        assertEquals(1, state.kphOptions.size)
        assertEquals("KPH Liwa", state.kphOptions[0].name)

        assertEquals("", state.selectedKphId)
    }

    @Test
    fun `kth search text filters options locally`() = runTest {
        advanceUntilIdle()
        val selectedKph = dummyKphList[0]
        viewModel.onEvent(UserFormEvent.OnKphSelected(selectedKph))
        advanceUntilIdle()

        viewModel.onEvent(UserFormEvent.OnKthSearchTextChange("KTH Z"))
        assertTrue(viewModel.uiState.value.kthOptions.isEmpty())

        viewModel.onEvent(UserFormEvent.OnKthSearchTextChange("KTH A"))
        assertEquals(1, viewModel.uiState.value.kthOptions.size)
        assertEquals("KTH A", viewModel.uiState.value.kthOptions[0].name)
    }

    @Test
    fun `kph search text auto selects id if name matches`() = runTest {
        advanceUntilIdle()

        viewModel.onEvent(UserFormEvent.OnKphSearchTextChange("KPH B"))
        val state1 = viewModel.uiState.value
        assertEquals("KPH B", state1.selectedKphName)
        assertEquals("", state1.selectedKphId)

        viewModel.onEvent(UserFormEvent.OnKphSearchTextChange("KPH Batutegi"))
        val state2 = viewModel.uiState.value
        assertEquals("KPH Batutegi", state2.selectedKphName)
        assertEquals("1", state2.selectedKphId)
    }

    @Test
    fun `kth search text auto selects id if name matches`() = runTest {
        advanceUntilIdle()
        val selectedKph = dummyKphList[0]
        viewModel.onEvent(UserFormEvent.OnKphSelected(selectedKph))
        advanceUntilIdle()

        viewModel.onEvent(UserFormEvent.OnKthSearchTextChange("KTH Asal"))
        val state1 = viewModel.uiState.value
        assertEquals("KTH Asal", state1.selectedKthName)
        assertEquals("", state1.selectedKthId)

        viewModel.onEvent(UserFormEvent.OnKthSearchTextChange("KTH A"))
        val state2 = viewModel.uiState.value
        assertEquals("KTH A", state2.selectedKthName)
        assertEquals("10", state2.selectedKthId)
    }

    @Test
    fun `identity number realtime validation logic`() = runTest {

        viewModel.onEvent(UserFormEvent.OnIdentityNumberChange("123"))
        assertEquals("123", viewModel.uiState.value.identityNumber)

        viewModel.onEvent(UserFormEvent.OnIdentityNumberChange("123a"))
        assertEquals("123", viewModel.uiState.value.identityNumber)

        val longString = "12345678901234567"
        viewModel.onEvent(UserFormEvent.OnIdentityNumberChange(longString))
        assertEquals("12345678901234567", viewModel.uiState.value.identityNumber)
    }

    @Test
    fun `whatsapp realtime validation logic`() = runTest {
        viewModel.onEvent(UserFormEvent.OnWhatsAppChange("08123"))
        assertEquals("Nomor telepon minimal 10 digit", viewModel.uiState.value.whatsAppNumberError)

        viewModel.onEvent(UserFormEvent.OnWhatsAppChange("08123456789abc"))
        assertTrue(viewModel.uiState.value.whatsAppNumberError!!.contains("nomor valid"))

        viewModel.onEvent(UserFormEvent.OnWhatsAppChange("081234567890"))
        assertNull(viewModel.uiState.value.whatsAppNumberError)

        val tooLongNumber = "08123456789012345"
        viewModel.onEvent(UserFormEvent.OnWhatsAppChange(tooLongNumber))
        assertEquals("Nomor telepon maksimal 14 digit", viewModel.uiState.value.whatsAppNumberError)
    }

    @Test
    fun `land area realtime validation logic`() = runTest {
        viewModel.onEvent(UserFormEvent.OnLandAreaChange("12"))
        assertEquals("12", viewModel.uiState.value.landArea)

        viewModel.onEvent(UserFormEvent.OnLandAreaChange("12."))
        assertEquals("12.", viewModel.uiState.value.landArea)

        viewModel.onEvent(UserFormEvent.OnLandAreaChange("12.."))
        assertEquals("12.", viewModel.uiState.value.landArea)

        viewModel.onEvent(UserFormEvent.OnLandAreaChange("12.a"))
        assertEquals("12.", viewModel.uiState.value.landArea)
    }

    @Test
    fun `update auth fields updates state correctly`() = runTest {
        viewModel.onEvent(UserFormEvent.OnEmailChange("baru@test.com"))
        assertEquals("baru@test.com", viewModel.uiState.value.email)
        assertNull(viewModel.uiState.value.emailError)

        viewModel.onEvent(UserFormEvent.OnPasswordChange("pass123"))
        assertEquals("pass123", viewModel.uiState.value.password)

        viewModel.onEvent(UserFormEvent.OnConfirmPasswordChange("pass123"))
        assertEquals("pass123", viewModel.uiState.value.confirmPassword)
    }

    @Test
    fun `toggle password visibility updates state`() = runTest {
        assertFalse(viewModel.uiState.value.isPasswordVisible)
        viewModel.onEvent(UserFormEvent.OnTogglePasswordVisibility)
        assertTrue(viewModel.uiState.value.isPasswordVisible)

        assertFalse(viewModel.uiState.value.isConfirmPasswordVisible)
        viewModel.onEvent(UserFormEvent.OnToggleConfirmPasswordVisibility)
        assertTrue(viewModel.uiState.value.isConfirmPasswordVisible)
    }

    @Test
    fun `submit success (create) calls create usecase with correct Role ID`() = runTest {
        coEvery { createUserUseCase(any()) } returns Resource.Success(Unit)

        advanceUntilIdle()

        viewModel.onEvent(UserFormEvent.OnNameChange("User Baru"))
        viewModel.onEvent(UserFormEvent.OnEmailChange("new@test.com"))
        viewModel.onEvent(UserFormEvent.OnPasswordChange("Pass123!"))

        viewModel.onEvent(UserFormEvent.OnSubmit)
        advanceUntilIdle()

        coVerify {
            createUserUseCase(match { input ->
                input.roleId == "role-2" &&
                        input.email == "new@test.com"
            })
        }
        assertEquals("Berhasil disimpan!", viewModel.uiState.value.successMessage)
    }

    @Test
    fun `submit failure (validation error) updates error states`() = runTest {
        val errors = mapOf(
            "email" to "Email wajib diisi",
            "password" to "Password kurang panjang"
        )
        every { validateUserManagementInputUseCase.execute(any()) } returns ListValidationResult(
            successful = false, fieldErrors = errors, errorMessage = "Data tidak valid"
        )

        viewModel.onEvent(UserFormEvent.OnSubmit)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)

        assertEquals("Email wajib diisi", state.emailError)
        assertEquals("Password kurang panjang", state.passwordError)
        assertEquals("Data tidak valid", state.error)

        coVerify(exactly = 0) { createUserUseCase(any()) }
    }

    @Test
    fun `submit failure (offline) shows error`() = runTest {
        val offlineObserver: ConnectivityObserver = mockk()
        every { offlineObserver.observe() } returns flowOf(ConnectivityObserver.Status.Lost)
        val viewModelTest = UserFormViewModel(
            createUserUseCase,
            getUserDetailUseCase,
            updateUserUseCase,
            validateUserManagementInputUseCase,
            getKphListUseCase,
            getKthListUseCase,
            getRolesUseCase,
            getMyProfileUseCase,
            userPreferences,
            offlineObserver,
            savedStateHandle
        )
        advanceUntilIdle()

        viewModelTest.onEvent(UserFormEvent.OnSubmit)

        assertEquals("Tidak ada koneksi internet", viewModelTest.uiState.value.error)
        coVerify(exactly = 0) { createUserUseCase(any()) }
    }

    @Test
    fun `submit failure (API Error) updates error state`() = runTest {
        val errorMsg = "Server Internal Error"
        coEvery { createUserUseCase(any()) } returns Resource.Error(errorMsg)

        viewModel.onEvent(UserFormEvent.OnNameChange("Test"))
        viewModel.onEvent(UserFormEvent.OnEmailChange("test@test.com"))
        viewModel.onEvent(UserFormEvent.OnPasswordChange("Pass123!"))

        viewModel.onEvent(UserFormEvent.OnSubmit)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertEquals(errorMsg, state.error)
        assertNull(state.successMessage)
    }

    @Test
    fun `dialog and message events update state correctly`() = runTest {
        viewModel.onEvent(UserFormEvent.OnShowConfirmDialog)
        assertTrue(viewModel.uiState.value.showConfirmDialog)

        viewModel.onEvent(UserFormEvent.OnDismissConfirmDialog)
        assertFalse(viewModel.uiState.value.showConfirmDialog)

        viewModel.onEvent(UserFormEvent.OnShowUserMessage("Pesan Sukses", MessageType.Success))
        advanceUntilIdle()
        assertEquals("Pesan Sukses", viewModel.uiState.value.successMessage)
        assertNull(viewModel.uiState.value.error)

        viewModel.onEvent(UserFormEvent.OnShowUserMessage("Pesan Error", MessageType.Error))
        advanceUntilIdle()
        assertEquals("Pesan Error", viewModel.uiState.value.error)
        assertNull(viewModel.uiState.value.successMessage)

        viewModel.onEvent(UserFormEvent.OnDismissMessage)
        assertNull(viewModel.uiState.value.error)
        assertNull(viewModel.uiState.value.successMessage)
    }
}