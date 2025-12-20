package com.dishut_lampung.sitanihut.presentation.penyuluh

import com.dishut_lampung.sitanihut.domain.model.Penyuluh

data class PenyuluhUiState(
    val isLoading: Boolean = false,
    val penyuluhList: List<Penyuluh> = emptyList(),
    val error: String? = null,
    val isRefreshing: Boolean = false
)

sealed class PenyuluhEvent {
    data object OnRefresh : PenyuluhEvent()
    data object OnDismissError : PenyuluhEvent()
}