package com.dishut_lampung.sitanihut.presentation.penyuluh

import com.dishut_lampung.sitanihut.domain.model.Penyuluh
import com.dishut_lampung.sitanihut.presentation.user_management.list.UserEvent

data class PenyuluhUiState(
    val isLoading: Boolean = false,
    val penyuluhList: List<Penyuluh> = emptyList(),
    val errorMessage: String? = null,
    val successMessage: String? = null,
    val isRefreshing: Boolean = false,
    val searchQuery: String = "",
)

sealed class PenyuluhEvent {
    data class OnSearchQueryChange(val query: String) : PenyuluhEvent()
    data object OnRefresh : PenyuluhEvent()
    data object OnDismissError : PenyuluhEvent()
    object OnExportList : PenyuluhEvent()
}