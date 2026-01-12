package com.dishut_lampung.sitanihut.domain.usecase.petani

import com.dishut_lampung.sitanihut.domain.model.CreatePetaniInput
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class ValidatePetaniInputUseCaseTest {

    private lateinit var validatePetaniInputUseCase: ValidatePetaniInputUseCase

    @Before
    fun setUp(){
        validatePetaniInputUseCase = ValidatePetaniInputUseCase()
    }

    @Test
    fun `execute should return error when name is blank`(){
        val input = CreatePetaniInput(name = "")
        val result = validatePetaniInputUseCase.execute(input)

        assertFalse(result.successful)
        assertEquals("Nama tidak boleh kosong", result.fieldErrors["name"])
    }

    @Test
    fun `execute should return error when address is blank`(){
        val input = CreatePetaniInput(address = "")
        val result = validatePetaniInputUseCase.execute(input)

        assertFalse(result.successful)
        assertEquals("Alamat tidak boleh kosong", result.fieldErrors["address"])
    }

    @Test
    fun `execute should return error when gender is blank`(){
        val input = CreatePetaniInput(gender = "")
        val result = validatePetaniInputUseCase.execute(input)

        assertFalse(result.successful)
        assertEquals("Pilih jenis kelamin", result.fieldErrors["gender"])
    }

    @Test
    fun `execute should return error when NIK number is invalid`() {
        val inputShort = CreatePetaniInput(identityNumber = "20028")
        val resultShort = validatePetaniInputUseCase.execute(inputShort)
        assertFalse(resultShort.successful)
        assertTrue(resultShort.fieldErrors.containsKey("identityNumber"))

        val inputLetter = CreatePetaniInput(whatsAppNumber = "20021013kkjs")
        val resultLetter = validatePetaniInputUseCase.execute(inputLetter)
        assertFalse(resultLetter.successful)
    }

    @Test
    fun `execute should return error when whatsapp number is invalid`() {
        val inputShort = CreatePetaniInput(whatsAppNumber = "08123")
        val resultShort = validatePetaniInputUseCase.execute(inputShort)
        assertFalse(resultShort.successful)
        assertTrue(resultShort.fieldErrors.containsKey("whatsAppNumber"))

        val inputLetter = CreatePetaniInput(whatsAppNumber = "08123abc")
        val resultLetter = validatePetaniInputUseCase.execute(inputLetter)
        assertFalse(resultLetter.successful)
    }

    @Test
    fun `execute should return error when last education is blank`(){
        val input = CreatePetaniInput(lastEducation = "")
        val result = validatePetaniInputUseCase.execute(input)

        assertFalse(result.successful)
        assertEquals("Pilih pendidikan terakhir", result.fieldErrors["lastEducation"])
    }

    @Test
    fun `execute should return error when side job is blank`(){
        val input = CreatePetaniInput(lastEducation = "")
        val result = validatePetaniInputUseCase.execute(input)

        assertFalse(result.successful)
        assertEquals("Pekerjaan sampingan tidak boleh kosong", result.fieldErrors["sideJob"])
    }

    @Test
    fun `execute should return error when landArea number is invalid`() {
        val inputNegative = CreatePetaniInput(landArea = "-5")
        val resultShort = validatePetaniInputUseCase.execute(inputNegative)
        assertFalse(resultShort.successful)
        assertTrue(resultShort.fieldErrors.containsKey("landArea"))

        val inputLetter = CreatePetaniInput(landArea = "2abc")
        val resultLetter = validatePetaniInputUseCase.execute(inputLetter)
        assertFalse(resultLetter.successful)
    }

    @Test
    fun `execute should return error when KPH is blank`(){
        val input = CreatePetaniInput(kphId = "")
        val result = validatePetaniInputUseCase.execute(input)

        assertFalse(result.successful)
        assertEquals("Asal KPH tidak boleh kosong", result.fieldErrors["kphId"])
    }

    @Test
    fun `execute should return error when KTH is blank`(){
        val input = CreatePetaniInput(kthId = "")
        val result = validatePetaniInputUseCase.execute(input)

        assertFalse(result.successful)
        assertEquals("Asal KTH tidak boleh kosong", result.fieldErrors["kthId"])
    }

    @Test
    fun `execute should return success when all data is valid`() {
        val input = CreatePetaniInput(
            name = "Petani Baru",
            identityNumber = "1871012002100003",
            gender = "Laki-laki",
            address = "mana ya",
            whatsAppNumber = "081234567890",
            lastEducation = "SMA",
            sideJob = "Merajut",
            landArea = "100",
            kphId = "1",
            kthId = "1",
        )
        val result = validatePetaniInputUseCase.execute(input)

        assertTrue(result.successful)
        assertTrue(result.fieldErrors.isEmpty())
    }
}