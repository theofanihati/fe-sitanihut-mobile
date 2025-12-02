package com.dishut_lampung.sitanihut.presentation.components.bottomsheet

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.dishut_lampung.sitanihut.domain.model.ReportStatus

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportFilterPengajuanBottomSheet(
    currentFilter: ReportStatus?,
    onDismissRequest: () -> Unit,
    onFilterSelected: (ReportStatus?) -> Unit
) {
    val sheetState = rememberModalBottomSheetState()
    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surfaceContainerLowest
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 32.dp)
        ) {
            Text(
                text = "Filter Status Laporan",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp)
            )

            HorizontalDivider(
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                modifier = Modifier.padding(horizontal = 20.dp)
            )

            FilterItem(
                text = "Tampilkan Semua",
                isSelected = currentFilter == null,
                onClick = { onFilterSelected(null) }
            )

            ReportStatus.values().forEach { status ->
                val label = when (status) {
                    ReportStatus.DRAFT -> "Belum Diajukan"
                    ReportStatus.PENDING -> "Menunggu"
                    ReportStatus.VERIFIED -> "Diverifikasi"
                    ReportStatus.APPROVED -> "Disetujui"
                    ReportStatus.REJECTED -> "Ditolak"
                }

                HorizontalDivider(
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                    modifier = Modifier.padding(horizontal = 20.dp)
                )

                FilterItem(
                    text = label,
                    isSelected = currentFilter == status,
                    onClick = { onFilterSelected(status) }
                )
            }
        }
    }
}

@Composable
private fun FilterItem(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    ListItem(
        headlineContent = { Text(text) },
        trailingContent = {
            RadioButton(
                selected = isSelected,
                onClick = null
            )
        },
        modifier = Modifier
            .clickable { onClick() }
            .padding(horizontal = 10.dp),
        colors = ListItemDefaults.colors(containerColor = Color.Transparent)
    )
}