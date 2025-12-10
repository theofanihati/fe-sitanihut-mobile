package com.dishut_lampung.sitanihut.domain.validator

class ValidationResult (
    val successful: Boolean,
    val errorMessage: String? = null
)

data class ListValidationResult(
    val successful: Boolean,
    val errorMessage: String? = null,
    val fieldErrors: Map<String, String> = emptyMap()
)