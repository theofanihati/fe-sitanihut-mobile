package com.dishut_lampung.sitanihut.presentation.landing_page

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.dishut_lampung.sitanihut.R
import com.dishut_lampung.sitanihut.presentation.ui.theme.SitanihutTheme
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.dishut_lampung.sitanihut.presentation.components.CenteredAuthImage
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection

data class LandingPage(
    val title: String,
    val subtitle: String,
    @DrawableRes val imageResId: Int = R.drawable.onboarding_img1,
    @StringRes val contentDescriptionResId: Int = R.string.auth_image_1
)

val landingPages = listOf(
    LandingPage(
        title = "Selamat datang di SITANIHUT Lampung",
        subtitle = "Silahkan masuk dengan akun yang terdaftar",
        imageResId = R.drawable.onboarding_img1,
        contentDescriptionResId = R.string.auth_image_1
    ),
    LandingPage(
        title = "Ajukan Laporanmu!",
        subtitle = "Yuk ajukan pelaporan hasil pertanian dan Nilai Transaksi Ekonomi yang didapatkan",
        imageResId = R.drawable.onboarding_img2,
        contentDescriptionResId = R.string.auth_image_4
    ),
    LandingPage(
        title = "Pantau Status Ajuanmu",
        subtitle = "Laporan akan diperiksa, cek berkala status pengajuan laporan anda, anda tetap bisa melakukan perubahan laporan yang ditolak dan mengajukannya kembali",
        imageResId = R.drawable.onboarding_img3,
        contentDescriptionResId = R.string.auth_image_5
    )
)

@Preview(showBackground = true)
@Composable
fun LandingPageScreenPreview() {
    SitanihutTheme(dynamicColor = false) {
        LandingPageScreen(onNavigateToLogin = {})
    }
}

@Composable
fun LandingPageRoute(
    modifier: Modifier = Modifier,
    onNavigateToLogin: () -> Unit,
    viewModel: LandingPageViewModel = hiltViewModel()
) {
    LandingPageScreen(
        modifier = modifier,
        onNavigateToLogin = {
            viewModel.setOnboardingCompleted()
            onNavigateToLogin()
        }
    )
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun LandingPageScreen(
    onNavigateToLogin: () -> Unit,
    modifier: Modifier = Modifier
) {
    val pagerState = rememberPagerState { landingPages.size }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.primary),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top,
    ) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.weight(1f)
            ) { pageIndex ->
            OnboardingPageItem(page = landingPages[pageIndex])
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            DotsIndicator(
                pageCount = landingPages.size,
                currentPage = pagerState.currentPage
            )

            Spacer(Modifier.height(32.dp))

            Button(
                onClick = onNavigateToLogin,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape = MaterialTheme.shapes.extraLarge,
                colors = ButtonDefaults.buttonColors(MaterialTheme.colorScheme.secondary)
            ) {
                Text(
                    text = "Masuk",
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
                )
            }

            Spacer(Modifier.height(48.dp))
        }
    }
}

@Composable
fun OnboardingPageItem(page: LandingPage) {
    Column(
        verticalArrangement = Arrangement.Top,
        modifier = Modifier.fillMaxHeight()
    ) {
        Image(
            painter = painterResource(id = page.imageResId),
            contentDescription = page.title,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxWidth()
        )

        Spacer(Modifier.height(20.dp))

        Text(
            text = page.title,
            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.surface,
            textAlign = TextAlign.End,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
        )

        Spacer(Modifier.height(8.dp))

        Text(
            text = page.subtitle,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.surfaceVariant,
            textAlign = TextAlign.End,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
        )
    }
}

@Composable
fun DotsIndicator(
    pageCount: Int,
    currentPage: Int,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.padding(vertical = 24.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        repeat(pageCount) { index ->
            val isSelected = (index == currentPage)
            val color = if (isSelected) {
                MaterialTheme.colorScheme.onPrimaryContainer
            } else {
                MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.5f)
            }
            Box(
                modifier = Modifier
                    .size(if (isSelected) 16.dp else 8.dp, 8.dp)
                    .clip(CircleShape)
                    .background(color)
            )
        }
    }
}

