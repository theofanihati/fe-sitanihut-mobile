package com.dishut_lampung.sitanihut.presentation.kth

import com.dishut_lampung.sitanihut.domain.model.Kth

data class KthUiState(
    val isLoading: Boolean = false,
    val kthList: List<Kth> = emptyList(),
    val error: String? = null,
    val query: String = "",
    val isOnline: Boolean = true
)

sealed class KthEvent {
    data class OnSearchQueryChange(val query: String) : KthEvent()
    data object OnRefresh : KthEvent()
    data object OnDismissError : KthEvent()
}