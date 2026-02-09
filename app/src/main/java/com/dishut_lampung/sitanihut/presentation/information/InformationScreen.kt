package com.dishut_lampung.sitanihut.presentation.information

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController

@Preview(showBackground = true)
@Composable
fun InformationScreenPreview() {
    InformationScreen(
        onAboutAppClick = {},
        onAboutCompanyClick = {},
        onContactClick = {}
    )
}

@Composable
fun InformationRoute(
    navController: NavHostController,
    onNavigateToAboutApp: () -> Unit,
    onNavigateToAboutCompany: () -> Unit,
    onNavigateToContact: () -> Unit
) {
    InformationScreen(
        onAboutAppClick = onNavigateToAboutApp,
        onAboutCompanyClick = onNavigateToAboutCompany,
        onContactClick = onNavigateToContact
    )
}

@Composable
fun InformationScreen(
    onAboutAppClick: () -> Unit,
    onAboutCompanyClick: () -> Unit,
    onContactClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(top = 80.dp)
    ) {
        InformationMenuItem(
            text = "Tentang aplikasi",
            onClick = onAboutAppClick
        )

        InformationMenuItem(
            text = "Tentang Dinas Kehutanan",
            onClick = onAboutCompanyClick
        )

        InformationMenuItem(
            text = "Hubungi kami",
            onClick = onContactClick
        )
    }
}

@Composable
private fun InformationMenuItem(
    text: String,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 18.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = text,
                style = MaterialTheme.typography.bodyLarge.copy(
//                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            )

            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurface
            )
        }

        HorizontalDivider(
            modifier = Modifier.padding(horizontal = 24.dp),
            thickness = 0.5.dp,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
        )
    }
}