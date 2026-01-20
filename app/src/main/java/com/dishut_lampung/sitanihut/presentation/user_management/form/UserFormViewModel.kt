package com.dishut_lampung.sitanihut.presentation.user_management.form

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dishut_lampung.sitanihut.data.local.UserPreferences
import com.dishut_lampung.sitanihut.domain.model.CreateUserInput
import com.dishut_lampung.sitanihut.domain.model.Kph
import com.dishut_lampung.sitanihut.domain.model.Kth
import com.dishut_lampung.sitanihut.domain.model.Role
import com.dishut_lampung.sitanihut.domain.model.UserDetail
import com.dishut_lampung.sitanihut.domain.usecase.kph.GetKphListUseCase
import com.dishut_lampung.sitanihut.domain.usecase.kth.GetKthListUseCase
import com.dishut_lampung.sitanihut.domain.usecase.profile.GetMyProfileUseCase
import com.dishut_lampung.sitanihut.domain.usecase.role.GetRolesUseCase
import com.dishut_lampung.sitanihut.domain.usecase.user_management.CreateUserUseCase
import com.dishut_lampung.sitanihut.domain.usecase.user_management.GetUserDetailUseCase
import com.dishut_lampung.sitanihut.domain.usecase.user_management.UpdateUserUseCase
import com.dishut_lampung.sitanihut.domain.usecase.user_management.ValidateUserManagementInputUseCase
import com.dishut_lampung.sitanihut.presentation.shared.components.animations.MessageType
import com.dishut_lampung.sitanihut.util.ConnectivityObserver
import com.dishut_lampung.sitanihut.util.Resource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

class UserFormViewModel  @Inject constructor(
    private val createUserUseCase: CreateUserUseCase,
    private val getUserDetailUseCase: GetUserDetailUseCase,
    private val updateUserUseCase: UpdateUserUseCase,
    private val validateUserManagementInputUseCase: ValidateUserManagementInputUseCase,
    private val getKphListUseCase: GetKphListUseCase,
    private val getKthListUseCase: GetKthListUseCase,
    private val getRolesUseCase: GetRolesUseCase,
    private val userPreferences: UserPreferences,
    private val connectivityObserver: ConnectivityObserver,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _uiState = MutableStateFlow(UserFormUiState())
    val uiState = _uiState.asStateFlow()

    private val currentUserId: String? = savedStateHandle.get<String>("id")
    private var allKthInKph: List<Kth> = emptyList()
    private var originalData: UserDetail? = null
    private var availableRoles: List<Role> = emptyList()
    private var allKphOptions: List<Kph> = emptyList()

    fun onEvent(event: UserFormEvent) {
        TODO()
    }
}