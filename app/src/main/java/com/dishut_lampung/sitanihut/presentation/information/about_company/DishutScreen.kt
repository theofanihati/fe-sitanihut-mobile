package com.dishut_lampung.sitanihut.presentation.information.about_company

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.dishut_lampung.sitanihut.R
import com.dishut_lampung.sitanihut.presentation.components.animations.AnimatedMessage
import com.dishut_lampung.sitanihut.presentation.components.animations.MessageType
import com.dishut_lampung.sitanihut.presentation.components.carousel.CarouselItemData
import com.dishut_lampung.sitanihut.presentation.components.carousel.CustomCarousel
import com.dishut_lampung.sitanihut.presentation.information.InformationEvent
import com.dishut_lampung.sitanihut.presentation.information.InformationState
import com.dishut_lampung.sitanihut.presentation.ui.theme.Dimens.ScreenPadding
import com.dishut_lampung.sitanihut.presentation.ui.theme.SitanihutTheme

@Preview(showBackground = true)
@Composable
fun DishutScreenPreview() {
    SitanihutTheme(dynamicColor = false) {
        DishutScreen(
            onDownloadClick = {},
            state = InformationState(),
            onEvent = {},
        )
    }
}

@Composable
fun DishutRoute(
    navController: NavHostController,
    viewModel: DishutViewModel = hiltViewModel()
){
    val state by viewModel.state.collectAsStateWithLifecycle()

    DishutScreen(
        state = state,
        onDownloadClick = {viewModel.onEvent(InformationEvent.onDownloadClick)},
        onEvent = viewModel::onEvent,
    )
}

@Composable
fun DishutScreen(
    state: InformationState,
    onDownloadClick : () -> Unit,
    onEvent: (InformationEvent) -> Unit,
) {
    val kphData = listOf(
        CarouselItemData(R.drawable.auth_image_1, "KPHK TAHURA Wan Abdul Rachman"),
        CarouselItemData(R.drawable.auth_image_1, "KPH Tangkit Teba"),
        CarouselItemData(R.drawable.auth_image_1, "KPH Way Waya")
    )

    val scrollState = rememberScrollState()

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(horizontal = ScreenPadding),
//        horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(Modifier.height(100.dp))

            Column(
                modifier = Modifier
                    .fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Image(
                    painter = painterResource(id = R.drawable.logo_prov_lampung),
                    contentDescription = "logo Provinsi Lampung",
                    contentScale = ContentScale.FillWidth,
                    modifier = Modifier
                        .width(160.dp),
                )

                Spacer(Modifier.height(8.dp))

                Text(
                    text = "Dinas Kehutanan",
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontWeight = FontWeight.Bold,
                    ),
                    color = MaterialTheme.colorScheme.tertiary,
                    textAlign = TextAlign.Center
                )

                Text(
                    text = "Provinsi Lampung",
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontWeight = FontWeight.Bold,
                    ),
                    color = MaterialTheme.colorScheme.tertiary,
                    textAlign = TextAlign.Center
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Dinas Kehutanan Provinsi Lampung adalah organisasi perangkat daerah yang mempunyai tugas menyelenggarakan sebagian urusan pemerintahan provinsi di bidang kehutanan serta tugas lain sesuai dengan kebijakan yang ditetapkan oleh Gubernur berdasarkan peraturan perundang-undangan yang berlaku",
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Start,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(40.dp))

            Text(
                text = "KPH Provinsi Lampung",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold,
                ),
                color = MaterialTheme.colorScheme.primary,
                textAlign = TextAlign.Start
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Provinsi Lampung memiliki 17 KPH (Kesatuan Pengelolaan Hutan) yang tersebar pada berbagai kabupaten/kota",
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Start,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(24.dp))

            CustomCarousel(
                items = kphData,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(40.dp))

            Text(
                text = "Susunan Organisasi",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                textAlign = TextAlign.Start
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Susunan organisasi Dinas Kehutanan Provinsi Lampung sesuai dengan Pergub Lampung Nomor 56 Tahun 2019.",
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Start,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(24.dp))

            Image(
                painter = painterResource(id = R.drawable.susunan_organisasi),
                contentDescription = "Langkah Melakukan Pelaporan",
                contentScale = ContentScale.FillWidth,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = onDownloadClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(40.dp),
                shape = MaterialTheme.shapes.extraLarge,
                enabled = !state.isLoading,
            ) {
                if (state.isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(
                        text = "Unduh",
                        style = MaterialTheme.typography.labelLarge
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
        AnimatedMessage(
            isVisible = state.successMessage != null,
            message = state.successMessage ?: "",
            messageType = MessageType.Success,
            onDismiss = { onEvent(InformationEvent.OnDismissSuccessMessage) },
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(horizontal = 24.dp)
                .padding(bottom = 16.dp)
        )
        AnimatedMessage(
            isVisible = state.generalError != null,
            message = state.generalError ?: "",
            messageType = MessageType.Error,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 80.dp),
            onDismiss = { onEvent(InformationEvent.OnDismissError) },
        )
    }
}