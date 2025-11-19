package com.dishut_lampung.sitanihut.presentation.home_page.penyuluh

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.dishut_lampung.sitanihut.R

@Preview(showBackground = true)
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun HomePagePenyuluhScreen(
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier.fillMaxSize()) {
        AsyncImage(
            model = R.drawable.homepage_background,
            alignment = Alignment.TopStart,
            contentDescription = "Background",
            contentScale = ContentScale.Fit,
            modifier = Modifier.matchParentSize()
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .statusBarsPadding()
        ) {
            Spacer(Modifier.height(80.dp))

            Text(
                text = "INI HOME PAGE",
                style = MaterialTheme.typography.labelLarge,
                modifier = Modifier.padding(horizontal = 16.dp),
                color = Color.White
            )
        }
    }
}