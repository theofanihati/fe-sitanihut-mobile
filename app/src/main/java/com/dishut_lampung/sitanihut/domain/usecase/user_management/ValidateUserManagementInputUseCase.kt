package com.dishut_lampung.sitanihut.domain.usecase.user_management

import com.dishut_lampung.sitanihut.domain.model.CreateUserInput
import com.dishut_lampung.sitanihut.domain.validator.ListValidationResult
import javax.inject.Inject

class ValidateUserManagementInputUseCase @Inject constructor() {
    private val waRegex = Regex("^(08[0-9]{8,14}|\\+628[0-9]{8,14})$")
    private val nikRegex = Regex("^[0-9]{16}$")
    private val nipRegex = Regex("^[0-9]{18}$")
    private val emailRegex = Regex("[a-zA-Z0-9\\+\\.\\_\\%\\-\\+]{1,256}" +
            "\\@" +
            "[a-zA-Z0-9][a-zA-Z0-9\\-]{0,64}" +
            "(" +
            "\\." +
            "[a-zA-Z0-9][a-zA-Z0-9\\-]{0,25}" +
            ")+")

    private val hasLowerCase = Regex("[a-z]")
    private val hasUpperCase = Regex("[A-Z]")
    private val hasDigit = Regex("\\d")
    private val hasSpecialChar = Regex("[!@#\$%^&*(),.?\":{}|<>]")

    fun execute(input: CreateUserInput): ListValidationResult {
        val errors = mutableMapOf<String, String>()

        if (input.name.isBlank()) {
            errors["name"] = "Nama lengkap wajib diisi"
        }

        if (input.email.isBlank()) {
            errors["email"] = "Email tidak boleh kosong"
        } else if (!input.email.matches(emailRegex)) {
            errors["email"] = "Format email tidak valid"
        }

        if (input.whatsAppNumber.isBlank()) {
            errors["whatsAppNumber"] = "Nomor telepon tidak boleh kosong"
        } else if (!input.whatsAppNumber.matches(waRegex)) {
            errors["whatsAppNumber"] = "Format nomor telepon tidak valid (contoh: 08... atau +628...)"
        }

        if (input.gender.isBlank()) {
            errors["gender"] = "Jenis kelamin wajib dipilih"
        }

        val isPasswordProvided = input.password.isNotEmpty()
        val shouldValidatePassword = if (input.isEditMode) isPasswordProvided else true

        if (shouldValidatePassword) {
            if (input.password.length < 8) {
                errors["password"] = "Password minimal 8 karakter"
            } else {
                if (!input.password.contains(hasLowerCase)) {
                    errors["password"] = "Password harus memiliki setidaknya satu huruf kecil"
                } else if (!input.password.contains(hasUpperCase)) {
                    errors["password"] = "Password harus memiliki setidaknya satu huruf besar (kapital)"
                } else if (!input.password.contains(hasDigit)) {
                    errors["password"] = "Password harus memiliki setidaknya satu angka"
                } else if (!input.password.contains(hasSpecialChar)) {
                    errors["password"] = "Password harus memiliki setidaknya satu karakter spesial (!@#$...)"
                }
            }

            if (input.password != input.confirmPassword) {
                errors["confirmPassword"] = "Password dan konfirmasi password tidak cocok"
            }
        }

        when (input.role) {
            "petani" -> validatePetani(input, errors)
            "penyuluh", "penanggung jawab" -> validatePenyuluhOrPJ(input, errors)
            else -> errors["role"] = "Role tidak valid atau konfigurasi role tidak ditemukan"
        }

        return ListValidationResult(
            successful = errors.isEmpty(),
            fieldErrors = errors,
            errorMessage = if (errors.isNotEmpty()) "Mohon perbaiki data yang salah" else null
        )
    }

    private fun validatePetani(input: CreateUserInput, errors: MutableMap<String, String>) {
        if (input.identityNumber.isBlank()) {
            errors["identityNumber"] = "NIK wajib diisi"
        } else if (!input.identityNumber.matches(Regex("^\\d+$"))) {
            errors["identityNumber"] = "NIK hanya boleh berisi angka"
        } else if (!input.identityNumber.matches(nikRegex)) {
            errors["identityNumber"] = "NIK harus terdiri dari 16 digit"
        }

        if (input.address.isBlank()) errors["address"] = "Alamat wajib diisi"
        if (input.lastEducation.isBlank()) errors["lastEducation"] = "Pendidikan terakhir wajib dipilih"
        if (input.sideJob.isBlank()) errors["sideJob"] = "Pekerjaan sampingan wajib diisi"

        if (!isValidLandArea(input.landArea)) {
            errors["landArea"] = "Luas lahan harus diisi angka lebih dari 0"
        }

        if (input.kphId.isNullOrBlank()) errors["kphId"] = "Asal KPH tidak boleh kosong"
        if (input.kthId.isNullOrBlank()) errors["kthId"] = "Asal KTH tidak boleh kosong"
    }

    private fun validatePenyuluhOrPJ(input: CreateUserInput, errors: MutableMap<String, String>) {
        if (input.identityNumber.isBlank()) {
            errors["identityNumber"] = "NIP wajib diisi"
        } else if (!input.identityNumber.matches(Regex("^\\d+$"))) {
            errors["identityNumber"] = "NIP hanya boleh berisi angka"
        } else if (!input.identityNumber.matches(nipRegex)) {
            errors["identityNumber"] = "NIP harus terdiri dari 18 digit"
        }

//        if (input.position.isBlank()) errors["position"] = "Jabatan wajib diisi"
        if (input.kphId.isNullOrBlank()) errors["kphId"] = "Asal KPH tidak boleh kosong"
    }

    private fun isValidLandArea(value: String): Boolean {
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