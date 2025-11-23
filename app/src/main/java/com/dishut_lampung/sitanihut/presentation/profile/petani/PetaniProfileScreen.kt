package com.dishut_lampung.sitanihut.presentation.profile.petani

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.dishut_lampung.sitanihut.R
import com.dishut_lampung.sitanihut.domain.model.UserDetail
import com.dishut_lampung.sitanihut.presentation.components.CustomCircularProgressIndicator
import com.dishut_lampung.sitanihut.presentation.components.card.KphKthCard
import com.dishut_lampung.sitanihut.presentation.ui.theme.Dimens
@Preview(showBackground = true, name = "Petani Profile")
@Composable
fun PetaniProfileScreenPreview() {
    val dummyUser = UserDetail(
        id = "1",
        name = "Budi Santoso",
        email = "budi@petani.com",
        role = "Petani",
        profilePictureUrl = "",
        roleId = "role-1",
        kphId = "kph-1",
        kphName = "UPTD KPH Batutegi",
        kthId = "kth-1",
        kthName = "KTH Makmur Jaya Sentosa",
        identityNumber = "1801020304050001",
        gender = "Pria",
        address = "Desa Suka Maju, Kec. Air Naningan",
        whatsAppNumber = "081234567890",
        lastEducation = "SMA",
        sideJob = "Pedagang",
        landArea = 2.5,
        position = "Anggota"
    )

    PetaniProfileScreen(
        isLoading = false,
        user = dummyUser,
        errorMessage = null,
    )
}
//
//@Preview(showBackground = true, name = "Loading State")
//@Composable
//fun ProfileLoadingPreview() {
//    PetaniProfileScreen(
//        isLoading = true,
//        user = null,
//        errorMessage = null,
//    )
//}
//
//@Preview(showBackground = true, name = "Error State")
//@Composable
//fun ProfileErrorPreview() {
//    PetaniProfileScreen(
//        isLoading = false,
//        user = null,
//        errorMessage = "Gagal memuat data profil. Periksa koneksi internet Anda.",
//
//    )
//}

@Composable
fun PetaniProfileRoute(
    viewModel: PetaniProfileViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsState()

    PetaniProfileScreen(
        isLoading = state.isLoading,
        user = state.user,
        errorMessage = state.generalError,
    )
}

@Composable
fun PetaniProfileScreen(
    isLoading: Boolean,
    user: UserDetail?,
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
            } else if (user != null) {
                ProfileContent(user = user)
            } else {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        text = errorMessage ?: "Data profil tidak ditemukan",
                        color = Color.White
                    )
                }
            }
        }
    }
}

@Composable
private fun ProfileContent(user: UserDetail) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(80.dp))

        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(user.profilePictureUrl)
                .crossfade(true)
                .placeholder(R.drawable.placeholder_profile_picture)
                .error(R.drawable.error_profile_picture)
                .build(),
            contentDescription = "Foto Profil",
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .size(100.dp)
                .clip(CircleShape)
                .border(3.dp, Color.White, CircleShape)
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = user.name,
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
            color = Color.White
        )
        Text(
            text = user.role,
            style = MaterialTheme.typography.bodyMedium,
            color = Color.White.copy(alpha = 0.9f)
        )

        Spacer(modifier = Modifier.height(24.dp))

        KphKthCard(
            modifier = Modifier.padding(horizontal = Dimens.ScreenPadding),
            kthName = user.kthName,
            kphName = user.kphName
        )

        Spacer(modifier = Modifier.height(24.dp))

        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.surfaceContainerLowest,
            shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
            shadowElevation = 4.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
                    .defaultMinSize(minHeight = 500.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                ProfileDetailRow("Nama lengkap", user.name)
                ProfileDetailRow("Role", user.role)
                ProfileDetailRow("Email", user.email)
                ProfileDetailRow("NIK", user.identityNumber)
                ProfileDetailRow("Nomor telepon", user.whatsAppNumber ?: "-")

                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 12.dp),
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                )

                ProfileDetailRow("Jenis kelamin", user.gender ?: "-")
                ProfileDetailRow("Alamat", user.address ?: "-")
                ProfileDetailRow("Pendidikan terakhir", user.lastEducation ?: "-")
                ProfileDetailRow("Pekerjaan sampingan", user.sideJob ?: "-")

                if (user.landArea != null && user.landArea > 0) {
                    ProfileDetailRow("Luas lahan", "${user.landArea} Ha")
                }

                Spacer(modifier = Modifier.height(40.dp))
            }
        }
    }
}

@Composable
private fun ProfileDetailRow(
    label: String,
    value: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
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
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Normal),
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(0.6f)
        )
    }
}