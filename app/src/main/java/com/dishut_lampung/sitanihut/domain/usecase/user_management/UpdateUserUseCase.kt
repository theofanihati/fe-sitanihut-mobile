package com.dishut_lampung.sitanihut.domain.usecase.user_management

import com.dishut_lampung.sitanihut.domain.repository.UserRepository
import com.dishut_lampung.sitanihut.util.Resource
import javax.inject.Inject

class UpdateUserUseCase @Inject constructor(
    private val repository: UserRepository
) {
    suspend operator fun invoke(id: String, changes: Map<String, Any?>): Resource<Unit> {
        return repository.updateUser(id, changes)
    }
}