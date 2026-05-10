package com.cityfix.presentation.screens.report_detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cityfix.domain.model.Comment
import com.cityfix.domain.model.Report
import com.cityfix.domain.model.ReportStatus
import com.cityfix.domain.repository.AuthRepository
import com.cityfix.domain.repository.ReportRepository
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
    private val deleteReportUseCase: DeleteReportUseCase,
    private val reportRepository: ReportRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val reportId: String = checkNotNull(savedStateHandle[NavArgs.REPORT_ID])

    private val _uiState = MutableStateFlow(ReportDetailUiState())
    val uiState: StateFlow<ReportDetailUiState> = _uiState.asStateFlow()

    private val _comments = MutableStateFlow<List<Comment>>(emptyList())
    val comments: StateFlow<List<Comment>> = _comments.asStateFlow()

    private val _commentText = MutableStateFlow("")
    val commentText: StateFlow<String> = _commentText.asStateFlow()

    /** Exposed for the screen so it can decide which comments show a delete button. */
    val currentUserId: String? get() = authRepository.currentUserId

    init {
        observeReport()
        observeComments()
    }

    fun onEvent(event: ReportDetailEvent) {
        when (event) {
            is ReportDetailEvent.UpdateStatus -> updateStatus(event.status)
            ReportDetailEvent.DeleteReport -> deleteReport()
            ReportDetailEvent.DismissSnackbar -> _uiState.update { it.copy(snackbarMessage = null) }
        }
    }

    fun onCommentTextChange(text: String) {
        _commentText.value = text
    }

    fun submitComment() {
        val text = _commentText.value.trim()
        if (text.isEmpty()) return
        viewModelScope.launch {
            val userId = authRepository.currentUserId ?: run {
                _uiState.update { it.copy(snackbarMessage = "Sign in to comment") }
                return@launch
            }
            val authorName = authRepository.currentUser.first()
                ?.displayName
                ?.takeIf { it.isNotBlank() }
                ?: "Anonymous"
            val comment = Comment(
                reportId = reportId,
                userId = userId,
                authorName = authorName,
                text = text,
                createdAt = System.currentTimeMillis()
            )
            runCatching { reportRepository.addComment(comment) }
                .onSuccess { _commentText.value = "" }
                .onFailure { e ->
                    _uiState.update {
                        it.copy(snackbarMessage = e.message ?: "Failed to post comment")
                    }
                }
        }
    }

    fun deleteComment(commentId: String) {
        viewModelScope.launch {
            runCatching { reportRepository.deleteComment(commentId, reportId) }
                .onFailure { e ->
                    _uiState.update {
                        it.copy(snackbarMessage = e.message ?: "Failed to delete comment")
                    }
                }
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

    private fun observeComments() {
        viewModelScope.launch {
            reportRepository.getComments(reportId)
                .catch { /* surface as snackbar but don't block the report itself */ }
                .collect { _comments.value = it }
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
