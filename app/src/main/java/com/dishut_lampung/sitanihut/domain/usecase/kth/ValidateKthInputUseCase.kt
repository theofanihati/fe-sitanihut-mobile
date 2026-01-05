package com.dishut_lampung.sitanihut.domain.usecase.kth

import com.dishut_lampung.sitanihut.domain.model.CreateKthInput
import com.dishut_lampung.sitanihut.domain.validator.ListValidationResult
import javax.inject.Inject

class ValidateKthInputUseCase @Inject constructor() {
    private val phoneRegex = Regex("^(08[0-9]{8,14}|\\+628[0-9]{8,14})$")

    fun execute(input: CreateKthInput): ListValidationResult{
        return TODO()
    }
}