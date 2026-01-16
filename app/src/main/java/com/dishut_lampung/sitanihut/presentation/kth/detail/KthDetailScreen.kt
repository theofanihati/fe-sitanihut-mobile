package com.dishut_lampung.sitanihut.presentation.kth.detail

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.dishut_lampung.sitanihut.R
import com.dishut_lampung.sitanihut.domain.model.Kth
import com.dishut_lampung.sitanihut.presentation.shared.components.CustomCircularProgressIndicator

@Preview(showBackground = true, name = "Kth Detail")
@Composable
fun KthDetailScreenPreview() {
    val dummyKth = Kth(
        id = "1",
        name = "KTH Cansz",
        desa = "Bangun rejo",
        kecamatan = "Kemiling",
        kabupaten = "Lampung Selatan",
        coordinator = "Budi Santoso",
        whatsappNumber = "081234567890",
        kphName = "UPTD KPHK Tahura WAR"
    )

    KthDetailScreen(
        isLoading = false,
        kth = dummyKth,
        errorMessage = null,
    )
}

@Composable
fun KthDetailRoute(
    viewModel: KthDetailViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    KthDetailScreen(
        isLoading = state.isLoading,
        kth = state.kth,
        errorMessage = state.error,
    )
}

@Composable
fun KthDetailScreen(
    isLoading: Boolean,
    kth: Kth?,
    errorMessage: String?,
) {
    Box(modifier = Modifier.fillMaxSize()) {
        val isPreview = LocalInspectionMode.current
        if (isPreview) {
            Image(
                painter = painterResource(id = R.drawable.homepage_background),
                contentDescription = "Background",
                contentScale = ContentScale.FillWidth,
                modifier = Modifier.matchParentSize(),
                alignment = Alignment.TopStart
            )
        } else {
            AsyncImage(
                model = R.drawable.homepage_background,
                alignment = Alignment.TopStart,
                contentDescription = "Background",
                contentScale = ContentScale.FillWidth,
                modifier = Modifier.matchParentSize()
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
        ) {
            if (isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CustomCircularProgressIndicator()
                }
            } else if (kth != null) {

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState()),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Spacer(modifier = Modifier.height(100.dp))

                    Text(
                        text = "Detail Data KTH",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = Color.White
                    )

                    Spacer(modifier = Modifier.height(32.dp))

                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        color = MaterialTheme.colorScheme.surfaceContainerLowest,
                        shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp)
                                .defaultMinSize(minHeight = 660.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            DetailRow("Nama KTH", kth.name)
                            DetailRow("Kabupaten", kth.kabupaten)
                            DetailRow("Kecamatan", kth.kecamatan?: "-")
                            DetailRow("Desa", kth.desa)
                            DetailRow("Ketua KTH", kth.coordinator ?: "-")
                            DetailRow("Nomor WhatsApp", kth.whatsappNumber ?: "-")
                            DetailRow("Asal KPH", kth.kphName)

                            Spacer(modifier = Modifier.height(40.dp))
                        }
                    }
                }

            } else {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        text = errorMessage ?: "Data KTH tidak ditemukan",
                        color = Color.White,
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }
        }
    }
}

@Composable
private fun DetailRow(
    label: String,
    value: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(0.4f)
        )

        Text(
            text = ":",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(end = 8.dp)
        )

        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(0.6f)
        )
    }
}