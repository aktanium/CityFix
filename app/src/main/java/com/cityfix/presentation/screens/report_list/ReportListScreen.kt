package com.cityfix.presentation.screens.report_list

import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.cityfix.domain.model.Report
import com.cityfix.domain.model.ReportCategory
import com.cityfix.domain.model.ReportStatus
import com.cityfix.presentation.components.*
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportListScreen(
    onReportClick: (Long) -> Unit,
    onAddReport: () -> Unit,
    viewModel: ReportListViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.snackbarMessage) {
        uiState.snackbarMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.onEvent(ReportListEvent.DismissSnackbar)
        }
    }

    if (uiState.isFilterSheetVisible) {
        FilterBottomSheet(
            selectedCategory = uiState.selectedCategory,
            selectedStatus = uiState.selectedStatus,
            onCategorySelected = { viewModel.onEvent(ReportListEvent.FilterByCategory(it)) },
            onStatusSelected = { viewModel.onEvent(ReportListEvent.FilterByStatus(it)) },
            onClearFilters = { viewModel.onEvent(ReportListEvent.ClearFilters) },
            onDismiss = { viewModel.onEvent(ReportListEvent.HideFilterSheet) }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "CityFix",
                            style = MaterialTheme.typography.titleLarge
                        )
                        Text(
                            text = "${uiState.reports.size} reports",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                actions = {
                    val hasActiveFilters =
                        uiState.selectedCategory != null || uiState.selectedStatus != null
                    IconButton(onClick = { viewModel.onEvent(ReportListEvent.ShowFilterSheet) }) {
                        BadgedBox(
                            badge = {
                                if (hasActiveFilters) Badge()
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Filled.FilterList,
                                contentDescription = "Filter reports"
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onAddReport,
                icon = { Icon(Icons.Filled.Add, contentDescription = null) },
                text = { Text("Report Issue") },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when {
                uiState.isLoading -> LoadingView()
                uiState.error != null -> ErrorView(
                    message = uiState.error!!,
                    onRetry = { viewModel.onEvent(ReportListEvent.ClearFilters) }
                )
                uiState.reports.isEmpty() -> EmptyStateView(
                    title = "No reports found",
                    message = if (uiState.selectedCategory != null || uiState.selectedStatus != null)
                        "No reports match your filters. Try clearing them."
                    else "Be the first to report an issue in your city!",
                    icon = Icons.Filled.LocationCity
                )
                else -> ReportList(
                    reports = uiState.reports,
                    onReportClick = onReportClick,
                    onDeleteReport = { viewModel.onEvent(ReportListEvent.DeleteReport(it)) }
                )
            }
        }
    }
}

@Composable
private fun ReportList(
    reports: List<Report>,
    onReportClick: (Long) -> Unit,
    onDeleteReport: (Long) -> Unit
) {
    LazyColumn(
        contentPadding = PaddingValues(bottom = 100.dp, top = 8.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        items(
            items = reports,
            key = { it.id }
        ) { report ->
            ReportCard(
                report = report,
                onClick = { onReportClick(report.id) },
                onDelete = { onDeleteReport(report.id) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .animateItem()
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportCard(
    report: Report,
    onClick: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showDeleteDialog by remember { mutableStateOf(false) }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete Report") },
            text = { Text("Are you sure you want to delete this report? This action cannot be undone.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDelete()
                        showDeleteDialog = false
                    },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) { Text("Delete") }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) { Text("Cancel") }
            }
        )
    }

    Card(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            report.imageUri?.let { uri ->
                AsyncImage(
                    model = uri,
                    contentDescription = "Report image",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
                )
            }

            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CategoryIcon(
                        category = report.category,
                        modifier = Modifier,
                        size = 40
                    )
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        StatusChip(status = report.status)
                        IconButton(
                            onClick = { showDeleteDialog = true },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.MoreVert,
                                contentDescription = "More options",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = report.title,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = report.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(12.dp))

                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.LocationOn,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = "%.4f, %.4f".format(
                                report.location.latitude,
                                report.location.longitude
                            ),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Text(
                        text = report.createdAt
                            .atZone(ZoneId.systemDefault())
                            .format(DateTimeFormatter.ofPattern("MMM d, yyyy")),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FilterBottomSheet(
    selectedCategory: ReportCategory?,
    selectedStatus: ReportStatus?,
    onCategorySelected: (ReportCategory?) -> Unit,
    onStatusSelected: (ReportStatus?) -> Unit,
    onClearFilters: () -> Unit,
    onDismiss: () -> Unit
) {
    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .padding(bottom = 32.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Filter Reports", style = MaterialTheme.typography.titleLarge)
                TextButton(onClick = onClearFilters) { Text("Clear all") }
            }

            Spacer(modifier = Modifier.height(16.dp))
            SectionHeader("Category")

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(ReportCategory.entries) { category ->
                    FilterChip(
                        selected = selectedCategory == category,
                        onClick = {
                            onCategorySelected(if (selectedCategory == category) null else category)
                        },
                        label = { Text(category.displayName) },
                        leadingIcon = if (selectedCategory == category) {
                            { Icon(Icons.Filled.Check, null, Modifier.size(16.dp)) }
                        } else null
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            SectionHeader("Status")

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                ReportStatus.entries.forEach { status ->
                    FilterChip(
                        selected = selectedStatus == status,
                        onClick = {
                            onStatusSelected(if (selectedStatus == status) null else status)
                        },
                        label = { Text(status.displayName) },
                        leadingIcon = if (selectedStatus == status) {
                            { Icon(Icons.Filled.Check, null, Modifier.size(16.dp)) }
                        } else null
                    )
                }
            }
        }
    }
}
