package com.dishut_lampung.sitanihut.domain.repository

import com.dishut_lampung.sitanihut.domain.model.CreateUserInput
import com.dishut_lampung.sitanihut.domain.model.UserDetail
import com.dishut_lampung.sitanihut.util.Resource
import kotlinx.coroutines.flow.Flow

interface UserRepository {
    fun getUserList(query: String): Flow<Resource<List<UserDetail>>>
    suspend fun deleteUser(id: String): Resource<Unit>
    suspend fun syncUserData(): Resource<Unit>
    fun getUserDetail(id: String): Flow<Resource<UserDetail>>
    suspend fun createUser(input: CreateUserInput): Resource<Unit>
    suspend fun updateUser(id: String, changes: Map<String, Any?>): Resource<Unit>
    suspend fun syncFcmToken(token: String): Resource<Unit>
}