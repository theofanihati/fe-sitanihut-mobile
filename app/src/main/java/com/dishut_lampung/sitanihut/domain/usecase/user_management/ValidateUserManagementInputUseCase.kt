package com.dishut_lampung.sitanihut.domain.usecase.user_management

import com.dishut_lampung.sitanihut.domain.model.CreateUserInput
import com.dishut_lampung.sitanihut.domain.validator.ListValidationResult
import javax.inject.Inject

class ValidateUserManagementInputUseCase @Inject constructor() {
    fun execute(input: CreateUserInput): ListValidationResult {
        return TODO()
    }
}
