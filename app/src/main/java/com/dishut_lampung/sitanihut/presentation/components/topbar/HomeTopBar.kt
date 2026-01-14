package com.dishut_lampung.sitanihut.presentation.components.topbar

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.dishut_lampung.sitanihut.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeTopBar(
    nama: String,
    role: String,
    onLogoutClick: () -> Unit,
    modifier: Modifier = Modifier,
    onProfileClick: (String) -> Unit,
) {
    val avatarResId = when (role.lowercase()) {
        "petani" -> R.drawable.avatar_1
        "penyuluh" -> R.drawable.avatar_2
        "penanggung jawab" -> R.drawable.avatar_3
        else -> R.drawable.avatar_1
    }
    TopAppBar(
        modifier = modifier,
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = Color.Transparent,
            titleContentColor = Color.White,
            actionIconContentColor = Color.White
        ),
        title = {
            Row(
                modifier = Modifier
                    .clip(CircleShape)
                    .clickable {
                        onProfileClick(role)
                    }
                    .padding(vertical = 4.dp, horizontal = 4.dp),
            ) {
                Image(
                    painter = painterResource(id = avatarResId),
                    contentDescription = "Foto Profil",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .border(2.dp, Color.White, CircleShape)
                )

                Spacer(Modifier.width(12.dp))

                Column {
                    Text(
                        text = nama,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        color = Color.White
                    )
                    Text(
                        text = role,
                        style = MaterialTheme.typography.titleSmall,
                        color = Color.White.copy(alpha = 0.8f)
                    )
                }
            }
        },
        actions = {
            IconButton(onClick = onLogoutClick) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ExitToApp,
                    contentDescription = "Logout",
                )
            }
        }
    )
}