package com.dishut_lampung.sitanihut.presentation.components.file_uploader

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Download
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.dishut_lampung.sitanihut.domain.model.ReportAttachment
import java.io.File

@Composable
fun ReadOnlyFileAttachment(
    modifier: Modifier = Modifier,
    files: List<ReportAttachment>,
    onDownloadClick: (ReportAttachment) -> Unit,
    label: String = "Lampiran"
) {
    Column(
        modifier = modifier.fillMaxWidth()
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.tertiary,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        if (files.isEmpty()) {
            Text(
                text = "- Tidak ada lampiran -",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                files.forEach { attachment ->
                    ReadOnlyFileItem(
                        attachment = attachment,
                        onClick = { onDownloadClick(attachment) }
                    )
                }
            }
        }
    }
}

@Composable
private fun ReadOnlyFileItem(
    attachment: ReportAttachment,
    onClick: () -> Unit
) {
    val fileName = if (attachment.isLocal) {
        File(attachment.filePath).name
    } else {
        attachment.fileName ?: attachment.filePath.substringAfterLast("/")
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

            Text(
                text = fileName,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                modifier = Modifier.weight(1f)
            )

            IconButton(onClick = onClick) {
                Icon(
                    imageVector = Icons.Default.Download,
                    contentDescription = "Unduh Lampiran",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}