package com.dishut_lampung.sitanihut.domain.usecase.petani

import com.dishut_lampung.sitanihut.domain.model.CreatePetaniInput
import com.dishut_lampung.sitanihut.domain.validator.ListValidationResult
import javax.inject.Inject

class ValidatePetaniInputUseCase @Inject constructor() {
    fun execute(input: CreatePetaniInput): ListValidationResult {
        return TODO()
    }
}