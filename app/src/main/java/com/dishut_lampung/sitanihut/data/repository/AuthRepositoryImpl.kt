package com.dishut_lampung.sitanihut.data.repository

import retrofit2.HttpException
import com.dishut_lampung.sitanihut.data.local.UserPreferences
import com.dishut_lampung.sitanihut.data.remote.AuthApiService
import com.dishut_lampung.sitanihut.data.remote.dto.AuthDto
import com.dishut_lampung.sitanihut.domain.model.AuthResult
import com.dishut_lampung.sitanihut.domain.model.User
import com.dishut_lampung.sitanihut.domain.repository.AuthRepository
import com.google.gson.Gson
import java.io.IOException
import javax.inject.Inject

class AuthRepositoryImpl @Inject constructor(
    private val apiService: AuthApiService,
    private val userPreferences: UserPreferences,
) : AuthRepository {

    override suspend fun login(email: String, password: String): AuthResult<User> {
        return try {
            val request = AuthDto.LoginRequest(email, password)
            val response = apiService.login(request)

            if (response.statusCode == 200 && response.data.isJsonObject) {

                val dataObject = response.data.asJsonObject

                val token = dataObject.get("token").asString
                val role = dataObject.get("role").asString.lowercase()
                val id = dataObject.get("id").asString
                val name = dataObject.get("name").asString
                val emailResp = dataObject.get("email").asString

                userPreferences.saveAuthToken(token)
                userPreferences.saveUserRole(role)
                userPreferences.saveUserName(name)

                AuthResult.Success(
                    data = User(
                        id = id,
                        name = name,
                        token = token,
                        role = role,
                        email = emailResp
                    )
                )
            } else {
                AuthResult.Error(response.message)
            }
        } catch (e: HttpException) {
            val errorBody = e.response()?.errorBody()?.string()
            val errorMessage = try {
                val errorResponse = Gson().fromJson(errorBody, AuthDto.ErrorResponse::class.java)
                errorResponse.message ?: "Terjadi kesalahan (pesan tidak ada)"
            } catch (jsonError: Exception) {
                when (e.code()) {
                    400 -> "Email dan password harus diisi."
                    401 -> "Email atau password salah."
                    403 -> "Akses ditolak. Silakan login kembali."
                    404 -> "Endpoint tidak ditemukan."
                    500 -> "Terdapat gangguan server."
                    else -> "Terjadi kesalahan pada server. Kode: ${e.code()}"
                }
            }
            AuthResult.Error(errorMessage)
        } catch (e: IOException) {
            AuthResult.Error("Tidak ada koneksi internet. Silakan periksa jaringan Anda.")
        } catch (e: Exception) {
            AuthResult.Error(e.message ?: "Terjadi kesalahan yang tidak diketahui.")
        }
    }

    override suspend fun requestPasswordReset(email: String): AuthResult<Unit> {
        return try {
            val request = AuthDto.ForgotPasswordRequest(email)
            apiService.requestPasswordReset(request)

            AuthResult.Success(Unit)
        } catch (e: HttpException) {
            val errorBody = e.response()?.errorBody()?.string()
            val errorMessage = try {
                val errorResponse = Gson().fromJson(errorBody, AuthDto.ErrorResponse::class.java)
                val responseFailed = "failed"
                if(errorResponse.message != responseFailed){
                    errorResponse.message ?: "Terjadi kesalahan (pesan tidak ada)"
                } else{
                    when (e.code()) {
                        400 -> "Email harus diisi."
                        401 -> "Email salah."
                        403 -> "Akses ditolak. Silakan login kembali."
                        404 -> "Email tidak ditemukan."
                        500 -> "Terdapat gangguan server."
                        else -> errorBody ?: "Terjadi kesalahan pada server. Kode: ${e.code()}"
                    }
                }
            } catch (jsonError: Exception) {
                when (e.code()) {
                    400 -> "Email harus diisi."
                    401 -> "Email salah."
                    403 -> "Akses ditolak. Silakan login kembali."
                    404 -> "Email tidak ditemukan."
                    500 -> "Terdapat gangguan server."
                    else -> errorBody ?: "Terjadi kesalahan pada server. Kode: ${e.code()}"
                }
            }
            AuthResult.Error(errorMessage)
        } catch (e: IOException) {
            AuthResult.Error("Tidak ada koneksi internet. Silakan periksa jaringan Anda.")
        } catch (e: Exception) {
            AuthResult.Error(e.message ?: "Terjadi kesalahan yang tidak diketahui.")
        }
    }

    override suspend fun logout() {
        userPreferences.clearAuthToken()
        userPreferences.clearUserRole()
    }

    override suspend fun isLoggedIn(): Boolean {
        return userPreferences.getAuthToken() != null
    }
}
