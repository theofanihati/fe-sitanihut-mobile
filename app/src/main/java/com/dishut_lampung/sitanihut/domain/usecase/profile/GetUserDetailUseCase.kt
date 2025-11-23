package com.dishut_lampung.sitanihut.domain.usecase.profile

import com.dishut_lampung.sitanihut.domain.model.Result
import com.dishut_lampung.sitanihut.domain.model.UserDetail
import com.dishut_lampung.sitanihut.domain.repository.ProfileRepository
import com.dishut_lampung.sitanihut.util.Resource
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetUserDetailUseCase @Inject constructor(
) {
    operator fun invoke(userId: String): Flow<Resource<UserDetail>> {
        TODO("blum buund")
    }
}