package com.dishut_lampung.sitanihut.domain.usecase.report

import com.dishut_lampung.sitanihut.domain.model.CreateReportInput
import com.dishut_lampung.sitanihut.domain.repository.ReportRepository
import com.dishut_lampung.sitanihut.domain.validator.ListValidationResult
import com.dishut_lampung.sitanihut.util.Resource
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class UpdateReportUseCaseTest {

    private val repository: ReportRepository = mockk()
    private val validator: ValidateReportInputUseCase = mockk()
    private lateinit var updateReportUseCase: UpdateReportUseCase

    @Before
    fun setUp() {
        updateReportUseCase = UpdateReportUseCase(repository, validator)
    }

    @Test
    fun `invoke should return Error when validation fails`() = runTest {
        val id = "123"
        val input = mockk<CreateReportInput>()
        val validationError = "Form tidak lengkap"

        every { validator.execute(input) } returns ListValidationResult(
            successful = false,
            errorMessage = validationError
        )
        val result = updateReportUseCase(id, input)

        assertTrue(result is Resource.Error)
        assertEquals(validationError, result.message)

        coVerify(exactly = 0) { repository.updateReport(any(), any()) }
    }

    @Test
    fun `invoke should call repository update when validation success`() = runTest {
        val id = "123"
        val input = mockk<CreateReportInput>()
        every { validator.execute(input) } returns ListValidationResult(successful = true)
        coEvery { repository.updateReport(id, input) } returns Resource.Success(true)

        val result = updateReportUseCase(id, input)
        assertTrue(result is Resource.Success)

        coVerify(exactly = 1) { repository.updateReport(id, input) }
    }

    @Test
    fun `invoke should return Error when validation success but repository fails`() = runTest {
        val id = "123"
        val input = mockk<CreateReportInput>()
        val repoError = "Database Error"
        every { validator.execute(input) } returns ListValidationResult(successful = true)
        coEvery { repository.updateReport(id, input) } returns Resource.Error(repoError)

        val result = updateReportUseCase(id, input)
        assertTrue(result is Resource.Error)
        assertEquals(repoError, result.message)
    }
}