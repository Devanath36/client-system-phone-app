package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.*
import com.example.ui.theme.*

@Composable
fun TasksScreen(
    tasks: List<TaskItem>,
    currentUser: User,
    searchQuery: String,
    onSearchChange: (String) -> Unit,
    statusFilter: TaskStatus?,
    onStatusFilterChange: (TaskStatus?) -> Unit,
    priorityFilter: Priority?,
    onPriorityFilterChange: (Priority?) -> Unit,
    dateRangeFilter: DateRangeFilter,
    assignedMemberFilter: String?,
    onOpenFilterSheet: () -> Unit,
    onOpenAddTask: () -> Unit,
    onOpenSubmitProof: (TaskItem) -> Unit,
    onOpenReviewProof: (TaskItem) -> Unit,
    onOpenTaskDetail: (TaskItem) -> Unit,
    onUpdateStatus: (String, TaskStatus) -> Unit
) {
    val filteredTasks = remember(tasks, searchQuery, statusFilter, priorityFilter, dateRangeFilter, assignedMemberFilter, currentUser) {
        tasks.filter { task ->
            val matchesQuery = searchQuery.isBlank() ||
                    task.title.contains(searchQuery, ignoreCase = true) ||
                    task.projectName.contains(searchQuery, ignoreCase = true) ||
                    task.assignedMemberName.contains(searchQuery, ignoreCase = true) ||
                    task.description.contains(searchQuery, ignoreCase = true)

            val matchesStatus = statusFilter == null || task.status == statusFilter
            val matchesPriority = priorityFilter == null || task.priority == priorityFilter
            val matchesMember = assignedMemberFilter == null || task.assignedMemberId == assignedMemberFilter

            val matchesDateRange = when (dateRangeFilter) {
                DateRangeFilter.ALL -> true
                DateRangeFilter.OVERDUE -> task.daysUntilDue < 0 && task.status != TaskStatus.DONE
                DateRangeFilter.DUE_2_DAYS -> task.daysUntilDue in 0..2 && task.status != TaskStatus.DONE
                DateRangeFilter.DUE_5_DAYS -> task.daysUntilDue in 0..5 && task.status != TaskStatus.DONE
                DateRangeFilter.DUE_THIS_WEEK -> task.daysUntilDue in 0..7 && task.status != TaskStatus.DONE
            }

            matchesQuery && matchesStatus && matchesPriority && matchesMember && matchesDateRange
        }
    }

    val activeFiltersCount = (if (statusFilter != null) 1 else 0) +
            (if (priorityFilter != null) 1 else 0) +
            (if (dateRangeFilter != DateRangeFilter.ALL) 1 else 0) +
            (if (assignedMemberFilter != null) 1 else 0)

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(top = 16.dp, bottom = 90.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Search Bar & Filter Sheet Button
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = onSearchChange,
                        placeholder = { Text("Search keywords, projects, assignees...") },
                        leadingIcon = {
                            Icon(imageVector = Icons.Default.Search, contentDescription = "Search")
                        },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { onSearchChange("") }) {
                                    Icon(imageVector = Icons.Default.Clear, contentDescription = "Clear")
                                }
                            }
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("task_search_input")
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    OutlinedButton(
                        onClick = onOpenFilterSheet,
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 14.dp),
                        modifier = Modifier.testTag("open_filters_button")
                    ) {
                        BadgedBox(
                            badge = {
                                if (activeFiltersCount > 0) {
                                    Badge { Text(activeFiltersCount.toString()) }
                                }
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.FilterList,
                                contentDescription = "Filters",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }

            // Quick Status Filter Tabs
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    FilterChip(
                        selected = statusFilter == null,
                        onClick = { onStatusFilterChange(null) },
                        label = { Text("All", fontSize = 11.sp) }
                    )
                    FilterChip(
                        selected = statusFilter == TaskStatus.NEW,
                        onClick = { onStatusFilterChange(TaskStatus.NEW) },
                        label = { Text("New", fontSize = 11.sp) }
                    )
                    FilterChip(
                        selected = statusFilter == TaskStatus.IN_PROGRESS,
                        onClick = { onStatusFilterChange(TaskStatus.IN_PROGRESS) },
                        label = { Text("In Progress", fontSize = 11.sp) }
                    )
                    FilterChip(
                        selected = statusFilter == TaskStatus.PENDING_REVIEW,
                        onClick = { onStatusFilterChange(TaskStatus.PENDING_REVIEW) },
                        label = { Text("Review", fontSize = 11.sp) }
                    )
                    FilterChip(
                        selected = statusFilter == TaskStatus.DONE,
                        onClick = { onStatusFilterChange(TaskStatus.DONE) },
                        label = { Text("Done", fontSize = 11.sp) }
                    )
                }
            }

            // Active filters pills if date range or assigned member is applied
            if (dateRangeFilter != DateRangeFilter.ALL || assignedMemberFilter != null || priorityFilter != null) {
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Active:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        if (dateRangeFilter != DateRangeFilter.ALL) {
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                            ) {
                                Text(
                                    text = dateRangeFilter.label,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                )
                            }
                        }
                        if (priorityFilter != null) {
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = StatusOverdue.copy(alpha = 0.15f)
                            ) {
                                Text(
                                    text = priorityFilter.label,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = StatusOverdue,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                )
                            }
                        }
                    }
                }
            }

            // List of Tasks
            if (filteredTasks.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(40.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.SearchOff,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = "No Matching Tasks",
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Try modifying your keywords or clearing the active filters",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            } else {
                items(filteredTasks) { task ->
                    val isAssignedToCurrent = task.assignedMemberId == currentUser.id

                    val (statusColor, statusBg) = when (task.status) {
                        TaskStatus.NEW -> Pair(StatusNew, StatusNew.copy(alpha = 0.12f))
                        TaskStatus.IN_PROGRESS -> Pair(StatusInProgress, StatusInProgress.copy(alpha = 0.12f))
                        TaskStatus.PENDING_REVIEW -> Pair(StatusReview, StatusReview.copy(alpha = 0.15f))
                        TaskStatus.DONE -> Pair(StatusDone, StatusDone.copy(alpha = 0.12f))
                    }

                    val isOverdue = task.daysUntilDue < 0 && task.status != TaskStatus.DONE

                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = MaterialTheme.colorScheme.surface,
                        tonalElevation = 2.dp,
                        border = androidx.compose.foundation.BorderStroke(
                            if (isOverdue) 1.5.dp else 1.dp,
                            if (isOverdue) StatusOverdue else MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onOpenTaskDetail(task) }
                            .testTag("task_card_${task.id}")
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            // Header: Project name + Deadline Tag
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = task.projectName,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.weight(1f)
                                )

                                Spacer(modifier = Modifier.width(8.dp))

                                val deadlineColor = when {
                                    isOverdue -> StatusOverdue
                                    task.daysUntilDue <= 2 -> StatusInProgress
                                    task.daysUntilDue <= 5 -> RoleTeamLeadColor
                                    else -> MaterialTheme.colorScheme.onSurfaceVariant
                                }

                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(deadlineColor.copy(alpha = 0.15f))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = when {
                                            isOverdue -> "OVERDUE"
                                            task.daysUntilDue == 0 -> "DUE TODAY"
                                            task.daysUntilDue == 2 -> "2 DAYS LEFT"
                                            task.daysUntilDue == 5 -> "5 DAYS LEFT"
                                            task.status == TaskStatus.DONE -> "COMPLETED"
                                            else -> task.dueFormatted
                                        },
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = deadlineColor
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(6.dp))

                            Text(
                                text = task.title,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )

                            if (task.description.isNotBlank()) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = task.description,
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            // Assignee + Status + Audit badge
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(24.dp)
                                            .clip(CircleShape)
                                            .background(MaterialTheme.colorScheme.primaryContainer),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = task.assignedMemberName.take(1),
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onPrimaryContainer
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = if (isAssignedToCurrent) "You (${task.assignedMemberName})" else task.assignedMemberName,
                                        fontSize = 11.sp,
                                        fontWeight = if (isAssignedToCurrent) FontWeight.Bold else FontWeight.Normal,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    if (task.auditHistory.isNotEmpty()) {
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(4.dp))
                                                .background(MaterialTheme.colorScheme.surfaceVariant)
                                                .padding(horizontal = 5.dp, vertical = 2.dp)
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Icon(Icons.Default.History, contentDescription = null, modifier = Modifier.size(10.dp))
                                                Spacer(modifier = Modifier.width(2.dp))
                                                Text(text = "${task.auditHistory.size}", fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                            }
                                        }
                                        Spacer(modifier = Modifier.width(6.dp))
                                    }

                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(statusBg)
                                            .padding(horizontal = 8.dp, vertical = 3.dp)
                                    ) {
                                        Text(
                                            text = task.status.label,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = statusColor
                                        )
                                    }
                                }
                            }

                            // Action buttons
                            Spacer(modifier = Modifier.height(10.dp))
                            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))
                            Spacer(modifier = Modifier.height(8.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                TextButton(
                                    onClick = { onOpenTaskDetail(task) },
                                    contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text("Audit & Details →", fontSize = 11.sp)
                                }

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    if (task.status == TaskStatus.PENDING_REVIEW && (currentUser.role in listOf(UserRole.TEAM_LEAD, UserRole.MANAGER))) {
                                        Button(
                                            onClick = { onOpenReviewProof(task) },
                                            colors = ButtonDefaults.buttonColors(containerColor = StatusReview),
                                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                                            shape = RoundedCornerShape(8.dp),
                                            modifier = Modifier.testTag("review_task_btn_${task.id}")
                                        ) {
                                            Icon(Icons.Default.Verified, contentDescription = null, modifier = Modifier.size(14.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("Review", fontSize = 11.sp)
                                        }
                                    } else if (task.status != TaskStatus.DONE && (isAssignedToCurrent || currentUser.role == UserRole.TEAM_MEMBER)) {
                                        Button(
                                            onClick = { onOpenSubmitProof(task) },
                                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                                            shape = RoundedCornerShape(8.dp),
                                            modifier = Modifier.testTag("submit_proof_btn_${task.id}")
                                        ) {
                                            Icon(Icons.Default.UploadFile, contentDescription = null, modifier = Modifier.size(14.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("Submit Proof", fontSize = 11.sp)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // FAB to Assign Task (Manager and Team Lead only)
        if (currentUser.role in listOf(UserRole.MANAGER, UserRole.TEAM_LEAD)) {
            FloatingActionButton(
                onClick = onOpenAddTask,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(16.dp)
                    .testTag("add_task_fab")
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Assign Task")
            }
        }
    }
}
