package com.dishut_lampung.sitanihut.domain.usecase.user_management

import com.dishut_lampung.sitanihut.domain.model.UserDetail
import com.dishut_lampung.sitanihut.domain.repository.UserRepository
import com.dishut_lampung.sitanihut.util.Resource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class GetUserListUseCase @Inject constructor(
    private val repository: UserRepository
) {
    operator fun invoke(role: String, query: String = ""): Flow<Resource<List<UserDetail>>> {
        if (!isAuthorized(role)) {
            return flow {
                emit(Resource.Error("Anda tidak memiliki akses untuk melihat data KTH."))
            }
        }
        return repository.getUserList(query)
    }

    fun isAuthorized(role: String?): Boolean {
        val authorizedRoles = listOf("penyuluh", "penanggung jawab", "penanggung-jawab")
        return role?.lowercase() in authorizedRoles
    }
}