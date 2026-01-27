package com.dishut_lampung.sitanihut.presentation.shared.components.card

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.outlined.Agriculture
import androidx.compose.material.icons.outlined.Group
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.dishut_lampung.sitanihut.domain.model.UserDetail

@Composable
fun UserCard(
    modifier: Modifier = Modifier,
    item: UserDetail,
    isOnline: Boolean,
    onClick: () -> Unit,
    onActionClick: () -> Unit
) {
    GenericDataCard(
        modifier = modifier,
        title = item.name,
        icon = Icons.Outlined.Group,
        onCardClick = onClick,
        actionIcon = Icons.Default.MoreVert,
        onActionClick = onActionClick,
        isActionEnabled = true,
        content = {
            Text(
                text = "Jenis kelamin: ${item.gender}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "Role: ${item.role}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    )
}