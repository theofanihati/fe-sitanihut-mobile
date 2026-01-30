package com.dishut_lampung.sitanihut.presentation.user_management.form

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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
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
import kotlinx.coroutines.delay

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun UserFormScreenPreview() {
    SitanihutTheme {
        UserFormScreen(
            uiState = UserFormUiState(
                roleName = "petani",
                name = "Budi Santoso",
                email = "budi@example.com",
                identityNumber = "1871012345678901",
                gender = "pria",
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
fun UserFormRoute(
    navController: NavController,
    viewModel: UserFormViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    // Handle navigasi balik saat sukses
    LaunchedEffect(uiState.successMessage) {
        if (uiState.successMessage != null) {
            delay(1500)
            navController.popBackStack()
            viewModel.onEvent(UserFormEvent.OnDismissMessage)
        }
    }

    UserFormScreen(
        uiState = uiState,
        onEvent = viewModel::onEvent
    )
}

@Composable
fun UserFormScreen(
    uiState: UserFormUiState,
    onEvent: (UserFormEvent) -> Unit
) {
    val isUpdate = uiState.isEditMode
    val isFormEnabled = !uiState.isLoading && uiState.isOnline
    val buttonText = if (isUpdate) "Perbarui" else "Simpan"
    val passwordPlaceholder = if (isUpdate) "Isi hanya jika ingin mengubah password" else "Masukkan kata sandi"

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
                text = if (isUpdate) "Edit Pengguna" else "Tambah Pengguna",
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.Bold,
                ),
                color = MaterialTheme.colorScheme.tertiary,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            CustomOutlinedTextField(
                value = uiState.roleName.replaceFirstChar { it.uppercase() },
                onValueChange = {},
                label = "Role",
                placeholder = "Role",
                asteriskAtEnd = true,
                modifier = Modifier.fillMaxWidth(),
                rounded = 40,
                isEnabled = false
            )

            Spacer(modifier = Modifier.height(4.dp))

            CustomOutlinedTextField(
                value = uiState.name,
                onValueChange = { onEvent(UserFormEvent.OnNameChange(it)) },
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
                value = uiState.email,
                onValueChange = { onEvent(UserFormEvent.OnEmailChange(it)) },
                label = "Email",
                placeholder = "Contoh: dishut@example.com",
                keyboardType = KeyboardType.Email,
                asteriskAtEnd = true,
                modifier = Modifier.fillMaxWidth(),
                rounded = 40,
                error = uiState.emailError,
                isEnabled = isFormEnabled,
            )

            Spacer(modifier = Modifier.height(4.dp))

            CustomOutlinedTextField(
                value = uiState.password,
                onValueChange = { onEvent(UserFormEvent.OnPasswordChange(it)) },
                label = "Kata Sandi",
                placeholder = passwordPlaceholder,
                asteriskAtEnd = !isUpdate,
                isPassword = true,
                isPasswordVisible = uiState.isPasswordVisible,
                onPasswordToggleClick = { onEvent(UserFormEvent.OnTogglePasswordVisibility) },
                error = uiState.passwordError,
                modifier = Modifier.fillMaxWidth(),
                rounded = 40,
            )

            Spacer(modifier = Modifier.height(4.dp))

            CustomOutlinedTextField(
                value = uiState.confirmPassword,
                onValueChange = { onEvent(UserFormEvent.OnConfirmPasswordChange(it)) },
                label = "Konfirmasi Kata Sandi",
                placeholder = "Ketik ulang kata sandi",
                asteriskAtEnd = true,
                isPassword = true,
                isPasswordVisible = uiState.isConfirmPasswordVisible,
                onPasswordToggleClick = { onEvent(UserFormEvent.OnToggleConfirmPasswordVisibility) },
                error = uiState.confirmPasswordError,
                modifier = Modifier.fillMaxWidth(),
                rounded = 40,
            )

            Spacer(modifier = Modifier.height(4.dp))

            CustomOutlinedTextField(
                value = uiState.identityNumber,
                onValueChange = { onEvent(UserFormEvent.OnIdentityNumberChange(it)) },
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
                onOptionSelected = { onEvent(UserFormEvent.OnGenderChange(it)) },
                error = uiState.genderError,
                enabled = isFormEnabled
            )

            Spacer(modifier = Modifier.height(24.dp))

            CustomOutlinedTextArea(
                value = uiState.address,
                onValueChange = { onEvent(UserFormEvent.OnAddressChange(it)) },
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
                onValueChange = { onEvent(UserFormEvent.OnWhatsAppChange(it)) },
                label = "Nomor telepon",
                placeholder = "Contoh: 08xxx atau +628xxx",
                keyboardType = KeyboardType.Phone,
                asteriskAtEnd = true,
                modifier = Modifier.fillMaxWidth(),
                rounded = 40,
                error = uiState.whatsAppNumberError,
                isEnabled = isFormEnabled,
            )

            Spacer(modifier = Modifier.height(4.dp))

            CustomOutlinedTextField(
                value = uiState.lastEducation,
                onValueChange = { onEvent(UserFormEvent.OnLastEducationChange(it)) },
                label = "Pendidikan terakhir",
                placeholder = "Masukkan pendidikan terakhir",
                asteriskAtEnd = true,
                modifier = Modifier.fillMaxWidth(),
                rounded = 40,
                error = uiState.lastEducationError,
                isEnabled = isFormEnabled,
            )

            Spacer(modifier = Modifier.height(4.dp))

            CustomOutlinedTextField(
                value = uiState.sideJob,
                onValueChange = { onEvent(UserFormEvent.OnSideJobChange(it)) },
                label = "Pekerjaan sampingan",
                placeholder = "Isikan '-' bila tidak memiliki",
                asteriskAtEnd = true,
                modifier = Modifier.fillMaxWidth(),
                rounded = 40,
                error = uiState.sideJobError,
                isEnabled = isFormEnabled,
            )

            Spacer(modifier = Modifier.height(4.dp))
            CustomOutlinedTextField(
                value = uiState.landArea,
                onValueChange = { onEvent(UserFormEvent.OnLandAreaChange(it)) },
                label = "Luas Lahan (Ha)",
                placeholder = "Contoh: 2.5",
                keyboardType = KeyboardType.Decimal,
                asteriskAtEnd = true,
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
                    onEvent(UserFormEvent.OnKphSelected(selectedKph))
                },
                onValueChange = { newText ->
                    onEvent(UserFormEvent.OnKphSearchTextChange(newText))
                },
                errorMessage = uiState.kphError,
                enabled = isFormEnabled && !uiState.isKphLocked
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
                    onEvent(UserFormEvent.OnKthSelected(selectedKth))
                },
                onValueChange = { newText ->
                    onEvent(UserFormEvent.OnKthSearchTextChange(newText))
                },
                errorMessage = uiState.kthError,
                enabled = isFormEnabled
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = { onEvent(UserFormEvent.OnShowConfirmDialog) },
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
            onDismiss = { onEvent(UserFormEvent.OnDismissMessage) },
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 80.dp)
                .zIndex(10f)
        )

        AnimatedMessage(
            isVisible = uiState.error != null,
            message = uiState.error ?: "",
            messageType = MessageType.Error,
            onDismiss = { onEvent(UserFormEvent.OnDismissMessage) },
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
                supportingText = "Apakah anda yakin ingin $actionMsg data Pengguna ini?",
                confirmButtonText = confirmText,
                dismissButtonText = "Batal",
                isLoading = uiState.isLoading,
                onConfirm = { onEvent(UserFormEvent.OnSubmit) },
                onDismiss = { onEvent(UserFormEvent.OnDismissConfirmDialog) },
                confirmColor = confirmColor
            )
        }
    }
}