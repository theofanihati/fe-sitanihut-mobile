package com.dishut_lampung.sitanihut.domain.usecase.report

import com.dishut_lampung.sitanihut.domain.model.CreateReportInput
import com.dishut_lampung.sitanihut.domain.validator.ListValidationResult
import com.dishut_lampung.sitanihut.domain.validator.ValidationResult
import javax.inject.Inject

class ValidateReportInputUseCase @Inject constructor() {
    fun execute(input: CreateReportInput): ListValidationResult {
        val errors = mutableMapOf<String, String>()
        var generalErrorMessage: String? = null

        if (input.period <= 0) errors["period"] = "Tahun wajib dipilih"
        if (input.month.isBlank()) errors["month"] = "Bulan wajib dipilih"
        if (!isValidNumber(input.modal)) {
            errors["modal"] = "Modal harus angka lebih dari 0"
        }

        if (input.plantingDetails.isEmpty()) {
            if (generalErrorMessage == null) generalErrorMessage = "Minimal isi satu data masa tanam"
        } else {
            input.plantingDetails.forEachIndexed { index, detail ->
                if (detail.commodityId.isBlank()) errors["plant_comm_$index"] = "Komoditas wajib dipilih"
                if (detail.plantType.equals("tahunan", ignoreCase = true)) {
                    if (detail.plantAge <= 0) errors["plant_age_$index"] = "Usia tanam wajib diisi untuk tanaman tahunan"
                    if (!isValidNumber(detail.amount)) errors["plant_amount_$index"] = "Jumlah tanam (batang) wajib diisi"
                } else if (detail.plantType.equals("semusim", ignoreCase = true)) {
                    if (detail.plantDate.isBlank()) errors["plant_date_$index"] = "Tanggal wajib diisi untuk tanaman semusim"
                    if (!isValidNumber(detail.amount)) errors["plant_amount_$index"] = "Jumlah tanam (kg) wajib diisi"
                } else {
                    errors["plant_type_$index"] = "Wajib pilih"
                }
            }
        }

        if (input.harvestDetails.isEmpty()) {
            if (generalErrorMessage == null) generalErrorMessage = "Minimal isi satu data panen"
        } else {
            input.harvestDetails.forEachIndexed { index, detail ->
                if (detail.commodityId.isBlank()) errors["harvest_comm_$index"] = "Komoditas wajib dipilih"
                if (detail.harvestDate.isBlank()) errors["harvest_date_$index"] = "Tanggal panen wajib diisi"
                if (!isValidNumber(detail.amount)) errors["harvest_amount_$index"] = "Jumlah tanam wajib diisi"
                if (!isValidNumber(detail.unitPrice)) errors["harvest_price_$index"] = "Harga satuan harus lebih dari 0"
            }
        }

        return ListValidationResult(
            successful = errors.isEmpty(),
            fieldErrors = errors
        )
    }

    private fun isValidNumber(value: String): Boolean {
        if (value.isBlank()) return false
        val normalizedValue = value.replace(",", ".")
        return try {
            val number = normalizedValue.toDouble()
            !number.isNaN() && number > 0
        } catch (e: NumberFormatException) {
            false
        }
    }
}