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

        validateNik(input.identityNumber)?.let { errors["identityNumber"] = it }
        validateWhatsApp(input.whatsAppNumber)?.let { errors["whatsAppNumber"] = it }

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

    fun validateNik(nik: String): String? = when {
        nik.isBlank() -> "NIK wajib diisi"
        !nik.matches(nikRegex) -> "NIK hanya boleh berisi angka."
        nik.length != 16 -> "NIK harus 16 digit"
        else -> null
    }

    fun validateWhatsApp(phone: String): String? = when {
        phone.isBlank() -> "Nomor telepon tidak boleh kosong."
        phone.length < 10 -> "Nomor telepon minimal 10 digit."
        phone.length > 14 -> "Nomor telepon maksimal 14 digit."
        !phone.matches(phoneRegex) -> "Masukkan nomor telepon Indonesia yang valid (08... atau +628...)"
        else -> null
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