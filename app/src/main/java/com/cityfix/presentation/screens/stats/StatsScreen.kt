package com.cityfix.presentation.screens.stats

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cityfix.domain.model.ReportCategory
import com.cityfix.presentation.theme.BrandPrimary
import com.cityfix.presentation.theme.StatusInProgress
import com.cityfix.presentation.theme.StatusNew
import com.cityfix.presentation.theme.StatusResolved

@Composable
fun StatsScreen(viewModel: StatsViewModel = hiltViewModel()) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    if (state.isLoading) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
        return
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "City Statistics",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Real-time city problem overview",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Gray
            )
        }

        // --- Overall stats grid ---
        item {
            Text(
                text = "Overview",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OverviewStatCard(
                    modifier = Modifier.weight(1f),
                    number = state.totalReports,
                    label = "Total Reports",
                    color = BrandPrimary
                )
                OverviewStatCard(
                    modifier = Modifier.weight(1f),
                    number = state.resolvedReports,
                    label = "Resolved",
                    color = StatusResolved
                )
            }
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OverviewStatCard(
                    modifier = Modifier.weight(1f),
                    number = state.newReports,
                    label = "New Issues",
                    color = StatusNew
                )
                OverviewStatCard(
                    modifier = Modifier.weight(1f),
                    number = state.inProgressReports,
                    label = "In Progress",
                    color = StatusInProgress
                )
            }
        }

        // --- My contributions ---
        item {
            Text(
                text = "My Contributions",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(8.dp))
            Card(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "${state.myReports}",
                            style = MaterialTheme.typography.headlineLarge,
                            fontWeight = FontWeight.Bold,
                            color = BrandPrimary
                        )
                        Text("My Reports", style = MaterialTheme.typography.bodySmall)
                    }
                    VerticalDivider(modifier = Modifier.height(50.dp))
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "${state.myResolved}",
                            style = MaterialTheme.typography.headlineLarge,
                            fontWeight = FontWeight.Bold,
                            color = StatusResolved
                        )
                        Text("Resolved", style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        }

        // --- Top categories bar chart ---
        if (state.topCategories.isNotEmpty()) {
            item {
                Text(
                    text = "Top Categories",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(8.dp))
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        val maxCount = state.topCategories.maxOf { it.second }.toFloat()
                        state.topCategories.forEach { (categoryKey, count) ->
                            Column {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    // Render the human-readable name when we recognise the enum,
                                    // fall back to the raw key for any unknown category.
                                    Text(
                                        text = ReportCategory.fromName(categoryKey).displayName,
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                    Text(
                                        text = "$count",
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                Spacer(Modifier.height(4.dp))
                                LinearProgressIndicator(
                                    progress = { count / maxCount },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(8.dp)
                                        .clip(RoundedCornerShape(4.dp)),
                                    color = BrandPrimary,
                                    trackColor = BrandPrimary.copy(alpha = 0.15f)
                                )
                            }
                        }
                    }
                }
            }
        }

        // --- Most voted reports ---
        if (state.mostVoted.isNotEmpty()) {
            item {
                Text(
                    text = "Most Voted Issues",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(8.dp))
            }
            items(state.mostVoted, key = { it.id }) { report ->
                Card(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Filled.ThumbUp,
                            contentDescription = null,
                            tint = BrandPrimary
                        )
                        Spacer(Modifier.width(8.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = report.title,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = ReportCategory.fromName(report.category).displayName,
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.Gray
                            )
                        }
                        Text(
                            text = "${report.voteCount} votes",
                            style = MaterialTheme.typography.labelMedium,
                            color = BrandPrimary,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun OverviewStatCard(
    modifier: Modifier = Modifier,
    number: Int,
    label: String,
    color: Color
) {
    Card(modifier = modifier) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "$number",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                color = color
            )
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray,
                textAlign = TextAlign.Center
            )
        }
    }
}
