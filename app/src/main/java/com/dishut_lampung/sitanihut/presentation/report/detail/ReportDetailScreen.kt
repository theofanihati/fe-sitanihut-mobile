package com.dishut_lampung.sitanihut.presentation.report.detail

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults.outlinedButtonColors
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.dishut_lampung.sitanihut.domain.model.MasaPanen
import com.dishut_lampung.sitanihut.domain.model.MasaTanam
import com.dishut_lampung.sitanihut.domain.model.ReportAttachment
import com.dishut_lampung.sitanihut.domain.model.ReportDetail
import com.dishut_lampung.sitanihut.domain.model.ReportStatus
import com.dishut_lampung.sitanihut.presentation.commodity.CommodityEvent
import com.dishut_lampung.sitanihut.presentation.components.CustomCircularProgressIndicator
import com.dishut_lampung.sitanihut.presentation.components.animations.AnimatedMessage
import com.dishut_lampung.sitanihut.presentation.components.animations.MessageType
import com.dishut_lampung.sitanihut.presentation.components.dialog.CustomConfirmationDialog
import com.dishut_lampung.sitanihut.presentation.components.file_uploader.ReadOnlyFileAttachment
import com.dishut_lampung.sitanihut.presentation.components.message.ErrorMessage
import com.dishut_lampung.sitanihut.presentation.report.form.ReportFormEvent
import com.dishut_lampung.sitanihut.presentation.report.list.ReportListEvent
import com.dishut_lampung.sitanihut.presentation.ui.theme.Dimens.ScreenPadding
import com.dishut_lampung.sitanihut.presentation.ui.theme.SitanihutTheme
import com.dishut_lampung.sitanihut.util.formatRupiah
import com.dishut_lampung.sitanihut.util.openFileOrUrl
import kotlinx.coroutines.delay

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun ReportDetailScreenPreview() {
    SitanihutTheme {
        val dummyPlanting = listOf(
            MasaTanam(
                commodityId = "1",
                commodityName = "Durian Monthong",
                plantType = "Tahunan",
                plantDate = "20-01-2024",
                plantAge = 2.0,
                amount = "150"
            ),
            MasaTanam(
                commodityId = "2",
                commodityName = "Alpukat Siger",
                plantType = "Tahunan",
                plantDate = "15-02-2024",
                plantAge = 1.0,
                amount = "50"
            )
        )

        val dummyHarvest = listOf(
            MasaPanen(
                harvestDate = "10-08-2024",
                commodityId = "1",
                commodityName = "Durian Monthong",
                unitPrice = "45000",
                amount = "200",
            ),
            MasaPanen(
                harvestDate = "05-09-2024",
                commodityId = "3",
                commodityName = "Kopi Robusta",
                unitPrice = "28000",
                amount = "1000",
            )
        )

        val dummyAttachments = listOf(
            ReportAttachment(
                id = "1",
                filePath = "https://example.com/foto_kebun.jpg",
                isLocal = false
            ),
            ReportAttachment(
                id = "2",
                filePath = "/storage/emulated/0/DCIM/Camera/IMG_2024.jpg",
                isLocal = true
            )
        )

        val dummyDetail = ReportDetail(
            id = "REP-2024-001",
            userName = "Budi Santoso",
            userNik = "1871052005900001",
            userGender = "Pria",
            userAddress = "Jl. Wan Abdurrahman No. 10, Kemiling, Bandar Lampung",
            userKphName = "KPH Batutegi",
            userKthName = "KTH Makmur Jaya",
            month = "Agustus",
            period = 2024,
            modal = "5000000",
            farmerNotes = "Panen tahun ini cukup melimpah meskipun sempat kemarau.",
            penyuluhNotes = "Panen tahun ini cukup melimpah meskipun sempat kemarau.",
            nte = 37000000.0,
            status = ReportStatus.VERIFIED,
            attachments = dummyAttachments,
            plantingDetails = dummyPlanting,
            harvestDetails = dummyHarvest
        )

        val state = ReportDetailUiState.Success(data = dummyDetail)

        ReportDetailScreen(
            state = state,
            onEvent = {},
            onDownloadAttachment = {}
        )
    }
}

@Composable
fun ReportDetailRoute(
    navController: NavController,
    viewModel: ReportDetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val successMessage = (uiState as? ReportDetailUiState.Success)?.successMessage

    LaunchedEffect(successMessage) {
        if (successMessage != null) {
            delay(2000)
            viewModel.onEvent(ReportDetailEvent.OnDismissMessage)
            navController.popBackStack()
        }
    }

    ReportDetailScreen(
        state = uiState,
        onEvent = viewModel::onEvent,
        onDownloadAttachment = { attachment ->
            openFileOrUrl(
                context = context,
                attachment = attachment,
                onMessage = { _, _ -> /* Handle toast/message if needed */ }
            )
        }
    )
}

@Composable
fun ReportDetailScreen(
    state: ReportDetailUiState,
    onEvent: (ReportDetailEvent) -> Unit,
    onDownloadAttachment: (ReportAttachment) -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        when (state) {
            is ReportDetailUiState.Loading -> {
                CustomCircularProgressIndicator()
            }
            is ReportDetailUiState.Error -> {
                ErrorMessage(
                    message = state.message,
                    modifier = Modifier.align(Alignment.Center)
                )
            }
            is ReportDetailUiState.Success -> {
                ReportDetailContent(
                    detail = state.data,
                    canVerify = state.canVerify,
                    canApprove = state.canApprove,
                    canReject = state.canReject,
                    onEvent = onEvent,
                    onDownload = onDownloadAttachment
                )
                if (state.isActionLoading) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(androidx.compose.ui.graphics.Color.Black.copy(alpha = 0.3f))
                            .clickable(enabled = false) {},
                        contentAlignment = Alignment.Center
                    ) {
                        CustomCircularProgressIndicator()
                    }
                }
                if (state.successMessage != null) {
                    AnimatedMessage(
                        isVisible = state.successMessage != null,
                        message = state.successMessage ?: "",
                        messageType = MessageType.Success,
                        onDismiss = { onEvent(ReportDetailEvent.OnDismissMessage)},
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(bottom = 80.dp)
                    )
                }
                if (state.errorMessage != null) {
                    AnimatedMessage(
                        isVisible = state.errorMessage != null,
                        message = state.errorMessage ?: "",
                        messageType = MessageType.Error,
                        onDismiss = { onEvent(ReportDetailEvent.OnDismissMessage) },
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(bottom = 80.dp)
                    )
                }
                if (state.pendingAction != null) {
                    val (actionText, actionColor) = when(state.pendingAction) {
                        ReportAction.VERIFY -> "memverifikasi" to MaterialTheme.colorScheme.tertiary
                        ReportAction.APPROVE -> "menyetujui" to MaterialTheme.colorScheme.tertiary
                        ReportAction.REJECT -> "menolak" to MaterialTheme.colorScheme.error
                    }
                    val title = actionText.replaceFirstChar { it.titlecase() }

                    CustomConfirmationDialog(
                        title = "$title?",
                        supportingText = "Apakah Anda yakin ingin $actionText laporan ini?",
                        confirmButtonText = "Ya",
                        dismissButtonText = "Batal",
                        isLoading = false,
                        onDismiss = { onEvent(ReportDetailEvent.OnDismissDialog) },
                        onConfirm = { onEvent(ReportDetailEvent.OnConfirmDialog) },
                        confirmColor = actionColor
                    )
                }
            }
        }
    }
}

@Composable
fun ReportDetailContent(
    detail: ReportDetail,
    canVerify: Boolean,
    canApprove: Boolean,
    canReject: Boolean,
    onEvent: (ReportDetailEvent) -> Unit,
    onDownload: (ReportAttachment) -> Unit
) {
    Column(
        modifier = Modifier
            .padding(horizontal = ScreenPadding)
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Spacer(modifier = Modifier.height(100.dp))

        Text(
            text = "Detail Laporan",
            style = MaterialTheme.typography.headlineSmall.copy(
                fontWeight = FontWeight.Bold,
            ),
            color = MaterialTheme.colorScheme.tertiary,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(16.dp))

        InfoItem("Nama lengkap", detail.userName)
        InfoItem("NIK", detail.userNik)
        InfoItem("Jenis kelamin", detail.userGender)
        InfoItem("Alamat", detail.userAddress)
        InfoItem("Asal KPH", detail.userKphName)
        InfoItem("Asal KTH", detail.userKthName)

        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

        InfoItem("Periode", "${detail.month} ${detail.period}")
        InfoItem("Modal", formatRupiah(detail.modal.toDoubleOrNull() ?: 0.0))

        Spacer(modifier = Modifier.height(8.dp))

        // --- MASA TANAM ---
        SectionHeader("Masa Tanam")
        if (detail.plantingDetails.isEmpty()) {
            Text(
                text = "- Data tidak termuat -",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(0.dp)) {
                detail.plantingDetails.forEach { item ->
                    PlantingItemRow(item)
                    HorizontalDivider(thickness = 0.5.dp, color = MaterialTheme.colorScheme.outline)
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // --- MASA PANEN ---
        SectionHeader("Masa Panen")
        if (detail.harvestDetails.isEmpty()) {
            Text(
                text = "- Data tidak termuat -",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(0.dp)) {
                detail.harvestDetails.forEach { item ->
                    HarvestItemRow(item)
                    HorizontalDivider(thickness = 0.5.dp, color = MaterialTheme.colorScheme.outline)
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        InfoItem("Total NTE", formatRupiah(detail.nte))

        ReadOnlyFileAttachment(
            files = detail.attachments,
            onDownloadClick = onDownload
        )


        if (!detail.farmerNotes.isNullOrBlank() || !detail.penyuluhNotes.isNullOrBlank()) {
            if (!detail.farmerNotes.isNullOrBlank()) {
                NoteItem(label = "Catatan Petani", content = detail.farmerNotes)
            }

            if (!detail.penyuluhNotes.isNullOrBlank()) {
                if (!detail.farmerNotes.isNullOrBlank()) Spacer(modifier = Modifier.height(8.dp))
                NoteItem(label = "Catatan Penyuluh", content = detail.penyuluhNotes)
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        InfoItem("Tanggal diajukan", detail.createdAt ?: "-")

        if (!detail.verifiedAt.isNullOrBlank()) {
            InfoItem("Tanggal diperiksa penyuluh", detail.verifiedAt)
        }

        if (!detail.acceptedAt.isNullOrBlank()) {
            InfoItem("Tanggal disetujui", detail.acceptedAt)
        }

        Spacer(modifier = Modifier.height(24.dp))

        // BUTTON ROLE BASED
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (canReject) {
                Button(
                    onClick = { onEvent(ReportDetailEvent.OnRejectClick) },
                    colors = ButtonColors(
                        contentColor = MaterialTheme.colorScheme.surface,
                        containerColor = MaterialTheme.colorScheme.error,
                        disabledContainerColor = MaterialTheme.colorScheme.error.copy(alpha = 0.1f),
                        disabledContentColor = MaterialTheme.colorScheme.error
                    ),
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Tolak")
                }
            }
            if (canVerify) {
                Button(
                    onClick = { onEvent(ReportDetailEvent.OnVerifyClick) },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Verifikasi")
                }
            }

            if (canApprove) {
                Button(
                    onClick = { onEvent(ReportDetailEvent.OnApproveClick) },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Setujui")
                }
            }
        }

        Spacer(modifier = Modifier.height(40.dp))
    }
}

@Composable
fun PlantingItemRow(item: MasaTanam) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = item.commodityName,
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.bodyMedium
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = item.plantDate?: "-",
                style = MaterialTheme.typography.bodySmall,
            )
            Text(
                text = "${item.plantAge} tahun",
                style = MaterialTheme.typography.bodySmall
            )
            Text(
                text = "${item.amount}",
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

@Composable
fun HarvestItemRow(item: MasaPanen) {
    val amount = item.amount.toDoubleOrNull() ?: 0.0
    val price = item.unitPrice.toDoubleOrNull() ?: 0.0
    val totalPrice = amount * price

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = item.commodityName,
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                text = item.harvestDate,
                style = MaterialTheme.typography.bodySmall,
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = formatRupiah(item.unitPrice.toDoubleOrNull() ?: 0.0),
                style = MaterialTheme.typography.bodySmall,
            )

            Text(
                text = "${item.amount} kg",
                style = MaterialTheme.typography.bodySmall
            )

            Text(
                text = formatRupiah(totalPrice),
                fontWeight = FontWeight.SemiBold,
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

@Composable
private fun SectionHeader(title: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.tertiary,
        )
        HorizontalDivider(
            color = MaterialTheme.colorScheme.tertiary.copy(alpha = 1f),
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 8.dp)
        )
    }
}

@Composable
fun InfoItem(label: String, value: String?) {
    val valueString = value ?: "tidak disebutkan"
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.Top
    ) {
        Text(
            text = "${label}",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(0.4f)
        )
        Text(
            text = ": ${valueString}",
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.weight(0.6f),
            textAlign = TextAlign.Start
        )
    }
}

@Composable
fun NoteItem(label: String, content: String) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = content,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(top = 2.dp)
        )
    }
}