package com.dishut_lampung.sitanihut.presentation.petani.list

import com.dishut_lampung.sitanihut.domain.model.Petani
import com.dishut_lampung.sitanihut.presentation.shared.components.animations.MessageType

data class PetaniListUiState(
    val isLoading: Boolean = false,
    val query: String = "",
    val isRefreshing: Boolean = false,
    val userRole: String = "",
    val petaniList: List<Petani> = emptyList(),

    val isOnline: Boolean = true,
    val isBottomSheetVisible: Boolean = false,
    val selectedPetaniId: String? = null,
    val isDeleteDialogVisible: Boolean = false,

    val errorMessage: String? = null,
    val successMessage: String? = null,
    )

sealed class PetaniEvent{
    data class OnSearchQueryChange(val query: String) : PetaniEvent()
    data object OnRefresh : PetaniEvent()

    data class OnMoreOptionClick(val id: String) : PetaniEvent()
    object OnBottomSheetDismiss : PetaniEvent()

    object OnDeleteClick : PetaniEvent()
    object OnDeleteConfirm : PetaniEvent()
    object OnDismissDeleteDialog : PetaniEvent()

    data object OnDismissError : PetaniEvent()
    data object OnDismissSuccessMessage : PetaniEvent()

    data class OnShowUserMessage(val message: String, val type: MessageType) : PetaniEvent()
}