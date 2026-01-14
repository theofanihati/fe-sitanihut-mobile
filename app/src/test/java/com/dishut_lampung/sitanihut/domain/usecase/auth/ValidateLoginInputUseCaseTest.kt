package com.dishut_lampung.sitanihut.domain.usecase.auth

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class ValidateLoginInputUseCaseTest {

    private lateinit var validateLoginInputUseCase: ValidateLoginInputUseCase

    @Before
    fun setUp() {
        validateLoginInputUseCase = ValidateLoginInputUseCase()
    }

    @Test
    fun `invoke with blank input returns error`() {
        val input = "   "
        val result = validateLoginInputUseCase(input)

        assertFalse(result.successful)
        assertEquals("Email / NIP / NIK tidak boleh kosong", result.errorMessage)
    }

    @Test
    fun `invoke with valid NIK (16 digits) returns success`() {
        val input = "1234567890123456"
        val result = validateLoginInputUseCase(input)

        assertTrue(result.successful)
    }

    @Test
    fun `invoke with valid NIP (18 digits) returns success`() {
        val input = "123456789012345678"
        val result = validateLoginInputUseCase(input)

        assertTrue(result.successful)
    }

    @Test
    fun `invoke with invalid numeric length (too short) returns error`() {
        val input = "12345"
        val result = validateLoginInputUseCase(input)

        assertFalse(result.successful)
        assertEquals("NIP/NIK berisi 16 atau 18 digit", result.errorMessage)
    }

    @Test
    fun `invoke with invalid numeric length (too long) returns error`() {
        val input = "12345678901234567890"
        val result = validateLoginInputUseCase(input)

        assertFalse(result.successful)
        assertEquals("NIP/NIK berisi 16 atau 18 digit", result.errorMessage)
    }

    @Test
    fun `invoke with valid email returns success`() {
        val input = "test@example.com"
        val result = validateLoginInputUseCase(input)

        assertTrue(result.successful)
    }

    @Test
    fun `invoke with valid email containing numbers and dots returns success`() {
        val input = "test.name123@sub.domain.co.id"
        val result = validateLoginInputUseCase(input)

        assertTrue(result.successful)
    }

    @Test
    fun `invoke with invalid email (no domain) returns error`() {
        val input = "test@"
        val result = validateLoginInputUseCase(input)

        assertFalse(result.successful)
        assertEquals("Format email tidak valid", result.errorMessage)
    }

    @Test
    fun `invoke with invalid email (no tld) returns error`() {
        val input = "test@domain"
        val result = validateLoginInputUseCase(input)

        assertFalse(result.successful)
        assertEquals("Format email tidak valid", result.errorMessage)
    }

    @Test
    fun `invoke with invalid email (no username) returns error`() {
        val input = "@domain.com"
        val result = validateLoginInputUseCase(input)

        assertFalse(result.successful)
        assertEquals("Format email tidak valid", result.errorMessage)
    }

    @Test
    fun `invoke with alphanumeric username (no digits only, no @) returns error`() {
        val input = "user123"
        val result = validateLoginInputUseCase(input)

        assertFalse(result.successful)
        assertEquals("Format tidak dikenali (Gunakan Email atau NIP/NIK)", result.errorMessage)
    }

    @Test
    fun `invoke with random string returns error`() {
        val input = "Fani"
        val result = validateLoginInputUseCase(input)

        assertFalse(result.successful)
        assertEquals("Format tidak dikenali (Gunakan Email atau NIP/NIK)", result.errorMessage)
    }
}