package com.dishut_lampung.sitanihut.presentation.information.contact

import androidx.annotation.DrawableRes
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Apartment
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Mail
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material.icons.filled.Whatsapp
import androidx.compose.material.icons.outlined.Call
import androidx.compose.material.icons.outlined.Domain
import androidx.compose.material.icons.outlined.Facebook
import androidx.compose.material.icons.outlined.Language
import androidx.compose.material.icons.outlined.Mail
import androidx.compose.material.icons.outlined.Whatsapp
import androidx.compose.material.icons.outlined.YoutubeSearchedFor
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dishut_lampung.sitanihut.R
import com.dishut_lampung.sitanihut.presentation.ui.theme.Dimens.ScreenPadding

@Preview(showBackground = true)
@Composable
fun ContactScreen() {
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surfaceContainerLowest)
            .verticalScroll(scrollState)
            .padding(horizontal = ScreenPadding),
    ) {
        Spacer(modifier = Modifier.height(100.dp))

        Text(
            text = "Hubungi Kami",
            style = MaterialTheme.typography.headlineSmall.copy(
                fontWeight = FontWeight.Bold,
            ),
            color = MaterialTheme.colorScheme.tertiary,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(32.dp))

        ContactItem(
            icon = Icons.Outlined.Call,
            text = "(0721) 703177"
        )

        ContactItem(
            icon = Icons.Outlined.Mail,
            text = "dishut@lampungprov.go.id"
        )

        ContactItem(
            icon = Icons.Outlined.Language,
            text = "https://dishut.lampungprov.go.id/"
        )

        ContactItem(
            icon = Icons.Outlined.Domain,
            text = "Jalan Zaenal Abidin Pagar Alam, Rajabasa, Kota Bandar Lampung, Lampung 35141 (0721) 703177"
        )

        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = "Sosial Media",
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            ),
            color = Color.Black
        )

        Spacer(modifier = Modifier.height(8.dp))

        SocialMediaItem(
            icon = R.drawable.ic_instagram,
            title = "Instagram",
            handle = "@kehutananprovlampung"
        )

        SocialMediaItem(
            icon = R.drawable.ic_facebook,
            title = "Facebook",
            handle = "Kehutanan ProvLampung"
        )

        SocialMediaItem(
            icon = R.drawable.ic_youtube,
            title = "YouTube",
            handle = "Kehutanan Prov Lampung"
        )

        SocialMediaItem(
            icon = R.drawable.ic_whatsapp,
            title = "WhatsApp",
            handle = "+62 821-8022-8516"
        )

        SocialMediaItem(
            icon = R.drawable.ic_twitter,
            title = "X",
            handle = "@humaslampung"
        )

        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
private fun ContactItem(
    icon: ImageVector,
    text: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.Top
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(24.dp)
        )

        Spacer(modifier = Modifier.width(16.dp))

        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = Color.Black,
            lineHeight = 20.sp
        )
    }
}

@Composable
private fun SocialMediaItem(
    @DrawableRes icon: Int,
    title: String,
    handle: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            painter = painterResource(id = icon),
            contentDescription = title,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(24.dp)
        )

        Spacer(modifier = Modifier.width(16.dp))

        Column {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.SemiBold
                ),
                color = Color.Black
            )
            Text(
                text = handle,
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray
            )
        }
    }
}
