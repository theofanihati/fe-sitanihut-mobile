package com.dishut_lampung.sitanihut.presentation.shared.components.card

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.outlined.Agriculture
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.dishut_lampung.sitanihut.domain.model.Petani

@Composable
fun PetaniCard(
    modifier: Modifier = Modifier,
    item: Petani,
    isOnline: Boolean,
    onClick: () -> Unit,
    onActionClick: () -> Unit
) {
    GenericDataCard(
        modifier = modifier,
        title = item.name,
        icon = Icons.Outlined.Agriculture,
        onCardClick = onClick,
        actionIcon = Icons.Default.MoreVert,
        onActionClick = onActionClick,
        isActionEnabled = isOnline,
        content = {
            Text(
                text = "NIK: ${item.identityNumber}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "Asal KTH: ${item.kthName}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "Asal KPH: ${item.kphName}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    )
}