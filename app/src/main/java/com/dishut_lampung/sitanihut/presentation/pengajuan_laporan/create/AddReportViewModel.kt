package com.dishut_lampung.sitanihut.presentation.pengajuan_laporan.create

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dishut_lampung.sitanihut.domain.model.CreateReportInput
import com.dishut_lampung.sitanihut.domain.model.MasaPanen
import com.dishut_lampung.sitanihut.domain.model.MasaTanam
import com.dishut_lampung.sitanihut.domain.model.ReportAttachment
import com.dishut_lampung.sitanihut.domain.usecase.commodity.GetCommoditiesUseCase // Reuse yg ada
import com.dishut_lampung.sitanihut.domain.usecase.report.CreateReportUseCase
import com.dishut_lampung.sitanihut.domain.usecase.report.GetReportDetailUseCase
import com.dishut_lampung.sitanihut.domain.usecase.report.UpdateReportUseCase
import com.dishut_lampung.sitanihut.domain.usecase.report.ValidateReportInputUseCase
import com.dishut_lampung.sitanihut.util.Resource
import com.dishut_lampung.sitanihut.util.changeDateFormat
import com.dishut_lampung.sitanihut.util.convertUiDateToApiDate
import com.dishut_lampung.sitanihut.util.formatApiToUiString
import com.dishut_lampung.sitanihut.util.parseIndonesianNumber
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class AddReportViewModel @Inject constructor(
    private val getCommoditiesUseCase: GetCommoditiesUseCase,
    private val createReportUseCase: CreateReportUseCase,
    private val validateReportInputUseCase: ValidateReportInputUseCase,
    private val getReportDetailUseCase: GetReportDetailUseCase,
    private val updateReportUseCase: UpdateReportUseCase,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {
    private var currentReportId: String? = null
    private val _uiState = MutableStateFlow(AddReportState())
    val uiState = _uiState.asStateFlow()

    init {
        loadCommodities()
        generatePeriodList()

        currentReportId = savedStateHandle.get<String>("reportId")
        if (currentReportId != null) {
            loadExistingReportData(currentReportId!!)
        } else {
            onEvent(AddReportEvent.OnAddPlantingDetail)
            onEvent(AddReportEvent.OnAddHarvestDetail)
        }
    }

    fun onEvent(event: AddReportEvent) {
        when (event) {
            is AddReportEvent.OnMonthChange -> _uiState.update { it.copy(month = event.month, monthError = null)}
            is AddReportEvent.OnPeriodChange -> _uiState.update { it.copy(period = event.period, periodError = null) }
            is AddReportEvent.OnModalChange -> {
                val errorMsg = validateNumberInput(event.value, "Modal")
                _uiState.update { it.copy(modal = event.value, modalError = errorMsg) }
            }
            is AddReportEvent.OnFarmerNotesChange -> _uiState.update { it.copy(farmerNotes = event.value) }

            // MASA TANAM
            AddReportEvent.OnAddPlantingDetail -> {
                val newList = _uiState.value.plantingDetails + PlantingDetailUiState()
                _uiState.update { it.copy(plantingDetails = newList) }
            }

            is AddReportEvent.OnRemovePlantingDetail -> {
                val newList = _uiState.value.plantingDetails.toMutableList().apply { removeAt(event.index) }
                _uiState.update { it.copy(plantingDetails = newList) }
            }

            is AddReportEvent.OnPlantingItemChange -> {
                val newList = _uiState.value.plantingDetails.toMutableList()
                var updatedItem = event.item

                if (updatedItem.plantType.equals("tahunan", ignoreCase = true)) {
                    updatedItem = updatedItem.copy(unit = "batang", plantDate = "")
                } else if (updatedItem.plantType.equals("semusim", ignoreCase = true)) {
                    updatedItem = updatedItem.copy(unit = "kg")
                }

                if (updatedItem.plantType.equals("semusim", ignoreCase = true) && updatedItem.plantDate.isNotEmpty()) {
                    updatedItem = updatedItem.copy(plantAge = calculatePlantAge(updatedItem.plantDate))
                }

                val amountError = validateIntegerInput(updatedItem.amount, "Jumlah")
                val ageError = if (updatedItem.plantType.equals("tahunan", true)) {
                    validateNumberInput(updatedItem.plantAge, "Usia")
                } else null
                val dateError = if (updatedItem.plantType.equals("semusim", true) && updatedItem.plantDate.isBlank()) {
                    "Wajib diisi"
                } else null

                updatedItem = updatedItem.copy(
                    amountError = amountError,
                    plantAgeError = ageError,
                    plantDateError = dateError
                )

                newList[event.index] = updatedItem
                _uiState.update { it.copy(plantingDetails = newList) }
            }

            // MASA PANEN
            AddReportEvent.OnAddHarvestDetail -> {
                val newList = _uiState.value.harvestDetails + HarvestDetailUiState()
                _uiState.update { it.copy(harvestDetails = newList) }
            }

            is AddReportEvent.OnRemoveHarvestDetail -> {
                val newHarvestList = _uiState.value.harvestDetails.toMutableList().apply { removeAt(event.index) }
                val newNte = newHarvestList.sumOf { it.totalPrice }
                _uiState.update { it.copy(harvestDetails = newHarvestList, nte = newNte) }
            }

            is AddReportEvent.OnHarvestItemChange -> {
                val newList = _uiState.value.harvestDetails.toMutableList()

                //total Price (Harga * Jumlah)
                val price = event.item.unitPrice.parseIndonesianNumber()
                val amount = event.item.amount.parseIndonesianNumber()
                val priceError = validateNumberInput(event.item.unitPrice, "Harga")
                val amountError = validateIntegerInput(event.item.amount, "Jumlah")

                val updatedItem = event.item.copy(
                    totalPrice = price * amount,
                    unitPriceError = priceError,
                    amountError = amountError,
                )

                newList[event.index] = updatedItem

                //total NTE (Sum semua total price)
                val newNte = newList.sumOf { it.totalPrice }

                _uiState.update { it.copy(harvestDetails = newList, nte = newNte) }
            }

            is AddReportEvent.OnAddAttachment -> {
                val currentList = _uiState.value.attachments.toMutableList()
                currentList.add(
                    ReportAttachment(
                        id = null,
                        filePath = event.filePath,
                        isLocal = true
                    )
                )
                _uiState.update { it.copy(attachments = currentList) }
            }
            is AddReportEvent.OnRemoveAttachment -> {
                val currentList = _uiState.value.attachments.toMutableList()
                if (event.index in currentList.indices) {
                    currentList.removeAt(event.index)
                    _uiState.update { it.copy(attachments = currentList) }
                }
            }
            is AddReportEvent.OnShowConfirmDialog -> {
                _uiState.update {
                    it.copy(
                        showConfirmDialog = true,
                        pendingActionIsAjukan = event.isAjukan
                    )
                }
            }
            is AddReportEvent.OnDismissConfirmDialog -> {
                _uiState.update { it.copy(showConfirmDialog = false) }
            }
            is AddReportEvent.OnSubmit -> {
                _uiState.update { it.copy(showConfirmDialog = false) }
                submitReport(event.isAjukan)
            }
            AddReportEvent.OnDismissMessage -> _uiState.update { it.copy(error = null, successMessage = null) }

            else -> {}
        }
    }

    private fun generatePeriodList() {
        val currentYear = java.util.Calendar.getInstance().get(java.util.Calendar.YEAR)
        val years = (0..4).map { (currentYear - it).toString() }
        _uiState.update { it.copy(periodList = years) }
    }

    private fun loadCommodities() {
        getCommoditiesUseCase("").onEach { result ->
            when (result) {
                is Resource.Success -> {
                    _uiState.update { it.copy(commodityList = result.data ?: emptyList()) }
                }
                is Resource.Error -> {
                    _uiState.update { it.copy(error = "Gagal memuat komoditas") }
                }
                else -> {}
            }
        }.launchIn(viewModelScope)
    }

    private fun loadExistingReportData(id: String) {
        getReportDetailUseCase(id).onEach { result ->
            when (result) {
                is Resource.Loading -> {
                    _uiState.update { it.copy(isLoading = true) }
                }
                is Resource.Success -> {
                    val data = result.data
                    if (data != null) {
//                        android.util.Log.d("DEBUG_EDIT", "Planting JSON Size: ${data.plantingDetails.size}")
//                        android.util.Log.d("DEBUG_EDIT", "Harvest JSON Size: ${data.harvestDetails.size}")

                        _uiState.update { state ->
                            state.copy(
                                isLoading = false,
                                month = data.month,
                                period = data.period.toString(),
                                modal = data.modal,
                                farmerNotes = data.farmerNotes,
                                nte = data.nte,
                                attachments = data.attachments,
                                plantingDetails = data.plantingDetails.map { domain ->
                                    val loadedUnit = if (domain.plantType.equals("semusim", true)) "kg" else "batang"
                                    PlantingDetailUiState(
                                        commodityId = domain.commodityId,
                                        commodityName = domain.commodityName,
                                        plantType = domain.plantType,
                                        unit = loadedUnit,
                                        plantDate = changeDateFormat(domain.plantDate),
                                        plantAge = calculatePlantAge(changeDateFormat(domain.plantDate)),
                                        amount = domain.amount.formatApiToUiString()
                                    )
                                },
                                harvestDetails = data.harvestDetails.map { domain ->
                                    HarvestDetailUiState(
                                        harvestDate = domain.harvestDate,
                                        commodityId = domain.commodityId,
                                        commodityName = domain.commodityName,
                                        unitPrice = domain.unitPrice.formatApiToUiString(),
                                        amount = domain.amount.formatApiToUiString(),
                                        totalPrice = (domain.unitPrice.toDoubleOrNull() ?: 0.0) * (domain.amount.toDoubleOrNull() ?: 0.0)
                                    )
                                }
                            )
                        }
                    }
                }
                is Resource.Error -> {
                    _uiState.update { it.copy(isLoading = false, error = result.message) }
                }
            }
        }.launchIn(viewModelScope)
    }

    private fun validateNumberInput(value: String, fieldName: String): String? {
        if (value.isBlank()) return "$fieldName wajib diisi"
        val number = value.replace(".", "").replace(",", ".").toDoubleOrNull()
        return if (number == null || number <= 0) "$fieldName harus angka > 0" else null
    }

    private fun validateIntegerInput(value: String, fieldName: String): String? {
        if (value.isBlank()) return "$fieldName wajib diisi"
        val hasIllegalChar = value.any { !it.isDigit() && it != '.' }
        if (hasIllegalChar) return "$fieldName hanya boleh angka (bulat)"

        val cleanValue = value.replace(".", "")
        val number = cleanValue.toLongOrNull()

        return if (number == null || number <= 0) "$fieldName harus lebih dari 0" else null
    }

    private fun calculatePlantAge(dateString: String): String {
        return try {
            val format = SimpleDateFormat("dd/MM/yyyy", Locale("id", "ID"))
            val date = format.parse(dateString) ?: return "0"

            val currentTime = System.currentTimeMillis()
            val plantedTime = date.time

            val diffInMillis = currentTime - plantedTime
            if (diffInMillis < 0) return "0"

            val oneYearInMillis = 31557600000.0
            val ageInYears = diffInMillis / oneYearInMillis

            String.format(Locale.US, "%.1f", ageInYears)
        } catch (e: Exception) {
            "0"
        }
    }

    private fun submitReport(isAjukan: Boolean) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            val input = mapStateToInput(isAjukan)
            val validationResult = validateReportInputUseCase.execute(input)

            if (!validationResult.successful) {
                val errors = validationResult.fieldErrors
//                android.util.Log.d("DEBUG_VALIDASI", "Validation Failed! Keys received: ${errors.keys}")
//                android.util.Log.d("DEBUG_VALIDASI", "Content: $errors")

                _uiState.update { s ->
                    s.copy(
                        isLoading = false,
                        error = validationResult.errorMessage,
                        periodError = errors["period"],
                        monthError = errors["month"],
                        modalError = errors["modal"],
                        plantingDetails = s.plantingDetails.mapIndexed { i, item ->
                            item.copy(
                                commodityError = errors["plant_comm_$i"],
                                plantTypeError = errors["plant_type_$i"],
                                amountError = errors["plant_amount_$i"],
                                plantAgeError = errors["plant_age_$i"],
                                plantDateError = errors["plant_date_$i"],
                            )
                        },
                        harvestDetails = s.harvestDetails.mapIndexed { i, item ->
                            item.copy(
                                commodityError = errors["harvest_comm_$i"],
                                amountError = errors["harvest_amount_$i"],
                                unitPriceError = errors["harvest_price_$i"],
                                harvestDateError = errors["harvest_date_$i"]
                            )
                        }
                    )
                }
                return@launch
            }

            val result = if (currentReportId == null) {
                createReportUseCase(input)
            } else {
                updateReportUseCase(currentReportId!!, input)
            }

            when (result) {
                is Resource.Success -> {
                    val message = if (currentReportId == null) "Berhasil disimpan!" else "Perubahan disimpan!"
                    _uiState.update { it.copy(isLoading = false, successMessage = message) }
                }
                is Resource.Error -> {
                    _uiState.update { it.copy(isLoading = false, error = result.message) }
                }
                else -> {}
            }
        }
    }

    private fun mapStateToInput(isAjukan: Boolean): CreateReportInput {
        val s = _uiState.value
        val newFiles = s.attachments
            .filter { it.isLocal }
            .map { it.filePath }
        val existingIds = s.attachments
            .filter { !it.isLocal && it.id != null }
            .map { it.id!! }

        return CreateReportInput(
            month = s.month,
            period = s.period.toIntOrNull() ?: 0,
            modal = s.modal,
            farmerNotes = s.farmerNotes,
            nte = s.nte,
            isAjukan = isAjukan,
            newAttachments = newFiles,
            existingAttachmentIds = existingIds,
            plantingDetails = s.plantingDetails.map {
                val cleanAmount = it.amount.replace(".", "").toLongOrNull() ?: 0L
                MasaTanam(
                    commodityId = it.commodityId,
                    commodityName = it.commodityName,
                    it.plantType,
                    plantDate = convertUiDateToApiDate(it.plantDate),
                    plantAge = it.plantAge.toDoubleOrNull() ?: 0.0,
                    amount = cleanAmount.toString()
                )
            },
            harvestDetails = s.harvestDetails.map {
                val cleanAmount = it.amount.replace(".", "").toLongOrNull() ?: 0L

                MasaPanen(
                    harvestDate =  convertUiDateToApiDate(it.harvestDate),
                    commodityName = it.commodityName,
                    commodityId =  it.commodityId,
                    unitPrice =  it.unitPrice.parseIndonesianNumber().toString(),
                    amount = cleanAmount.toString()
                )
            }
        )
    }
}