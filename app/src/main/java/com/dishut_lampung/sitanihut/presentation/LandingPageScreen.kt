package com.dishut_lampung.sitanihut.presentation

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.dishut_lampung.sitanihut.presentation.components.CenteredAuthImage

data class LandingPage(
    val title: String,
    val subtitle: String,
    @DrawableRes val imageResId: Int = R.drawable.auth_image_1,
    @StringRes val contentDescriptionResId: Int = R.string.auth_image_1
)

val landingPages = listOf(
    LandingPage(
        title = "Selamat datang di SITANIHUT Lampung",
        subtitle = "Silahkan masuk dengan akun yang terdaftar",
        imageResId = R.drawable.auth_image_3,
        contentDescriptionResId = R.string.auth_image_3
    ),
    LandingPage(
        title = "Ajukan Laporanmu!",
        subtitle = "Yuk ajukan pelaporan hasil pertanian dan Nilai Transaksi Ekonomi yang didapatkan",
        imageResId = R.drawable.auth_image_4,
        contentDescriptionResId = R.string.auth_image_4
    ),
    LandingPage(
        title = "Pantau Status Ajuanmu",
        subtitle = "Laporan akan diperiksa, cek berkala status pengajuan laporan anda, anda tetap bisa melakukan perubahan laporan yang ditolak dan mengajukannya kembali",
        imageResId = R.drawable.auth_image_5,
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
    onNavigateToLogin: () -> Unit
) {
    LandingPageScreen(
        modifier = modifier,
        onNavigateToLogin = onNavigateToLogin
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
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Spacer(Modifier.height(20.dp))

            LogoHeader(
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(52.dp))

            HorizontalPager(
                state = pagerState,
            ) { pageIndex ->
                OnboardingPageItem(page = landingPages[pageIndex])
            }

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
                shape = MaterialTheme.shapes.extraLarge
            ) {
                Text(
                    text = "Masuk",
                    style = MaterialTheme.typography.labelLarge
                )
            }

            Spacer(Modifier.height(48.dp))
        }
}

@Composable
private fun LogoHeader(modifier: Modifier = Modifier) {
    Row(
        modifier = modifier.padding(vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Start
    ) {
        Image(
            painter = painterResource(id = R.drawable.logo_sitanihut),
            contentDescription = stringResource(id = R.string.logo),
            modifier = Modifier.height(52.dp)
        )

        Spacer(Modifier.width(8.dp))

        Text(
            text = "Sitanihut",
            style = MaterialTheme.typography.titleLarge.copy(
                fontWeight = FontWeight.Bold
            ),
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
fun OnboardingPageItem(page: LandingPage) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        CenteredAuthImage(
            imageResId = page.imageResId,
            contentDescriptionResId = page.contentDescriptionResId
        )

        Spacer(Modifier.height(48.dp))

        Text(
            text = page.title,
            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center
        )

        Spacer(Modifier.height(16.dp))

        Text(
            text = page.subtitle,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
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
            val color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant

            Box(
                modifier = Modifier
                    .size(if (isSelected) 16.dp else 8.dp, 8.dp)
                    .clip(CircleShape)
                    .background(color)
            )
        }
    }
}
