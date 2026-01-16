package com.dishut_lampung.sitanihut.presentation.kth.form

import androidx.lifecycle.SavedStateHandle
import com.dishut_lampung.sitanihut.domain.model.Kph
import com.dishut_lampung.sitanihut.domain.model.Kth
import com.dishut_lampung.sitanihut.domain.usecase.kph.GetKphListUseCase
import com.dishut_lampung.sitanihut.domain.usecase.kth.CreateKthUseCase
import com.dishut_lampung.sitanihut.domain.usecase.kth.GetKthDetailUseCase
import com.dishut_lampung.sitanihut.domain.usecase.kth.UpdateKthUseCase
import com.dishut_lampung.sitanihut.domain.usecase.kth.ValidateKthInputUseCase
import com.dishut_lampung.sitanihut.domain.validator.ListValidationResult
import com.dishut_lampung.sitanihut.presentation.shared.components.animations.MessageType
import com.dishut_lampung.sitanihut.util.ConnectivityObserver
import com.dishut_lampung.sitanihut.util.MainCoroutineRule
import com.dishut_lampung.sitanihut.util.Resource
import com.dishut_lampung.sitanihut.util.Wilayah
import com.dishut_lampung.sitanihut.util.WilayahLampungData
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@ExperimentalCoroutinesApi
class KthFormViewModelTest {

    @get:Rule
    val mainCoroutineRule = MainCoroutineRule()

    private lateinit var viewModel: KthFormViewModel
    private val createKthUseCase: CreateKthUseCase = mockk()
    private val validateKthInputUseCase: ValidateKthInputUseCase = mockk()
    private val getKphListUseCase: GetKphListUseCase = mockk()
    private val getKthDetailUseCase: GetKthDetailUseCase = mockk()
    private val updateKthUseCase: UpdateKthUseCase = mockk()
    private val connectivityObserver: ConnectivityObserver = mockk()
    private val savedStateHandle: SavedStateHandle = mockk(relaxed = true)

    @Before
    fun setUp() {
        mockkObject(WilayahLampungData)
        every {WilayahLampungData.getKecamatanByKabupaten("Lampung Selatan") } returns listOf("Kalianda", "Natar")
        every {WilayahLampungData.getDesaByKecamatan("Lampung Selatan", "Kalianda") } returns listOf("Way Urang", "Maja")
        every { savedStateHandle.get<String>("id") } returns null
        val dummyKphList = listOf(Kph("1", "KPH Batutegi"), Kph("2", "KPH Liwa"))
        every { getKphListUseCase() } returns flowOf(dummyKphList)
        every { connectivityObserver.observe() } returns flowOf(ConnectivityObserver.Status.Available)
        every { validateKthInputUseCase.execute(any()) } returns ListValidationResult(true)

        viewModel = KthFormViewModel(
            createKthUseCase,
            getKthDetailUseCase,
            updateKthUseCase,
            validateKthInputUseCase,
            getKphListUseCase,
            connectivityObserver,
            savedStateHandle
        )
    }

    @Test
    fun `init loads kph options correctly`() = runTest {
        advanceUntilIdle()
        val state = viewModel.uiState.value
        assertEquals(2, state.kphOptions.size)
        assertEquals("KPH Batutegi", state.kphOptions[0].name)
    }

    @Test
    fun `init with id loads existing data correctly`() = runTest {
        val editStateHandle = mockk<SavedStateHandle>(relaxed = true)
        every { editStateHandle.get<String>("id") } returns "kth-123"

        val existingKth = Kth(
            id = "kth-123",
            name = "KTH Lama",
            kabupaten = "Lampung Selatan",
            kecamatan = "Kalianda",
            desa = "Maja",
            coordinator = "Pak Ketua",
            whatsappNumber = "081234567890",
            kphId = "1",
            kphName = "KPH Batutegi",
        )
        every { getKthDetailUseCase("kth-123") } returns flowOf(Resource.Success(existingKth))

        val editViewModel = KthFormViewModel(
            createKthUseCase,
            getKthDetailUseCase,
            updateKthUseCase,
            validateKthInputUseCase,
            getKphListUseCase,
            connectivityObserver,
            editStateHandle
        )

        advanceUntilIdle()

        val state = editViewModel.uiState.value
        assertTrue(state.isEditMode)
        assertEquals("KTH Lama", state.name)
        assertEquals("Lampung Selatan", state.selectedKabupaten)
        assertEquals("Kalianda", state.selectedKecamatan)
        assertEquals("Maja", state.selectedDesa)
        assertEquals("KPH Batutegi", state.selectedKphName)
        assertEquals("1", state.selectedKphId)
    }

    @Test
    fun `submit existing kth calls update usecase`() = runTest {
        val editStateHandle = mockk<SavedStateHandle>(relaxed = true)
        every { editStateHandle.get<String>("id") } returns "kth-123"

        every { getKthDetailUseCase("kth-123") } returns flowOf(Resource.Success(mockk(relaxed = true)))
        coEvery { updateKthUseCase(any(), any()) } returns Resource.Success(Unit)

        val editViewModel = KthFormViewModel(
            createKthUseCase,
            getKthDetailUseCase,
            updateKthUseCase,
            validateKthInputUseCase,
            getKphListUseCase,
            connectivityObserver,
            editStateHandle
        )

        advanceUntilIdle()

        editViewModel.onEvent(KthFormUiEvent.OnSubmit)
        advanceUntilIdle()

        coVerify { updateKthUseCase(eq("kth-123"), any()) }
        coVerify(exactly = 0) { createKthUseCase(any()) }

        assertEquals("Berhasil disimpan!", editViewModel.uiState.value.successMessage)
    }

    @Test
    fun `selecting kabupaten updates kecamatan options and resets selections`() = runTest {
        viewModel.onEvent(KthFormUiEvent.OnKabupatenSelected("Lampung Selatan"))

        val state = viewModel.uiState.value
        assertEquals("Lampung Selatan", state.selectedKabupaten)
        assertEquals(listOf("Kalianda", "Natar"), state.kecamatanOptions)
        assertEquals("", state.selectedKecamatan)
        assertEquals("", state.selectedDesa)
    }

    @Test
    fun `selecting kecamatan updates desa options`() = runTest {
        viewModel.onEvent(KthFormUiEvent.OnKabupatenSelected("Lampung Selatan"))
        viewModel.onEvent(KthFormUiEvent.OnKecamatanSelected("Kalianda"))

        val state = viewModel.uiState.value
        assertEquals("Kalianda", state.selectedKecamatan)
        assertEquals(listOf("Way Urang", "Maja"), state.desaOptions)
        assertEquals("", state.selectedDesa)
    }
    @Test
    fun `kph search text auto selects id if name matches`() = runTest {
        advanceUntilIdle()

        viewModel.onEvent(KthFormUiEvent.OnKphSearchTextChange("KPH Batu"))
        val state1 = viewModel.uiState.value
        assertEquals("KPH Batu", state1.selectedKphName)
        assertEquals("", state1.selectedKphId)

        viewModel.onEvent(KthFormUiEvent.OnKphSearchTextChange("kph batutegi"))
        val state2 = viewModel.uiState.value
        assertEquals("kph batutegi", state2.selectedKphName)
        assertEquals("1", state2.selectedKphId)
    }

    @Test
    fun `whatsapp realtime validation logic`() = runTest {
        viewModel.onEvent(KthFormUiEvent.OnWhatsappChange("08123456789"))
        assertNull(viewModel.uiState.value.whatsappError)

        viewModel.onEvent(KthFormUiEvent.OnWhatsappChange("0812abc"))
        assertEquals("Masukkan nomor valid (08.. atau +628..) tanpa spasi", viewModel.uiState.value.whatsappError)

        viewModel.onEvent(KthFormUiEvent.OnWhatsappChange("08123"))
        assertEquals("Nomor telepon minimal 10 digit", viewModel.uiState.value.whatsappError)

        viewModel.onEvent(KthFormUiEvent.OnWhatsappChange("0812345678912345"))
        assertEquals("Nomor telepon maksimal 14 digit", viewModel.uiState.value.whatsappError)
    }

    @Test
    fun `validation failure updates error states`() = runTest {
        val errors = mapOf(
            "name" to "Nama wajib diisi",
            "whatsappNumber" to "Nomor tidak valid"
        )
        every { validateKthInputUseCase.execute(any()) } returns ListValidationResult(
            successful = false,
            fieldErrors = errors,
            errorMessage = "Perbaiki input"
        )

        viewModel.onEvent(KthFormUiEvent.OnSubmit)

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertEquals("Nama wajib diisi", state.nameError)
        assertEquals("Nomor tidak valid", state.whatsappError)
        assertEquals("Perbaiki input", state.error)
    }

    @Test
    fun `submit success calls usecase and updates state`() = runTest {
        coEvery { createKthUseCase(any()) } returns Resource.Success(Unit)
        every { validateKthInputUseCase.execute(any()) } returns ListValidationResult(true)

        viewModel.onEvent(KthFormUiEvent.OnNameChange("KTH Maju Jaya"))
        viewModel.onEvent(KthFormUiEvent.OnKabupatenSelected("Lampung Selatan"))
        viewModel.onEvent(KthFormUiEvent.OnKecamatanSelected("Kalianda"))
        viewModel.onEvent(KthFormUiEvent.OnDesaSelected("Way Urang"))
        viewModel.onEvent(KthFormUiEvent.OnSubmit)

        coVerify { createKthUseCase(any()) }
        val state = viewModel.uiState.value
        assertEquals("Berhasil disimpan!", state.successMessage)
        assertFalse(state.showConfirmDialog)
    }

    @Test
    fun `update fields updates state correctly`() = runTest {
        viewModel.onEvent(KthFormUiEvent.OnCoordinatorChange("Budi"))
        assertEquals("Budi", viewModel.uiState.value.coordinator)
        assertNull(viewModel.uiState.value.coordinatorError)

        viewModel.onEvent(KthFormUiEvent.OnWhatsappChange("08123456789"))
        assertEquals("08123456789", viewModel.uiState.value.whatsappNumber)
        assertNull(viewModel.uiState.value.whatsappError)

        val selectedKph = Kph("1", "KPH Batutegi")
        viewModel.onEvent(KthFormUiEvent.OnKphSelected(selectedKph))
        assertEquals("1", viewModel.uiState.value.selectedKphId)
        assertEquals("KPH Batutegi", viewModel.uiState.value.selectedKphName)
        assertNull(viewModel.uiState.value.kphError)

        viewModel.onEvent(KthFormUiEvent.OnDesaSelected("Desa A"))
        assertEquals("Desa A", viewModel.uiState.value.selectedDesa)
        assertNull(viewModel.uiState.value.desaError)

        viewModel.onEvent(KthFormUiEvent.OnNameChange("Nama Baru"))
        assertEquals("Nama Baru", viewModel.uiState.value.name)
        assertNull(viewModel.uiState.value.nameError)
    }

    @Test
    fun `dialog and message events update state correctly`() = runTest {
        viewModel.onEvent(KthFormUiEvent.OnShowConfirmDialog)
        assertTrue(viewModel.uiState.value.showConfirmDialog)

        viewModel.onEvent(KthFormUiEvent.OnDismissConfirmDialog)
        assertFalse(viewModel.uiState.value.showConfirmDialog)

        viewModel.onEvent(KthFormUiEvent.OnShowUserMessage("Pesan Sukses", MessageType.Success))
        advanceUntilIdle()
        assertEquals("Pesan Sukses", viewModel.uiState.value.successMessage)
        assertNull(viewModel.uiState.value.error)

        viewModel.onEvent(KthFormUiEvent.OnShowUserMessage("Pesan Error", MessageType.Error))
        advanceUntilIdle()
        assertEquals("Pesan Error", viewModel.uiState.value.error)
        assertNull(viewModel.uiState.value.successMessage)

        viewModel.onEvent(KthFormUiEvent.OnDismissMessage)
        assertNull(viewModel.uiState.value.error)
        assertNull(viewModel.uiState.value.successMessage)
    }

    @Test
    fun `submit failure updates error state`() = runTest {
        coEvery { createKthUseCase(any()) } returns Resource.Error("Gagal koneksi ke server")
        every { validateKthInputUseCase.execute(any()) } returns ListValidationResult(true)

        viewModel.onEvent(KthFormUiEvent.OnSubmit)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertEquals("Gagal koneksi ke server", state.error)
        assertFalse(state.showConfirmDialog)
    }

    @Test
    fun `submit fails when offline`() = runTest {
        val offlineObserver: ConnectivityObserver = mockk()
        every { offlineObserver.observe() } returns flowOf(ConnectivityObserver.Status.Lost)

        val offlineViewModel = KthFormViewModel(
            createKthUseCase,
            getKthDetailUseCase,
            updateKthUseCase,
            validateKthInputUseCase,
            getKphListUseCase,
            offlineObserver,
            savedStateHandle
        )
        advanceUntilIdle()

        offlineViewModel.onEvent(KthFormUiEvent.OnSubmit)
        advanceUntilIdle()

        val state = offlineViewModel.uiState.value

        coVerify(exactly = 0) { createKthUseCase(any()) }
        coVerify(exactly = 0) { updateKthUseCase(any(), any()) }
        assertEquals("Tidak ada koneksi internet", state.error)
    }
}