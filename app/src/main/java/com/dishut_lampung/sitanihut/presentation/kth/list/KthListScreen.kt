package com.dishut_lampung.sitanihut.presentation.kth.list

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.dishut_lampung.sitanihut.R
import com.dishut_lampung.sitanihut.presentation.components.CustomCircularProgressIndicator
import com.dishut_lampung.sitanihut.presentation.components.animations.AnimatedMessage
import com.dishut_lampung.sitanihut.presentation.components.animations.MessageType
import com.dishut_lampung.sitanihut.presentation.components.textfield.CustomSearchTextField
import com.dishut_lampung.sitanihut.presentation.ui.theme.Dimens.ScreenPadding
import com.dishut_lampung.sitanihut.presentation.ui.theme.SitanihutTheme
import kotlinx.coroutines.launch
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import com.dishut_lampung.sitanihut.domain.model.Kth
import com.dishut_lampung.sitanihut.presentation.components.bottomsheet.GenericActionBottomSheet
import com.dishut_lampung.sitanihut.presentation.components.card.KthCard
import com.dishut_lampung.sitanihut.presentation.components.dialog.CustomConfirmationDialog

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun KthListScreenPreview() {
    SitanihutTheme {
        val dummyKth = listOf(
            Kth(
                "1", "KTH Mekar", "Desa ABC", "Kecamatan DEF","Kab XYZ", "Saya", "088829903982","KPH C"
            ),
            Kth(
                "2", "KTH Kuncup wkwkwk", "Desa ABC", "Kecamatan DEF","Kab XYZ", "Saya", "088829903982","KPH C"
            )
        )

        val state = KthUiState(
            query = "",
            isLoading = false,
            kthList = dummyKth
        )

        KthListScreen(
            state = state,
            onEvent = {},
            onRefresh = {},
            onNavigateToDetail = {},
            onNavigateToEdit = {},
            onNavigateToAddKth = {}
        )
    }
}

@Composable
fun KthListRoute(
    modifier: Modifier = Modifier,
    viewModel: KthListViewModel = hiltViewModel(),
    onNavigateToDetail: (String) -> Unit,
    onNavigateToEdit: (String) -> Unit,
    onNavigateToAddKth: () -> Unit,
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    KthListScreen(
        state = state,
        modifier = modifier,
        onEvent = viewModel::onEvent,
        onRefresh = { viewModel.onEvent(KthEvent.OnRefresh) },
        onNavigateToDetail = onNavigateToDetail,
        onNavigateToEdit = onNavigateToEdit,
        onNavigateToAddKth = onNavigateToAddKth,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KthListScreen(
    state: KthUiState,
    modifier: Modifier = Modifier,
    onEvent: (KthEvent) -> Unit,
    onRefresh: () -> Unit,
    onNavigateToDetail: (String) -> Unit,
    onNavigateToEdit: (String) -> Unit,
    onNavigateToAddKth: () -> Unit,
) {
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()

    val showScrollToTop by remember {
        derivedStateOf {
            listState.firstVisibleItemIndex > 0
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        PullToRefreshBox(
            isRefreshing = state.isRefreshing,
            onRefresh = onRefresh,
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 100.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = ScreenPadding)
            ) {
                Text(
                    text = "Data KTH",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.tertiary,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )

                AnimatedVisibility(visible = !state.isOnline) {
                    Text(
                        text = "Mode Offline (Data Lokal)",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.error,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    CustomSearchTextField(
                        modifier = Modifier.weight(1f),
                        query = state.query,
                        onQueryChange = { onEvent(KthEvent.OnSearchQueryChange(it)) },
                        placeholder = "Cari KTH..."
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                LazyColumn(
                    state = listState,
                    contentPadding = PaddingValues(bottom = 80.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    if (!state.isLoading && state.kthList.isEmpty()) {
                        item { EmptyStateView() }
                    }

                    items(
                        items = state.kthList,
                        key = { it.id }
                    ) { kth ->
                        KthCard(
                            item = kth,
                            isOnline = state.isOnline,
                            onClick = {onNavigateToDetail(kth.id)},
                            onActionClick = {
                                if (state.isOnline) {
                                    onEvent(KthEvent.OnMoreOptionClick(kth.id))
                                }
                            }
                        )
                    }
                }
            }
        }
        if (state.isBottomSheetVisible) {
            GenericActionBottomSheet(
                onDismiss = { onEvent(KthEvent.OnBottomSheetDismiss) },
                onDetailClick = {
                    onEvent(KthEvent.OnBottomSheetDismiss)
                    state.selectedKthId?.let { onNavigateToDetail(it) }
                },
                onEditClick = {
                    onEvent(KthEvent.OnBottomSheetDismiss)
                    state.selectedKthId?.let { onNavigateToEdit(it) }
                },
                onDeleteClick = {
                    onEvent(KthEvent.OnDeleteClick)
                },
                isEditable = state.isOnline
            )
        }

        if (state.isDeleteDialogVisible) {
            CustomConfirmationDialog(
                title = "Hapus KTH?",
                supportingText = "Data yang dihapus tidak dapat dikembalikan.",
                confirmButtonText = "Hapus",
                dismissButtonText = "Batal",
                onConfirm = { onEvent(KthEvent.OnDeleteConfirm) },
                onDismiss = { onEvent(KthEvent.OnDismissDeleteDialog) },
                confirmColor = MaterialTheme.colorScheme.error
            )
        }

        FloatingActionButton(
            onClick = onNavigateToAddKth,
            containerColor = MaterialTheme.colorScheme.secondary,
            contentColor = MaterialTheme.colorScheme.onSecondary,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
        ) {
            Icon(Icons.Default.Add, contentDescription = "Tambah KTH")
        }

        AnimatedVisibility(
            visible = showScrollToTop,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(bottom = 92.dp, end = 28.dp)
        ) {
            FloatingActionButton(
                onClick = {
                    coroutineScope.launch {
                        listState.animateScrollToItem(0)
                    }
                },
                containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                contentColor = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(40.dp)
            ) {
                Icon(Icons.Default.ArrowUpward, contentDescription = "Ke Atas")
            }
        }

        if (state.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.scrim.copy(alpha = 0.3f))
                    .clickable(enabled = false) {},
                contentAlignment = Alignment.Center
            ) {
                CustomCircularProgressIndicator()
            }
        }

        AnimatedMessage(
            isVisible = state.successMessage != null,
            message = state.successMessage ?: "",
            messageType = MessageType.Success,
            onDismiss = { onEvent(KthEvent.OnDismissSuccessMessage) },
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(top = 80.dp)
        )

        AnimatedMessage(
            isVisible = state.errorMessage != null,
            message = state.errorMessage ?: "",
            messageType = MessageType.Error,
            onDismiss = { onEvent(KthEvent.OnDismissError) },
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(top = 80.dp)
        )
    }
}

@Composable
fun EmptyStateView() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 100.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(
            painter = painterResource(id = R.drawable.no_data_image),
            contentDescription = "Kosong",
            modifier = Modifier.size(200.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Belum ada data KTH",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = "Tunggu ya, kalau sudah ada akan tampil disini!",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}