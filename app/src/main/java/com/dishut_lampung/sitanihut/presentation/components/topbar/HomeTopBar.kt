package com.dishut_lampung.sitanihut.presentation.components.topbar

import android.util.Log
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
    imageUrl: String?,
    onLogoutClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Log.d("SITANIHUT_LOG", "Nilai imageUrl yang diterima: $imageUrl")
    TopAppBar(
        modifier = modifier,
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = Color.Transparent,
            titleContentColor = Color.White,
            actionIconContentColor = Color.White
        ),
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(imageUrl)
                        .crossfade(true)
                        .build(),
                    contentDescription = "Foto Profil",
                    placeholder = painterResource(id = R.drawable.placeholder_profile_picture),
                    error = painterResource(id = R.drawable.error_profile_picture),
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                )

                Spacer(Modifier.width(12.dp))

                Column {
                    Text(
                        text = nama,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold
                        ),
//                        color = MaterialTheme.colorScheme.onSurface
                        color = Color.White
                    )
                    Text(
                        text = role,
                        style = MaterialTheme.typography.titleSmall,
//                        color = MaterialTheme.colorScheme.onSurface
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
//                    tint = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    )
}