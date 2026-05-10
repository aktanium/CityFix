package com.cityfix.presentation.screens.stats

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cityfix.domain.model.Report
import com.cityfix.domain.repository.AuthRepository
import com.cityfix.domain.repository.ReportRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class StatsViewModel @Inject constructor(
    private val reportRepository: ReportRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    data class StatsUiState(
        val totalReports: Int = 0,
        val newReports: Int = 0,
        val inProgressReports: Int = 0,
        val resolvedReports: Int = 0,
        val myReports: Int = 0,
        val myResolved: Int = 0,
        val topCategories: List<Pair<String, Int>> = emptyList(),
        val mostVoted: List<Report> = emptyList(),
        val isLoading: Boolean = true
    )

    private val _uiState = MutableStateFlow(StatsUiState())
    val uiState: StateFlow<StatsUiState> = _uiState.asStateFlow()

    init {
        loadStats()
    }

    private fun loadStats() {
        viewModelScope.launch {
            reportRepository.getAllReports().collect { reports ->
                val userId = authRepository.currentUserId
                // Snapshot already filters DELETED out, but defensive filter in case the
                // offline Room fallback ever surfaces a soft-deleted row.
                val activeReports = reports.filter { it.status != "DELETED" }

                val topCategories = activeReports
                    .groupBy { it.category }
                    .mapValues { it.value.size }
                    .entries
                    .sortedByDescending { it.value }
                    .take(5)
                    .map { it.key to it.value }

                val mostVoted = activeReports
                    .sortedByDescending { it.voteCount }
                    .take(3)

                _uiState.value = StatsUiState(
                    totalReports = activeReports.size,
                    newReports = activeReports.count { it.status == "NEW" },
                    inProgressReports = activeReports.count { it.status == "IN_PROGRESS" },
                    resolvedReports = activeReports.count { it.status == "RESOLVED" },
                    myReports = activeReports.count { it.userId == userId },
                    myResolved = activeReports.count { it.userId == userId && it.status == "RESOLVED" },
                    topCategories = topCategories,
                    mostVoted = mostVoted,
                    isLoading = false
                )
            }
        }
    }
}
