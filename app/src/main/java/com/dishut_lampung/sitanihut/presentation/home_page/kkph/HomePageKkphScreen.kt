package com.dishut_lampung.sitanihut.presentation.home_page.kkph

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview

@Preview(showBackground = true)
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun HomePageKkphScreen(
    modifier: Modifier = Modifier
) {
    Column {
        Text(
            text = "INI HOME PAGE KKPH",
            style = MaterialTheme.typography.labelLarge
        )
    }
}