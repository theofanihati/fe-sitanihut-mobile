package com.dishut_lampung.sitanihut.presentation.user_management.detail

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
import androidx.compose.material3.HorizontalDivider
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
import com.dishut_lampung.sitanihut.domain.model.User
import com.dishut_lampung.sitanihut.domain.model.UserDetail
import com.dishut_lampung.sitanihut.presentation.shared.components.CustomCircularProgressIndicator
import java.util.Locale

@Preview(showBackground = true, name = "User Detail - Petani")
@Composable
fun UserDetailPetaniPreview() {
    val dummyUser = UserDetail(
        id = "1",
        name = "Mustika Weni",
        email = "wenwen@gmail.com",
        role = "petani",
        gender = "wanita",
        identityNumber = "1234567890123456",
        // Petani
        address = "Desa Makmur Gg.Saleh",
        whatsAppNumber = "08123456789",
        lastEducation = "SMA",
        sideJob = "Pedagang",
        landArea = 2.5,
        kthName = "KTH Suka Jaya",
        kphName = "Liwa",
        // Penyuluh (Null)
        position = null
    )
    UserDetailScreen(isLoading = false, user = dummyUser, errorMessage = null)
}

@Preview(showBackground = true, name = "User Detail - Penyuluh")
@Composable
fun UserDetailPenyuluhPreview() {
    val dummyUser = UserDetail(
        id = "2",
        name = "Siti Aminah",
        email = "siti.penyuluh@dinas.go.id",
        role = "penyuluh",
        gender = "wanita",
        identityNumber = "123456789012345678",
        whatsAppNumber = null,
        // Petani (Null/)
        address = null,
        // Penyuluh
        position = "Koordinator Lapangan",
        kphName = "Liwa",
    )
    UserDetailScreen(isLoading = false, user = dummyUser, errorMessage = null)
}

@Composable
fun UserDetailRoute(
    viewModel: UserDetailViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    UserDetailScreen(
        isLoading = state.isLoading,
        user = state.user,
        errorMessage = state.error,
    )
}

@Composable
fun UserDetailScreen(
    isLoading: Boolean,
    user: UserDetail?,
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
            } else if (user != null) {

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState()),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Spacer(modifier = Modifier.height(100.dp))

                    Text(
                        text = "Detail Data Pengguna",
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
                            // UMUM
                            DetailRow("Nama lengkap", user.name)
                            DetailRow("Role", user.role.replaceFirstChar {
                                if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString()
                            })
                            DetailRow("Email", user.email.orWaiting())
                            DetailRow(if (user.role == "petani") "NIK" else "NIP", user.identityNumber.orWaiting())
                            DetailRow("Nomor telepon", user.whatsAppNumber.orWaiting())

                            HorizontalDivider(
                                modifier = Modifier.padding(vertical = 12.dp),
                                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                            )

                            DetailRow("Jenis kelamin", user.gender.orWaiting())

                            when (user.role.lowercase()) {
                                "petani" -> {
                                    DetailRow("Alamat", user.address.orWaiting())
                                    DetailRow("Pendidikan terakhir", user.lastEducation.orWaiting())
                                    DetailRow("Pekerjaan sampingan", user.sideJob.orWaiting())
                                    DetailRow("Luas lahan", user.landArea.toLandAreaString())
                                }

                                "penyuluh" -> {
                                    DetailRow("Jabatan", user.position.orWaiting())
                                }

                                "admin", "penanggung jawab" -> {
                                    DetailRow("Jabatan", user.position.orWaiting())
                                }

                                else -> {
                                }
                            }
                            DetailRow("Asal KPH", user.kphName.orWaiting())

                            if (user.role == "petani"){
                                DetailRow("Asal KTH", user.kthName.orWaiting())
                            }

                            Spacer(modifier = Modifier.height(40.dp))
                        }
                    }
                }

            } else {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        text = errorMessage ?: "Data Pengguna tidak ditemukan",
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