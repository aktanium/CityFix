package com.cityfix.presentation.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.SubcomposeAsyncImage
import com.cityfix.domain.model.ReportCategory
import com.cityfix.domain.model.ReportStatus
import com.cityfix.presentation.theme.*

@Composable
fun StatusChip(
    status: ReportStatus,
    modifier: Modifier = Modifier
) {
    val (backgroundColor, textColor, icon) = when (status) {
        ReportStatus.NEW -> Triple(StatusNew.copy(alpha = 0.12f), StatusNew, Icons.Filled.FiberNew)
        ReportStatus.IN_PROGRESS -> Triple(StatusInProgress.copy(alpha = 0.12f), StatusInProgress, Icons.Filled.Autorenew)
        ReportStatus.RESOLVED -> Triple(StatusResolved.copy(alpha = 0.12f), StatusResolved, Icons.Filled.CheckCircle)
    }

    val animatedColor by animateColorAsState(
        targetValue = backgroundColor,
        animationSpec = tween(300),
        label = "status_chip_color"
    )

    Surface(
        shape = RoundedCornerShape(50),
        color = animatedColor,
        modifier = modifier
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = textColor,
                modifier = Modifier.size(14.dp)
            )
            Text(
                text = status.displayName,
                style = MaterialTheme.typography.labelSmall,
                color = textColor
            )
        }
    }
}

@Composable
fun CategoryIcon(
    category: ReportCategory,
    modifier: Modifier = Modifier,
    size: Int = 40
) {
    val icon = categoryIcon(category)
    val color = categoryColor(category)

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .size(size.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(color.copy(alpha = 0.15f))
    ) {
        Icon(
            imageVector = icon,
            contentDescription = category.displayName,
            tint = color,
            modifier = Modifier.size((size * 0.55f).dp)
        )
    }
}

@Composable
fun EmptyStateView(
    title: String,
    message: String,
    icon: ImageVector = Icons.Filled.Inbox,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(96.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primaryContainer)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(48.dp),
                tint = MaterialTheme.colorScheme.primary
            )
        }
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 32.dp)
        )
    }
}

@Composable
fun LoadingView(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(
            modifier = Modifier.size(48.dp),
            strokeWidth = 4.dp
        )
    }
}

@Composable
fun ErrorView(
    message: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Filled.ErrorOutline,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.error
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Something went wrong",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 32.dp)
        )
        Spacer(modifier = Modifier.height(24.dp))
        Button(onClick = onRetry) {
            Text("Retry")
        }
    }
}

@Composable
fun SectionHeader(
    title: String,
    modifier: Modifier = Modifier
) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleSmall,
        color = MaterialTheme.colorScheme.primary,
        modifier = modifier.padding(horizontal = 16.dp, vertical = 8.dp)
    )
}

@Composable
fun StatCard(
    value: String,
    label: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.10f)),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.headlineMedium,
                color = color
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = color.copy(alpha = 0.8f),
                textAlign = TextAlign.Center,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

/**
 * Renders the report photo from a local content:// or file:// URI.
 *
 * Images are stored locally only (Firebase Storage requires a paid plan), so the
 * URI only resolves on the device that created the report. On other devices —
 * or when the source file has been removed — we show a placeholder.
 */
@Composable
fun ReportImage(
    uri: String,
    contentDescription: String?,
    modifier: Modifier = Modifier
) {
    if (uri.isBlank()) {
        ImageUnavailablePlaceholder(modifier = modifier)
        return
    }
    SubcomposeAsyncImage(
        model = uri,
        contentDescription = contentDescription,
        contentScale = ContentScale.Crop,
        modifier = modifier,
        loading = {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.size(28.dp),
                    strokeWidth = 2.dp
                )
            }
        },
        error = { ImageUnavailablePlaceholder(modifier = Modifier.fillMaxSize()) }
    )
}

@Composable
private fun ImageUnavailablePlaceholder(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.background(MaterialTheme.colorScheme.surfaceVariant),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(
                imageVector = Icons.Filled.BrokenImage,
                contentDescription = null,
                modifier = Modifier.size(40.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "Image unavailable",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

fun categoryIcon(category: ReportCategory): ImageVector = when (category) {
    ReportCategory.DAMAGED_ROAD -> Icons.Filled.Warning
    ReportCategory.GARBAGE -> Icons.Filled.Delete
    ReportCategory.BROKEN_STREETLIGHT -> Icons.Filled.LightbulbCircle
    ReportCategory.WATER_LEAKAGE -> Icons.Filled.WaterDrop
    ReportCategory.ILLEGAL_DUMPING -> Icons.Filled.ReportProblem
    ReportCategory.PUBLIC_SAFETY -> Icons.Filled.Security
}

fun categoryColor(category: ReportCategory): Color = when (category) {
    ReportCategory.DAMAGED_ROAD -> Color(0xFFE53935)
    ReportCategory.GARBAGE -> Color(0xFF43A047)
    ReportCategory.BROKEN_STREETLIGHT -> Color(0xFFFDD835)
    ReportCategory.WATER_LEAKAGE -> Color(0xFF1E88E5)
    ReportCategory.ILLEGAL_DUMPING -> Color(0xFFE65100)
    ReportCategory.PUBLIC_SAFETY -> Color(0xFF8E24AA)
}
