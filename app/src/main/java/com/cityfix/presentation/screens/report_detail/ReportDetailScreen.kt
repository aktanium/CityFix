package com.cityfix.presentation.screens.report_detail

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cityfix.domain.model.Comment
import com.cityfix.domain.model.Report
import com.cityfix.domain.model.ReportCategory
import com.cityfix.domain.model.ReportStatus
import com.cityfix.presentation.components.*
import com.cityfix.presentation.theme.BrandPrimary
import com.cityfix.utils.toTimeAgo
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
    val comments by viewModel.comments.collectAsStateWithLifecycle()
    val commentText by viewModel.commentText.collectAsStateWithLifecycle()
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
        Column(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            Box(modifier = Modifier.weight(1f)) {
                when {
                    uiState.isLoading -> LoadingView()
                    uiState.error != null -> ErrorView(message = uiState.error!!, onRetry = {})
                    uiState.report == null -> EmptyStateView(
                        title = "Report not found",
                        message = "This report may have been deleted"
                    )
                    else -> ReportDetailContent(
                        report = uiState.report!!,
                        comments = comments,
                        currentUserId = viewModel.currentUserId,
                        onUpdateStatus = { showStatusDialog = true },
                        onDeleteComment = { viewModel.deleteComment(it) }
                    )
                }
            }

            // Sticky comment input — only visible once the report is loaded
            if (uiState.report != null) {
                CommentInputBar(
                    text = commentText,
                    onTextChange = { viewModel.onCommentTextChange(it) },
                    onSubmit = { viewModel.submitComment() }
                )
            }
        }
    }
}

@Composable
private fun ReportDetailContent(
    report: Report,
    comments: List<Comment>,
    currentUserId: String?,
    onUpdateStatus: () -> Unit,
    onDeleteComment: (String) -> Unit
) {
    Column(
        modifier = Modifier
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

            Text(text = report.title, style = MaterialTheme.typography.headlineSmall)
            Text(
                text = report.description,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            HorizontalDivider()

            DetailInfoCard(report = report)

            Button(
                onClick = onUpdateStatus,
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Filled.Update, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Update Status")
            }

            HorizontalDivider()

            // --- Comments ---
            Text(
                text = "${comments.size} ${if (comments.size == 1) "Comment" else "Comments"}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }

        if (comments.isEmpty()) {
            Text(
                text = "Be the first to comment.",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Gray,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )
        } else {
            comments.forEach { comment ->
                CommentItem(
                    comment = comment,
                    currentUserId = currentUserId,
                    onDelete = { onDeleteComment(comment.id) }
                )
            }
        }

        // Bottom spacer so the last comment isn't hidden by the sticky input.
        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
private fun CommentItem(
    comment: Comment,
    currentUserId: String?,
    onDelete: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
        verticalAlignment = Alignment.Top
    ) {
        // Avatar with initials
        Surface(
            modifier = Modifier.size(36.dp),
            shape = CircleShape,
            color = BrandPrimary.copy(alpha = 0.15f)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(
                    text = comment.authorName.take(1).uppercase().ifBlank { "?" },
                    style = MaterialTheme.typography.labelLarge,
                    color = BrandPrimary,
                    fontWeight = FontWeight.Bold
                )
            }
        }
        Spacer(Modifier.width(10.dp))
        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = comment.authorName,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.width(6.dp))
                Text(
                    text = comment.createdAt.toTimeAgo(),
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.Gray
                )
            }
            Spacer(Modifier.height(2.dp))
            Text(text = comment.text, style = MaterialTheme.typography.bodyMedium)
        }
        if (comment.userId == currentUserId && currentUserId != null) {
            IconButton(onClick = onDelete, modifier = Modifier.size(24.dp)) {
                Icon(
                    Icons.Filled.Delete,
                    contentDescription = "Delete comment",
                    tint = Color.Gray,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

@Composable
private fun CommentInputBar(
    text: String,
    onTextChange: (String) -> Unit,
    onSubmit: () -> Unit
) {
    Surface(
        tonalElevation = 4.dp,
        color = MaterialTheme.colorScheme.surface
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = text,
                onValueChange = onTextChange,
                modifier = Modifier.weight(1f),
                placeholder = { Text("Add a comment…") },
                maxLines = 3,
                shape = RoundedCornerShape(24.dp)
            )
            Spacer(Modifier.width(8.dp))
            IconButton(
                onClick = onSubmit,
                enabled = text.isNotBlank()
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Send,
                    contentDescription = "Send",
                    tint = if (text.isNotBlank()) BrandPrimary else Color.Gray
                )
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
