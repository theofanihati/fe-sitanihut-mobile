package com.dishut_lampung.sitanihut.presentation.shared.components.file_uploader

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.dishut_lampung.sitanihut.domain.model.ReportAttachment
import com.dishut_lampung.sitanihut.util.openFileOrUrl
import java.io.File

@Composable
fun FileUploader(
    modifier: Modifier = Modifier,
    files: List<ReportAttachment>,
    onAddFileClick: () -> Unit,
    onRemoveFileClick: (Int) -> Unit,
    onFileClick: (ReportAttachment) -> Unit,
    maxFiles: Int = 10,
    label: String = "Lampiran"
) {
    val context = LocalContext.current
    val dashedColor = MaterialTheme.colorScheme.secondary

    Column(
        modifier = modifier.fillMaxWidth()
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        if (files.isEmpty()) {
            EmptyUploadBox(
                dashedColor = dashedColor,
                onClick = onAddFileClick
            )
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                files.forEachIndexed { index, attachment ->
                    SelectedFileItem(
                        attachment = attachment,
                        onRemove = { onRemoveFileClick(index) },
                        onClick = { onFileClick(attachment) }
                    )
                }

                if (files.size < maxFiles) {
                    SmallAddFileButton(
                        dashedColor = dashedColor,
                        onClick = onAddFileClick
                    )
                }
            }
        }
    }
}

@Composable
private fun EmptyUploadBox(
    dashedColor: Color,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(100.dp)
            .dashedBorder(color = dashedColor, cornerRadius = 12.dp)
            .background(
                color = dashedColor.copy(alpha = 0.05f),
                shape = RoundedCornerShape(20.dp)
            )
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "Ketuk untuk unggah file",
                style = MaterialTheme.typography.bodyMedium,
                color = dashedColor,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
private fun SmallAddFileButton(
    dashedColor: Color,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(50.dp)
            .dashedBorder(color = dashedColor, cornerRadius = 8.dp)
            .clickable { onClick() }
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.Add,
            contentDescription = "Tambah File",
            tint = dashedColor
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = "Tambah File Lain",
            style = MaterialTheme.typography.labelLarge,
            color = dashedColor
        )
    }
}

@Composable
private fun SelectedFileItem(
    attachment: ReportAttachment,
    onRemove: () -> Unit,
    onClick: () -> Unit
) {
    val fileName = if (attachment.isLocal) {
        File(attachment.filePath).name
    } else {
        attachment.filePath.substringAfterLast("/")
    }

    val fileSizeString = if (attachment.isLocal) {
        val file = File(attachment.filePath)
        if (file.exists()) "${file.length() / 1024} KB" else "File tidak ditemukan"
    } else {
        "File Server"
    }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.surfaceContainerLowest,
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Description,
                contentDescription = "File",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = fileName,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1
                )
                Text(
                    text = fileSizeString,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            IconButton(onClick = onRemove) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Hapus",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

fun Modifier.dashedBorder(
    color: Color,
    strokeWidth: Dp = 2.dp,
    dashLength: Dp = 8.dp,
    gapLength: Dp = 4.dp,
    cornerRadius: Dp = 0.dp
) = this.drawBehind {
    val strokeWidthPx = strokeWidth.toPx()
    val dashLengthPx = dashLength.toPx()
    val gapLengthPx = gapLength.toPx()
    val cornerRadiusPx = cornerRadius.toPx()

    drawRoundRect(
        color = color,
        cornerRadius = CornerRadius(cornerRadiusPx, cornerRadiusPx),
        style = Stroke(
            width = strokeWidthPx,
            pathEffect = PathEffect.dashPathEffect(floatArrayOf(dashLengthPx, gapLengthPx), 0f)
        )
    )
}