package com.dishut_lampung.sitanihut.presentation.profile

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.dishut_lampung.sitanihut.R
import com.dishut_lampung.sitanihut.presentation.ui.theme.Dimens

@Preview(showBackground = true)
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun PetaniProfileScreen(
    modifier: Modifier = Modifier,

//    imageUrl: String?,
) {
    Box(modifier = modifier.fillMaxSize()) {
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
                .verticalScroll(rememberScrollState())
                .statusBarsPadding()
        ) {
            Spacer(Modifier.height(80.dp))

            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data("/* FOTO AMBIL DARI PREFERENCE */")
                    .crossfade(true)
                    .build(),
                contentDescription = "Foto Profil",
                placeholder = painterResource(id = R.drawable.placeholder_profile_picture),
                error = painterResource(id = R.drawable.error_profile_picture),
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(60.dp)
                    .clip(CircleShape)
                    .border(2.dp, Color.White, CircleShape)
            )

            Text(
                text = "/* NAMA AMBIL DARI PREFERENCE*/",
                style = MaterialTheme.typography.labelLarge,
                modifier = Modifier.padding(horizontal = Dimens.ScreenPadding),
                color = MaterialTheme.colorScheme.surface
            )
        }
    }
}