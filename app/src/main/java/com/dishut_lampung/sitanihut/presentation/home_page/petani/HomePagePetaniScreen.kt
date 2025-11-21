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
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Eco
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.Eco
import androidx.compose.material.icons.outlined.Info
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role.Companion.Image
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.dishut_lampung.sitanihut.R
import com.dishut_lampung.sitanihut.domain.model.ReportStatus
import com.dishut_lampung.sitanihut.domain.model.ReportUiModel
import com.dishut_lampung.sitanihut.presentation.ReportSummaryCard
import com.dishut_lampung.sitanihut.presentation.components.CustomCircularProgressIndicator
import com.dishut_lampung.sitanihut.presentation.components.HomeMenuGrid
import com.dishut_lampung.sitanihut.presentation.components.HomeMenuItem
import com.dishut_lampung.sitanihut.presentation.components.ReportCard
import com.dishut_lampung.sitanihut.presentation.home_page.HomeEvent
import com.dishut_lampung.sitanihut.presentation.home_page.HomeUiEvent
import com.dishut_lampung.sitanihut.presentation.home_page.HomeUiState
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
            state = HomeUiState(
                isLoading = false,
                latestReports = dummyReports
            )
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
    viewModel: HomePagePetaniViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()

    LaunchedEffect(key1 = true) {
        viewModel.eventFlow.collect { event ->
            when(event) {
                is HomeUiEvent.NavigateToReportDetail -> {
                    onNavigateToDetail(event.reportId)
                }
                else -> {}
            }
        }
    }

    HomePagePetaniScreen(
        state = state,
        modifier = modifier,
        onReportClick = { reportId -> onNavigateToDetail(reportId) },
        onActionClick = { reportId ->
            viewModel.onEvent(HomeEvent.OnReportMoreOptionClick(reportId))
        },
        onNavigateToCommodity = onNavigateToCommodity,
        onNavigateToReportSubmission = onNavigateToReportSubmission,
        onNavigateToInfo = onNavigateToInfo,
    )
}

@Composable
fun HomePagePetaniScreen(
    modifier: Modifier = Modifier,
    state: HomeUiState,
    onReportClick: (String) -> Unit,
    onActionClick: (String) -> Unit,
    onNavigateToCommodity: () -> Unit = {},
    onNavigateToReportSubmission: () -> Unit,
    onNavigateToInfo: () -> Unit,
) {
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

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .statusBarsPadding()
        ) {
        Spacer(Modifier.height(80.dp))

            ReportSummaryCard(
                modifier = Modifier.padding(horizontal = Dimens.ScreenPadding),
                summary = state.reportSummary,
            )

            Spacer(Modifier.height(16.dp))

            HomeMenuGrid(
                modifier = Modifier.padding(horizontal = Dimens.ScreenPadding),
                menuItems = petaniMenus
            )

            Spacer(Modifier.height(16.dp))

            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.surface,
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