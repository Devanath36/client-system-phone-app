package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.data.model.UserRole
import com.example.ui.components.*
import com.example.ui.screens.*
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.CmsScreen
import com.example.ui.viewmodel.CmsViewModel
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private val viewModel: CmsViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val isDarkMode by viewModel.isDarkMode.collectAsState()
            val currentUser by viewModel.currentUser.collectAsState()
            val currentScreen by viewModel.currentScreen.collectAsState()
            val clients by viewModel.clients.collectAsState()
            val teams by viewModel.teams.collectAsState()
            val projects by viewModel.projects.collectAsState()
            val tasks by viewModel.tasks.collectAsState()
            val auditLogs by viewModel.auditLogs.collectAsState()
            val notifications by viewModel.notifications.collectAsState()
            val systemUsers = viewModel.systemUsers

            val searchQuery by viewModel.searchQuery.collectAsState()
            val taskStatusFilter by viewModel.taskStatusFilter.collectAsState()
            val taskPriorityFilter by viewModel.taskPriorityFilter.collectAsState()
            val dateRangeFilter by viewModel.dateRangeFilter.collectAsState()
            val assignedMemberFilter by viewModel.assignedMemberFilter.collectAsState()

            // Dialog states
            val showRoleSwitcher by viewModel.showRoleSwitcherDialog.collectAsState()
            val showNotifications by viewModel.showNotificationsSheet.collectAsState()
            val showFilterSheet by viewModel.showFilterSheet.collectAsState()
            val showAddClient by viewModel.showAddClientDialog.collectAsState()
            val showAddProject by viewModel.showAddProjectDialog.collectAsState()
            val showAddTask by viewModel.showAddTaskDialog.collectAsState()
            val proofTaskToSubmit by viewModel.proofTaskToSubmit.collectAsState()
            val proofTaskToReview by viewModel.proofTaskToReview.collectAsState()
            val selectedTaskDetail by viewModel.selectedTaskDetail.collectAsState()

            val uiMessage by viewModel.uiMessage.collectAsState()

            val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
            val scope = rememberCoroutineScope()
            val snackbarHostState = remember { SnackbarHostState() }

            val deadlineAlerts = remember(tasks, currentUser) {
                viewModel.getDeadlineAlerts()
            }
            val metrics = remember(tasks, clients, projects, currentUser) {
                viewModel.getDashboardMetrics()
            }

            val unreadNotifCount = remember(notifications, deadlineAlerts) {
                notifications.count { !it.isRead } + deadlineAlerts.size
            }

            // Show snackbar notifications
            LaunchedEffect(uiMessage) {
                uiMessage?.let {
                    snackbarHostState.showSnackbar(it.message)
                    viewModel.clearUiMessage()
                }
            }

            // Handle back navigation
            BackHandler(enabled = drawerState.isOpen || selectedTaskDetail != null || currentScreen != CmsScreen.DASHBOARD) {
                if (drawerState.isOpen) {
                    scope.launch { drawerState.close() }
                } else if (selectedTaskDetail != null) {
                    viewModel.setSelectedTaskDetail(null)
                } else {
                    viewModel.handleBackPress()
                }
            }

            MyApplicationTheme(darkTheme = isDarkMode) {
                ModalNavigationDrawer(
                    drawerState = drawerState,
                    drawerContent = {
                        ModalDrawerSheet(
                            modifier = Modifier.width(310.dp)
                        ) {
                            NavDrawerContent(
                                currentScreen = currentScreen,
                                currentUser = currentUser,
                                reviewQueueCount = metrics.reviewQueueCount,
                                overdueCount = metrics.overdueTasksCount,
                                onSelectScreen = { screen ->
                                    viewModel.navigateTo(screen)
                                },
                                onOpenRoleSwitcher = {
                                    scope.launch { drawerState.close() }
                                    viewModel.setShowRoleSwitcher(true)
                                },
                                onResetDemoData = {
                                    viewModel.resetDemoData()
                                },
                                onCloseDrawer = {
                                    scope.launch { drawerState.close() }
                                }
                            )
                        }
                    }
                ) {
                    Scaffold(
                        modifier = Modifier
                            .fillMaxSize()
                            .testTag("cms_root_scaffold"),
                        topBar = {
                            TopHeaderBar(
                                currentUser = currentUser,
                                alertCount = unreadNotifCount,
                                isDarkMode = isDarkMode,
                                onToggleDarkMode = { viewModel.toggleDarkMode() },
                                onOpenRoleSwitcher = { viewModel.setShowRoleSwitcher(true) },
                                onOpenNotifications = { viewModel.setShowNotificationsSheet(true) },
                                onOpenMenu = { scope.launch { drawerState.open() } }
                            )
                        },
                        snackbarHost = { SnackbarHost(snackbarHostState) }
                    ) { innerPadding ->
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(innerPadding)
                        ) {
                            when (currentScreen) {
                                CmsScreen.DASHBOARD -> {
                                    DashboardScreen(
                                        currentUser = currentUser,
                                        metrics = metrics,
                                        alerts = deadlineAlerts,
                                        projects = projects,
                                        auditLogs = auditLogs,
                                        onNavigate = { screen -> viewModel.navigateTo(screen) },
                                        onOpenAddClient = { viewModel.setShowAddClientDialog(true) },
                                        onOpenAddProject = { viewModel.setShowAddProjectDialog(true) },
                                        onOpenAddTask = { viewModel.setShowAddTaskDialog(true) },
                                        onOpenNotifications = { viewModel.setShowNotificationsSheet(true) }
                                    )
                                }
                                CmsScreen.CLIENTS -> {
                                    ClientsScreen(
                                        clients = clients,
                                        userRole = currentUser.role,
                                        searchQuery = searchQuery,
                                        onSearchChange = { viewModel.setSearchQuery(it) },
                                        onOpenAddClient = { viewModel.setShowAddClientDialog(true) },
                                        onDeleteClient = { viewModel.removeClient(it) }
                                    )
                                }
                                CmsScreen.PROJECTS -> {
                                    ProjectsScreen(
                                        projects = projects,
                                        userRole = currentUser.role,
                                        searchQuery = searchQuery,
                                        onSearchChange = { viewModel.setSearchQuery(it) },
                                        onOpenAddProject = { viewModel.setShowAddProjectDialog(true) }
                                    )
                                }
                                CmsScreen.TASKS -> {
                                    TasksScreen(
                                        tasks = tasks,
                                        currentUser = currentUser,
                                        searchQuery = searchQuery,
                                        onSearchChange = { viewModel.setSearchQuery(it) },
                                        statusFilter = taskStatusFilter,
                                        onStatusFilterChange = { viewModel.setTaskStatusFilter(it) },
                                        priorityFilter = taskPriorityFilter,
                                        onPriorityFilterChange = { viewModel.setTaskPriorityFilter(it) },
                                        dateRangeFilter = dateRangeFilter,
                                        assignedMemberFilter = assignedMemberFilter,
                                        onOpenFilterSheet = { viewModel.setShowFilterSheet(true) },
                                        onOpenAddTask = { viewModel.setShowAddTaskDialog(true) },
                                        onOpenSubmitProof = { viewModel.openSubmitProofDialog(it) },
                                        onOpenReviewProof = { viewModel.openReviewProofDialog(it) },
                                        onOpenTaskDetail = { viewModel.setSelectedTaskDetail(it) },
                                        onUpdateStatus = { taskId, status -> viewModel.updateTaskStatus(taskId, status) }
                                    )
                                }
                                CmsScreen.REVIEW_QUEUE -> {
                                    ReviewQueueScreen(
                                        tasks = tasks,
                                        currentUser = currentUser,
                                        onOpenReviewProof = { viewModel.openReviewProofDialog(it) }
                                    )
                                }
                                CmsScreen.TEAMS -> {
                                    TeamsScreen(
                                        teams = teams,
                                        users = systemUsers,
                                        tasks = tasks,
                                        currentUser = currentUser
                                    )
                                }
                                CmsScreen.ANALYTICS -> {
                                    AnalyticsScreen(
                                        metrics = metrics,
                                        tasks = tasks,
                                        projects = projects,
                                        currentUser = currentUser
                                    )
                                }
                                CmsScreen.AUDIT_LOG -> {
                                    AuditLogScreen(logs = auditLogs)
                                }
                            }
                        }

                        // Task Details Modal with Per-Task Audit Trail
                        selectedTaskDetail?.let { task ->
                            TaskDetailDialog(
                                task = task,
                                currentUser = currentUser,
                                allUsers = systemUsers,
                                onDismiss = { viewModel.setSelectedTaskDetail(null) },
                                onUpdateStatus = { newStatus -> viewModel.updateTaskStatus(task.id, newStatus) },
                                onReassign = { newMemberId -> viewModel.reassignTask(task.id, newMemberId) },
                                onOpenSubmitProof = { viewModel.openSubmitProofDialog(task) },
                                onOpenReviewProof = { viewModel.openReviewProofDialog(task) }
                            )
                        }

                        // Advanced Filter Sheet
                        if (showFilterSheet) {
                            AdvancedFilterSheet(
                                dateRangeFilter = dateRangeFilter,
                                assignedMemberFilter = assignedMemberFilter,
                                priorityFilter = taskPriorityFilter,
                                allMembers = systemUsers,
                                onDateRangeChange = { viewModel.setDateRangeFilter(it) },
                                onAssignedMemberChange = { viewModel.setAssignedMemberFilter(it) },
                                onPriorityChange = { viewModel.setTaskPriorityFilter(it) },
                                onClearAll = { viewModel.clearAllFilters() },
                                onDismiss = { viewModel.setShowFilterSheet(false) }
                            )
                        }

                        // Role Switcher Dialog
                        if (showRoleSwitcher) {
                            RoleSwitcherDialog(
                                users = systemUsers,
                                currentUser = currentUser,
                                onSelectUser = { userId -> viewModel.selectUser(userId) },
                                onDismiss = { viewModel.setShowRoleSwitcher(false) }
                            )
                        }

                        // Real-time Notification & Deadline Center
                        if (showNotifications) {
                            RealtimeNotificationSheet(
                                notifications = notifications,
                                alerts = deadlineAlerts,
                                userRole = currentUser.role,
                                onDismiss = { viewModel.setShowNotificationsSheet(false) },
                                onMarkAllRead = { viewModel.markAllNotificationsAsRead() },
                                onInspectTask = { taskId ->
                                    val targetTask = tasks.find { it.id == taskId }
                                    if (targetTask != null) {
                                        viewModel.setSelectedTaskDetail(targetTask)
                                    }
                                    viewModel.navigateTo(CmsScreen.TASKS)
                                }
                            )
                        }

                        // Add Client Dialog
                        if (showAddClient) {
                            AddClientDialog(
                                onDismiss = { viewModel.setShowAddClientDialog(false) },
                                onConfirm = { name, company, email, phone, industry, contractValue, status ->
                                    viewModel.createClient(name, company, email, phone, industry, contractValue, status)
                                }
                            )
                        }

                        // Add Project Dialog
                        if (showAddProject) {
                            AddProjectDialog(
                                clients = clients,
                                teams = teams,
                                onDismiss = { viewModel.setShowAddProjectDialog(false) },
                                onConfirm = { title, clientId, teamId, priority, startDate, dueDate, dueDays, budget ->
                                    viewModel.createProject(title, clientId, teamId, priority, startDate, dueDate, dueDays, budget)
                                }
                            )
                        }

                        // Add Task Dialog
                        if (showAddTask) {
                            AddTaskDialog(
                                projects = projects,
                                members = systemUsers,
                                onDismiss = { viewModel.setShowAddTaskDialog(false) },
                                onConfirm = { title, desc, projId, memberId, priority, dueDays, dueFormatted ->
                                    viewModel.createTask(title, desc, projId, memberId, priority, dueDays, dueFormatted)
                                }
                            )
                        }

                        // Submit Proof Dialog (Member workflow)
                        proofTaskToSubmit?.let { task ->
                            SubmitProofDialog(
                                task = task,
                                onDismiss = { viewModel.closeSubmitProofDialog() },
                                onSubmit = { notes, attachment, type, mediaUri ->
                                    viewModel.submitProof(task.id, notes, attachment, type, mediaUri)
                                }
                            )
                        }

                        // Review Proof Dialog (Lead & Manager workflow)
                        proofTaskToReview?.let { task ->
                            ReviewProofDialog(
                                task = task,
                                onDismiss = { viewModel.closeReviewProofDialog() },
                                onReview = { approve, feedback ->
                                    viewModel.reviewProof(task.id, approve, feedback)
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}
