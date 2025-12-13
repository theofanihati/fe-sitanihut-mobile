package com.dishut_lampung.sitanihut.data.repository

import retrofit2.HttpException
import com.dishut_lampung.sitanihut.data.local.UserPreferences
import com.dishut_lampung.sitanihut.data.local.dao.ReportDao
import com.dishut_lampung.sitanihut.data.local.dao.UserDao
import com.dishut_lampung.sitanihut.data.local.entity.UserEntity
import com.dishut_lampung.sitanihut.data.remote.api.AuthApiService
import com.dishut_lampung.sitanihut.data.remote.api.UserApiService
import com.dishut_lampung.sitanihut.data.remote.dto.AuthDto
import com.dishut_lampung.sitanihut.domain.model.AuthResult
import com.dishut_lampung.sitanihut.domain.model.User
import com.dishut_lampung.sitanihut.domain.repository.AuthRepository
import com.google.gson.Gson
import java.io.IOException
import javax.inject.Inject

class AuthRepositoryImpl @Inject constructor(
    private val apiService: AuthApiService,
    private val userApiService: UserApiService,
    private val userPreferences: UserPreferences,
    private val reportDao: ReportDao,
    private val userDao: UserDao
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

                saveSession(token, role, name, id)
                fetchAndSaveUserDetails(id, name, role, emailResp)

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
        } catch (e: Exception) {
            handleException(e)
        }
    }

    private suspend fun saveBasicUserToDb(id: String, name: String, role: String, email: String, pic: String?) {
        val basicUser = UserEntity(
            id = id,
            name = name,
            role = role,
            email = email,
            profilePictureUrl = pic
        )
        userDao.upsertUser(basicUser)
    }

    override suspend fun requestPasswordReset(email: String): AuthResult<Unit> {
        return try {
            val request = AuthDto.ForgotPasswordRequest(email)
            val response = apiService.requestPasswordReset(request)
            if (response.isSuccessful) {
                AuthResult.Success(Unit)
            } else {
                val errorMessage = parseErrorBody(response.code(), response.errorBody()?.string())
                AuthResult.Error(errorMessage)
            }
        } catch (e: Exception) {
            handleException(e)
        }
    }

    override suspend fun logout() {
        userPreferences.clearAllSession()
        reportDao.clearAllLaporan()
    }

    override suspend fun isLoggedIn(): Boolean {
        return userPreferences.getAuthToken() != null
    }

    private suspend fun saveSession(token: String, role: String, name: String, id: String) {
        userPreferences.apply {
            saveAuthToken(token)
            saveUserRole(role)
            saveUserName(name)
            saveUserId(id)
        }
    }

    private fun <T> handleException(e: Exception): AuthResult<T> {
        return when (e) {
            is HttpException -> {
                val errorBody = e.response()?.errorBody()?.string()
                AuthResult.Error(parseErrorBody(e.code(), errorBody))
            }
            is IOException -> AuthResult.Error("Tidak ada koneksi internet. Silakan periksa jaringan Anda.")
            else -> AuthResult.Error(e.message ?: "Terjadi kesalahan yang tidak diketahui.")
        }
    }

    private fun parseErrorBody(code: Int, errorBody: String?): String {
        return try {
            val errorResponse = Gson().fromJson(errorBody, AuthDto.ErrorResponse::class.java)
            if (errorResponse.message != null && errorResponse.message != "failed") {
                errorResponse.message
            } else {
                getReadableErrorMessage(code, errorBody)
            }
        } catch (e: Exception) {
            getReadableErrorMessage(code, errorBody)
        }
    }

    private suspend fun fetchAndSaveUserDetails(id: String, name: String, role: String, email: String) {
        try {
            val userDetailResponse = userApiService.getUserDetail(id)
            if (userDetailResponse.statusCode == 200 && userDetailResponse.data != null) {
                val dto = userDetailResponse.data
                val userEntity = UserEntity(
                    id = dto.id, name = dto.name, role = role, email = dto.email,
                    profilePictureUrl = dto.profilePictureUrl, roleId = dto.roleId,
                    kphId = dto.kphId, kphName = dto.kphName, kthId = dto.kthId, kthName = dto.kthName,
                    identityNumber = dto.identityNumber, gender = dto.gender, address = dto.address,
                    whatsAppNumber = dto.whatsAppNumber, lastEducation = dto.lastEducation,
                    sideJob = dto.sideJob, landArea = dto.landArea, position = dto.position
                )
                userDao.upsertUser(userEntity)
            } else {
                saveBasicUserToDb(id, name, role, email, null)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            saveBasicUserToDb(id, name, role, email, null)
        }
        }

    private fun getReadableErrorMessage(code: Int, rawBody: String?): String {
        return when (code) {
            400 -> "Format data tidak valid."
            401 -> "Sesi berakhir atau kredensial salah."
            403 -> "Akses ditolak. Silakan login kembali."
            404 -> "Data tidak ditemukan."
            500 -> "Terdapat gangguan server."
            else -> rawBody ?: "Terjadi kesalahan pada server. Kode: $code"
        }
    }
}
