package com.cityfix.presentation.screens.report_list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cityfix.domain.model.Report
import com.cityfix.domain.model.ReportCategory
import com.cityfix.domain.model.ReportStatus
import com.cityfix.domain.repository.AuthRepository
import com.cityfix.domain.repository.SettingsRepository
import com.cityfix.domain.usecase.DeleteReportUseCase
import com.cityfix.domain.usecase.GetAllReportsUseCase
import com.cityfix.domain.usecase.RefreshReportsUseCase
import com.cityfix.domain.usecase.VoteReportUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ReportListUiState(
    val isLoading: Boolean = true,
    val isRefreshing: Boolean = false,
    val error: String? = null,
    val isFilterSheetVisible: Boolean = false,
    val snackbarMessage: String? = null
)

sealed interface ReportListEvent {
    data class FilterByCategory(val category: ReportCategory?) : ReportListEvent
    data class FilterByStatus(val status: ReportStatus?) : ReportListEvent
    data class DeleteReport(val id: String) : ReportListEvent
    data class VoteReport(val id: String) : ReportListEvent
    data object ShowFilterSheet : ReportListEvent
    data object HideFilterSheet : ReportListEvent
    data object ClearFilters : ReportListEvent
    data object DismissSnackbar : ReportListEvent
    data object Refresh : ReportListEvent
    data object ToggleSearchActive : ReportListEvent
}

@HiltViewModel
class ReportListViewModel @Inject constructor(
    private val getAllReportsUseCase: GetAllReportsUseCase,
    private val deleteReportUseCase: DeleteReportUseCase,
    private val refreshReportsUseCase: RefreshReportsUseCase,
    private val voteReportUseCase: VoteReportUseCase,
    private val authRepository: AuthRepository,
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ReportListUiState())
    val uiState: StateFlow<ReportListUiState> = _uiState.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedCategory = MutableStateFlow<ReportCategory?>(null)
    val selectedCategory: StateFlow<ReportCategory?> = _selectedCategory.asStateFlow()

    private val _selectedStatus = MutableStateFlow<ReportStatus?>(null)
    val selectedStatus: StateFlow<ReportStatus?> = _selectedStatus.asStateFlow()

    private val _isSearchActive = MutableStateFlow(false)
    val isSearchActive: StateFlow<Boolean> = _isSearchActive.asStateFlow()

    private val _allReports = MutableStateFlow<List<Report>>(emptyList())

    /**
     * Search + filter happen in-memory off the single getAllReports flow so a query
     * and a category filter can compose without re-querying Room.
     */
    val filteredReports: StateFlow<List<Report>> = combine(
        _allReports,
        _searchQuery,
        _selectedCategory,
        _selectedStatus
    ) { reports, query, category, status ->
        reports
            .filter { report ->
                if (query.isBlank()) true
                else report.title.contains(query, ignoreCase = true) ||
                        report.description.contains(query, ignoreCase = true) ||
                        report.category.contains(query, ignoreCase = true)
            }
            .filter { report ->
                category == null || report.category == category.name
            }
            .filter { report ->
                status == null || report.status == status.name
            }
            .sortedByDescending { it.voteCount }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = emptyList()
    )

    init {
        observeReports()
        restoreLastSearch()
    }

    fun onEvent(event: ReportListEvent) {
        when (event) {
            is ReportListEvent.FilterByCategory -> _selectedCategory.value = event.category
            is ReportListEvent.FilterByStatus -> _selectedStatus.value = event.status
            is ReportListEvent.DeleteReport -> deleteReport(event.id)
            is ReportListEvent.VoteReport -> voteReport(event.id)
            ReportListEvent.ShowFilterSheet -> _uiState.update { it.copy(isFilterSheetVisible = true) }
            ReportListEvent.HideFilterSheet -> _uiState.update { it.copy(isFilterSheetVisible = false) }
            ReportListEvent.ClearFilters -> {
                _selectedCategory.value = null
                _selectedStatus.value = null
                _uiState.update { it.copy(isFilterSheetVisible = false) }
            }
            ReportListEvent.DismissSnackbar -> _uiState.update { it.copy(snackbarMessage = null) }
            ReportListEvent.Refresh -> refresh()
            ReportListEvent.ToggleSearchActive -> toggleSearchActive()
        }
    }

    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
        viewModelScope.launch {
            settingsRepository.saveLastSearch(query)
        }
    }

    fun clearSearch() {
        _searchQuery.value = ""
        viewModelScope.launch {
            settingsRepository.saveLastSearch("")
        }
    }

    private fun toggleSearchActive() {
        val nowActive = !_isSearchActive.value
        _isSearchActive.value = nowActive
        // Closing the search bar clears the query so the next open starts fresh.
        if (!nowActive) clearSearch()
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

    fun voteReport(reportId: String) {
        viewModelScope.launch {
            val userId = authRepository.currentUserId ?: run {
                _uiState.update {
                    it.copy(snackbarMessage = "Sign in to vote on reports")
                }
                return@launch
            }
            val report = _allReports.value.find { it.id == reportId } ?: return@launch
            voteReportUseCase(reportId, userId, hasVoted = report.votedByMe)
                .onFailure { e ->
                    _uiState.update {
                        it.copy(snackbarMessage = e.message ?: "Failed to register vote")
                    }
                }
        }
    }

    private fun observeReports() {
        viewModelScope.launch {
            getAllReportsUseCase()
                .catch { e -> _uiState.update { it.copy(isLoading = false, error = e.message) } }
                .collect { reports ->
                    _allReports.value = reports
                    _uiState.update { it.copy(isLoading = false, error = null) }
                }
        }
    }

    private fun restoreLastSearch() {
        viewModelScope.launch {
            val last = settingsRepository.getLastSearch()
            if (last.isNotBlank()) {
                _searchQuery.value = last
                _isSearchActive.value = true
            }
        }
    }

    private fun deleteReport(id: String) {
        viewModelScope.launch {
            deleteReportUseCase(id)
                .onSuccess { _uiState.update { it.copy(snackbarMessage = "Report deleted") } }
                .onFailure { _uiState.update { it.copy(snackbarMessage = "Failed to delete report") } }
        }
    }
}
