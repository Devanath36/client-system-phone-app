package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.*
import com.example.ui.theme.*

@Composable
fun AnalyticsScreen(
    metrics: DashboardMetrics,
    tasks: List<TaskItem>,
    projects: List<Project>,
    currentUser: User
) {
    val newCount = tasks.count { it.status == TaskStatus.NEW }
    val inProgressCount = tasks.count { it.status == TaskStatus.IN_PROGRESS }
    val reviewCount = tasks.count { it.status == TaskStatus.PENDING_REVIEW }
    val doneCount = tasks.count { it.status == TaskStatus.DONE }
    val totalTasks = tasks.size.coerceAtLeast(1)

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 90.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header summary
        item {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 2.dp,
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("analytics_header_card")
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        text = "Operational Analytics & SLA Health",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    Text(
                        text = "Cross-client delivery efficiency and throughput metrics",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(text = "Overall SLA Compliance", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(
                                text = "${100 - (metrics.overdueTasksCount * 100 / totalTasks)}%",
                                fontSize = 24.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = StatusDone
                            )
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text(text = "Total Deliverables", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(
                                text = tasks.size.toString(),
                                fontSize = 24.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }
        }

        // Task Status Distribution Chart
        item {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 2.dp,
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        text = "Task Status Distribution",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                    Text(
                        text = "Real-time pipeline breakdown across all projects",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Multi-colored segment bar
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(12.dp)
                            .clip(RoundedCornerShape(6.dp))
                    ) {
                        if (doneCount > 0) {
                            Box(
                                modifier = Modifier
                                    .weight(doneCount.toFloat())
                                    .fillMaxHeight()
                                    .background(StatusDone)
                            )
                        }
                        if (reviewCount > 0) {
                            Box(
                                modifier = Modifier
                                    .weight(reviewCount.toFloat())
                                    .fillMaxHeight()
                                    .background(StatusReview)
                            )
                        }
                        if (inProgressCount > 0) {
                            Box(
                                modifier = Modifier
                                    .weight(inProgressCount.toFloat())
                                    .fillMaxHeight()
                                    .background(StatusInProgress)
                            )
                        }
                        if (newCount > 0) {
                            Box(
                                modifier = Modifier
                                    .weight(newCount.toFloat())
                                    .fillMaxHeight()
                                    .background(StatusNew)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Legend & Counts
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        StatusLegendItem(label = "Done / Approved", count = doneCount, total = totalTasks, color = StatusDone)
                        StatusLegendItem(label = "Under Review", count = reviewCount, total = totalTasks, color = StatusReview)
                        StatusLegendItem(label = "In Progress", count = inProgressCount, total = totalTasks, color = StatusInProgress)
                        StatusLegendItem(label = "New / Backlog", count = newCount, total = totalTasks, color = StatusNew)
                    }
                }
            }
        }

        // Project Health Breakdown
        item {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 2.dp,
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        text = "Project Delivery Health",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    projects.forEach { proj ->
                        Column(modifier = Modifier.padding(vertical = 6.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = proj.title,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    modifier = Modifier.weight(1f)
                                )
                                Text(
                                    text = "${proj.progressPercent}%",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (proj.progressPercent == 100) StatusDone else MaterialTheme.colorScheme.primary
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            LinearProgressIndicator(
                                progress = { proj.progressPercent / 100f },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(6.dp)
                                    .clip(RoundedCornerShape(3.dp)),
                                color = if (proj.progressPercent >= 80) StatusDone else MaterialTheme.colorScheme.primary,
                                trackColor = MaterialTheme.colorScheme.surfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun StatusLegendItem(label: String, count: Int, total: Int, color: Color) {
    val percent = (count * 100) / total
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .clip(CircleShape)
                    .background(color)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = label, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface)
        }

        Text(
            text = "$count tasks ($percent%)",
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
