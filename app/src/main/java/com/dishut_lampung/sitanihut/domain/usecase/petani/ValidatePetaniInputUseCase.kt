package com.dishut_lampung.sitanihut.domain.usecase.petani

import com.dishut_lampung.sitanihut.domain.model.CreatePetaniInput
import com.dishut_lampung.sitanihut.domain.validator.ListValidationResult
import javax.inject.Inject

class ValidatePetaniInputUseCase @Inject constructor() {
    private val phoneRegex = Regex("^(08[0-9]{8,14}|\\+628[0-9]{8,14})$")
    private val nikRegex = Regex("^\\d+$")

    fun execute(input: CreatePetaniInput): ListValidationResult {
        val errors = mutableMapOf<String, String>()
        if (input.name.isBlank()) errors["name"] = "Nama tidak boleh kosong"
        if (input.address.isBlank()) errors["address"] = "Alamat tidak boleh kosong"
        if (input.gender.isBlank()) errors["gender"] = "Pilih jenis kelamin"

        if (input.identityNumber.isBlank()) {
            errors["identityNumber"] = "NIK wajib diisi"
        } else {
            if (!input.identityNumber.matches(nikRegex)) {
                errors["identityNumber"] = "NIK hanya boleh berisi angka."
            } else if (input.identityNumber.length != 16) {
                errors["identityNumber"] = "NIK harus 16 digit"
            }
        }

        if (input.whatsAppNumber.isBlank()) {
            errors["whatsAppNumber"] = "Nomor telepon tidak boleh kosong."
        } else {
            if (input.whatsAppNumber.length < 10) {
                errors["whatsAppNumber"] = "Nomor telepon minimal 10 digit."
            } else if (input.whatsAppNumber.length > 14) {
                errors["whatsAppNumber"] = "Nomor telepon maksimal 14 digit."
            } else if (!input.whatsAppNumber.matches(phoneRegex)) {
                errors["whatsAppNumber"] = "Masukkan nomor telepon Indonesia yang valid (08... atau +628...)"
            }
        }
        if (input.lastEducation.isBlank()) { errors["lastEducation"] = "Pilih pendidikan terakhir" }
        if (input.sideJob.isBlank()) { errors["sideJob"] = "Pekerjaan sampingan tidak boleh kosong" }

        if (!isValidNumber(input.landArea)) {
            errors["landArea"] = "Luas lahan harus diisi angka lebih dari 0"
        }

        if (input.kphId.isBlank()) errors["kphId"] = "Asal KPH tidak boleh kosong"
        if (input.kthId.isBlank()) errors["kthId"] = "Asal KTH tidak boleh kosong"

        return ListValidationResult(
            successful = errors.isEmpty(),
            fieldErrors = errors,
            errorMessage = if (errors.isNotEmpty()) "Mohon perbaiki data yang salah" else null
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