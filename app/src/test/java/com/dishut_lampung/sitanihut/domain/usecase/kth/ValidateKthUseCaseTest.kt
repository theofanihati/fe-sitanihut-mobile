package com.dishut_lampung.sitanihut.domain.usecase.kth

import com.dishut_lampung.sitanihut.domain.model.CreateKthInput
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class ValidateKthInputUseCaseTest {

    private lateinit var validateKthInputUseCase: ValidateKthInputUseCase

    @Before
    fun setUp() {
        validateKthInputUseCase = ValidateKthInputUseCase()
    }

    @Test
    fun `execute should return error when name is blank`() {
        val input = CreateKthInput(name = "")
        val result = validateKthInputUseCase.execute(input)

        assertFalse(result.successful)
        assertEquals("Nama KTH tidak boleh kosong", result.fieldErrors["name"])
    }

    @Test
    fun `execute should return error when whatsapp number is invalid`() {
        val inputShort = CreateKthInput(whatsappNumber = "08123")
        val resultShort = validateKthInputUseCase.execute(inputShort)
        assertFalse(resultShort.successful)
        assertTrue(resultShort.fieldErrors.containsKey("whatsappNumber"))

        val inputLetter = CreateKthInput(whatsappNumber = "08123abc")
        val resultLetter = validateKthInputUseCase.execute(inputLetter)
        assertFalse(resultLetter.successful)
    }

    @Test
    fun `execute should return error when kphId is blank`() {
        val input = CreateKthInput(kphName = "KPH Way Kanan", kphId = "")
        val result = validateKthInputUseCase.execute(input)

        assertFalse(result.successful)
        assertEquals("Pilih KPH", result.fieldErrors["kphId"])
    }

    @Test
    fun `execute should return success when all data is valid`() {
        val input = CreateKthInput(
            name = "KTH Cuantikz",
            desa = "Desa A",
            kecamatan = "Kec B",
            kabupaten = "Kab C",
            coordinator = "Budi",
            whatsappNumber = "081234567890",
            kphId = "123",
            kphName = "KPH X"
        )
        val result = validateKthInputUseCase.execute(input)

        assertTrue(result.successful)
        assertTrue(result.fieldErrors.isEmpty())
    }
}