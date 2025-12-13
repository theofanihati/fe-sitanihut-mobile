package com.dishut_lampung.sitanihut.presentation.home_page.petani

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.Eco
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.dishut_lampung.sitanihut.R
import com.dishut_lampung.sitanihut.domain.model.ReportStatus
import com.dishut_lampung.sitanihut.domain.model.ReportUiModel
import com.dishut_lampung.sitanihut.presentation.components.card.ReportSummaryCard
import com.dishut_lampung.sitanihut.presentation.components.CustomCircularProgressIndicator
import com.dishut_lampung.sitanihut.presentation.components.card.HomeMenuGrid
import com.dishut_lampung.sitanihut.presentation.components.card.HomeMenuItem
import com.dishut_lampung.sitanihut.presentation.components.card.ReportCard
import com.dishut_lampung.sitanihut.presentation.components.bottomsheet.ReportOptionBottomSheet
import com.dishut_lampung.sitanihut.presentation.components.card.SummaryRole
import com.dishut_lampung.sitanihut.presentation.components.dialog.CustomConfirmationDialog
import com.dishut_lampung.sitanihut.presentation.home_page.HomeEvent
import com.dishut_lampung.sitanihut.presentation.home_page.HomeUiEvent
import com.dishut_lampung.sitanihut.presentation.home_page.HomeUiState
import com.dishut_lampung.sitanihut.presentation.report_submission.list.ReportListEvent
import com.dishut_lampung.sitanihut.presentation.ui.theme.Dimens
import com.dishut_lampung.sitanihut.presentation.ui.theme.SitanihutTheme
import com.dishut_lampung.sitanihut.presentation.ui.theme.lightGreen
import com.dishut_lampung.sitanihut.presentation.ui.theme.lightOrange
import com.dishut_lampung.sitanihut.presentation.ui.theme.lightPink

@Preview(showBackground = true)
@Composable
fun HomePagePetaniScreenPreview() {
    SitanihutTheme(dynamicColor = false) {
        HomePagePetaniScreen(
            onReportClick = { },
            onActionClick = { },
            onNavigateToCommodity = { },
            onNavigateToReportSubmission = { },
            onNavigateToInfo = { },
            onOptionDismiss = {},
            onDeleteReportClick = {},
            onEditReportClick = {},
            onSubmitReportClick = {},
            onRefresh = {},
            modifier = Modifier.fillMaxSize(),
            state = HomeUiState(
                isLoading = false,
                latestReports = dummyReports
            ),
            onEvent = {},
        )
    }
}

@Composable
fun HomePagePetaniRoute(
    modifier: Modifier = Modifier,
    onNavigateToDetail: (String) -> Unit,
    onNavigateToReportSubmission: () -> Unit,
    onNavigateToCommodity: () -> Unit,
    onNavigateToInfo: () -> Unit,
    onNavigateToEdit: (String) -> Unit,
    viewModel: HomePagePetaniViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()

    LaunchedEffect(key1 = true) {
        viewModel.eventFlow.collect { event ->
            when(event) {
                is HomeUiEvent.NavigateToReportDetail -> {
                    onNavigateToDetail(event.reportId)
                }
                is HomeUiEvent.NavigateToEditReport -> {
                    onNavigateToEdit(event.reportId)
                }
                else -> {}
            }
        }
    }

    HomePagePetaniScreen(
        state = state,
        onEvent = viewModel::onEvent,
        modifier = modifier,
        onReportClick = { reportId -> onNavigateToDetail(reportId) },
        onActionClick = { reportId ->
            viewModel.onEvent(HomeEvent.OnReportMoreOptionClick(reportId))
        },
        onNavigateToCommodity = onNavigateToCommodity,
        onNavigateToReportSubmission = onNavigateToReportSubmission,
        onNavigateToInfo = onNavigateToInfo,
        onOptionDismiss = {
            viewModel.onEvent(HomeEvent.OnReportOptionSheetDismiss)
        },
        onDeleteReportClick = { reportId ->
            viewModel.onEvent(HomeEvent.OnDeleteClick(reportId))
        },
        onEditReportClick = { reportId ->
            viewModel.onEvent(HomeEvent.OnEditClick(reportId))
        },
        onSubmitReportClick = { reportId ->
            viewModel.onEvent(HomeEvent.OnSubmitClick(reportId))
        },
        onRefresh = {
            viewModel.onEvent(HomeEvent.OnRefreshData)
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomePagePetaniScreen(
    modifier: Modifier = Modifier,
    state: HomeUiState,
    onEvent: (HomeEvent) -> Unit,
    onReportClick: (String) -> Unit,
    onActionClick: (String) -> Unit,
    onNavigateToCommodity: () -> Unit = {},
    onNavigateToReportSubmission: () -> Unit,
    onNavigateToInfo: () -> Unit,
    onOptionDismiss: () -> Unit,
    onDeleteReportClick: (String) -> Unit,
    onEditReportClick: (String) -> Unit,
    onSubmitReportClick: (String) -> Unit,
    onRefresh: () -> Unit
) {
    val isRefreshing = state.isRefreshing
    val petaniMenus = remember {
        listOf(
            HomeMenuItem(
                labelTop = "Data",
                labelBottom = "Komoditas",
                icon = Icons.Outlined.Eco,
                color = lightOrange,
                onClick = onNavigateToCommodity
            ),
            HomeMenuItem(
                labelTop = "Pengajuan",
                labelBottom = "Laporan",
                icon = Icons.Outlined.Description,
                color = lightGreen,
                onClick = onNavigateToReportSubmission
            ),
            HomeMenuItem(
                labelTop = "Informasi",
                icon = Icons.Outlined.Info,
                color = lightPink,
                onClick = onNavigateToInfo
            )
        )
    }

    Box(modifier = modifier.fillMaxSize()) {
        val isPreview = LocalInspectionMode.current
        if (isPreview) {
            Image(
                painter = painterResource(id = R.drawable.homepage_background),
                contentDescription = "Background",
                contentScale = ContentScale.FillWidth,
                modifier = Modifier.matchParentSize(),
                alignment = Alignment.TopStart
            )
        } else {
            AsyncImage(
                model = R.drawable.homepage_background,
                alignment = Alignment.TopStart,
                contentDescription = "Background",
                contentScale = ContentScale.FillWidth,
                modifier = Modifier.matchParentSize()
            )
        }
        PullToRefreshBox(
            isRefreshing = isRefreshing,
            onRefresh = onRefresh,
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
            ) {
                Spacer(Modifier.height(80.dp))

                ReportSummaryCard(
                    modifier = Modifier.padding(horizontal = Dimens.ScreenPadding),
                    summary = state.reportSummary,
                    role = SummaryRole.PETANI
                )

                Spacer(Modifier.height(16.dp))

                HomeMenuGrid(
                    modifier = Modifier.padding(horizontal = Dimens.ScreenPadding),
                    menuItems = petaniMenus
                )

                Spacer(Modifier.height(16.dp))

                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.surfaceContainerLowest,
                    shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
                    shadowElevation = 4.dp
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 16.dp, horizontal = Dimens.ScreenPadding)
                            .defaultMinSize(minHeight = 600.dp)
                    ) {
                        Text(
                            text = "Laporan Terbaru",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.padding(bottom = 4.dp, top = 8.dp)
                        )

                        when {
                            state.isLoading -> {
                                CustomCircularProgressIndicator()
                            }

                            state.latestReports.isEmpty() -> {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(200.dp),
                                ) {
                                    Spacer(modifier = Modifier.height(16.dp))

                                    Text(
                                        text = "Yahh, belum ada laporan",
                                        style = MaterialTheme.typography.bodyLarge,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )

                                    Spacer(modifier = Modifier.height(4.dp))

                                    Text(
                                        text = "Yuk, Ajukan Laporan!",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                    )
                                }
                            }

                            else -> {
                                Column(
                                    verticalArrangement = Arrangement.spacedBy(2.dp)
                                ) {
                                    state.latestReports.forEach { report ->
                                        ReportCard(
                                            item = report,
                                            isPetani = true,
                                            onCardClick = onReportClick,
                                            onActionClick = onActionClick
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(32.dp))
                                }
                            }
                        }
                    }
                }
            }
        }

        if (state.reportIdForOptionSheet != null) {
            val selectedReportId = state.reportIdForOptionSheet
            val selectedReport = state.latestReports.find { it.id == selectedReportId }
            val isEditable = selectedReport?.isEditable ?: false

            ReportOptionBottomSheet(
                onDismiss = onOptionDismiss,
                onDetailClick = {
                    onOptionDismiss()
                    onReportClick(selectedReportId)
                },
                onEditClick = {
                    onOptionDismiss()
                    onEditReportClick(selectedReportId)
                },
                onDeleteClick = {
                    onOptionDismiss()
                    onDeleteReportClick(selectedReportId)
                },
                onSubmitClick = {
                    onOptionDismiss()
                    onSubmitReportClick(selectedReportId)
                },
                isEditable = isEditable
            )
        }
        if (state.reportIdToDelete != null) {
            CustomConfirmationDialog(
                title = "Hapus laporan?",
                supportingText = "Data yang dihapus tidak dapat dikembalikan.",
                confirmButtonText = "Hapus",
                dismissButtonText = "Batal",
                confirmColor = MaterialTheme.colorScheme.error,
                onConfirm = {
                    onEvent(HomeEvent.OnDeleteConfirm)
                },
                onDismiss = {
                    onEvent(HomeEvent.OnDeleteCancel)
                }
            )
        }

        if (state.reportIdToSubmit != null) {
            CustomConfirmationDialog(
                title = "Mengajukan?",
                supportingText = "Periksa kembali data anda, pastikan sudah benar",
                confirmButtonText = "Ajukan",
                dismissButtonText = "Batal",
                confirmColor = MaterialTheme.colorScheme.tertiary,
                onConfirm = {
                    val idLaporan = state.reportIdToSubmit ?: ""
                    onEvent(HomeEvent.OnSubmitConfirm(idLaporan))
                },
                onDismiss = {
                    onEvent(HomeEvent.OnSubmitCancel)
                }
            )
        }
    }
}

val dummyReports = listOf(
    ReportUiModel(
        id = "1",
        periodTitle = "Laporan Periode Mei 2025",
        dateDisplay = "12 Mei 2025",
        nteDisplay = "Rp 1.500.000",
        statusDisplay = "Menunggu",
        domainStatus = ReportStatus.PENDING,
        isEditable = false,
        isDeletable = false
    ),
    ReportUiModel(
        id = "2",
        periodTitle = "Laporan Periode April 2025",
        dateDisplay = "10 April 2025",
        nteDisplay = "Rp 2.300.000",
        statusDisplay = "Disetujui",
        domainStatus = ReportStatus.APPROVED,
        isEditable = false,
        isDeletable = false
    ),
    ReportUiModel(
        id = "3",
        periodTitle = "Laporan Periode Maret 2025",
        dateDisplay = "5 Maret 2025",
        nteDisplay = "Rp 800.000",
        statusDisplay = "Ditolak",
        domainStatus = ReportStatus.REJECTED,
        isEditable = true,
        isDeletable = true
    ),
    ReportUiModel(
        id = "4",
        periodTitle = "Laporan Periode Februari 2025",
        dateDisplay = "2 Februari 2025",
        nteDisplay = "Rp 4.000.000",
        statusDisplay = "Pemeriksaan Penyuluh",
        domainStatus = ReportStatus.PENDING,
        isEditable = false,
        isDeletable = false
    )
)