package com.dishut_lampung.sitanihut.domain.usecase.role

import com.dishut_lampung.sitanihut.domain.model.Role
import com.dishut_lampung.sitanihut.domain.repository.RoleRepository
import com.dishut_lampung.sitanihut.util.Resource
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetRolesUseCase @Inject constructor(
    private val repository: RoleRepository
) {
    operator fun invoke(): Flow<Resource<List<Role>>> {
        return TODO()
    }
}