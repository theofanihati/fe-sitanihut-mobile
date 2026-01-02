package com.dishut_lampung.sitanihut.presentation.components.card

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Spa // Icon Daun
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.dishut_lampung.sitanihut.domain.model.Commodity

@Composable
fun CommodityCard(
    modifier: Modifier = Modifier,
    item: Commodity,
    onClick: (String) -> Unit
) {
    GenericDataCard(
        modifier = modifier,
        title = item.name,
        icon = Icons.Outlined.Spa,
        onCardClick = { onClick(item.id) },
        content = {
            Text(
                text = "Kode: ${item.code}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.primary
            )

            Text(
                text = "Jenis: ${item.category}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    )
}