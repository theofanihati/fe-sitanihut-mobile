package com.dishut_lampung.sitanihut.presentation.information.about_app

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.dishut_lampung.sitanihut.R
import com.dishut_lampung.sitanihut.presentation.components.carousel.CarouselItemData
import com.dishut_lampung.sitanihut.presentation.ui.theme.Dimens.ScreenPadding

@Preview(showBackground = true)
@Composable
fun AboutScreen() {
    val kphData = listOf(
        CarouselItemData(R.drawable.auth_image_1, "KPHK TAHURA Wan Abdul Rachman"),
        CarouselItemData(R.drawable.auth_image_1, "KPH Tangkit Teba"),
        CarouselItemData(R.drawable.auth_image_1, "KPH Way Waya")
    )

    val scrollState = rememberScrollState()
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surfaceContainerLowest)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(horizontal = ScreenPadding),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(Modifier.height(100.dp))

            Text(
                text = "SITANIHUT Lampung",
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.Bold,
                ),
                color = MaterialTheme.colorScheme.tertiary,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Sistem Informasi Petani Hutan Provinsi Lampung",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.SemiBold
                ),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Platform digital guna mendukung pengelolaan data petani hutan dan kelompok tani hutan secara terpadu, akurat, dan mudah diakses. Bertujuan membantu Dinas Kehutanan dalam pencatatan, pemantauan, serta pelaporan kegiatan petani hutan di seluruh wilayah Provinsi Lampung.",
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Start,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(24.dp))

            Image(
                painter = painterResource(id = R.drawable.roadmap_pelaporan),
                contentDescription = "Langkah Melakukan Pelaporan",
                contentScale = ContentScale.FillWidth,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Versi 1.0.0 (Beta)",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "© 2025 Dinas Kehutanan Provinsi Lampung",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                textAlign = TextAlign.Center,
            )
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}