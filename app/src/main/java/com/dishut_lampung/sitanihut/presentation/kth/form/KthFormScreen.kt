package com.dishut_lampung.sitanihut.presentation.kth.form

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.dishut_lampung.sitanihut.domain.model.Kph
import com.dishut_lampung.sitanihut.presentation.shared.components.CustomCircularProgressIndicator
import com.dishut_lampung.sitanihut.presentation.shared.components.animations.AnimatedMessage
import com.dishut_lampung.sitanihut.presentation.shared.components.animations.MessageType
import com.dishut_lampung.sitanihut.presentation.shared.components.dialog.CustomConfirmationDialog
import com.dishut_lampung.sitanihut.presentation.shared.components.dropdown.CustomSearchableOutlinedDropdown
import com.dishut_lampung.sitanihut.presentation.shared.components.textfield.CustomOutlinedTextField
import com.dishut_lampung.sitanihut.presentation.shared.theme.Dimens.ScreenPadding
import com.dishut_lampung.sitanihut.presentation.shared.theme.SitanihutTheme
import kotlinx.coroutines.delay

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun KthFormScreenPreview() {
    SitanihutTheme {
        KthFormScreen(
            uiState = KthFormUiState(
                name = "KTH Wana Lestari",
                kabupatenOptions = listOf("Lampung Barat", "Tanggamus"),
                selectedKabupaten = "Lampung Barat",
                kecamatanOptions = listOf("Balik Bukit", "Sekincau"),
                selectedKecamatan = "Balik Bukit",
                desaOptions = listOf("Padang Cahya", "Way Empulau Ulu"),
                selectedDesa = "Padang Cahya",
                coordinator = "Budi Santoso",
                whatsappNumber = "081234567890",
                kphOptions = listOf(Kph("1", "KPH Liwa"), Kph("2", "KPH Batutegi")),
                selectedKphName = "KPH Liwa"
            ),
            onEvent = {},
        )
    }
}

//@Preview(showBackground = true)
//@Composable
//fun KthFormScreenErrorPreview() {
//    SitanihutTheme {
//        KthFormScreen(
//            uiState = KthFormUiState(
//                name = "",
//                nameError = "Nama KTH wajib diisi",
//                error = "Terjadi kesalahan validasi",
//                kabupatenOptions = listOf("Lampung Selatan", "Lampung Timur")
//            ),
//            onEvent = {},
//            onNavigateUp = {}
//        )
//    }
//}

@Composable
fun KthFormRoute(
    navController: NavController,
    viewModel: KthFormViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(uiState.successMessage) {
        if (uiState.successMessage != null) {
            delay(1500)
            navController.popBackStack()
            viewModel.onEvent(KthFormUiEvent.OnDismissMessage)
        }
    }

    KthFormScreen(
        uiState = uiState,
        onEvent = viewModel::onEvent,
    )
}

@Composable
fun KthFormScreen(
    uiState: KthFormUiState,
    onEvent: (KthFormUiEvent) -> Unit,
) {
    val isUpdate = uiState.isEditMode
    val isFormEnabled = !uiState.isLoading && uiState.isOnline
    val buttonText = if (isUpdate) "Perbarui" else "Simpan"

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = ScreenPadding),
        ) {

            Spacer(modifier = Modifier.height(100.dp))

            Text(
                text = if (isUpdate) "Edit Data KTH" else "Tambah Data KTH",
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.Bold,
                ),
                color = MaterialTheme.colorScheme.tertiary,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

                CustomOutlinedTextField(
                    value = uiState.name,
                    onValueChange = { onEvent(KthFormUiEvent.OnNameChange(it)) },
                    label = "Nama KTH",
                    placeholder = "Masukkan nama KTH",
                    keyboardType = KeyboardType.Text,
                    asteriskAtEnd = true,
                    modifier = Modifier.fillMaxWidth(),
                    rounded = 40,
                    error = uiState.nameError,
                    isEnabled = isFormEnabled,
                )

            Spacer(modifier = Modifier.height(4.dp))

                CustomSearchableOutlinedDropdown(
                    modifier = Modifier.fillMaxWidth(),
                    value = uiState.selectedKabupaten,
                    itemLabel = {it},
                    label = "Kabupaten",
                    placeholder = "Pilih Kabupaten",
                    asteriskAtEnd = true,
                    onValueChange = { onEvent(KthFormUiEvent.OnKabupatenSearchTextChange(it)) },
                    onOptionSelected = { selectedKabupaten ->
                        onEvent(KthFormUiEvent.OnKabupatenSelected(selectedKabupaten))
                    },
                    options = uiState.kabupatenOptions,
                    errorMessage = uiState.kabupatenError,
                    enabled = isFormEnabled,
                )

            Spacer(modifier = Modifier.height(4.dp))

                CustomSearchableOutlinedDropdown(
                    modifier = Modifier.fillMaxWidth(),
                    value = uiState.selectedKecamatan,
                    itemLabel = { it },
                    label = "Kecamatan",
                    placeholder = "Pilih Kecamatan",
                    asteriskAtEnd = true,
                    onValueChange = { onEvent(KthFormUiEvent.OnKecamatanSearchTextChange(it)) },
                    onOptionSelected = { selectedKecamatan ->
                        onEvent(KthFormUiEvent.OnKecamatanSelected(selectedKecamatan))
                    },
                    options = uiState.kecamatanOptions,
                    errorMessage = uiState.kecamatanError,
                    enabled = isFormEnabled && uiState.selectedKabupaten.isNotEmpty(),
                )

            Spacer(modifier = Modifier.height(4.dp))

                CustomSearchableOutlinedDropdown(
                    modifier = Modifier.fillMaxWidth(),
                    value = uiState.selectedDesa,
                    itemLabel = { it },
                    label = "Desa",
                    placeholder = "Pilih Desa",
                    asteriskAtEnd = true,
                    onValueChange = { onEvent(KthFormUiEvent.OnDesaSearchTextChange(it)) },
                    onOptionSelected = { selectedDesa ->
                        onEvent(KthFormUiEvent.OnDesaSelected(selectedDesa))
                    },
                    options = uiState.desaOptions,
                    errorMessage = uiState.desaError,
                    enabled = isFormEnabled && uiState.selectedKecamatan.isNotEmpty(),
                )

            Spacer(modifier = Modifier.height(4.dp))

                CustomOutlinedTextField(
                    value = uiState.coordinator,
                    onValueChange = { onEvent(KthFormUiEvent.OnCoordinatorChange(it)) },
                    label = "Ketua KTH",
                    placeholder = "Masukkan nama ketua KTH",
                    keyboardType = KeyboardType.Text,
                    modifier = Modifier.fillMaxWidth(),
                    rounded = 40,
                    error = uiState.coordinatorError,
                    isEnabled = isFormEnabled,
                )

            Spacer(modifier = Modifier.height(4.dp))

                CustomOutlinedTextField(
                    value = uiState.whatsappNumber,
                    onValueChange = { onEvent(KthFormUiEvent.OnWhatsappChange(it)) },
                    label = "Nomor WhatsApp",
                    placeholder = "Masukkan nomor whatsApp",
                    keyboardType = KeyboardType.Phone,
                    modifier = Modifier.fillMaxWidth(),
                    rounded = 40,
                    error = uiState.whatsappError,
                    isEnabled = isFormEnabled,
                )

            Spacer(modifier = Modifier.height(4.dp))

                CustomSearchableOutlinedDropdown(
                    modifier = Modifier.fillMaxWidth(),
                    value = uiState.selectedKphName,
                    itemLabel = { it.name },
                    label = "Asal KPH",
                    placeholder = "Pilih KPH",
                    asteriskAtEnd = true,
                    options = uiState.kphOptions,
                    onOptionSelected = { selectedKph ->
                        onEvent(KthFormUiEvent.OnKphSelected(selectedKph))
                    },
                    onValueChange = { newText ->
                        onEvent(KthFormUiEvent.OnKphSearchTextChange(newText))
                    },
                    errorMessage = uiState.kphError,
                    enabled = isFormEnabled
                )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = { onEvent(KthFormUiEvent.OnShowConfirmDialog) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.tertiary
                ),
                enabled = isFormEnabled
            ) {
                Text(text = if (uiState.isOnline) buttonText else "Offline (Hanya Lihat)")
            }

            Spacer(modifier = Modifier.height(50.dp))
        }

        if (uiState.isLoading && !uiState.showConfirmDialog) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.scrim.copy(alpha = 0.3f))
                    .clickable(enabled = false) {},
                contentAlignment = Alignment.Center
            ) {
                CustomCircularProgressIndicator()
            }
        }

        AnimatedMessage(
            isVisible = uiState.successMessage != null,
            message = uiState.successMessage ?: "",
            messageType = MessageType.Success,
            onDismiss = {
                onEvent(KthFormUiEvent.OnDismissMessage)
            },
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 80.dp)
                .zIndex(10f)
        )

        AnimatedMessage(
            isVisible = uiState.error != null,
            message = uiState.error ?: "",
            messageType = MessageType.Error,
            onDismiss = { onEvent(KthFormUiEvent.OnDismissMessage) },
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 80.dp)
                .zIndex(10f)
        )

        if (uiState.showConfirmDialog) {
            val dialogTitle = if (isUpdate) "Memperbarui?" else "Menyimpan?"
            val actionMsg = if (isUpdate) "memperbarui" else "menyimpan"
            val confirmText = if (isUpdate) "Perbarui" else "Simpan"
            val confirmColor = MaterialTheme.colorScheme.tertiary

            CustomConfirmationDialog(
                title = dialogTitle,
                supportingText = "Apakah anda yakin ingin $actionMsg data KTH ini?",
                confirmButtonText = confirmText,
                dismissButtonText = "Batal",
                isLoading = uiState.isLoading,
                onConfirm = { onEvent(KthFormUiEvent.OnSubmit) },
                onDismiss = { onEvent(KthFormUiEvent.OnDismissConfirmDialog) },
                confirmColor = confirmColor
            )
        }
    }
}
