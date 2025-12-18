package com.dishut_lampung.sitanihut.presentation.report.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dishut_lampung.sitanihut.domain.usecase.report.GetReportDetailUseCase
import com.dishut_lampung.sitanihut.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ReportDetailViewModel @Inject constructor(
    private val getReportDetailUseCase: GetReportDetailUseCase,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val reportId: String = checkNotNull(savedStateHandle["reportId"])

    private val _uiState = MutableStateFlow<ReportDetailUiState>(ReportDetailUiState.Loading)
    val uiState = _uiState.asStateFlow()

    init {
        getReportDetail()
    }

    fun getReportDetail() {
        viewModelScope.launch {
            getReportDetailUseCase(reportId).collect { result ->
                when (result) {
                    is Resource.Loading -> {
                        if (_uiState.value !is ReportDetailUiState.Success) {
                            _uiState.value = ReportDetailUiState.Loading
                        }
                    }
                    is Resource.Success -> {
                        result.data?.let { detailData ->
                            _uiState.value = ReportDetailUiState.Success(data = detailData)
                        }
                    }
                    is Resource.Error -> {
                        if (_uiState.value !is ReportDetailUiState.Success) {
                            _uiState.value = ReportDetailUiState.Error(result.message ?: "Terjadi kesalahan")
                        } else {
                        }
                    }
                }
            }
        }
    }
}