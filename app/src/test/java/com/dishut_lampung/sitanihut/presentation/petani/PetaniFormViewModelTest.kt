package com.dishut_lampung.sitanihut.presentation.petani

import androidx.lifecycle.SavedStateHandle
import com.dishut_lampung.sitanihut.data.local.UserPreferences
import com.dishut_lampung.sitanihut.domain.model.Kph
import com.dishut_lampung.sitanihut.domain.model.Kth
import com.dishut_lampung.sitanihut.domain.model.Petani
import com.dishut_lampung.sitanihut.domain.model.UserDetail
import com.dishut_lampung.sitanihut.domain.usecase.kph.GetKphListUseCase
import com.dishut_lampung.sitanihut.domain.usecase.kth.GetKthListUseCase
import com.dishut_lampung.sitanihut.domain.usecase.petani.CreatePetaniUseCase
import com.dishut_lampung.sitanihut.domain.usecase.petani.GetPetaniDetailUseCase
import com.dishut_lampung.sitanihut.domain.usecase.petani.UpdatePetaniUseCase
import com.dishut_lampung.sitanihut.domain.usecase.petani.ValidatePetaniInputUseCase
import com.dishut_lampung.sitanihut.domain.usecase.profile.GetUserDetailUseCase
import com.dishut_lampung.sitanihut.domain.validator.ListValidationResult
import com.dishut_lampung.sitanihut.presentation.components.animations.MessageType
import com.dishut_lampung.sitanihut.presentation.kth.form.KthFormUiEvent
import com.dishut_lampung.sitanihut.presentation.kth.form.KthFormViewModel
import com.dishut_lampung.sitanihut.presentation.petani.form.PetaniFormEvent
import com.dishut_lampung.sitanihut.presentation.petani.form.PetaniFormViewModel
import com.dishut_lampung.sitanihut.util.ConnectivityObserver
import com.dishut_lampung.sitanihut.util.MainCoroutineRule
import com.dishut_lampung.sitanihut.util.Resource
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@ExperimentalCoroutinesApi
class PetaniFormViewModelTest {

    @get:Rule
    val mainCoroutineRule = MainCoroutineRule()

    private lateinit var viewModel: PetaniFormViewModel

    private val createPetaniUseCase: CreatePetaniUseCase = mockk()
    private val getPetaniDetailUseCase: GetPetaniDetailUseCase = mockk()
    private val updatePetaniUseCase: UpdatePetaniUseCase = mockk()
    private val validatePetaniInputUseCase: ValidatePetaniInputUseCase = mockk()
    private val getKphListUseCase: GetKphListUseCase = mockk()
    private val getKthListUseCase: GetKthListUseCase = mockk()
    private val userPreferences: UserPreferences = mockk()
    private val getUserDetailUseCase: GetUserDetailUseCase = mockk()
    private val connectivityObserver: ConnectivityObserver = mockk()
    private val savedStateHandle: SavedStateHandle = mockk(relaxed = true)

    private val dummyKphList = listOf(Kph("1", "KPH Batutegi"), Kph("2", "KPH Liwa"))
    private val dummyKthList = listOf(
        Kth(
            id = "10",
            name = "KTH A",
            desa = "Desa A",
            kecamatan = "Kec A",
            kabupaten = "Kab A",
            coordinator = "Ketua A",
            whatsappNumber = "081",
            kphId = "1",
            kphName = "KPH Batutegi"
        ),
        Kth(
            id = "11",
            name = "KTH B",
            desa = "Desa B",
            kecamatan = "Kec B",
            kabupaten = "Kab B",
            coordinator = "Ketua B",
            whatsappNumber = "082",
            kphId = "2",
            kphName = "KPH Liwa"
        )
    )
    private val dummyUserDetail = UserDetail(
        id = "user123", name = "Penyuluh 1", email = "test@test.com",
        roleId = "1", role = "penyuluh", gender = "wanita", kphId = "1", kphName = "KPH Batutegi", identityNumber = "123",
    )

    @Before
    fun setUp() {
        every { savedStateHandle.get<String>("id") } returns null
        every { connectivityObserver.observe() } returns flowOf(ConnectivityObserver.Status.Available)

        every { userPreferences.userId } returns flowOf("user123")
        every { userPreferences.userRole } returns flowOf("penyuluh")
        every { getUserDetailUseCase("user123") } returns flowOf(Resource.Success(dummyUserDetail))

        every { getKphListUseCase() } returns flowOf(dummyKphList)
        every { getKthListUseCase(any(), any()) } returns flowOf(Resource.Success(dummyKthList))
        every { validatePetaniInputUseCase.execute(any()) } returns ListValidationResult(true)

        viewModel = PetaniFormViewModel(
            createPetaniUseCase,
            getPetaniDetailUseCase,
            updatePetaniUseCase,
            validatePetaniInputUseCase,
            getKphListUseCase,
            getKthListUseCase,
            userPreferences,
            getUserDetailUseCase,
            connectivityObserver,
            savedStateHandle
        )
    }

    @Test
    fun `init loads user kph and filters kth options correctly`() = runTest {
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals("1", state.selectedKphId)
        assertEquals("KPH Batutegi", state.selectedKphName)

        assertEquals(1, state.kthOptions.size)
        assertEquals("KTH A", state.kthOptions[0].name)
    }

    @Test
    fun `init with id loads existing data correctly`() = runTest {
        val editStateHandle = mockk<SavedStateHandle>(relaxed = true)
        every { editStateHandle.get<String>("id") } returns "petani-999"

        val existingPetani = Petani(
            id = "petani-999",
            name = "Petani Local",
            identityNumber = "1234567890123456",
            gender = "Laki-laki",
            address = "Alamat Lama",
            whatsAppNumber = "081234567890",
            lastEducation = "SMA",
            sideJob = "Dagang",
            landArea = 2.5,
            kphId = "1", kphName = "KPH Batutegi",
            kthId = "10", kthName = "KTH A"
        )
        every { getPetaniDetailUseCase("petani-999") } returns flowOf(Resource.Success(existingPetani))

        val editViewModel = PetaniFormViewModel(
            createPetaniUseCase, getPetaniDetailUseCase, updatePetaniUseCase,
            validatePetaniInputUseCase, getKphListUseCase, getKthListUseCase,
            userPreferences, getUserDetailUseCase, connectivityObserver, editStateHandle
        )
        advanceUntilIdle()

        val state = editViewModel.uiState.value

        assertTrue(state.isEditMode)
        assertEquals("Petani Local", state.name)
        assertEquals("2.5", state.landArea)
        assertEquals("KTH A", state.selectedKthName)
    }

    @Test
    fun `submit existing petani calls update usecase`() = runTest{
        val originalPetani = mockk<Petani>(relaxed = true) {
            every { id } returns "petani-123"
            every { name } returns "Nama Lama"
            every { kphId } returns "kph-1"
            every { kthId } returns "kth-1"
        }
        val editStateHandle = mockk<SavedStateHandle>(relaxed = true)
        every { editStateHandle.get<String>("id") } returns "petani-123"

        every { getPetaniDetailUseCase("petani-123") } returns flowOf(Resource.Success(originalPetani))
        coEvery { updatePetaniUseCase(any(), any()) } returns Resource.Success(Unit)

        val editViewModel = PetaniFormViewModel(
            createPetaniUseCase, getPetaniDetailUseCase, updatePetaniUseCase,
            validatePetaniInputUseCase, getKphListUseCase, getKthListUseCase,
            userPreferences, getUserDetailUseCase, connectivityObserver, editStateHandle
        )
        advanceUntilIdle()
        editViewModel.onEvent(PetaniFormEvent.OnNameChange("Nama Baru"))
        editViewModel.onEvent(PetaniFormEvent.OnSubmit)
        advanceUntilIdle()

        coVerify {
            updatePetaniUseCase(
                eq("petani-123"),
                match { map -> map["nama_petani"] == "Nama Baru" } // Cek isi map
            )
        }
        coVerify(exactly = 0) { createPetaniUseCase(any()) }

        assertEquals("Berhasil disimpan!", editViewModel.uiState.value.successMessage)

    }

    @Test
    fun `kth search text filters options locally`() = runTest {
        advanceUntilIdle()
        viewModel.onEvent(PetaniFormEvent.OnKthSearchTextChange("KTH Z"))
        assertTrue(viewModel.uiState.value.kthOptions.isEmpty())

        viewModel.onEvent(PetaniFormEvent.OnKthSearchTextChange("KTH A"))
        assertEquals(1, viewModel.uiState.value.kthOptions.size)
        assertEquals("KTH A", viewModel.uiState.value.kthOptions[0].name)
    }

    @Test
    fun `kph search text auto selects id if name matches`() = runTest {
        advanceUntilIdle()

        viewModel.onEvent(PetaniFormEvent.OnKphSearchTextChange("KPH B"))
        val state1 = viewModel.uiState.value
        assertEquals("KPH B", state1.selectedKphName)
        assertEquals("", state1.selectedKphId)

        viewModel.onEvent(PetaniFormEvent.OnKphSearchTextChange("KPH Batutegi"))
        val state2 = viewModel.uiState.value
        assertEquals("KPH Batutegi", state2.selectedKphName)
        assertEquals("1", state2.selectedKphId)
    }

    @Test
    fun `kth search text auto selects id if name matches`() = runTest {
        advanceUntilIdle()

        viewModel.onEvent(PetaniFormEvent.OnKthSearchTextChange("KTH Asal"))
        val state1 = viewModel.uiState.value
        assertEquals("KTH Asal", state1.selectedKthName)
        assertEquals("", state1.selectedKthId)

        viewModel.onEvent(PetaniFormEvent.OnKthSearchTextChange("KTH A"))
        val state2 = viewModel.uiState.value
        assertEquals("KTH A", state2.selectedKthName)
        assertEquals("10", state2.selectedKthId)
    }

    @Test
    fun `update fields updates state correctly`() = runTest {
        viewModel.onEvent(PetaniFormEvent.OnNameChange("Budi"))
        assertEquals("Budi", viewModel.uiState.value.name)
        assertNull(viewModel.uiState.value.nameError)

        viewModel.onEvent(PetaniFormEvent.OnLandAreaChange("5.5"))
        assertEquals("5.5", viewModel.uiState.value.landArea)

        viewModel.onEvent(PetaniFormEvent.OnIdentityNumberChange("1234567890123456"))
        assertEquals("1234567890123456", viewModel.uiState.value.identityNumber)
        assertNull(viewModel.uiState.value.identityNumberError)

        viewModel.onEvent(PetaniFormEvent.OnGenderChange("Laki-laki"))
        assertEquals("Laki-laki", viewModel.uiState.value.gender)
        assertNull(viewModel.uiState.value.genderError)

        viewModel.onEvent(PetaniFormEvent.OnAddressChange("Jalan Mawar"))
        assertEquals("Jalan Mawar", viewModel.uiState.value.address)
        assertNull(viewModel.uiState.value.addressError)

        viewModel.onEvent(PetaniFormEvent.OnLastEducationChange("SMA"))
        assertEquals("SMA", viewModel.uiState.value.lastEducation)
        assertNull(viewModel.uiState.value.lastEducationError)

        viewModel.onEvent(PetaniFormEvent.OnSideJobChange("Pedagang"))
        assertEquals("Pedagang", viewModel.uiState.value.sideJob)
        assertNull(viewModel.uiState.value.sideJobError)

        val selectedKph = dummyKphList[1]
        viewModel.onEvent(PetaniFormEvent.OnKphSelected(selectedKph))

        advanceUntilIdle()
        val state = viewModel.uiState.value
        assertEquals("2", state.selectedKphId)
        assertEquals("KPH Liwa", state.selectedKphName)
        assertEquals("", state.selectedKthId)
        assertEquals("", state.selectedKthName)

        val selectedKth = dummyKthList[0]
        viewModel.onEvent(PetaniFormEvent.OnKthSelected(selectedKth))
        assertEquals("10", viewModel.uiState.value.selectedKthId)
        assertEquals("KTH A", viewModel.uiState.value.selectedKthName)
    }

    @Test
    fun `whatsapp realtime validation logic`() = runTest {
        viewModel.onEvent(PetaniFormEvent.OnWhatsAppChange("08123"))
        assertEquals("Nomor telepon minimal 10 digit", viewModel.uiState.value.whatsAppNumberError)

        viewModel.onEvent(PetaniFormEvent.OnWhatsAppChange("08123456789abc"))
        assertTrue(viewModel.uiState.value.whatsAppNumberError!!.contains("nomor valid"))

        viewModel.onEvent(PetaniFormEvent.OnWhatsAppChange("081234567890"))
        assertNull(viewModel.uiState.value.whatsAppNumberError)

        val tooLongNumber = "08123456789012345"
        viewModel.onEvent(PetaniFormEvent.OnWhatsAppChange(tooLongNumber))
        assertEquals("Nomor telepon maksimal 14 digit", viewModel.uiState.value.whatsAppNumberError)
    }

    @Test
    fun `validation failure updates error states`() = runTest {
        val errors = mapOf(
            "name" to "Nama wajib diisi",
            "whatsAppNumber" to "Nomor tidak valid"
        )
        every { validatePetaniInputUseCase.execute(any()) } returns ListValidationResult(
            successful = false,
            fieldErrors = errors,
            errorMessage = "Perbaiki input"
        )

        viewModel.onEvent(PetaniFormEvent.OnSubmit)

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertEquals("Nama wajib diisi", state.nameError)
        assertEquals("Nomor tidak valid", state.whatsAppNumberError)
        assertEquals("Perbaiki input", state.error)
    }


    @Test
    fun `submit success (create) calls create usecase`() = runTest {
        coEvery { createPetaniUseCase(any()) } returns Resource.Success(Unit)

        viewModel.onEvent(PetaniFormEvent.OnNameChange("Petani Baru"))

        viewModel.onEvent(PetaniFormEvent.OnSubmit)
        advanceUntilIdle()

        coVerify { createPetaniUseCase(any()) }
        assertEquals("Berhasil disimpan!", viewModel.uiState.value.successMessage)
    }

    @Test
    fun `submit failure (validation error) updates error states`() = runTest {
        val errors = mapOf("name" to "Nama wajib diisi")
        every { validatePetaniInputUseCase.execute(any()) } returns ListValidationResult(
            successful = false, fieldErrors = errors, errorMessage = "Data tidak valid"
        )

        viewModel.onEvent(PetaniFormEvent.OnSubmit)

        val state = viewModel.uiState.value
        assertEquals("Nama wajib diisi", state.nameError)
        assertEquals("Data tidak valid", state.error)
        coVerify(exactly = 0) { createPetaniUseCase(any()) }
    }

    @Test
    fun `submit failure (offline) shows error`() = runTest {
        val offlineObserver: ConnectivityObserver = mockk()
        every { offlineObserver.observe() } returns flowOf(ConnectivityObserver.Status.Lost)

        val offlineViewModel = PetaniFormViewModel(
            createPetaniUseCase, getPetaniDetailUseCase, updatePetaniUseCase,
            validatePetaniInputUseCase, getKphListUseCase, getKthListUseCase,
            userPreferences, getUserDetailUseCase, offlineObserver, savedStateHandle
        )
        advanceUntilIdle()

        offlineViewModel.onEvent(PetaniFormEvent.OnSubmit)

        assertEquals("Tidak ada koneksi internet", offlineViewModel.uiState.value.error)
        coVerify(exactly = 0) { createPetaniUseCase(any()) }
    }

    @Test
    fun `dialog and message events update state correctly`() = runTest {
        viewModel.onEvent(PetaniFormEvent.OnShowConfirmDialog)
        assertTrue(viewModel.uiState.value.showConfirmDialog)

        viewModel.onEvent(PetaniFormEvent.OnDismissConfirmDialog)
        assertFalse(viewModel.uiState.value.showConfirmDialog)

        viewModel.onEvent(PetaniFormEvent.OnShowUserMessage("Pesan Sukses", MessageType.Success))
        advanceUntilIdle()
        assertEquals("Pesan Sukses", viewModel.uiState.value.successMessage)
        assertNull(viewModel.uiState.value.error)

        viewModel.onEvent(PetaniFormEvent.OnShowUserMessage("Pesan Error", MessageType.Error))
        advanceUntilIdle()
        assertEquals("Pesan Error", viewModel.uiState.value.error)
        assertNull(viewModel.uiState.value.successMessage)

        viewModel.onEvent(PetaniFormEvent.OnDismissMessage)
        assertNull(viewModel.uiState.value.error)
        assertNull(viewModel.uiState.value.successMessage)
    }
}