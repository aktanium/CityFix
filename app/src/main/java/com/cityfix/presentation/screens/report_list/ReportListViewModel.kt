package com.cityfix.presentation.screens.report_list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cityfix.domain.model.Report
import com.cityfix.domain.model.ReportCategory
import com.cityfix.domain.model.ReportStatus
import com.cityfix.domain.usecase.DeleteReportUseCase
import com.cityfix.domain.usecase.GetFilteredReportsUseCase
import com.cityfix.domain.usecase.RefreshReportsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ReportListUiState(
    val reports: List<Report> = emptyList(),
    val isLoading: Boolean = true,
    val isRefreshing: Boolean = false,
    val error: String? = null,
    val selectedCategory: ReportCategory? = null,
    val selectedStatus: ReportStatus? = null,
    val isFilterSheetVisible: Boolean = false,
    val snackbarMessage: String? = null
)

sealed interface ReportListEvent {
    data class FilterByCategory(val category: ReportCategory?) : ReportListEvent
    data class FilterByStatus(val status: ReportStatus?) : ReportListEvent
    data class DeleteReport(val id: String) : ReportListEvent
    data object ShowFilterSheet : ReportListEvent
    data object HideFilterSheet : ReportListEvent
    data object ClearFilters : ReportListEvent
    data object DismissSnackbar : ReportListEvent
    data object Refresh : ReportListEvent
}

@HiltViewModel
class ReportListViewModel @Inject constructor(
    private val getFilteredReportsUseCase: GetFilteredReportsUseCase,
    private val deleteReportUseCase: DeleteReportUseCase,
    private val refreshReportsUseCase: RefreshReportsUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(ReportListUiState())
    val uiState: StateFlow<ReportListUiState> = _uiState.asStateFlow()

    private var observeJob: Job? = null

    init {
        observeReports()
    }

    fun onEvent(event: ReportListEvent) {
        when (event) {
            is ReportListEvent.FilterByCategory -> applyFilter(category = event.category)
            is ReportListEvent.FilterByStatus -> applyFilter(status = event.status)
            is ReportListEvent.DeleteReport -> deleteReport(event.id)
            ReportListEvent.ShowFilterSheet -> _uiState.update { it.copy(isFilterSheetVisible = true) }
            ReportListEvent.HideFilterSheet -> _uiState.update { it.copy(isFilterSheetVisible = false) }
            ReportListEvent.ClearFilters -> applyFilter(null, null)
            ReportListEvent.DismissSnackbar -> _uiState.update { it.copy(snackbarMessage = null) }
            ReportListEvent.Refresh -> refresh()
        }
    }

    fun refresh() {
        viewModelScope.launch {
            _uiState.update { it.copy(isRefreshing = true) }
            refreshReportsUseCase()
                .onFailure { e ->
                    _uiState.update {
                        it.copy(snackbarMessage = e.message ?: "Failed to refresh")
                    }
                }
            _uiState.update { it.copy(isRefreshing = false) }
        }
    }

    private fun observeReports() {
        observeJob?.cancel()
        observeJob = viewModelScope.launch {
            getFilteredReportsUseCase(
                category = _uiState.value.selectedCategory?.name,
                status = _uiState.value.selectedStatus?.name
            ).catch { e ->
                _uiState.update { it.copy(isLoading = false, error = e.message) }
            }.collect { reports ->
                _uiState.update { it.copy(reports = reports, isLoading = false, error = null) }
            }
        }
    }

    private fun applyFilter(
        category: ReportCategory? = _uiState.value.selectedCategory,
        status: ReportStatus? = _uiState.value.selectedStatus
    ) {
        _uiState.update {
            it.copy(
                selectedCategory = category,
                selectedStatus = status,
                isLoading = true,
                isFilterSheetVisible = false
            )
        }
        observeReports()
    }

    private fun deleteReport(id: String) {
        viewModelScope.launch {
            deleteReportUseCase(id)
                .onSuccess { _uiState.update { it.copy(snackbarMessage = "Report deleted") } }
                .onFailure { _uiState.update { it.copy(snackbarMessage = "Failed to delete report") } }
        }
    }
}
