package com.dishut_lampung.sitanihut.presentation.information.about_company

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dishut_lampung.sitanihut.domain.repository.CompanyRepository
import com.dishut_lampung.sitanihut.domain.usecase.information.DownloadStructureImageUseCase
import com.dishut_lampung.sitanihut.presentation.information.InformationEvent
import com.dishut_lampung.sitanihut.presentation.information.InformationState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DishutViewModel @Inject constructor(
    private val downloadStructureImageUseCase: DownloadStructureImageUseCase,
) : ViewModel() {

    private val _state = MutableStateFlow(InformationState())
    val state = _state.asStateFlow()

    fun onEvent(event: InformationEvent) {
        when(event) {
            is InformationEvent.onDownloadClick -> {
                downloadStructureImage()
            }
            is InformationEvent.OnDismissError -> {
                _state.update { it.copy(generalError = null) }
            }
            is InformationEvent.OnDismissSuccessMessage -> {
                _state.update { it.copy(successMessage = null) }
            }
        }
    }

    private fun downloadStructureImage() {
        _state.update {
            it.copy(
                isLoading = true,
                generalError = null,
                successMessage = null
            )
        }

        viewModelScope.launch {
            val result = downloadStructureImageUseCase()
            _state.update { currentState ->
                if (result.isSuccess) {
                    currentState.copy(
                        isLoading = false,
                        successMessage = result.getOrNull() ?: "Berhasil diunduh"
                    )
                } else {
                    currentState.copy(
                        isLoading = false,
                        generalError = result.exceptionOrNull()?.message ?: "Gagal mengunduh gambar"
                    )
                }
            }
        }
    }

}