package com.dishut_lampung.sitanihut.presentation

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.dishut_lampung.sitanihut.R
import com.dishut_lampung.sitanihut.presentation.ui.theme.SitanihutTheme

@Preview(showBackground = true)
@Composable
fun SplashScreenPreview() {
    SitanihutTheme(dynamicColor = false) {
        SplashScreen(
            onNavigateToMain = {}
        )
    }
}

@Composable
fun SplashScreen(
    onNavigateToMain: () -> Unit = {}
) {
    val alpha = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        alpha.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 1000)
        )
        kotlinx.coroutines.delay(2000)
        onNavigateToMain()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.primary),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Image(
                painter = painterResource(id = R.drawable.logo_sitanihut),
                contentDescription = stringResource(id = R.string.logo),
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(
                        width = 168.dp,
                        height = 200.dp
                    )
            )
        }
    }
}
