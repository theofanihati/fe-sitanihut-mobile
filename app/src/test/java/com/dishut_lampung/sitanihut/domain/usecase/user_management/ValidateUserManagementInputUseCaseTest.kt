package com.dishut_lampung.sitanihut.domain.usecase.user_management

import com.dishut_lampung.sitanihut.domain.model.CreateUserInput
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class ValidateUserManagementInputUseCaseTest {

    private lateinit var validateUseCase: ValidateUserManagementInputUseCase

    private val validBaseInput = CreateUserInput(
        name = "User Test",
        email = "test@example.com",
        whatsAppNumber = "081234567890",
        gender = "pria",
        password = "Password123!",
        confirmPassword = "Password123!",
        role = "petani",
        identityNumber = "1234567890123456",
        address = "Jl. Mawar",
        lastEducation = "SMA",
        sideJob = "Dagang",
        landArea = "2.5",
        kphId = "kph-1",
        kthId = "kth-1"
    )

    @Before
    fun setUp() {
        validateUseCase = ValidateUserManagementInputUseCase()
    }

    @Test
    fun `execute should return error when fields are blank`() {
        val input = validBaseInput.copy(name = "", gender = "", email = "", whatsAppNumber = "")
        val result = validateUseCase.execute(input)

        assertFalse(result.successful)
        assertEquals("Nama lengkap wajib diisi", result.fieldErrors["name"])
        assertEquals("Jenis kelamin wajib dipilih", result.fieldErrors["gender"])
        assertEquals("Email tidak boleh kosong", result.fieldErrors["email"])
        assertEquals("Nomor telepon tidak boleh kosong", result.fieldErrors["whatsAppNumber"])

    }

    @Test
    fun `execute should return error when email is invalid`() {
        val input = validBaseInput.copy(email = "bukan-email")
        val result = validateUseCase.execute(input)

        assertFalse(result.successful)
        assertEquals("Format email tidak valid", result.fieldErrors["email"])
    }

    @Test
    fun `execute should return error when whatsAppNumber is invalid`() {
        val inputShort = validBaseInput.copy(whatsAppNumber = "081")
        val resultShort = validateUseCase.execute(inputShort)
        assertTrue(resultShort.fieldErrors.containsKey("whatsAppNumber"))

        val inputLetter = validBaseInput.copy(whatsAppNumber = "08123abc")
        val resultLetter = validateUseCase.execute(inputLetter)
        assertTrue(resultLetter.fieldErrors.containsKey("whatsAppNumber"))
    }

    @Test
    fun `execute should return error when password is too short`() {
        val input = validBaseInput.copy(password = "Pass1!", confirmPassword = "Pass1!")
        val result = validateUseCase.execute(input)

        assertFalse(result.successful)
        assertEquals("Password minimal 8 karakter", result.fieldErrors["password"])
    }

    @Test
    fun `execute should return error when password complexity is missing`() {
        val inputNoUpper = validBaseInput.copy(password = "password123!", confirmPassword = "password123!")
        val resultNoUpper = validateUseCase.execute(inputNoUpper)
        assertEquals("Password harus memiliki setidaknya satu huruf besar (kapital)", resultNoUpper.fieldErrors["password"])

        val inputNoDigit = validBaseInput.copy(password = "Password!", confirmPassword = "Password!")
        val resultNoDigit = validateUseCase.execute(inputNoDigit)
        assertEquals("Password harus memiliki setidaknya satu angka", resultNoDigit.fieldErrors["password"])

        val inputNoLower = validBaseInput.copy(password = "PASSWORD123!", confirmPassword = "PASSWORD123!")
        val resultNoLower = validateUseCase.execute(inputNoLower)
        assertEquals("Password harus memiliki setidaknya satu huruf kecil", resultNoLower.fieldErrors["password"])

        val inputNoSpecial = validBaseInput.copy(password = "Password123", confirmPassword = "Password123")
        val resultNoSpecial = validateUseCase.execute(inputNoSpecial)
        assertEquals("Password harus memiliki setidaknya satu karakter spesial (!@#$...)", resultNoSpecial.fieldErrors["password"])
    }

    @Test
    fun `execute should return error when password confirmation does not match`() {
        val input = validBaseInput.copy(
            password = "Password123!",
            confirmPassword = "Password1234!"
        )
        val result = validateUseCase.execute(input)

        assertFalse(result.successful)
        assertEquals("Password dan konfirmasi password tidak cocok", result.fieldErrors["confirmPassword"])
    }

    @Test
    fun `execute should SKIP password validation when isEditMode is true and password is empty`() {
        val input = validBaseInput.copy(
            isEditMode = true,
            password = "",
            confirmPassword = ""
        )
        val result = validateUseCase.execute(input)
        assertTrue(result.successful)
    }

    @Test
    fun `execute should FAIL in Edit Mode if provided password is invalid`() {
        val input = validBaseInput.copy(
            isEditMode = true,
            password = "123",
            confirmPassword = "123"
        )
        val result = validateUseCase.execute(input)

        assertFalse(result.successful)
        assertEquals("Password minimal 8 karakter", result.fieldErrors["password"])
    }

    @Test
    fun `execute should return error for Petani when NIK is invalid`() {
        val inputShort = validBaseInput.copy(
            role = "petani",
            identityNumber = "123"
        )
        val resultShort = validateUseCase.execute(inputShort)
        assertFalse(resultShort.successful)
        assertEquals("NIK harus terdiri dari 16 digit", resultShort.fieldErrors["identityNumber"])

        val input = validBaseInput.copy(
            role = "petani",
            identityNumber = "123456789012345A"
        )
        val result = validateUseCase.execute(input)
        assertFalse(result.successful)
        assertEquals("NIK hanya boleh berisi angka", result.fieldErrors["identityNumber"])
    }

    @Test
    fun `execute should return error for Petani when landArea is invalid`() {
        val input = validBaseInput.copy(role = "petani", landArea = "0")
        val result = validateUseCase.execute(input)

        assertFalse(result.successful)
        assertTrue(result.fieldErrors.containsKey("landArea"))
    }

    @Test
    fun `execute should return error for Petani when fields are blank`() {
        val input = validBaseInput.copy(
            role = "petani",
            kphId = "",
            kthId = "",
            address = "",
            lastEducation = "",
            sideJob = "",
            identityNumber = "",
        )
        val result = validateUseCase.execute(input)

        assertFalse(result.successful)
        assertEquals("Asal KPH tidak boleh kosong", result.fieldErrors["kphId"])
        assertEquals("Asal KTH tidak boleh kosong", result.fieldErrors["kthId"])
        assertEquals("Alamat wajib diisi", result.fieldErrors["address"])
        assertEquals("Pendidikan terakhir wajib dipilih", result.fieldErrors["lastEducation"])
        assertEquals("Pekerjaan sampingan wajib diisi", result.fieldErrors["sideJob"])
        assertEquals("NIK wajib diisi", result.fieldErrors["identityNumber"])
    }

    @Test
    fun `execute should return error for Penyuluh when NIP is invalid`() {
        val inputShort = validBaseInput.copy(
            role = "penyuluh",
            identityNumber = "1234567890123456"
        )
        val resultShort = validateUseCase.execute(inputShort)

        assertFalse(resultShort.successful)
        assertEquals("NIP harus terdiri dari 18 digit", resultShort.fieldErrors["identityNumber"])

        val input = validBaseInput.copy(
            role = "penyuluh",
            identityNumber = "123456789012345AAA"
        )
        val result = validateUseCase.execute(input)
        assertFalse(result.successful)
        assertEquals("NIP hanya boleh berisi angka", result.fieldErrors["identityNumber"])
    }

    @Test
    fun `execute should return error for Penyuluh when fields are blank`() {
        val input = validBaseInput.copy(
            role = "penyuluh",
//            position = "",
            kphId = "",
            identityNumber = "",
        )
        val result = validateUseCase.execute(input)

        assertFalse(result.successful)
//        assertEquals("Jabatan wajib diisi", result.fieldErrors["position"])
        assertEquals("Asal KPH tidak boleh kosong", result.fieldErrors["kphId"])
        assertEquals("NIP wajib diisi", result.fieldErrors["identityNumber"])
    }

    @Test
    fun `execute should return error for Penanggung Jawab when NIP is invalid`() {
        val inputShort = validBaseInput.copy(
            role = "penanggung jawab",
            identityNumber = "1234567890123456"
        )
        val resultShort = validateUseCase.execute(inputShort)

        assertFalse(resultShort.successful)
        assertEquals("NIP harus terdiri dari 18 digit", resultShort.fieldErrors["identityNumber"])

        val input = validBaseInput.copy(
            role = "penanggung jawab",
            identityNumber = "123456789012345A"
        )
        val result = validateUseCase.execute(input)
        assertFalse(result.successful)
        assertEquals("NIP hanya boleh berisi angka", result.fieldErrors["identityNumber"])
    }

    @Test
    fun `execute should return error for Penanggung Jawab when fields are blank`() {
        val input = validBaseInput.copy(
            role = "penanggung jawab",
//            position = "",
            kphId = "",
            identityNumber = "",
        )
        val result = validateUseCase.execute(input)

        assertFalse(result.successful)
//        assertEquals("Jabatan wajib diisi", result.fieldErrors["position"])
        assertEquals("Asal KPH tidak boleh kosong", result.fieldErrors["kphId"])
        assertEquals("NIP wajib diisi", result.fieldErrors["identityNumber"])
    }

    @Test
    fun `execute should return error when role is unknown`() {
        val input = validBaseInput.copy(role = "aku hacker wahaha")
        val result = validateUseCase.execute(input)

        assertFalse(result.successful)
        assertTrue(result.fieldErrors.containsKey("role"))
    }

    @Test
    fun `execute should return success for valid Petani data`() {
        val result = validateUseCase.execute(validBaseInput)

        assertTrue(result.successful)
        assertTrue(result.fieldErrors.isEmpty())
    }

    @Test
    fun `execute should return success for valid Penyuluh data`() {
        val inputPenyuluh = validBaseInput.copy(
            role = "penyuluh",
            identityNumber = "123456789012345678",
//            position = "Penyuluh Ahli Utama",
            kphId = "kph-2",
            kthId = ""
        )
        val result = validateUseCase.execute(inputPenyuluh)

        assertTrue(result.successful)
        assertTrue(result.fieldErrors.isEmpty())
    }

    @Test
    fun `execute should return success for valid Penanggung Jawab data`() {
        val inputPj = validBaseInput.copy(
            role = "penanggung jawab",
            identityNumber = "123456789012345678",
//            position = "Penyuluh Ahli Utama",
            kphId = "kph-2",
            kthId = ""
        )
        val result = validateUseCase.execute(inputPj)

        assertTrue(result.successful)
        assertTrue(result.fieldErrors.isEmpty())
    }
}