package com.cityfix.presentation.screens.add_report

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cityfix.domain.model.GeoLocation
import com.cityfix.domain.model.Report
import com.cityfix.domain.model.ReportCategory
import com.cityfix.domain.model.ReportStatus
import com.cityfix.domain.usecase.CreateReportUseCase
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AddReportUiState(
    val title: String = "",
    val description: String = "",
    val selectedCategory: ReportCategory = ReportCategory.DAMAGED_ROAD,
    val imageUri: Uri? = null,
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val hasLocation: Boolean = false,
    val isFetchingLocation: Boolean = false,
    val titleError: String? = null,
    val descriptionError: String? = null,
    val locationError: String? = null,
    val isSubmitting: Boolean = false,
    val isSubmitted: Boolean = false,
    val error: String? = null
)

sealed interface AddReportEvent {
    data class TitleChanged(val value: String) : AddReportEvent
    data class DescriptionChanged(val value: String) : AddReportEvent
    data class CategorySelected(val category: ReportCategory) : AddReportEvent
    data class ImageSelected(val uri: Uri?) : AddReportEvent
    data class LocationUpdated(val latitude: Double, val longitude: Double) : AddReportEvent
    data object LocationFetchStarted : AddReportEvent
    data class LocationFetchFailed(val message: String) : AddReportEvent
    data object Submit : AddReportEvent
    data object DismissError : AddReportEvent
}

@HiltViewModel
class AddReportViewModel @Inject constructor(
    private val createReportUseCase: CreateReportUseCase,
    private val auth: FirebaseAuth
) : ViewModel() {

    private val _uiState = MutableStateFlow(AddReportUiState())
    val uiState: StateFlow<AddReportUiState> = _uiState.asStateFlow()

    fun onEvent(event: AddReportEvent) {
        when (event) {
            is AddReportEvent.TitleChanged -> _uiState.update {
                it.copy(title = event.value, titleError = null)
            }
            is AddReportEvent.DescriptionChanged -> _uiState.update {
                it.copy(description = event.value, descriptionError = null)
            }
            is AddReportEvent.CategorySelected -> _uiState.update {
                it.copy(selectedCategory = event.category)
            }
            is AddReportEvent.ImageSelected -> _uiState.update {
                it.copy(imageUri = event.uri)
            }
            is AddReportEvent.LocationUpdated -> _uiState.update {
                it.copy(
                    latitude = event.latitude,
                    longitude = event.longitude,
                    hasLocation = true,
                    isFetchingLocation = false,
                    locationError = null
                )
            }
            AddReportEvent.LocationFetchStarted -> _uiState.update {
                it.copy(isFetchingLocation = true, locationError = null)
            }
            is AddReportEvent.LocationFetchFailed -> _uiState.update {
                it.copy(isFetchingLocation = false, locationError = event.message)
            }
            AddReportEvent.Submit -> submitReport()
            AddReportEvent.DismissError -> _uiState.update { it.copy(error = null) }
        }
    }

    private fun submitReport() {
        if (!validateInputs()) return

        val state = _uiState.value
        viewModelScope.launch {
            _uiState.update { it.copy(isSubmitting = true) }

            val currentUserId = auth.currentUser?.uid ?: ""
            val now = System.currentTimeMillis()
            val report = Report(
                userId = currentUserId,
                title = state.title.trim(),
                description = state.description.trim(),
                category = state.selectedCategory.name,
                imageUri = state.imageUri?.toString() ?: "",
                latitude = state.latitude,
                longitude = state.longitude,
                status = "NEW",
                createdAt = now
            )

            createReportUseCase(report, state.imageUri)
                .onSuccess { _uiState.update { it.copy(isSubmitted = true, isSubmitting = false) } }
                .onFailure { e ->
                    _uiState.update {
                        it.copy(isSubmitting = false, error = e.message ?: "Failed to create report")
                    }
                }
        }
    }

    private fun validateInputs(): Boolean {
        val state = _uiState.value
        var isValid = true

        if (state.title.isBlank()) {
            _uiState.update { it.copy(titleError = "Title is required") }
            isValid = false
        } else if (state.title.length < 5) {
            _uiState.update { it.copy(titleError = "Title must be at least 5 characters") }
            isValid = false
        }

        if (state.description.isBlank()) {
            _uiState.update { it.copy(descriptionError = "Description is required") }
            isValid = false
        } else if (state.description.length < 10) {
            _uiState.update { it.copy(descriptionError = "Description must be at least 10 characters") }
            isValid = false
        }

        if (!state.hasLocation) {
            _uiState.update { it.copy(locationError = "Tap \"Get Location\" to attach your current position") }
            isValid = false
        }

        return isValid
    }
}
