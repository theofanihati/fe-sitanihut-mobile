package com.dishut_lampung.sitanihut.presentation.shared.components.animations

import androidx.compose.runtime.getValue
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.offset
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.dishut_lampung.sitanihut.presentation.components.message.ErrorMessage
import com.dishut_lampung.sitanihut.presentation.components.message.SuccessMessage
import kotlinx.coroutines.delay

@Composable
fun AnimatedMessage(
    isVisible: Boolean,
    message: String,
    modifier: Modifier = Modifier,
    messageType: MessageType = MessageType.Success,
    onDismiss: () -> Unit
) {
    LaunchedEffect(isVisible) {
        if (isVisible) {
            delay(3000L) // Tunggu 3 detik
            onDismiss()
        }
    }
    AnimatedVisibility(
        visible = isVisible,
        modifier = modifier,
        enter = slideInVertically(      // 'enter' (isVisible = true)
            initialOffsetY = { -it },   // -it = -fullHeight)
            animationSpec = tween(durationMillis = 1000, easing = FastOutSlowInEasing)
        ) + fadeIn(),
        exit = slideOutVertically(      // 'exit' ( isVisible = false)
            targetOffsetY = { -it },
            animationSpec = tween(durationMillis = 1000, easing = FastOutSlowInEasing)
        ) + fadeOut()
    ) {
        when (messageType) {
            MessageType.Success -> {
                SuccessMessage(
                    message = message
                )
            }

            MessageType.Error -> {
                ErrorMessage(
                    message = message
                )
            }
        }
    }
}

enum class MessageType {
    Success, Error
}
