package com.dishut_lampung.sitanihut.data.repository

import com.dishut_lampung.sitanihut.data.local.UserPreferences
import com.dishut_lampung.sitanihut.data.local.dao.ReportDao
import com.dishut_lampung.sitanihut.data.local.dao.UserDao
import com.dishut_lampung.sitanihut.data.remote.api.AuthApiService
import com.dishut_lampung.sitanihut.data.remote.api.UserApiService
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
    private lateinit var userApiService: UserApiService
    private val mockUserPreferences: UserPreferences = mockk(relaxed = true)
    private val mockReportDao: ReportDao = mockk(relaxed = true)
    private val mockUserDao: UserDao = mockk(relaxed = true)

    @Before
    fun setUp() {
        mockWebServer = MockWebServer()
        apiService = Retrofit.Builder()
            .baseUrl(mockWebServer.url("/api/"))
            .addConverterFactory(GsonConverterFactory.create(Gson()))
            .build()
            .create(AuthApiService::class.java)
        userApiService = Retrofit.Builder()
            .baseUrl(mockWebServer.url("/api/"))
            .addConverterFactory(GsonConverterFactory.create(Gson()))
            .build()
            .create(UserApiService::class.java)

        authRepository = AuthRepositoryImpl(apiService, userApiService, mockUserPreferences, mockReportDao, mockUserDao)
    }

    @After
    fun tearDown() {
        mockWebServer.shutdown()
    }

    // BERHASIL POST v1/login
    @Test
    fun `login success, should return Success result and save token AND complete user data to DB`() = runTest {
        val loginResponseJson = """
            {
                "message": "Berhasil masuk",
                "statusCode": 200,
                "data": {
                    "token": "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJpc3MiOiJodHRwOi8vMTI3LjAuMC4xOjgwMDAvYXBpL3YxL2xvZ2luIiwiaWF0IjoxNzU1OTM2NzMwLCJleHAiOjE3NTU5NzI3MzAsIm5iZiI6MTc1NTkzNjczMCwianRpIjoiVUtOMlk2Mm9USVF3VlF2cSIsInN1YiI6IjAxOThkNWQ0LWI5YjctNzFmMS04YzAyLThjMjQ5MzAzNzcyZiIsInBydiI6IjIzYmQ1Yzg5NDlmNjAwYWRiMzllNzAxYzQwMDg3MmRiN2E1OTc2ZjciLCJyb2xlIjoicGV0YW5pIn0.bPAXLiV2FYZSP92wnsVDucCIS-FbtEH4kPg945wBUxg",
                    "id": "user-001",
                    "email": "petani2@example.com",
                    "role": "Petani",
                    "name": "Petani Dua"
                }
            }
        """.trimIndent()
        val userDetailResponseJson = """
            {
                "message": "Success",
                "statusCode": 200,
                "data": {
                    "id_user": "user-001",
                    "email": "petani@baru.com",
                    "id_role": "0198d5d4-b007-71d0-b0ca-4b46be02f40b",
                    "id_kph": "0198d5d4-b025-73a3-bc01-663f2adee7d6",
                    "nama_kph": "UPTD KPH Kota Agung Utara",
                    "id_kth": "0198d5d4-b068-703b-aee7-cbcfabedcd69",
                    "nama_kth": "KTH Curah Sejuk",
                    "id_detail": "0198db13-c6fe-7047-a175-de5cb71dbcc7",
                    "nama_user": "Petani Dua",
                    "nomor_induk": "1234567890111",
                    "jenis_kelamin": "wanita",
                    "profile_picture_url": "http://127.0.0.1:8000/storage/profile_pictures/0198db13-c6ef-73d4-b3a5-9a400cafae8d_j2GFhzoJ_Screenshot2025-07-06210849.png",
                    "alamat": null,
                    "nomor_wa": null,
                    "pendidikan_terakhir": null,
                    "pekerjaan_sampingan": null,
                    "luas_lahan": null,
                    "jabatan": null
                }
            }
        """.trimIndent()

        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(200)
                .setBody(loginResponseJson)
        )
        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(200)
                .setBody(userDetailResponseJson)
        )

        val result = authRepository.login("petani2@example.com", "passwordbenar")
        assertTrue(result is AuthResult.Success)
        coVerify(exactly = 1) { mockUserPreferences.saveAuthToken("eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJpc3MiOiJodHRwOi8vMTI3LjAuMC4xOjgwMDAvYXBpL3YxL2xvZ2luIiwiaWF0IjoxNzU1OTM2NzMwLCJleHAiOjE3NTU5NzI3MzAsIm5iZiI6MTc1NTkzNjczMCwianRpIjoiVUtOMlk2Mm9USVF3VlF2cSIsInN1YiI6IjAxOThkNWQ0LWI5YjctNzFmMS04YzAyLThjMjQ5MzAzNzcyZiIsInBydiI6IjIzYmQ1Yzg5NDlmNjAwYWRiMzllNzAxYzQwMDg3MmRiN2E1OTc2ZjciLCJyb2xlIjoicGV0YW5pIn0.bPAXLiV2FYZSP92wnsVDucCIS-FbtEH4kPg945wBUxg") }
        coVerify { mockUserPreferences.saveUserId("user-001") }
        coVerify { mockUserPreferences.saveUserName("Petani Dua") }
        coVerify { mockUserPreferences.saveUserRole("petani") }
        coVerify(exactly = 1) { mockUserDao.upsertUser(any()) }
    }
    @Test
    fun `login success BUT fetchUserDetails fails, should save BASIC user data to DB`() = runTest {
        val loginResponse = """
            {
                "message": "Berhasil", 
                "statusCode": 200,
                "data": {
                    "token": "token123", 
                    "id": "user-123", 
                    "email": "petani@ex.com", 
                    "role": "petani", 
                    "name": "Pak Tani"
                }
            }
        """.trimIndent()

        mockWebServer.enqueue(MockResponse().setResponseCode(200).setBody(loginResponse))
        mockWebServer.enqueue(MockResponse().setResponseCode(500).setBody("{}"))

        val result = authRepository.login("petani@ex.com", "pass")
        assertTrue(result is AuthResult.Success)
        coVerify(exactly = 1) { mockUserDao.upsertUser(any()) }
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

    @Test
    fun `logout success, should call api and clear local data`() = runTest {
        val fcmToken = "sample-fcm-token"
        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(200)
                .setBody("""{"message": "Logged out successfully"}""")
        )

        authRepository.logout(fcmToken)

        val recordedRequest = mockWebServer.takeRequest()
        assertEquals("/api/v1/logout", recordedRequest.path)

        coVerify(exactly = 1) { mockUserPreferences.clearAllSession() }
        coVerify(exactly = 1) { mockReportDao.clearAllLaporan() }
    }

    @Test
    fun `logout fails on API, should STILL clear local data (finally block)`() = runTest {
        val fcmToken = "sample-fcm-token"
        mockWebServer.enqueue(MockResponse().setResponseCode(500))
        authRepository.logout(fcmToken)
        coVerify(exactly = 1) { mockUserPreferences.clearAllSession() }
        coVerify(exactly = 1) { mockReportDao.clearAllLaporan() }
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