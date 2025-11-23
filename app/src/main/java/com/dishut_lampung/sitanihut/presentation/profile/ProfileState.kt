package com.dishut_lampung.sitanihut.presentation.profile

import com.dishut_lampung.sitanihut.domain.model.UserDetail

data class ProfileState(
    val isLoading: Boolean = false,
    val generalError: String? = null,
    val successMessage: String? = null,
    val user: UserDetail? = null,
)
