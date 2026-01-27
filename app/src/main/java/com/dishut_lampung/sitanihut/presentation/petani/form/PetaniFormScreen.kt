package com.dishut_lampung.sitanihut.presentation.petani.form

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
import com.dishut_lampung.sitanihut.domain.model.Kth
import com.dishut_lampung.sitanihut.presentation.shared.components.CustomCircularProgressIndicator
import com.dishut_lampung.sitanihut.presentation.shared.components.animations.AnimatedMessage
import com.dishut_lampung.sitanihut.presentation.shared.components.animations.MessageType
import com.dishut_lampung.sitanihut.presentation.shared.components.dialog.CustomConfirmationDialog
import com.dishut_lampung.sitanihut.presentation.shared.components.dropdown.CustomSearchableOutlinedDropdown
import com.dishut_lampung.sitanihut.presentation.shared.components.radio_button.CustomRadioButton
import com.dishut_lampung.sitanihut.presentation.shared.components.textfield.CustomOutlinedTextArea
import com.dishut_lampung.sitanihut.presentation.shared.components.textfield.CustomOutlinedTextField
import com.dishut_lampung.sitanihut.presentation.shared.theme.Dimens.ScreenPadding
import com.dishut_lampung.sitanihut.presentation.shared.theme.SitanihutTheme
import com.dishut_lampung.sitanihut.presentation.user_management.form.UserFormEvent
import kotlinx.coroutines.delay

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun PetaniFormScreenPreview() {
    SitanihutTheme {
        PetaniFormScreen(
            uiState = PetaniFormUiState(
                name = "Budi Santoso",
                identityNumber = "1871012345678901",
                gender = "Laki-laki",
                address = "Desa Bangun Rejo, Kec. Kemiling",
                whatsAppNumber = "081234567890",
                lastEducation = "SMA",
                sideJob = "Pedagang",
                landArea = "2.5",
                selectedKphName = "KPH Batutegi",
                kthOptions = listOf(
                    Kth("1", "KTH Maju Jaya", "", "", "", "", "", "1", ""),
                    Kth("2", "KTH Makmur", "", "", "", "", "", "1", "")
                ),
                selectedKthName = "KTH Maju Jaya"
            ),
            onEvent = {}
        )
    }
}

@Composable
fun PetaniFormRoute(
    navController: NavController,
    viewModel: PetaniFormViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(uiState.successMessage) {
        if (uiState.successMessage != null) {
            delay(1500)
            navController.popBackStack()
            viewModel.onEvent(PetaniFormEvent.OnDismissMessage)
        }
    }

    PetaniFormScreen(
        uiState = uiState,
        onEvent = viewModel::onEvent
    )
}

@Composable
fun PetaniFormScreen(
    uiState: PetaniFormUiState,
    onEvent: (PetaniFormEvent) -> Unit
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
                text = if (isUpdate) "Edit Data Petani" else "Tambah Data Petani",
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
                onValueChange = { onEvent(PetaniFormEvent.OnNameChange(it)) },
                label = "Nama Lengkap",
                placeholder = "Masukkan nama",
                asteriskAtEnd = true,
                modifier = Modifier.fillMaxWidth(),
                rounded = 40,
                error = uiState.nameError,
                isEnabled = isFormEnabled,
            )

            Spacer(modifier = Modifier.height(4.dp))

            CustomOutlinedTextField(
                value = uiState.identityNumber,
                onValueChange = { onEvent(PetaniFormEvent.OnIdentityNumberChange(it)) },
                label = "NIK",
                placeholder = "Masukkan 16 digit NIK",
                keyboardType = KeyboardType.Number,
                asteriskAtEnd = true,
                modifier = Modifier.fillMaxWidth(),
                rounded = 40,
                error = uiState.identityNumberError,
                isEnabled = isFormEnabled,
            )

            Spacer(modifier = Modifier.height(8.dp))

            CustomRadioButton(
                label = "Jenis kelamin",
                options = listOf("pria", "wanita"),
                selectedOption = uiState.gender,
                onOptionSelected = { onEvent(PetaniFormEvent.OnGenderChange(it)) },
                error = uiState.genderError,
                enabled = isFormEnabled
            )

            Spacer(modifier = Modifier.height(8.dp))

            CustomOutlinedTextArea(
                value = uiState.address,
                onValueChange = { onEvent(PetaniFormEvent.OnAddressChange(it)) },
                label = "Alamat",
                placeholder = "Masukkan alamat",
                modifier = Modifier.fillMaxWidth(),
                error = uiState.addressError,
                isEnabled = isFormEnabled,
                minLines = 3
            )

            Spacer(modifier = Modifier.height(4.dp))

            CustomOutlinedTextField(
                value = uiState.whatsAppNumber,
                onValueChange = { onEvent(PetaniFormEvent.OnWhatsAppChange(it)) },
                label = "Nomor telepon",
                placeholder = "Contoh: 08xxx atau +628xxx",
                keyboardType = KeyboardType.Phone,
                modifier = Modifier.fillMaxWidth(),
                rounded = 40,
                error = uiState.whatsAppNumberError,
                isEnabled = isFormEnabled,
            )

            Spacer(modifier = Modifier.height(4.dp))

            CustomOutlinedTextField(
                value = uiState.lastEducation,
                onValueChange = { onEvent(PetaniFormEvent.OnLastEducationChange(it)) },
                label = "Pendidikan terakhir",
                placeholder = "Masukkan pendidikan terakhir",
                modifier = Modifier.fillMaxWidth(),
                rounded = 40,
                error = uiState.lastEducationError,
                isEnabled = isFormEnabled,
            )

            Spacer(modifier = Modifier.height(4.dp))

            CustomOutlinedTextField(
                value = uiState.sideJob,
                onValueChange = { onEvent(PetaniFormEvent.OnSideJobChange(it)) },
                label = "Pekerjaan sampingan",
                placeholder = "Isikan '-' bila tidak memiliki",
                modifier = Modifier.fillMaxWidth(),
                rounded = 40,
                error = uiState.sideJobError,
                isEnabled = isFormEnabled,
            )

            Spacer(modifier = Modifier.height(4.dp))

            CustomOutlinedTextField(
                value = uiState.landArea,
                onValueChange = { onEvent(PetaniFormEvent.OnLandAreaChange(it)) },
                label = "Luas Lahan (Ha)",
                placeholder = "Contoh: 2.5",
                keyboardType = KeyboardType.Decimal,
                modifier = Modifier.fillMaxWidth(),
                rounded = 40,
                error = uiState.landAreaError,
                isEnabled = isFormEnabled,
            )

            Spacer(modifier = Modifier.height(4.dp))

            CustomSearchableOutlinedDropdown(
                modifier = Modifier.fillMaxWidth(),
                value = uiState.selectedKphName,
                itemLabel = { it.name },
                label = "Asal KPH",
                placeholder = "Ketik dan pilih salah satu",
                asteriskAtEnd = true,
                options = uiState.kphOptions,
                onOptionSelected = { selectedKph ->
                    onEvent(PetaniFormEvent.OnKphSelected(selectedKph))
                },
                onValueChange = { newText ->
                    onEvent(PetaniFormEvent.OnKphSearchTextChange(newText))
                },
                errorMessage = uiState.kphError,
                enabled = !uiState.isKphLocked
            )

            Spacer(modifier = Modifier.height(4.dp))

            CustomSearchableOutlinedDropdown(
                modifier = Modifier.fillMaxWidth(),
                value = uiState.selectedKthName,
                itemLabel = { it.name },
                label = "Asal KTH",
                placeholder = "Ketik dan pilih salah satu",
                asteriskAtEnd = true,
                options = uiState.kthOptions,
                onOptionSelected = { selectedKth ->
                    onEvent(PetaniFormEvent.OnKthSelected(selectedKth))
                },
                onValueChange = { newText ->
                    onEvent(PetaniFormEvent.OnKthSearchTextChange(newText))
                },
                errorMessage = uiState.kthError,
                enabled = isFormEnabled
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = { onEvent(PetaniFormEvent.OnShowConfirmDialog) },
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
            onDismiss = { onEvent(PetaniFormEvent.OnDismissMessage) },
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 80.dp)
                .zIndex(10f)
        )

        AnimatedMessage(
            isVisible = uiState.error != null,
            message = uiState.error ?: "",
            messageType = MessageType.Error,
            onDismiss = { onEvent(PetaniFormEvent.OnDismissMessage) },
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
                supportingText = "Apakah anda yakin ingin $actionMsg data Petani ini?",
                confirmButtonText = confirmText,
                dismissButtonText = "Batal",
                isLoading = uiState.isLoading,
                onConfirm = { onEvent(PetaniFormEvent.OnSubmit) },
                onDismiss = { onEvent(PetaniFormEvent.OnDismissConfirmDialog) },
                confirmColor = confirmColor
            )
        }
    }
}