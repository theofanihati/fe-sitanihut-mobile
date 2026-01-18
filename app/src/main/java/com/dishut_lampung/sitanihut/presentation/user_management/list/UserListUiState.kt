package com.dishut_lampung.sitanihut.presentation.user_management.list

import com.dishut_lampung.sitanihut.domain.model.UserDetail
import com.dishut_lampung.sitanihut.presentation.shared.components.animations.MessageType

data class UserListUiState(
    val isLoading: Boolean = false,
    val query: String = "",
    val isRefreshing: Boolean = false,
    val userRole: String = "",
    val userList: List<UserDetail> = emptyList(),

    val isOnline: Boolean = true,
    val isBottomSheetVisible: Boolean = false,
    val selectedUserId: String? = null,
    val isDeleteDialogVisible: Boolean = false,

    val errorMessage: String? = null,
    val successMessage: String? = null,
)

sealed class UserEvent{
    data class OnSearchQueryChange(val query: String) : UserEvent()
    data object OnRefresh : UserEvent()

    data class OnMoreOptionClick(val id: String) : UserEvent()
    object OnBottomSheetDismiss : UserEvent()

    object OnDeleteClick : UserEvent()
    object OnDeleteConfirm : UserEvent()
    object OnDismissDeleteDialog : UserEvent()

    data object OnDismissError : UserEvent()
    data object OnDismissSuccessMessage : UserEvent()

    data class OnShowUserMessage(val message: String, val type: MessageType) : UserEvent()
}