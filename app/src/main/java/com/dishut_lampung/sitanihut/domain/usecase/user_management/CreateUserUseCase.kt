package com.dishut_lampung.sitanihut.domain.usecase.user_management

import com.dishut_lampung.sitanihut.domain.model.CreateUserInput
import com.dishut_lampung.sitanihut.domain.repository.UserRepository
import com.dishut_lampung.sitanihut.util.Resource
import javax.inject.Inject

class CreateUserUseCase  @Inject constructor(
    private val repository: UserRepository
) {
    suspend operator fun invoke(input: CreateUserInput): Resource<Unit> {
        return TODO()
    }
}