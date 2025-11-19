package com.dishut_lampung.sitanihut.data.repository

import com.dishut_lampung.sitanihut.data.local.UserPreferences
import com.dishut_lampung.sitanihut.data.local.dao.ReportDao
import com.dishut_lampung.sitanihut.data.remote.AuthApiService
import com.dishut_lampung.sitanihut.domain.model.AuthResult
import com.google.gson.Gson
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertTrue
import org.junit.Assert.assertFalse
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class AuthRepositoryImplTest {
    private lateinit var authRepository: AuthRepositoryImpl
    private lateinit var mockWebServer: MockWebServer
    private lateinit var apiService: AuthApiService
    private val mockUserPreferences: UserPreferences = mockk(relaxed = true)
    private val mockReportDao: ReportDao = mockk(relaxed = true)

    @Before
    fun setUp() {
        mockWebServer = MockWebServer()
        apiService = Retrofit.Builder()
            .baseUrl(mockWebServer.url("/api/"))
            .addConverterFactory(GsonConverterFactory.create(Gson()))
            .build()
            .create(AuthApiService::class.java)

        authRepository = AuthRepositoryImpl(apiService, mockUserPreferences, mockReportDao)
    }

    @After
    fun tearDown() {
        mockWebServer.shutdown()
    }

    // BERHASIL POST v1/login
    @Test
    fun `login success, should return Success result and save token`() = runTest {
        val successResponseJson = """
            {
                "message": "Berhasil masuk",
                "statusCode": 200,
                "data": {
                    "token": "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJpc3MiOiJodHRwOi8vMTI3LjAuMC4xOjgwMDAvYXBpL3YxL2xvZ2luIiwiaWF0IjoxNzU1OTM2NzMwLCJleHAiOjE3NTU5NzI3MzAsIm5iZiI6MTc1NTkzNjczMCwianRpIjoiVUtOMlk2Mm9USVF3VlF2cSIsInN1YiI6IjAxOThkNWQ0LWI5YjctNzFmMS04YzAyLThjMjQ5MzAzNzcyZiIsInBydiI6IjIzYmQ1Yzg5NDlmNjAwYWRiMzllNzAxYzQwMDg3MmRiN2E1OTc2ZjciLCJyb2xlIjoicGV0YW5pIn0.bPAXLiV2FYZSP92wnsVDucCIS-FbtEH4kPg945wBUxg",
                    "id": "0198d5d4-b9b7-71f1-8c02-8c249303772f",
                    "email": "petani2@example.com",
                    "role": "Petani",
                    "name": "Petani Dua"
                }
            }
        """.trimIndent()

        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(200)
                .setBody(successResponseJson)
        )

        val result = authRepository.login("petani2@example.com", "passwordbenar")
        assertTrue(result is AuthResult.Success)
        coVerify(exactly = 1) { mockUserPreferences.saveAuthToken("eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJpc3MiOiJodHRwOi8vMTI3LjAuMC4xOjgwMDAvYXBpL3YxL2xvZ2luIiwiaWF0IjoxNzU1OTM2NzMwLCJleHAiOjE3NTU5NzI3MzAsIm5iZiI6MTc1NTkzNjczMCwianRpIjoiVUtOMlk2Mm9USVF3VlF2cSIsInN1YiI6IjAxOThkNWQ0LWI5YjctNzFmMS04YzAyLThjMjQ5MzAzNzcyZiIsInBydiI6IjIzYmQ1Yzg5NDlmNjAwYWRiMzllNzAxYzQwMDg3MmRiN2E1OTc2ZjciLCJyb2xlIjoicGV0YW5pIn0.bPAXLiV2FYZSP92wnsVDucCIS-FbtEH4kPg945wBUxg") }
    }

    // GAGAL POST v1/login
    @Test
    fun `login with empty fields, when api returns 400, should return Error result`() = runTest {
        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(400)
                .setBody("""{"message": "Email dan password harus diisi."}""")
        )

        val result = authRepository.login("", "")

        assertTrue(result is AuthResult.Error)
        assertEquals("Email dan password harus diisi.", (result as AuthResult.Error).message)
        coVerify(exactly = 0) { mockUserPreferences.saveAuthToken(any()) }
    }
    @Test
    fun `login with invalid credentials, when api returns 401, should return Error result`() = runTest {
        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(401)
                .setBody("""{"message": "Email atau password salah."}""")
        )

        val email = "petanisalah@example.com"
        val password = "passwordsalah"
        val result = authRepository.login(email, password)

        assertTrue(result is AuthResult.Error)
        assertEquals("Email atau password salah.", (result as AuthResult.Error).message)
        coVerify(exactly = 0) { mockUserPreferences.saveAuthToken(any()) }
    }
    @Test
    fun `login with expired session, when api returns 403, should return Error result`() = runTest {
        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(403)
                .setBody("""{"message": "Akses ditolak."}""")
        )

        val result = authRepository.login("email@example.com", "password")

        assertTrue(result is AuthResult.Error)
        assertEquals("Akses ditolak.", (result as AuthResult.Error).message)
        coVerify(exactly = 0) { mockUserPreferences.saveAuthToken(any()) }
    }
    @Test
    fun `login with wrong endpoint, when api returns 404, should return Error result`() = runTest {
        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(404)
                .setBody("""{"message": "Endpoint tidak ditemukan."}""")
        )

        val result = authRepository.login("email@example.com", "password")

        // Assert
        assertTrue(result is AuthResult.Error)
        assertEquals("Endpoint tidak ditemukan.", (result as AuthResult.Error).message)
        coVerify(exactly = 0) { mockUserPreferences.saveAuthToken(any()) }
    }
    @Test
    fun `login with server issue, when api returns 500, should return Error result`() = runTest {
        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(500)
                .setBody("""{"message": "Terdapat gangguan server."}""")
        )

        val result = authRepository.login("email@example.com", "password")

        assertTrue(result is AuthResult.Error)
        assertEquals("Terdapat gangguan server.", (result as AuthResult.Error).message)
        coVerify(exactly = 0) { mockUserPreferences.saveAuthToken(any()) }
    }

    // I/O EXCEPTION
    @Test
    fun `login with network error, should return IO Error result`() = runTest {
        mockWebServer.shutdown()
        val email = "petani@example.com"
        val password = "password"
        val result = authRepository.login(email, password)

        assertTrue(result is AuthResult.Error)
        assertEquals("Tidak ada koneksi internet. Silakan periksa jaringan Anda.", (result as AuthResult.Error).message)
        coVerify(exactly = 0) { mockUserPreferences.saveAuthToken(any()) }
    }

    // LOGOUT
    @Test
    fun `logout, should call clearAuthToken`() = runTest {
        authRepository.logout()
        coVerify(exactly = 1) { mockUserPreferences.clearAllSession() }
    }

    // token save di datastore
    @Test
    fun `isLoggedIn when token exists, should return true`() = runTest {
        io.mockk.coEvery { mockUserPreferences.getAuthToken() } returns "some-valid-token"

        val isLoggedIn = authRepository.isLoggedIn()

        assertTrue(isLoggedIn)
    }

    // token ga di save di datastore
    @Test
    fun `isLoggedIn when token does not exist, should return false`() = runTest {
        io.mockk.coEvery { mockUserPreferences.getAuthToken() } returns null

        val isLoggedIn = authRepository.isLoggedIn()

        assertFalse(isLoggedIn)
    }

    //    BERHASIL POST v1/forgot-password
    @Test
    fun `requestPasswordReset success, should return Success result`() = runTest {
        val successJson = """
        {
            "message": "Email verifikasi telah dikirim",
            "statusCode": 200,
            "data": []
        }
    """.trimIndent()
        mockWebServer.enqueue(MockResponse().setResponseCode(200).setBody(successJson))

        val result = authRepository.requestPasswordReset("email@example.com")

        assertTrue(result is AuthResult.Success)
    }

    //    GAGAL POST v1/forgot-password  | 404 email gaada
    @Test
    fun `requestPasswordReset with unregistered email, when api returns 404, should return Error result`() = runTest {
        mockWebServer.enqueue(MockResponse().setResponseCode(404).setBody("""{"message":"Email tidak ditemukan"}"""))

        val result = authRepository.requestPasswordReset("tidak.terdaftar@example.com")

        assertTrue(result is AuthResult.Error)
    }
}