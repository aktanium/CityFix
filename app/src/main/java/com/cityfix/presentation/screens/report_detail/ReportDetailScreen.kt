package com.cityfix.presentation.screens.report_detail

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cityfix.domain.model.Report
import com.cityfix.domain.model.ReportCategory
import com.cityfix.domain.model.ReportStatus
import com.cityfix.presentation.components.*
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.Instant

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportDetailScreen(
    onNavigateUp: () -> Unit,
    viewModel: ReportDetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showStatusDialog by remember { mutableStateOf(false) }

    LaunchedEffect(uiState.isDeleted) {
        if (uiState.isDeleted) onNavigateUp()
    }

    LaunchedEffect(uiState.snackbarMessage) {
        uiState.snackbarMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.onEvent(ReportDetailEvent.DismissSnackbar)
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete Report") },
            text = { Text("This action cannot be undone. The report will be permanently removed.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.onEvent(ReportDetailEvent.DeleteReport)
                        showDeleteDialog = false
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) { Text("Delete") }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) { Text("Cancel") }
            }
        )
    }

    if (showStatusDialog) {
        uiState.report?.let { report ->
            StatusUpdateDialog(
                currentStatus = ReportStatus.fromName(report.status),
                onStatusSelected = {
                    viewModel.onEvent(ReportDetailEvent.UpdateStatus(it))
                    showStatusDialog = false
                },
                onDismiss = { showStatusDialog = false }
            )
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Report Details") },
                navigationIcon = {
                    IconButton(onClick = onNavigateUp) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { showDeleteDialog = true }) {
                        Icon(
                            Icons.Filled.Delete,
                            contentDescription = "Delete",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        when {
            uiState.isLoading -> LoadingView(modifier = Modifier.padding(paddingValues))
            uiState.error != null -> ErrorView(
                message = uiState.error!!,
                onRetry = {},
                modifier = Modifier.padding(paddingValues)
            )
            uiState.report == null -> EmptyStateView(
                title = "Report not found",
                message = "This report may have been deleted",
                modifier = Modifier.padding(paddingValues)
            )
            else -> ReportDetailContent(
                report = uiState.report!!,
                onUpdateStatus = { showStatusDialog = true },
                modifier = Modifier.padding(paddingValues)
            )
        }
    }
}

@Composable
private fun ReportDetailContent(
    report: Report,
    onUpdateStatus: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        ReportImage(
            uri = report.imageUri,
            contentDescription = "Report image",
            modifier = Modifier
                .fillMaxWidth()
                .height(240.dp)
        )

        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                CategoryIcon(category = ReportCategory.fromName(report.category), size = 48)
                StatusChip(status = ReportStatus.fromName(report.status))
            }

            Text(
                text = report.title,
                style = MaterialTheme.typography.headlineSmall
            )

            Text(
                text = report.description,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            HorizontalDivider()

            DetailInfoCard(report = report)

            Button(
                onClick = onUpdateStatus,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Filled.Update, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Update Status")
            }
        }
    }
}

@Composable
private fun DetailInfoCard(report: Report) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("Report Information", style = MaterialTheme.typography.titleSmall)

            InfoRow(
                icon = Icons.Filled.Category,
                label = "Category",
                value = ReportCategory.fromName(report.category).displayName
            )
            InfoRow(
                icon = Icons.Filled.LocationOn,
                label = "Location",
                value = "%.6f, %.6f".format(report.latitude, report.longitude)
            )
            InfoRow(
                icon = Icons.Filled.CalendarToday,
                label = "Reported",
                value = Instant.ofEpochMilli(report.createdAt)
                    .atZone(ZoneId.systemDefault())
                    .format(DateTimeFormatter.ofPattern("MMMM d, yyyy 'at' HH:mm"))
            )
        }
    }
}

@Composable
private fun InfoRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(20.dp)
        )
        Column {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(text = value, style = MaterialTheme.typography.bodyMedium)
        }
    }
}

@Composable
private fun StatusUpdateDialog(
    currentStatus: ReportStatus,
    onStatusSelected: (ReportStatus) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Update Status") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                ReportStatus.entries.forEach { status ->
                    Surface(
                        onClick = { onStatusSelected(status) },
                        shape = RoundedCornerShape(12.dp),
                        color = if (currentStatus == status)
                            MaterialTheme.colorScheme.primaryContainer
                        else MaterialTheme.colorScheme.surface,
                        tonalElevation = if (currentStatus == status) 4.dp else 0.dp
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            StatusChip(status = status)
                            if (currentStatus == status) {
                                Icon(
                                    Icons.Filled.CheckCircle,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
