package com.dishut_lampung.sitanihut.presentation.kth.detail

import com.dishut_lampung.sitanihut.domain.model.Kth

data class KthDetailUiState (
    val isLoading: Boolean = false,
    val kth: Kth? = null,
    val error: String? = null
)