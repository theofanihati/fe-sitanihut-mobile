package com.dishut_lampung.sitanihut.presentation.report.list

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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.outlined.FilterList
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.paging.LoadState
import androidx.paging.PagingData
import androidx.paging.compose.collectAsLazyPagingItems
import com.dishut_lampung.sitanihut.R
import com.dishut_lampung.sitanihut.domain.model.Report
import com.dishut_lampung.sitanihut.domain.model.ReportStatus
import com.dishut_lampung.sitanihut.domain.model.ReportUiModel
import com.dishut_lampung.sitanihut.presentation.components.CustomCircularProgressIndicator
import com.dishut_lampung.sitanihut.presentation.components.animations.AnimatedMessage
import com.dishut_lampung.sitanihut.presentation.components.animations.MessageType
import com.dishut_lampung.sitanihut.presentation.components.bottomsheet.ReportFilterPengajuanBottomSheet
import com.dishut_lampung.sitanihut.presentation.components.bottomsheet.ReportOptionBottomSheet
import com.dishut_lampung.sitanihut.presentation.components.card.ReportCard
import com.dishut_lampung.sitanihut.presentation.components.dialog.CustomConfirmationDialog
import com.dishut_lampung.sitanihut.presentation.components.message.ErrorMessage
import com.dishut_lampung.sitanihut.presentation.components.textfield.CustomSearchTextField
import com.dishut_lampung.sitanihut.presentation.home_page.petani.toUiModel
import com.dishut_lampung.sitanihut.presentation.ui.theme.Dimens.ScreenPadding
import com.dishut_lampung.sitanihut.presentation.ui.theme.SitanihutTheme
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun ReportListScreenPreview() {
    SitanihutTheme {
        val dummyReports = listOf(
            Report(
                id = "1",
                period = 2025,
                monthPeriod = "Januari",
                submissionDate = "2025-01-20",
                totalTransaction = 500000.0,
                status = ReportStatus.VERIFIED
            ).toUiModel(),
            Report(
                id = "2",
                period = 2025,
                monthPeriod = "Februari",
                submissionDate = "2025-02-15",
                totalTransaction = 1250000.0,
                status = ReportStatus.PENDING
            ).toUiModel(),
            Report(
                id = "3",
                period = 2025,
                monthPeriod = "Maret",
                submissionDate = "2025-03-10",
                totalTransaction = 0.0,
                status = ReportStatus.DRAFT
            ).toUiModel(),
            Report(
                id = "4",
                period = 2025,
                monthPeriod = "April",
                submissionDate = "2025-04-05",
                totalTransaction = 750000.0,
                status = ReportStatus.REJECTED
            ).toUiModel()
        )
        val pagingFlow = flowOf(PagingData.from(dummyReports))
        val pagingItems = pagingFlow.collectAsLazyPagingItems()

        val state = ReportListUiState(
            searchQuery = "",
            isFilterSheetVisible = false,
            isLoading = false
        )

        ReportListScreen(
            state = state,
            pagingItems = pagingItems,
            onEvent = {},
            onNavigateToAddReport = {},
            onNavigateToDetail = {},
            onNavigateToEdit = {}
        )
    }
}

//@Preview(showBackground = true)
//@Composable
//private fun ReportListEmptyStatePreview() {
//    SitanihutTheme {
//        val pagingFlow = flowOf(PagingData.from(emptyList<Report>()))
//        val pagingItems = pagingFlow.collectAsLazyPagingItems()
//
//        ReportListScreen(
//            state = ReportListUiState(),
//            pagingItems = pagingItems,
//            onEvent = {},
//            onNavigateToAddReport = {},
//            onNavigateToDetail = {},
//            onNavigateToEdit = {}
//        )
//    }
//}

@Composable
fun ReportListRoute(
    modifier: Modifier = Modifier,
    onNavigateToAddReport: () -> Unit,
    onNavigateToDetail: (String) -> Unit,
    onNavigateToEdit: (String) -> Unit,
    viewModel: ReportListViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val pagingItems = viewModel.reportPagingFlow.collectAsLazyPagingItems()

    ReportListScreen(
        state = state,
        pagingItems = pagingItems,
        modifier = modifier,
        onEvent = viewModel::onEvent,
        onNavigateToAddReport = onNavigateToAddReport,
        onNavigateToDetail = onNavigateToDetail,
        onNavigateToEdit = onNavigateToEdit
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportListScreen(
    viewModel: ReportListViewModel = hiltViewModel(),
    state: ReportListUiState,
    pagingItems: androidx.paging.compose.LazyPagingItems<ReportUiModel>,
    modifier: Modifier = Modifier,
    onEvent: (ReportListEvent) -> Unit,
    onNavigateToAddReport: () -> Unit,
    onNavigateToDetail: (String) -> Unit,
    onNavigateToEdit: (String) -> Unit
) {
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()

    val showScrollToTop by remember {
        derivedStateOf {
            listState.firstVisibleItemIndex > 0
        }
    }

    val isRefreshing = pagingItems.loadState.refresh is LoadState.Loading
    val onRefresh: () -> Unit = {
        pagingItems.refresh()
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        PullToRefreshBox(
            isRefreshing = isRefreshing,
            onRefresh = onRefresh,
            modifier = Modifier
                .fillMaxSize()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = ScreenPadding)
            ) {

                Spacer(modifier = Modifier.height(100.dp))

                Text(
                    text = if (state.isPetani) {
                        "Pengajuan Laporan"
                    } else {
                        "Periksa Laporan"
                    },
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontWeight = FontWeight.Bold,
                    ),
                    color = MaterialTheme.colorScheme.tertiary,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    CustomSearchTextField(
                        modifier = Modifier.weight(1f),
                        query = state.searchQuery,
                        onQueryChange = { onEvent(ReportListEvent.OnSearchQueryChange(it)) },
                        placeholder = "Cari laporan..."
                    )

                    FilterButton(
                        onClick = { onEvent(ReportListEvent.OnFilterClick) },
                        isActive = state.selectedStatus != null
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                LazyColumn(
                    state = listState,
                    contentPadding = PaddingValues(bottom = 80.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    if (pagingItems.loadState.refresh is LoadState.Loading && pagingItems.itemCount == 0) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillParentMaxSize()
                                    .height(200.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                CustomCircularProgressIndicator()
                            }
                        }
                    }

                    if (pagingItems.loadState.refresh is LoadState.Error) {
                        item {
                            ErrorMessage(
                                message = "Gagal memuat data, menampilkan data offline",
                            )
                        }
                    }

                    if (pagingItems.loadState.refresh is LoadState.NotLoading && pagingItems.itemCount == 0) {
                        item {
                            EmptyStateView()
                        }
                    }

                    items(
                        count = pagingItems.itemCount,
                        key = { index ->
                            pagingItems[index]?.id ?: index
                        }
                    ) { index ->
                        val report = pagingItems[index]
                        if (report != null) {
                            ReportCard(
                                item = report,
                                isPetani = state.isPetani,
                                onCardClick = { onNavigateToDetail(report.id) },
                                onActionClick = {reportId ->
                                    if (state.isPetani) {
                                        viewModel.onEvent(ReportListEvent.OnReportMoreOptionClick(reportId))
                                    } else {
                                        onNavigateToDetail(reportId)
                                    }
                                }
                            )
                        }
                    }

                    if (pagingItems.loadState.append is LoadState.Loading) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                CustomCircularProgressIndicator()
                            }
                        }
                    }
                }
            }

            FloatingActionButton(
                onClick = onNavigateToAddReport,
                containerColor = MaterialTheme.colorScheme.secondary,
                contentColor = MaterialTheme.colorScheme.onSecondary,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(16.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Tambah Laporan")
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
                onDismiss = { onEvent(ReportListEvent.OnDismissSuccessMessage) },
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 80.dp)
            )

            AnimatedMessage(
                isVisible = state.errorMessage != null,
                message = state.errorMessage ?: "",
                messageType = MessageType.Error,
                onDismiss = { onEvent(ReportListEvent.OnDismissError) },
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 80.dp)
            )
        }
    }

    if (state.isFilterSheetVisible) {
        ReportFilterPengajuanBottomSheet(
            currentFilter = state.selectedStatus,
            onDismissRequest = { onEvent(ReportListEvent.OnReportOptionSheetDismiss) },
            onFilterSelected = { status ->
                onEvent(ReportListEvent.OnFilterChange(status))
            }
        )
    }

    if (state.isOptionSheetVisible && state.selectedReportId != null) {
        val selectedReportId = state.selectedReportId!!
        val report = pagingItems.itemSnapshotList.items.find { it.id == selectedReportId }
        val isEditable = report?.isEditable ?: false

        ReportOptionBottomSheet(
            onDismiss = { onEvent(ReportListEvent.OnReportOptionSheetDismiss) },
            onDetailClick = {
                onEvent(ReportListEvent.OnReportOptionSheetDismiss)
                onNavigateToDetail(selectedReportId)
            },
            onEditClick = {
                onEvent(ReportListEvent.OnReportOptionSheetDismiss)
                onNavigateToEdit(selectedReportId)
            },
            onDeleteClick = {
                onEvent(ReportListEvent.OnDeleteClick)
            },
            onSubmitClick = {
                onEvent(ReportListEvent.OnSubmitClick)
            },
            isEditable = isEditable
        )
    }

    if (state.isDeleteDialogVisible) {
        CustomConfirmationDialog(
            title = "Hapus laporan?",
            supportingText = "Data yang dihapus tidak dapat dikembalikan.",
            onConfirm = { onEvent(ReportListEvent.OnDeleteConfirm) },
            onDismiss = { onEvent(ReportListEvent.OnDismissDeleteDialog) },
            confirmButtonText = "Hapus",
            dismissButtonText = "Batal",
            confirmColor = MaterialTheme.colorScheme.error
        )
    }
    if (state.isSubmitDialogVisible) {
        CustomConfirmationDialog(
            title = "Mengajukan?",
            supportingText = "Periksa kembali data anda, pastikan sudah benar",
            confirmButtonText = "Ajukan",
            dismissButtonText = "Batal",
            confirmColor = MaterialTheme.colorScheme.tertiary,
            onConfirm = { onEvent(ReportListEvent.OnSubmitConfirm) },
            onDismiss = { onEvent(ReportListEvent.OnDismissSubmitDialog) }
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
            text = "Belum ada laporan",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = "Yuk, buat laporan pertamamu!",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun FilterButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isActive: Boolean = false
) {
    val containerColor = if (isActive) MaterialTheme.colorScheme.surfaceContainerLowest else MaterialTheme.colorScheme.surfaceContainerLowest
    val contentColor = if (isActive) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface

    Surface(
        modifier = modifier
            .size(56.dp)
            .clip(CircleShape)
            .clickable { onClick() },
        shape = CircleShape,
        color = containerColor,
        tonalElevation = 4.dp,
        shadowElevation = 2.dp
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                imageVector = Icons.Outlined.FilterList,
                contentDescription = "Filter",
                tint = contentColor
            )
        }
    }
}