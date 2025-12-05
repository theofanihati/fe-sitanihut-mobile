package com.dishut_lampung.sitanihut.presentation.components.card

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.dishut_lampung.sitanihut.domain.model.ReportUiModel
import com.dishut_lampung.sitanihut.presentation.ui.theme.blue
import com.dishut_lampung.sitanihut.presentation.ui.theme.purple
import com.dishut_lampung.sitanihut.presentation.ui.theme.warning

@Composable
fun ReportCard(
    modifier: Modifier = Modifier,
    item: ReportUiModel,
    isPetani: Boolean,
    isKomoditas: Boolean = false,
    onCardClick: (String) -> Unit,
    onActionClick: (String) -> Unit
) {
    val (statusBgColor, statusTextColor) = getStatusColors(item.statusDisplay)

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .clip(RoundedCornerShape(28.dp))
            .clickable { if (!isPetani) onCardClick(item.id) },
        shape = RoundedCornerShape(28.dp),
        border = BorderStroke(
            width = 1.dp,
            color = MaterialTheme.colorScheme.tertiary
        ),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLowest
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Default.FolderOpen,
                    contentDescription = "Icon Dokumen",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.periodTitle,
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.tertiary
                )

                Text(
                    text = item.dateDisplay,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Text(
                    text = item.nteDisplay,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary
                )

                Spacer(modifier = Modifier.height(4.dp))

                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(statusBgColor)
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                        .defaultMinSize(minWidth = 64.dp)
                ) {
                    Text(
                        text = item.statusDisplay,
                        style = MaterialTheme.typography.bodySmall.copy(
//                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        ),
                        color = statusTextColor
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.Center
            ) {
                IconButton(
                    onClick = { onActionClick(item.id) },
                    modifier = Modifier.size(32.dp)
                ) {
                    if (isPetani) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "Opsi",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else if(isKomoditas){
                    }
                    else {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = "Detail",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }
    }
}

data class StatusColor(
    val container: Color,
    val content: Color
)

@Composable
fun getStatusColors(statusText: String): StatusColor {
    return when (statusText.lowercase()) {
        "belum diajukan", "draft" -> StatusColor(Color.LightGray, Color.White)
        "menunggu" -> StatusColor(warning, Color.White)
        "disetujui" -> StatusColor(MaterialTheme.colorScheme.tertiary, Color.White)
        "ditolak" -> StatusColor(MaterialTheme.colorScheme.error, Color.White)

        "pemeriksaan penyuluh" -> StatusColor(blue, Color.White)
        "pemeriksaan kph" -> StatusColor(purple, Color.White)

        else -> StatusColor(Color.Transparent, Color.Black)
    }
}