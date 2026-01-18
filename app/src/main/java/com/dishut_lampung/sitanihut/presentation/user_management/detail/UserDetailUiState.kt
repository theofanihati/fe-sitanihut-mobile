package com.dishut_lampung.sitanihut.presentation.user_management.detail

import com.dishut_lampung.sitanihut.domain.model.UserDetail

data class UserDetailUiState (
    val isLoading: Boolean = false,
    val user: UserDetail? = null,
    val error: String? = null,
    val userRole: String = "",
)