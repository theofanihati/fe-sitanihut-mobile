package com.dishut_lampung.sitanihut.presentation.pengajuan_laporan.create

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.dishut_lampung.sitanihut.domain.model.Commodity
import com.dishut_lampung.sitanihut.presentation.commodity.CommodityScreen
import com.dishut_lampung.sitanihut.presentation.commodity.CommodityUiState
import com.dishut_lampung.sitanihut.presentation.components.CustomCircularProgressIndicator
import com.dishut_lampung.sitanihut.presentation.components.animations.AnimatedMessage
import com.dishut_lampung.sitanihut.presentation.components.animations.MessageType
import com.dishut_lampung.sitanihut.presentation.components.date_picker.CustomDatePicker
import com.dishut_lampung.sitanihut.presentation.components.dropdown.CustomOutlinedDropdown
import com.dishut_lampung.sitanihut.presentation.components.dropdown.CustomSearchableOutlinedDropdown
import com.dishut_lampung.sitanihut.presentation.components.file_uploader.FileUploader
import com.dishut_lampung.sitanihut.presentation.components.textfield.CustomOutlinedTextArea
import com.dishut_lampung.sitanihut.presentation.components.textfield.CustomOutlinedTextField
import com.dishut_lampung.sitanihut.presentation.ui.theme.Dimens.ScreenPadding
import com.dishut_lampung.sitanihut.presentation.ui.theme.SitanihutTheme
import java.text.NumberFormat
import java.util.Locale


@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun AddReportScreenPreview() {
    SitanihutTheme {
        val state = AddReportState(
            monthList = listOf("Januari", "Februari", "Maret",
                "April", "Mei", "Juni", "Juli", "Agustus", "September",),

        )

        AddReportScreen(
            state = state,
            onEvent = {},
            onNavigateUp = {},
            onPickFile = {}
        )
    }
}
@Composable
fun AddReportRoute(
    onNavigateUp: () -> Unit,
    viewModel: AddReportViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    AddReportScreen(
        state = state,
        onEvent = viewModel::onEvent,
        onNavigateUp = onNavigateUp,
        // TODO: Handle File Picker Launch here (Activity Result Launcher)
        onPickFile = { /* Launch File Picker */ }
    )
}

@Composable
fun AddReportScreen(
    state: AddReportState,
    onEvent: (AddReportEvent) -> Unit,
    onNavigateUp: () -> Unit,
    onPickFile: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = ScreenPadding)
        ) {
            Spacer(modifier = Modifier.height(100.dp))

            Text(
                text = "Pengajuan Laporan",
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
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    CustomOutlinedDropdown(
                        modifier = Modifier.weight(1f),
                        value = state.month,
                        onValueChange = { onEvent(AddReportEvent.OnMonthChange(it)) },
                        label = "Bulan",
                        options = state.monthList,
                        asteriskAtEnd = true,
                        rounded = 40,
                    )

                    CustomOutlinedDropdown(
                        modifier = Modifier.weight(1f),
                        value = state.period,
                        onValueChange = { onEvent(AddReportEvent.OnPeriodChange(it)) },
                        label = "Tahun",
                        options = state.periodList,
                        asteriskAtEnd = true,
                        rounded = 40,
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                CustomOutlinedTextField(
                    value = state.modal,
                    onValueChange = { onEvent(AddReportEvent.OnModalChange(it)) },
                    label = "Modal",
                    placeholder = "0",
                    keyboardType = KeyboardType.Number,
                    asteriskAtEnd = true,
                    modifier = Modifier.fillMaxWidth(),
                    rounded = 40,
                )

                Spacer(modifier = Modifier.height(16.dp))

                // --- SECTION MASA TANAM ---
                SectionHeader("Masa Tanam", onAddClick = { onEvent(AddReportEvent.OnAddPlantingDetail) })

                state.plantingDetails.forEachIndexed { index, item ->
                    PlantingItemCard(
                        index = index,
                        item = item,
                        commodityList = state.commodityList,
                        plantTypes = state.plantTypes,
                        onEvent = onEvent
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                }

                Spacer(modifier = Modifier.height(24.dp))

                // --- SECTION MASA PANEN ---
                SectionHeader("Masa Panen", onAddClick = { onEvent(AddReportEvent.OnAddHarvestDetail) })

                state.harvestDetails.forEachIndexed { index, item ->
                    HarvestItemCard(
                        index = index,
                        item = item,
                        commodityList = state.commodityList,
                        onEvent = onEvent
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Total NTE
                val nteFormatted = NumberFormat.getCurrencyInstance(Locale("id", "ID")).format(state.nte)
                CustomOutlinedTextField(
                    value = nteFormatted,
                    onValueChange = {},
                    label = "Total Nilai Transaksi Ekonomi (NTE)",
                    readOnly = true,
                    isEnabled = false,
                    modifier = Modifier.fillMaxWidth(),
                    rounded = 40,
                )

                CustomOutlinedTextArea(
                    value = state.farmerNotes,
                    onValueChange = { onEvent(AddReportEvent.OnFarmerNotesChange(it)) },
                    label = "Catatan Penyuluh",
                    modifier = Modifier.fillMaxWidth(),
                )

                Spacer(modifier = Modifier.height(16.dp))

                // --- LAMPIRAN ---
                FileUploader(
                    files = state.attachments,
                    onAddFileClick = onPickFile,
                    onRemoveFileClick = { index -> onEvent(AddReportEvent.OnRemoveAttachment(index)) },
                    label = "Unggah lampiran (jika ada)"
                )

                Spacer(modifier = Modifier.height(32.dp))

                // --- BUTTONS ---
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = { onEvent(AddReportEvent.OnSubmit(isAjukan = false)) },
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp),
                        shape = MaterialTheme.shapes.large
                    ) {
                        Text("Simpan")
                    }

                    Button(
                        onClick = { onEvent(AddReportEvent.OnSubmit(isAjukan = true)) },
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp),
                        shape = MaterialTheme.shapes.large,
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiary)
                    ) {
                        Text("Ajukan")
                    }
                }

                Spacer(modifier = Modifier.height(48.dp))
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
                onDismiss = {
                    onEvent(AddReportEvent.OnDismissMessage)
                    if (state.successMessage != null) onNavigateUp()
                },
                modifier = Modifier.align(Alignment.TopCenter).padding(top = 16.dp)
            )

            AnimatedMessage(
                isVisible = state.error != null,
                message = state.error ?: "",
                messageType = MessageType.Error,
                onDismiss = { onEvent(AddReportEvent.OnDismissMessage) },
                modifier = Modifier.align(Alignment.TopCenter).padding(top = 16.dp)
            )
        }

}

@Composable
fun PlantingItemCard(
    index: Int,
    item: PlantingDetailUiState,
    commodityList: List<Commodity>,
    plantTypes: List<String>,
    onEvent: (AddReportEvent) -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLowest),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(vertical = 16.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                CustomSearchableOutlinedDropdown(
                    modifier = Modifier.weight(1f),
                    value = item.commodityName,
                    onValueChange = { name ->
                        onEvent(AddReportEvent.OnPlantingItemChange(index, item.copy(commodityName = name)))
                    },
                    onOptionSelected = { commodity ->
                        onEvent(AddReportEvent.OnPlantingItemChange(index, item.copy(commodityId = commodity.id, commodityName = commodity.name)))
                    },
                    options = commodityList,
                    label = "Komoditas",
                )
                CustomOutlinedDropdown(
                    value = item.plantType,
                    onValueChange = { type ->
                        onEvent(AddReportEvent.OnPlantingItemChange(index, item.copy(plantType = type)))
                    },
                    asteriskAtEnd = true,
                    label = "Jenis Tanaman",
                    options = plantTypes,
                    modifier = Modifier.weight(1f),
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (item.plantType.equals("Semusim", ignoreCase = true)) {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    CustomDatePicker(
                        value = item.plantDate,
                        onValueChange = { date ->
                            // TODO: Hitung plantAge otomatis di ViewModel saat date berubah
                            onEvent(AddReportEvent.OnPlantingItemChange(index, item.copy(plantDate = date)))
                        },
                        asteriskAtEnd = true,
                        label = "Tanggal Tanam",
                        modifier = Modifier.weight(1f)
                    )

                    CustomOutlinedTextField(
                        value = item.plantAge,
                        onValueChange = {},
                        label = "Usia (Tahun)",
                        readOnly = true,
                        isEnabled = false,
                        modifier = Modifier.weight(1f),
                    )
                }
                CustomOutlinedTextField(
                    value = item.amount,
                    onValueChange = { amount ->
                        onEvent(AddReportEvent.OnPlantingItemChange(index, item.copy(amount = amount)))
                    },
                    label = "Jumlah (${item.unit.ifEmpty { "-" }})",
                    keyboardType = KeyboardType.Number,
                    modifier = Modifier.fillMaxWidth(),
                )
            } else if (item.plantType.equals("Tahunan", ignoreCase = true)) {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    CustomOutlinedTextField(
                        value = item.plantAge,
                        onValueChange = { age ->
                            onEvent(AddReportEvent.OnPlantingItemChange(index, item.copy(plantAge = age)))
                        },
                        label = "Usia Tanaman (Tahun)",
                        keyboardType = KeyboardType.Number,
                        modifier = Modifier.weight(1.5f),
                    )
                    CustomOutlinedTextField(
                        value = item.amount,
                        onValueChange = { amount ->
                            onEvent(AddReportEvent.OnPlantingItemChange(index, item.copy(amount = amount)))
                        },
                        label = "Jumlah (${item.unit.ifEmpty { "-" }})",
                        keyboardType = KeyboardType.Number,
                        modifier = Modifier.weight(1f),
                    )
                }
            }
            Row(horizontalArrangement = Arrangement.End) {
                Box(
                    modifier = Modifier
                        .weight(0.5f)
                ){
                }
                OutlinedButton(
                    onClick = { onEvent(AddReportEvent.OnRemovePlantingDetail(index)) },
                    modifier = Modifier
                        .weight(0.5f)
                        .height(40.dp),
                    shape = MaterialTheme.shapes.extraLarge,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.error),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("Hapus")
                }
            }
        }
    }
}

@Composable
fun HarvestItemCard(
    index: Int,
    item: HarvestDetailUiState,
    commodityList: List<Commodity>,
    onEvent: (AddReportEvent) -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "Panen #${index + 1}",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.weight(1f)
                )
                IconButton(onClick = { onEvent(AddReportEvent.OnRemoveHarvestDetail(index)) }) {
                    Icon(Icons.Default.Delete, contentDescription = "Hapus", tint = MaterialTheme.colorScheme.error)
                }
            }

            CustomDatePicker(
                value = item.harvestDate,
                onValueChange = { date ->
                    onEvent(AddReportEvent.OnHarvestItemChange(index, item.copy(harvestDate = date)))
                },
                label = "Tanggal Panen",
                modifier = Modifier.fillMaxWidth(),
                rounded = 40,
            )

            Spacer(modifier = Modifier.height(12.dp))

            CustomSearchableOutlinedDropdown(
                value = item.commodityName,
                onValueChange = { name ->
                    onEvent(AddReportEvent.OnHarvestItemChange(index, item.copy(commodityName = name)))
                },
                onOptionSelected = { commodity ->
                    onEvent(AddReportEvent.OnHarvestItemChange(index, item.copy(commodityId = commodity.id, commodityName = commodity.name)))
                },
                options = commodityList,
                label = "Komoditas",
                modifier = Modifier.fillMaxWidth(),
                rounded = 40,
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                CustomOutlinedTextField(
                    value = item.amount,
                    onValueChange = {
                        onEvent(AddReportEvent.OnHarvestItemChange(index, item.copy(amount = it)))
                    },
                    label = "Jumlah",
                    keyboardType = KeyboardType.Number,
                    modifier = Modifier.weight(1f),
                    rounded = 40,
                )

                CustomOutlinedTextField(
                    value = item.unitPrice,
                    onValueChange = {
                        onEvent(AddReportEvent.OnHarvestItemChange(index, item.copy(unitPrice = it)))
                    },
                    label = "Harga Satuan",
                    keyboardType = KeyboardType.Number,
                    modifier = Modifier.weight(1f),
                    rounded = 40,
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            val totalFormatted = NumberFormat.getCurrencyInstance(Locale("id", "ID")).format(item.totalPrice)
//            Text(
//                text = "Total: $totalFormatted",
//                style = MaterialTheme.typography.labelLarge,
//                color = MaterialTheme.colorScheme.tertiary,
//                fontWeight = FontWeight.Bold,
//                modifier = Modifier.align(Alignment.End)
//            )
            CustomOutlinedTextField(
                value = totalFormatted,
                onValueChange = {
                },
                readOnly = true,
                label = "Harga Total",
                modifier = Modifier.weight(1f),
                rounded = 40,
            )
        }
    }
}

@Composable
fun SectionHeader(title: String, onAddClick: (() -> Unit)? = null) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.headlineSmall.copy(
                fontWeight = FontWeight.Bold,
            ),
            color = MaterialTheme.colorScheme.tertiary,
        )
        if (onAddClick != null) {
            Text(
                text = "+ Tambah",
                color = MaterialTheme.colorScheme.primary,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.clickable { onAddClick() }
            )
        }
    }
    HorizontalDivider(
        color = MaterialTheme.colorScheme.primary.copy(alpha = 1f),
        modifier = Modifier
            .fillMaxWidth()
    )
}