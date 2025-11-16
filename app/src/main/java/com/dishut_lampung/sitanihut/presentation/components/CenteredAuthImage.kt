package com.dishut_lampung.sitanihut.presentation.components

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.dishut_lampung.sitanihut.R

@Composable
@Preview(showBackground = true)
fun CenteredAuthImage(
    @DrawableRes imageResId: Int = R.drawable.auth_image_1,
    @StringRes contentDescriptionResId: Int = R.string.auth_image_1
) {
    Box(
        modifier = Modifier
            .fillMaxWidth(),
        contentAlignment = Alignment.Center,
        ) {
        Image(
            painter = painterResource(id = imageResId),
            contentDescription = stringResource(id = contentDescriptionResId),
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxWidth()
                .height(220.dp) // Tentukan tingginya saja
                .clip(RoundedCornerShape(16.dp))
        )
    }
}