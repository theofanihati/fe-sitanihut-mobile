package com.dishut_lampung.sitanihut.presentation.report.form

import com.dishut_lampung.sitanihut.domain.model.Commodity
import com.dishut_lampung.sitanihut.domain.model.ReportAttachment
import com.dishut_lampung.sitanihut.presentation.shared.components.animations.MessageType

data class ReportFormState(
    val isAjukan: Boolean= false,
    val isLoading: Boolean = false,
    val error: String? = null,
    val successMessage: String? = null,
    val isFormValid: Boolean = false,val
    showConfirmDialog: Boolean = false,
    val pendingActionIsAjukan: Boolean = false,

    val commodityList: List<Commodity> = emptyList(),
    val periodList: List<String> = emptyList(),
    val monthList: List<String> = listOf(
        "Januari", "Februari", "Maret", "April", "Mei", "Juni",
        "Juli", "Agustus", "September", "Oktober", "November", "Desember"
    ),
    val plantTypes: List<String> = listOf("Semusim", "Tahunan"),
    val month: String = "",
    val monthError: String? = null,
    val period: String = "",
    val periodError: String? = null,
    val modal: String = "",
    val modalError: String? = null,
    val farmerNotes: String = "",
    val nte: Double = 0.0,
    val nteError: String? = null,

    val plantingDetails: List<PlantingDetailUiState> = emptyList(),
    val harvestDetails: List<HarvestDetailUiState> = emptyList(),
    val attachments: List<ReportAttachment> = emptyList()
)

data class PlantingDetailUiState(
    val id: Long = System.currentTimeMillis(),
    val commodityId: String = "",
    val commodityName: String = "",
    val commodityError: String? = null,
    val plantType: String = "",
    val plantTypeError: String? = null,
    val plantDate: String = "",
    val plantDateError: String? = null,
    val plantAge: String = "",
    val plantAgeError: String? = null,
    val amount: String = "",
    val amountError: String? = null,
    val unit: String = ""      // kg / batang, kan auto beb
)

data class HarvestDetailUiState(
    val id: Long = System.currentTimeMillis(),
    val commodityId: String = "",
    val commodityName: String = "",
    val commodityError: String? = null,
    val harvestDate: String = "",
    val harvestDateError: String? = null,
    val unitPrice: String = "",
    val unitPriceError: String? = null,
    val amount: String = "",
    val amountError: String? = null,
    val totalPrice: Double = 0.0,
    val totalPriceError: String? = null,
)

sealed class ReportFormEvent {
    data class OnMonthChange(val month: String) : ReportFormEvent()
    data class OnPeriodChange(val period: String) : ReportFormEvent()
    data class OnModalChange(val value: String) : ReportFormEvent()
    data class OnFarmerNotesChange(val value: String) : ReportFormEvent()

    object OnAddPlantingDetail : ReportFormEvent()
    data class OnRemovePlantingDetail(val index: Int) : ReportFormEvent()
    data class OnPlantingItemChange(val index: Int, val item: PlantingDetailUiState) : ReportFormEvent()

    object OnAddHarvestDetail : ReportFormEvent()
    data class OnRemoveHarvestDetail(val index: Int) : ReportFormEvent()
    data class OnHarvestItemChange(val index: Int, val item: HarvestDetailUiState) : ReportFormEvent()

    data class OnAddAttachment(val filePath: String) : ReportFormEvent()
    data class OnRemoveAttachment(val index: Int) : ReportFormEvent()

    data class OnSubmit(val isAjukan: Boolean) : ReportFormEvent() // True=Ajukan, False=Simpan
    object OnDismissMessage : ReportFormEvent()
    data class OnShowConfirmDialog(val isAjukan: Boolean) : ReportFormEvent()
    object OnDismissConfirmDialog : ReportFormEvent()
    data class OnShowUserMessage(val message: String, val type: MessageType) : ReportFormEvent()
}