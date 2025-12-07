package com.dishut_lampung.sitanihut.domain.usecase.report

import com.dishut_lampung.sitanihut.domain.model.CreateReportInput
import com.dishut_lampung.sitanihut.domain.validator.ValidationResult
import javax.inject.Inject

class ValidateReportInputUseCase @Inject constructor() {
    fun execute(input: CreateReportInput): ValidationResult {
        if (input.period <= 0) {
            return ValidationResult(false, "Periode (Tahun) wajib dipilih")
        }
        if (input.month.isBlank()) {
            return ValidationResult(false, "Periode (Bulan) wajib dipilih")
        }
        if (!isValidNumber(input.modal)) {
            return ValidationResult(false, "Modal harus terisi dan lebih dari 0")
        }
        val nteString = input.totalNte.toString()
        if (input.totalNte <= 0) {
            return ValidationResult(false, "NTE harus terisi")
        }

        if (input.plantingDetails.isEmpty()) {
            return ValidationResult(successful = false, errorMessage = "Minimal isi satu data masa tanam")
        }
        input.plantingDetails.forEach { detail ->
            if (detail.commodityId.isBlank()) {
                return ValidationResult(false, "Komoditas wajib dipilih")
            }
            if (detail.plantType.isBlank()) {
                return ValidationResult(false, "Jenis tanaman wajib dipilih")
            }
            if (detail.plantType.equals("tahunan", ignoreCase = true)) {
                if (detail.plantAge <= 0) {
                    return ValidationResult(false, "Usia tanam wajib diisi untuk tanaman tahunan")
                }
                if (!isValidNumber(detail.amount)) {
                    return ValidationResult(false, "Jumlah tanam (batang) wajib diisi")
                }
            }
            else if (detail.plantType.equals("semusim", ignoreCase = true)) {
                if (detail.plantDate.isBlank()) {
                    return ValidationResult(false, "Tanggal wajib diisi untuk tanaman semusim")
                }
                if (!isValidNumber(detail.amount)) {
                    return ValidationResult(false, "Jumlah tanam (kg) wajib diisi")
                }
            }
        }

        if (input.harvestDetails.isEmpty()) {
             return ValidationResult(successful = false, errorMessage = "Minimal isi satu data panen")
        }
        input.harvestDetails.forEach { detail ->
            if (detail.commodityId.isBlank()) {
                return ValidationResult(false, "Komoditas wajib dipilih")
            }
            if (detail.harvestDate.isBlank()) {
                return ValidationResult(false, "Tanggal panen wajib diisi")
            }
            if (!isValidNumber(detail.amount)) {
                return ValidationResult(false, "Jumlah panen harus lebih besar dari 0")
            }
            if (!isValidNumber(detail.unitPrice)) {
                return ValidationResult(false, "Harga satuan harus lebih dari 0")
            }
        }

        return ValidationResult(successful = true)
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