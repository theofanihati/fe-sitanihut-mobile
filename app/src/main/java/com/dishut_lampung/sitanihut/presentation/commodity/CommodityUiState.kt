package com.dishut_lampung.sitanihut.presentation.commodity

import com.dishut_lampung.sitanihut.domain.model.Commodity

data class CommodityUiState (
    val items: List<Commodity> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val query: String = "",

    val successMessage: String? = null,
    val errorMessage: String? = null
)

sealed class CommodityEvent {
    data class OnSearchQueryChange(val query: String) : CommodityEvent()
    object OnRefresh : CommodityEvent()

    object OnDismissError : CommodityEvent()
    object OnDismissSuccessMessage : CommodityEvent()
}