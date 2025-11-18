package com.dishut_lampung.sitanihut.presentation.home_page.petani

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import com.dishut_lampung.sitanihut.R

@Preview(showBackground = true)
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun HomePagePetaniScreen(
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.fillMaxSize()
    ) {
        Image(
            painter = painterResource(id = R.drawable.homepage_background),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )

        Column {
            Text(
                text = "INI HOME PAGE PETANI",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.surface
            )
        }
    }
}