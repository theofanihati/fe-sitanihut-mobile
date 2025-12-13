package com.dishut_lampung.sitanihut.domain.repository

import com.dishut_lampung.sitanihut.domain.model.UserDetail
import com.dishut_lampung.sitanihut.util.Resource
import kotlinx.coroutines.flow.Flow

interface ProfileRepository {
    fun getUserDetail(userId: String): Flow<Resource<UserDetail>>
    suspend fun syncUserDetail(): Resource<Unit>
}