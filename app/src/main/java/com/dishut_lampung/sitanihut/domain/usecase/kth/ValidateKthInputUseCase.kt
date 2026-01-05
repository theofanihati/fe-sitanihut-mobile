package com.dishut_lampung.sitanihut.domain.usecase.kth

import com.dishut_lampung.sitanihut.domain.model.CreateKthInput
import com.dishut_lampung.sitanihut.domain.validator.ListValidationResult
import javax.inject.Inject

class ValidateKthInputUseCase @Inject constructor() {
    private val phoneRegex = Regex("^(08[0-9]{8,14}|\\+628[0-9]{8,14})$")

    fun execute(input: CreateKthInput): ListValidationResult{
        val errors = mutableMapOf<String, String>()

        if (input.name.isBlank()) errors["name"] = "Nama KTH tidak boleh kosong"
        if (input.kabupaten.isBlank()) errors["kabupaten"] = "Pilih kabupaten"
        if (input.kecamatan.isBlank()) errors["kecamatan"] = "Pilih kecamatan"
        if (input.desa.isBlank()) errors["desa"] = "Pilih desa"
        if (input.coordinator.isBlank()) errors["coordinator"] = "Nama ketua KPH tidak boleh kosong"
        if (input.whatsappNumber.isBlank()) {
            errors["whatsappNumber"] = "Nomor telepon tidak boleh kosong."
        } else {
            if (input.whatsappNumber.length < 10) {
                errors["whatsappNumber"] = "Nomor telepon minimal 10 digit."
            } else if (input.whatsappNumber.length > 14) {
                errors["whatsappNumber"] = "Nomor telepon maksimal 14 digit."
            } else if (!input.whatsappNumber.matches(phoneRegex)) {
                errors["whatsappNumber"] = "Masukkan nomor valid (08.. atau +628..) tanpa spasi."
            }
        }
        if (input.kphId.isBlank()) errors["kphId"] = "Pilih KPH"
//        if (input.kphName.isBlank()) errors["kph_name"] = "Pilih KPH"

        return ListValidationResult(
            successful = errors.isEmpty(),
            fieldErrors = errors,
            errorMessage = if (errors.isNotEmpty()) "Mohon perbaiki data yang salah" else null
        )
    }
}