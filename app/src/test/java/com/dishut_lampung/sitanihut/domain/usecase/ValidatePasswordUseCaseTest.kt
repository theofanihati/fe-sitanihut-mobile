package com.dishut_lampung.sitanihut.domain.usecase

import com.dishut_lampung.sitanihut.domain.use_case.auth.ValidatePasswordUseCase
import org.junit.Assert.assertEquals
import org.junit.Test

class ValidatePasswordUseCaseTest {
    private val validatePasswordUseCase = ValidatePasswordUseCase()

    @Test
    fun `Password is blank, returns error`() {
        val result = validatePasswordUseCase("")
        assertEquals(false, result.successful)
        assertEquals("Password tidak boleh kosong", result.errorMessage)
    }

    @Test
    fun `Password length lower than 8, returns error`() {
        val result = validatePasswordUseCase("Min8#")
        assertEquals(false, result.successful)
        assertEquals("Password minimal 8 karakter", result.errorMessage)
    }

    @Test
    fun `Password does not contain Kapital, returns error`() {
        val result = validatePasswordUseCase("password8$")
        assertEquals(false, result.successful)
        assertEquals("Password harus memiliki setidaknya satu huruf besar (kapital)", result.errorMessage)
    }

    @Test
    fun `Password does not contain lowercase, returns error`() {
        val result = validatePasswordUseCase("PASSWORD8$")
        assertEquals(false, result.successful)
        assertEquals("Password harus memiliki setidaknya satu huruf kecil", result.errorMessage)
    }

    @Test
    fun `Password does not contain number, returns error`() {
        val result = validatePasswordUseCase("Password$")
        assertEquals(false, result.successful)
        assertEquals("Password harus memiliki setidaknya satu angka", result.errorMessage)
    }

    @Test
    fun `Password does not contain special character, returns error`() {
        val result = validatePasswordUseCase("Pasword278")
        assertEquals(false, result.successful)
        assertEquals("Password harus memiliki setidaknya satu karakter spesial (!@#$...)", result.errorMessage)
    }

    @Test
    fun `Password is valid, returns success`() {
        val result = validatePasswordUseCase("Petani123#")
        assertEquals(true, result.successful)
        assertEquals(null, result.errorMessage)
    }
}