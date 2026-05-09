package com.cityfix.presentation.screens.report_detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cityfix.domain.model.Report
import com.cityfix.domain.model.ReportStatus
import com.cityfix.domain.usecase.DeleteReportUseCase
import com.cityfix.domain.usecase.GetReportByIdUseCase
import com.cityfix.domain.usecase.UpdateReportStatusUseCase
import com.cityfix.presentation.navigation.NavArgs
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ReportDetailUiState(
    val report: Report? = null,
    val isLoading: Boolean = true,
    val error: String? = null,
    val isDeleted: Boolean = false,
    val snackbarMessage: String? = null
)

sealed interface ReportDetailEvent {
    data class UpdateStatus(val status: ReportStatus) : ReportDetailEvent
    data object DeleteReport : ReportDetailEvent
    data object DismissSnackbar : ReportDetailEvent
}

@HiltViewModel
class ReportDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val getReportByIdUseCase: GetReportByIdUseCase,
    private val updateReportStatusUseCase: UpdateReportStatusUseCase,
    private val deleteReportUseCase: DeleteReportUseCase
) : ViewModel() {

    private val reportId: String = checkNotNull(savedStateHandle[NavArgs.REPORT_ID])

    private val _uiState = MutableStateFlow(ReportDetailUiState())
    val uiState: StateFlow<ReportDetailUiState> = _uiState.asStateFlow()

    init {
        observeReport()
    }

    fun onEvent(event: ReportDetailEvent) {
        when (event) {
            is ReportDetailEvent.UpdateStatus -> updateStatus(event.status)
            ReportDetailEvent.DeleteReport -> deleteReport()
            ReportDetailEvent.DismissSnackbar -> _uiState.update { it.copy(snackbarMessage = null) }
        }
    }

    private fun observeReport() {
        viewModelScope.launch {
            getReportByIdUseCase(reportId)
                .catch { e -> _uiState.update { it.copy(isLoading = false, error = e.message) } }
                .collect { report ->
                    _uiState.update { it.copy(report = report, isLoading = false) }
                }
        }
    }

    private fun updateStatus(newStatus: ReportStatus) {
        val report = _uiState.value.report ?: return
        viewModelScope.launch {
            updateReportStatusUseCase(report, newStatus.name)
                .onSuccess {
                    _uiState.update { it.copy(snackbarMessage = "Status updated to ${newStatus.displayName}") }
                }
                .onFailure { e ->
                    _uiState.update { it.copy(snackbarMessage = e.message ?: "Failed to update status") }
                }
        }
    }

    private fun deleteReport() {
        viewModelScope.launch {
            deleteReportUseCase(reportId)
                .onSuccess { _uiState.update { it.copy(isDeleted = true) } }
                .onFailure { e ->
                    _uiState.update { it.copy(snackbarMessage = e.message ?: "Failed to delete report") }
                }
        }
    }
}
