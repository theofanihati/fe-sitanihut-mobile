package com.dishut_lampung.sitanihut.presentation.forgot_password

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.dishut_lampung.sitanihut.R
import com.dishut_lampung.sitanihut.presentation.components.CenteredAuthImage
import com.dishut_lampung.sitanihut.presentation.components.CenteredLogo
import com.dishut_lampung.sitanihut.presentation.components.animations.AnimatedMessage
import com.dishut_lampung.sitanihut.presentation.components.animations.MessageType
import com.dishut_lampung.sitanihut.presentation.components.textfield.CustomOutlinedTextField
import com.dishut_lampung.sitanihut.presentation.ui.theme.SitanihutTheme

@Preview(showBackground = true)
@Composable
fun ForgotPasswordScreenPreview() {
    SitanihutTheme(dynamicColor = false) {
        ForgotPasswordScreen(
            state = ForgotPasswordState(email = "test@email.com"),
            onEvent = {},
            onNavigateBack = {}
        )
    }
}

//@Preview(showBackground = true)
//@Composable
//fun ForgotPasswordScreenErrorPreview() {
//    SitanihutTheme(dynamicColor = false) {
//        ForgotPasswordScreen(
//            state = ForgotPasswordState(
//                email = "aaaa.kl" ,
//                emailError = "Format email tidak valid",
//                isLoading = true,
//            ),
//            onEvent = {},
//            onNavigateBack = {}
//        )
//    }
//}

@Composable
fun ForgotPasswordRoute(
    navController: NavHostController,
    viewModel: ForgotPasswordViewModel = hiltViewModel()
) {
    val state = viewModel.forgotPasswordState
    val context = LocalContext.current

    LaunchedEffect(key1 = true) {
        viewModel.eventFlow.collect { event ->
            when (event) {
                is ForgotPasswordUiEvent.SubmitSuccess -> {
                   navController.navigate("login_screen") {
                        popUpTo("forgot_password_screen") { inclusive = true }
                    }
                }
                is ForgotPasswordUiEvent.NavigateBack -> {
                    navController.navigate("login_screen")
                }
            }
        }
    }

    ForgotPasswordScreen(
        state = state,
        onEvent = viewModel::onEvent,
        onNavigateBack = {
            navController.popBackStack()
        }
    )
}

@Composable
fun ForgotPasswordScreen(
    modifier: Modifier = Modifier,
    state: ForgotPasswordState,
    onEvent: (ForgotPasswordEvent) -> Unit,
    onNavigateBack: () -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxSize()
    ) {
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            Spacer(Modifier.height(52.dp))
            TopSection()
            Spacer(Modifier.height(24.dp))
            BottomSection(
                onEmailChange = { email -> onEvent(ForgotPasswordEvent.OnEmailChange(email)) },
                onSubmitClick = { onEvent(ForgotPasswordEvent.OnSubmitClick) },
                onNavigateBack = onNavigateBack,
                state = state
            )
            Spacer(Modifier.height(16.dp))
        }
        AnimatedMessage(
            isVisible = state.generalError != null,
            message = state.generalError ?: "",
            messageType = MessageType.Error,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 80.dp),
            onDismiss = { onEvent(ForgotPasswordEvent.OnDismissError) },
        )
        AnimatedMessage(
            isVisible = state.successMessage != null,
            message = state.successMessage ?: "",
            messageType = MessageType.Success,
            onDismiss = { onEvent(ForgotPasswordEvent.OnDismissSuccessMessage) },
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(horizontal = 24.dp)
                .padding(bottom = 16.dp)
        )
    }
}

@Composable
fun TopSection(){
    CenteredLogo()
    Spacer(Modifier.height(16.dp))

    Text(
        text = "Lupa Kata Sandi",
        style = MaterialTheme.typography.headlineMedium,
        color = MaterialTheme.colorScheme.onSurface,
        fontWeight = FontWeight.Bold
    )
    Text(
        text = "Masukkan email Anda untuk menerima notifikasi reset kata sandi",
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        textAlign = TextAlign.Center,
    )

    Spacer(Modifier.height(24.dp))
    CenteredAuthImage(
        imageResId = R.drawable.auth_image_1,
        contentDescriptionResId = R.string.auth_image_1
    )
}

@Composable
fun BottomSection(
    state: ForgotPasswordState,
    onEmailChange : (String) -> Unit,
    onSubmitClick : () -> Unit,
    onNavigateBack: () -> Unit,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(0.dp),
        modifier = Modifier
            .fillMaxWidth()
    ) {
        CustomOutlinedTextField(
            value = state.email,
            onValueChange = { onEmailChange(it) },
            label = "Email Pengguna",
            placeholder = "Contoh: dishut@example.com",
            asteriskAtEnd = true,
            keyboardType = KeyboardType.Email,
            error = state.emailError,
            modifier = Modifier.fillMaxWidth(),
            rounded = 40,
        )

        Button(
            onClick = onSubmitClick,
            modifier = Modifier
                .fillMaxWidth()
                .height(40.dp),
            shape = MaterialTheme.shapes.extraLarge,
            enabled = !state.isLoading
        ) {
            if (state.isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = MaterialTheme.colorScheme.onPrimary,
                    strokeWidth = 2.dp
                )
            } else {
                Text(
                    text = "Kirim Verifikasi",
                    style = MaterialTheme.typography.labelLarge
                )
            }
        }

        TextButton(
            onClick = onNavigateBack,
            colors = ButtonDefaults.textButtonColors(
                contentColor = MaterialTheme.colorScheme.onSurface
            )
        ) {
            Text(
                text = "Ingat kata sandi? ",
                style = MaterialTheme.typography.labelLarge,
            )
            Text(
                text = "Masuk",
                style = MaterialTheme.typography.labelLarge,
                textDecoration = TextDecoration.Underline
            )
        }
    }
}