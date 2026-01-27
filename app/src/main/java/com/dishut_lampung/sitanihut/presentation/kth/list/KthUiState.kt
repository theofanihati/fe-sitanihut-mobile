package com.dishut_lampung.sitanihut.presentation.kth.list

import com.dishut_lampung.sitanihut.domain.model.Kth
import com.dishut_lampung.sitanihut.presentation.shared.components.animations.MessageType
import com.dishut_lampung.sitanihut.presentation.user_management.list.UserEvent

data class KthUiState(
    val isLoading: Boolean = false,
    val query: String = "",
    val isRefreshing: Boolean = false,
    val kthList: List<Kth> = emptyList(),

    val isOnline: Boolean = true,
    val isBottomSheetVisible: Boolean = false,
    val selectedKthId: String? = null,
    val isDeleteDialogVisible: Boolean = false,

    val errorMessage: String? = null,
    val successMessage: String? = null,
)

sealed class KthEvent {
    data class OnSearchQueryChange(val query: String) : KthEvent()
    data object OnRefresh : KthEvent()

    data class OnMoreOptionClick(val id: String) : KthEvent()
    object OnBottomSheetDismiss : KthEvent()

    object OnExportList : KthEvent()
    data class OnExportDetail(val id: String) : KthEvent()

    object OnDeleteClick : KthEvent()
    object OnDeleteConfirm : KthEvent()
    object OnDismissDeleteDialog : KthEvent()

    data object OnDismissError : KthEvent()
    data object OnDismissSuccessMessage : KthEvent()

    data class OnShowUserMessage(val message: String, val type: MessageType) : KthEvent()
}