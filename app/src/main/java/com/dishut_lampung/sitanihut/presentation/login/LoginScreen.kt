package com.dishut_lampung.sitanihut.presentation.login

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
import com.dishut_lampung.sitanihut.presentation.shared.navigation.Screen
import com.dishut_lampung.sitanihut.presentation.shared.theme.SitanihutTheme


//@Preview(showBackground = true)
//@Composable
//fun LoginScreenPreview() {
//    SitanihutTheme(dynamicColor = false) {
//        LoginScreen(
//            state = LoginState(email = "test@email.com"),
//            onEvent = {},
//            onNavigateBack = {}
//        )
//    }
//}

@Preview(showBackground = true)
@Composable
fun LoginScreenErrorPreview() {
    SitanihutTheme(dynamicColor = false) {
        LoginScreen(
            state = LoginState(
                email = "aaaa.kl" ,
                emailError = "Format email tidak valid",
                isLoading = true,
                ),
            onEvent = {},
        )
    }
}

@Composable
fun LoginRoute(
    navController: NavHostController,
    viewModel: LoginViewModel = hiltViewModel()
) {
    val state = viewModel.loginState
    val context = LocalContext.current

    LaunchedEffect(key1 = true) {
        viewModel.eventFlow.collect { event ->
            when (event) {
                is UiEvent.NavigateToHome -> {
                    navController.navigate(event.route) {
                        popUpTo("auth") { inclusive = true }
                    }
                }
                is UiEvent.NavigateToForgotPassword -> {
                    navController.navigate("forgot_password_screen")
                }
            }
        }
    }

    LoginScreen(
        state = state,
        onEvent = viewModel::onEvent,
    )
}

@Composable
fun LoginScreen(
    modifier : Modifier = Modifier,
    state: LoginState,
    onEvent: (LoginEvent) -> Unit,
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
                onEmailChange = { email -> onEvent(LoginEvent.OnEmailChange(email)) },
                onPasswordChange = { password -> onEvent(LoginEvent.OnPasswordChange(password)) },
                onLoginClick = { onEvent(LoginEvent.OnLoginClick) },
                onNavigateToForgotPassword = { onEvent(LoginEvent.OnForgotPasswordClick) },
                onTogglePasswordVisibility = { onEvent(LoginEvent.OnTogglePasswordVisibility) },
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
            onDismiss = { onEvent(LoginEvent.OnDismissError) },
        )
        AnimatedMessage(
            isVisible = state.successMessage != null,
            message = state.successMessage ?: "",
            messageType = MessageType.Success,
            onDismiss = { onEvent(LoginEvent.OnDismissSuccessMessage) },
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
        text = "Selamat datang di",
        style = MaterialTheme.typography.headlineMedium,
        color = MaterialTheme.colorScheme.onSurface,
        fontWeight = FontWeight.Bold
    )
    Text(
        text = "SITANIHUT Lampung",
        style = MaterialTheme.typography.headlineLarge,
        color = MaterialTheme.colorScheme.tertiary,
        fontWeight = FontWeight.Bold
    )
    Text(
        text = "Silahkan masuk dengan akun yang terdaftar",
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )

    Spacer(Modifier.height(24.dp))
    CenteredAuthImage(
        imageResId = R.drawable.auth_image_2,
        contentDescriptionResId = R.string.auth_image_2
    )
}

@Composable
fun BottomSection(
    state: LoginState,
    onEmailChange : (String) -> Unit,
    onPasswordChange : (String) -> Unit,
    onLoginClick : () -> Unit,
    onNavigateToForgotPassword : () -> Unit,
    onTogglePasswordVisibility: () -> Unit,
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
            label = "Email / NIP / NIK",
            placeholder = "Isi dengan NIK/NIP/email terdaftar",
            asteriskAtEnd = true,
            keyboardType = KeyboardType.Text,
            error = state.emailError,
            modifier = Modifier.fillMaxWidth(),
            rounded = 40,
        )

        CustomOutlinedTextField(
            value = state.password,
            onValueChange = { onPasswordChange(it) },
            label = "Kata Sandi",
            placeholder = "Masukkan kata sandi",
            asteriskAtEnd = true,
            isPassword = true,
            isPasswordVisible = state.isPasswordVisible,
            onPasswordToggleClick = onTogglePasswordVisibility,
            error = state.passwordError,
            modifier = Modifier.fillMaxWidth(),
            rounded = 40,
        )

        TextButton(
            onClick = onNavigateToForgotPassword,
            modifier = Modifier.align(Alignment.End),
            colors = ButtonDefaults.textButtonColors(
                contentColor = MaterialTheme.colorScheme.onSurface
            )
        ) {
            Text(
                text = "Lupa kata sandi",
                textDecoration = TextDecoration.Underline
            )
        }

        Button(
            onClick = onLoginClick,
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
                    text = "Masuk",
                    style = MaterialTheme.typography.labelLarge
                )
            }
        }
    }
}