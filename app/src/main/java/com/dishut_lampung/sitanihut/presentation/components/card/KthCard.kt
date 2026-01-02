package com.dishut_lampung.sitanihut.presentation.components.card

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.outlined.Diversity3
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.dishut_lampung.sitanihut.domain.model.Kth

@Composable
fun KthCard(
    modifier: Modifier = Modifier,
    item: Kth,
    isOnline: Boolean,
    onClick: () -> Unit,
    onActionClick: () -> Unit
) {
    GenericDataCard(
        modifier = modifier,
        title = item.name,
        icon = Icons.Outlined.Diversity3,
        onCardClick = onClick,
        actionIcon = Icons.Default.MoreVert,
        onActionClick = onActionClick,
        isActionEnabled = isOnline,
        content = {
            Text(
                text = "Desa: ${item.desa}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "Kabupaten: ${item.kabupaten}",
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