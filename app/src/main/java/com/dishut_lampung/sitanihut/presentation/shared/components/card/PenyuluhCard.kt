package com.dishut_lampung.sitanihut.presentation.shared.components.card

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.outlined.Forest
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import com.dishut_lampung.sitanihut.domain.model.Penyuluh

@Composable
fun PenyuluhCard(
    modifier: Modifier = Modifier,
    item: Penyuluh,
    onClick: (String) -> Unit
) {
    GenericDataCard(
        modifier = modifier,
        title = item.name,
        icon = Icons.Outlined.Forest,
        onCardClick = { onClick(item.id) },
        actionIcon = Icons.Default.ChevronRight,
        onActionClick = { onClick(item.id) },
        isActionEnabled = true,
        content = {
            Text(
                text = "NIP: ${item.identityNumber}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.primary
            )

            Text(
                text = item.position,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Text(
                text = item.kphName,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.outline
            )
        }
    )
}