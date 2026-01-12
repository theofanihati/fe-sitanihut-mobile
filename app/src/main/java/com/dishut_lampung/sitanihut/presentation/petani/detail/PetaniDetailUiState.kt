package com.dishut_lampung.sitanihut.presentation.petani.detail

import com.dishut_lampung.sitanihut.domain.model.Petani

data class PetaniDetailUiState (
    val isLoading: Boolean = false,
    val petani: Petani? = null,
    val error: String? = null,
    val userRole: String = "",
)