package com.dishut_lampung.sitanihut.data.remote.api

import com.dishut_lampung.sitanihut.data.remote.dto.CreateUserRequestDto
import com.dishut_lampung.sitanihut.data.remote.dto.RoleDto
import com.dishut_lampung.sitanihut.data.remote.dto.UserDetailDto
import com.dishut_lampung.sitanihut.data.remote.dto.UserListItemDto
import com.dishut_lampung.sitanihut.data.remote.response.ApiResponse
import com.dishut_lampung.sitanihut.data.remote.response.PaginatedData
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface UserApiService {
    @GET("v1/role")
    suspend fun getRoles(): ApiResponse<List<RoleDto>>

    @GET("v1/users")
    suspend fun getUserList(
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 50,
        @Query("search") search: String? = null
    ): ApiResponse<PaginatedData<UserListItemDto>>

    @GET("v1/users/{id}")
    suspend fun getUserDetail(
        @Path("id") id: String
    ): ApiResponse<UserDetailDto>

    @POST("v1/users")
    suspend fun createUser(
        @Body request: CreateUserRequestDto
    ): ApiResponse<UserDetailDto>

    @PATCH("v1/users/{id}")
    suspend fun updateUser(
        @Path("id") id: String,
        @Body request: Map<String, @JvmSuppressWildcards Any?>
    ): ApiResponse<Unit>

    @DELETE("v1/users/{id}")
    suspend fun deleteUser(
        @Path("id") id: String
    ): ApiResponse<Unit>
}