package com.dishut_lampung.sitanihut.presentation.petani.detail

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.dishut_lampung.sitanihut.R
import com.dishut_lampung.sitanihut.domain.model.Petani
import com.dishut_lampung.sitanihut.presentation.shared.components.CustomCircularProgressIndicator

@Preview(showBackground = true, name = "Petani Detail")
@Composable
fun PetaniDetailScreenPreview() {
    val dummyPetani = Petani(
        id = "1",
        name = "Petani Jelita",
        identityNumber = "1871012345678901",
        gender = "Wanita",
        address = "Desa Bangun Rejo",
        whatsAppNumber = "081234567890",
        lastEducation = "SMA",
        sideJob = "Pedagang",
        landArea = 2.5,
        kphName = "UPTD KPHK Tahura WAR",
        kthName = "KTH Makmur Jaya",
        kphId = "1",
        kthId = "2"
    )

    PetaniDetailScreen(
        isLoading = false,
        petani = dummyPetani,
        errorMessage = null,
    )
}

@Composable
fun PetaniDetailRoute(
    viewModel: PetaniDetailViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    PetaniDetailScreen(
        isLoading = state.isLoading,
        petani = state.petani,
        errorMessage = state.error,
    )
}

@Composable
fun PetaniDetailScreen(
    isLoading: Boolean,
    petani: Petani?,
    errorMessage: String?,
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
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
            } else if (petani != null) {

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState()),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Spacer(modifier = Modifier.height(100.dp))

                    Text(
                        text = "Detail Data Petani",
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
                            DetailRow("Nama Petani", petani.name)
                            DetailRow("NIK", petani.identityNumber)
                            DetailRow("Jenis Kelamin", petani.gender.orWaiting())
                            DetailRow("Alamat", petani.address.orWaiting())
                            DetailRow("Nomor Telepon", petani.whatsAppNumber.orWaiting())
                            DetailRow("Pendidikan", petani.lastEducation.orWaiting())
                            DetailRow("Pekerjaan Sampingan", petani.sideJob.orWaiting())
                            DetailRow("Luas Lahan", "${petani.landArea.toLandAreaString()} Ha")
                            DetailRow("Asal KPH", petani.kphName)
                            DetailRow("Asal KTH", petani.kthName)

                            Spacer(modifier = Modifier.height(40.dp))
                        }
                    }
                }

            } else {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        text = errorMessage ?: "Data Petani tidak ditemukan",
                        color = Color.White,
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }
        }
    }
}

private fun String?.orWaiting(): String {
    return if (this.isNullOrBlank()) "Belum dimuat (Menunggu Online)" else this
}

private fun Double?.toLandAreaString(): String {
    return this?.let { "$it Ha" } ?: "Belum dimuat (Menunggu Online)"
}

@Composable
private fun DetailRow(
    label: String,
    value: String
) {
    val isPlaceholder = value.contains("Menunggu Online")

    val valueColor = if (isPlaceholder) {
        MaterialTheme.colorScheme.onSurfaceVariant
    } else {
        MaterialTheme.colorScheme.onSurface
    }

    val fontStyle = if (isPlaceholder) FontStyle.Italic else FontStyle.Normal

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
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            fontStyle = fontStyle,
            color = valueColor,
            modifier = Modifier.weight(0.6f)
        )
    }
}