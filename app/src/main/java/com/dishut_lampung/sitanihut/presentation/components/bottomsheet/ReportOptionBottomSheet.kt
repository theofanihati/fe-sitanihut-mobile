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
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportOptionBottomSheet(
    onDismiss: () -> Unit,
    onDetailClick: () -> Unit,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit,
    onSubmitClick: () -> Unit,
    isEditable: Boolean = true
) {
    val sheetState = rememberModalBottomSheetState()
    val disabledColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
    val outlineColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surfaceContainerLowest
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 32.dp)
        ) {
            ListItem(
                headlineContent = { Text("Lihat detail") },
                modifier = Modifier
                    .clickable { onDetailClick() }
                    .padding(start = 40.dp, end = 40.dp),
                colors = ListItemDefaults.colors(containerColor = Color.Transparent)
            )

            HorizontalDivider(
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 20.dp, end = 20.dp)

            )

            val editColor = if (isEditable) MaterialTheme.colorScheme.onSurface else disabledColor
            val deleteColor = if (isEditable) MaterialTheme.colorScheme.error else disabledColor
            val submitColor = if (isEditable) MaterialTheme.colorScheme.tertiary else disabledColor

            ListItem(
                headlineContent = { Text("Edit", color = editColor) },
                modifier = Modifier
                    .clickable(enabled = isEditable) { onEditClick() }
                        .padding(start = 40.dp, end = 40.dp),
                colors = ListItemDefaults.colors(containerColor = Color.Transparent)
            )

            HorizontalDivider(
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 20.dp, end = 20.dp)
            )

            ListItem(
                headlineContent = { Text("Hapus data", color = deleteColor) },
                modifier = Modifier
                    .clickable(enabled = isEditable) { onDeleteClick() }
                        .padding(start = 40.dp, end = 40.dp),
                colors = ListItemDefaults.colors(containerColor = Color.Transparent)
            )

            HorizontalDivider(
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 20.dp, end = 20.dp)
            )

            ListItem(
                headlineContent = { Text("Ajukan Laporan", color = submitColor) },
                modifier = Modifier
                    .clickable(enabled = isEditable) { onSubmitClick() }
                        .padding(start = 40.dp, end = 40.dp),
                colors = ListItemDefaults.colors(containerColor = Color.Transparent)
            )
        }

    }
}