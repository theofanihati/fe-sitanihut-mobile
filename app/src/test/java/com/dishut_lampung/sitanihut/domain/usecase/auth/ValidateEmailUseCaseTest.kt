package com.dishut_lampung.sitanihut.domain.usecase.auth

import org.junit.Assert.assertEquals
import org.junit.Test

class ValidateEmailUseCaseTest {

    private val validateEmailUseCase = ValidateEmailUseCase()

    @Test
    fun `Email is blank, returns error`() {
        val result = validateEmailUseCase("")
        assertEquals(false, result.successful)
        assertEquals("Email tidak boleh kosong", result.errorMessage)
    }

    @Test
    fun `Email is not valid format, returns error`() {
        val result = validateEmailUseCase("ini.bukan.email")
        assertEquals(false, result.successful)
        assertEquals("Format email tidak valid", result.errorMessage)
    }

    @Test
    fun `Email is valid, returns success`() {
        val result = validateEmailUseCase("test@example.com")
        assertEquals(true, result.successful)
        assertEquals(null, result.errorMessage)
    }
}