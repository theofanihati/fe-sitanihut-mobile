package com.dishut_lampung.sitanihut.domain.usecase.pengajuan_laporan


import com.dishut_lampung.sitanihut.domain.model.CreateReportInput
import com.dishut_lampung.sitanihut.domain.model.MasaPanen
import com.dishut_lampung.sitanihut.domain.model.MasaTanam
import com.dishut_lampung.sitanihut.domain.usecase.report.ValidateReportInputUseCase
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class ValidateReportUseCaseTest {

    private lateinit var validateReportInput: ValidateReportInputUseCase

    @Before
    fun setUp() {
        validateReportInput = ValidateReportInputUseCase()
    }
    private val validPlantingSemusim = MasaTanam(
        commodityId = "1",
        plantType = "semusim",
        plantDate = "2024-01-01",
        plantAge = 0.5,
        amount = "100"
    )
    private val validPlantingTahunan = MasaTanam(
        commodityId = "2",
        plantType = "tahunan",
        plantDate = "",
        plantAge = 2.5,
        amount = "50",
    )
    private val validHarvest = MasaPanen(
        harvestDate = "2024-05-01",
        commodityId = "1",
        unitPrice = "5000",
        amount = "10"
    )
    private val baseInput = CreateReportInput(
        month = "Januari",
        period = 2024,
        modal = "100000",
        plantingDetails = listOf(validPlantingSemusim),
        harvestDetails = listOf(validHarvest),
        farmerNotes = "",
        attachments = emptyList(),
        isAjukan = true,
        totalNte = 50000.0
    )

    @Test
    fun `execute should return success when modal uses comma`() {
        val input = baseInput.copy(modal = "10,500")
        val result = validateReportInput.execute(input)
        assertTrue("Harusnya valid karena koma diganti titik", result.successful)
    }

    @Test
    fun `execute should return error when modal is zero or negative`() {
        val input = baseInput.copy(modal = "0")
        val result = validateReportInput.execute(input)
        assertFalse(result.successful)
        assertEquals("Modal harus terisi dan lebih dari 0", result.errorMessage)
    }

    // --- TANAMAN SEMUSIM ---
    @Test
    fun `execute should return error if semusim but date is empty`() {
        val invalidPlanting = validPlantingSemusim.copy(plantDate = "")
        val input = baseInput.copy(plantingDetails = listOf(invalidPlanting))

        val result = validateReportInput.execute(input)
        assertFalse(result.successful)
        assertEquals("Tanggal wajib diisi untuk tanaman semusim", result.errorMessage)
    }

    // --- TANAMAN TAHUNAN ---
    @Test
    fun `execute should return error if tahunan but age is invalid`() {
        val invalidPlanting = validPlantingTahunan.copy(plantAge = 0.0)
        val input = baseInput.copy(plantingDetails = listOf(invalidPlanting))

        val result = validateReportInput.execute(input)
        assertFalse(result.successful)
        assertEquals("Usia tanam wajib diisi untuk tanaman tahunan", result.errorMessage)
    }

    // --- TEST PANEN ---
    @Test
    fun `execute should return error if harvest price contains invalid chars`() {
        val invalidHarvest = MasaPanen(
            harvestDate = "2024-01-01", commodityId = "1",
            unitPrice = "abc", amount = "10"
        )
        val input = baseInput.copy(harvestDetails = listOf(invalidHarvest))

        val result = validateReportInput.execute(input)
        assertFalse(result.successful)
        assertEquals("Harga satuan harus lebih dari 0", result.errorMessage)
    }
}