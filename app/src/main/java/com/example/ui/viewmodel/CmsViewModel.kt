package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.example.data.model.*
import com.example.data.repository.CmsRepository
import kotlinx.coroutines.flow.*

enum class CmsScreen(val title: String) {
    DASHBOARD("Executive Dashboard"),
    CLIENTS("Client Directory"),
    PROJECTS("Projects & Milestones"),
    TASKS("Tasks & Kanban"),
    REVIEW_QUEUE("Proof Review Queue"),
    TEAMS("Teams & Organization"),
    ANALYTICS("Analytics & Reports"),
    AUDIT_LOG("Compliance Audit Log")
}

data class UiNotificationMessage(
    val id: Long = System.currentTimeMillis(),
    val message: String,
    val isError: Boolean = false
)

class CmsViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = CmsRepository(application)

    val currentUser = repository.currentUser
    val systemUsers = repository.systemUsers
    val clients = repository.clients
    val teams = repository.teams
    val projects = repository.projects
    val tasks = repository.tasks
    val auditLogs = repository.auditLogs
    val notifications = repository.notifications

    // Active screen
    private val _currentScreen = MutableStateFlow(CmsScreen.DASHBOARD)
    val currentScreen: StateFlow<CmsScreen> = _currentScreen.asStateFlow()

    // Navigation backstack
    private val screenStack = mutableListOf(CmsScreen.DASHBOARD)

    // Dark mode toggle
    private val _isDarkMode = MutableStateFlow(false)
    val isDarkMode: StateFlow<Boolean> = _isDarkMode.asStateFlow()

    // Search and filters
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _taskStatusFilter = MutableStateFlow<TaskStatus?>(null)
    val taskStatusFilter: StateFlow<TaskStatus?> = _taskStatusFilter.asStateFlow()

    private val _taskPriorityFilter = MutableStateFlow<Priority?>(null)
    val taskPriorityFilter: StateFlow<Priority?> = _taskPriorityFilter.asStateFlow()

    private val _dateRangeFilter = MutableStateFlow(DateRangeFilter.ALL)
    val dateRangeFilter: StateFlow<DateRangeFilter> = _dateRangeFilter.asStateFlow()

    private val _assignedMemberFilter = MutableStateFlow<String?>(null)
    val assignedMemberFilter: StateFlow<String?> = _assignedMemberFilter.asStateFlow()

    private val _selectedClientFilter = MutableStateFlow<String?>(null)
    val selectedClientFilter: StateFlow<String?> = _selectedClientFilter.asStateFlow()

    // Dialog & Sheet States
    private val _showRoleSwitcherDialog = MutableStateFlow(false)
    val showRoleSwitcherDialog: StateFlow<Boolean> = _showRoleSwitcherDialog.asStateFlow()

    private val _showNotificationsSheet = MutableStateFlow(false)
    val showNotificationsSheet: StateFlow<Boolean> = _showNotificationsSheet.asStateFlow()

    private val _showFilterSheet = MutableStateFlow(false)
    val showFilterSheet: StateFlow<Boolean> = _showFilterSheet.asStateFlow()

    private val _showAddClientDialog = MutableStateFlow(false)
    val showAddClientDialog: StateFlow<Boolean> = _showAddClientDialog.asStateFlow()

    private val _showAddProjectDialog = MutableStateFlow(false)
    val showAddProjectDialog: StateFlow<Boolean> = _showAddProjectDialog.asStateFlow()

    private val _showAddTaskDialog = MutableStateFlow(false)
    val showAddTaskDialog: StateFlow<Boolean> = _showAddTaskDialog.asStateFlow()

    private val _proofTaskToSubmit = MutableStateFlow<TaskItem?>(null)
    val proofTaskToSubmit: StateFlow<TaskItem?> = _proofTaskToSubmit.asStateFlow()

    private val _proofTaskToReview = MutableStateFlow<TaskItem?>(null)
    val proofTaskToReview: StateFlow<TaskItem?> = _proofTaskToReview.asStateFlow()

    private val _selectedTaskDetail = MutableStateFlow<TaskItem?>(null)
    val selectedTaskDetail: StateFlow<TaskItem?> = _selectedTaskDetail.asStateFlow()

    // Transient UI toast message
    private val _uiMessage = MutableStateFlow<UiNotificationMessage?>(null)
    val uiMessage: StateFlow<UiNotificationMessage?> = _uiMessage.asStateFlow()

    fun navigateTo(screen: CmsScreen) {
        if (_currentScreen.value != screen) {
            screenStack.add(screen)
            _currentScreen.value = screen
        }
    }

    fun handleBackPress(): Boolean {
        if (_selectedTaskDetail.value != null) {
            _selectedTaskDetail.value = null
            return true
        }
        if (screenStack.size > 1) {
            screenStack.removeAt(screenStack.lastIndex)
            _currentScreen.value = screenStack.last()
            return true
        }
        return false
    }

    fun toggleDarkMode() {
        _isDarkMode.value = !_isDarkMode.value
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setTaskStatusFilter(status: TaskStatus?) {
        _taskStatusFilter.value = status
    }

    fun setTaskPriorityFilter(priority: Priority?) {
        _taskPriorityFilter.value = priority
    }

    fun setDateRangeFilter(range: DateRangeFilter) {
        _dateRangeFilter.value = range
    }

    fun setAssignedMemberFilter(memberId: String?) {
        _assignedMemberFilter.value = memberId
    }

    fun setSelectedClientFilter(clientId: String?) {
        _selectedClientFilter.value = clientId
    }

    fun clearAllFilters() {
        _searchQuery.value = ""
        _taskStatusFilter.value = null
        _taskPriorityFilter.value = null
        _dateRangeFilter.value = DateRangeFilter.ALL
        _assignedMemberFilter.value = null
        _selectedClientFilter.value = null
        showToast("All filters reset")
    }

    fun setShowFilterSheet(show: Boolean) {
        _showFilterSheet.value = show
    }

    // Role switching
    fun selectUser(userId: String) {
        repository.switchUser(userId)
        _showRoleSwitcherDialog.value = false
        val user = systemUsers.find { it.id == userId }
        showToast("Switched role to ${user?.name} (${user?.role?.displayName})")
    }

    fun setShowRoleSwitcher(show: Boolean) {
        _showRoleSwitcherDialog.value = show
    }

    fun setShowNotificationsSheet(show: Boolean) {
        _showNotificationsSheet.value = show
    }

    fun setShowAddClientDialog(show: Boolean) {
        if (show && currentUser.value.role != UserRole.MANAGER) {
            showToast("Role restriction: Only Managers can create clients.", isError = true)
            return
        }
        _showAddClientDialog.value = show
    }

    fun setShowAddProjectDialog(show: Boolean) {
        if (show && currentUser.value.role != UserRole.MANAGER) {
            showToast("Role restriction: Only Managers can create projects.", isError = true)
            return
        }
        _showAddProjectDialog.value = show
    }

    fun setShowAddTaskDialog(show: Boolean) {
        if (show && currentUser.value.role == UserRole.TEAM_MEMBER) {
            showToast("Role restriction: Only Managers and Team Leads can create tasks.", isError = true)
            return
        }
        _showAddTaskDialog.value = show
    }

    fun openSubmitProofDialog(task: TaskItem) {
        _proofTaskToSubmit.value = task
    }

    fun closeSubmitProofDialog() {
        _proofTaskToSubmit.value = null
    }

    fun openReviewProofDialog(task: TaskItem) {
        if (currentUser.value.role == UserRole.TEAM_MEMBER) {
            showToast("Role restriction: Only Team Leads and Managers can review submissions.", isError = true)
            return
        }
        _proofTaskToReview.value = task
    }

    fun closeReviewProofDialog() {
        _proofTaskToReview.value = null
    }

    fun setSelectedTaskDetail(task: TaskItem?) {
        _selectedTaskDetail.value = task
    }

    fun clearUiMessage() {
        _uiMessage.value = null
    }

    private fun showToast(msg: String, isError: Boolean = false) {
        _uiMessage.value = UiNotificationMessage(message = msg, isError = isError)
    }

    // Client Actions
    fun createClient(
        name: String,
        company: String,
        email: String,
        phone: String,
        industry: String,
        contractValue: String,
        status: ClientStatus
    ) {
        if (currentUser.value.role != UserRole.MANAGER) {
            showToast("Unauthorized: Manager privileges required.", isError = true)
            return
        }
        repository.addClient(name, company, email, phone, industry, contractValue, status)
        _showAddClientDialog.value = false
        showToast("Client '$company' registered successfully.")
    }

    fun removeClient(clientId: String) {
        if (currentUser.value.role != UserRole.MANAGER) {
            showToast("Unauthorized: Manager privileges required.", isError = true)
            return
        }
        repository.deleteClient(clientId)
        showToast("Client removed from directory.")
    }

    // Project Actions
    fun createProject(
        title: String,
        clientId: String,
        teamId: String,
        priority: Priority,
        startDate: String,
        dueDate: String,
        dueDays: Int,
        budget: String
    ) {
        if (currentUser.value.role != UserRole.MANAGER) {
            showToast("Unauthorized: Manager privileges required.", isError = true)
            return
        }
        repository.addProject(title, clientId, teamId, priority, startDate, dueDate, dueDays, budget)
        _showAddProjectDialog.value = false
        showToast("Project '$title' initialized and assigned.")
    }

    // Task Actions
    fun createTask(
        title: String,
        description: String,
        projectId: String,
        assignedMemberId: String,
        priority: Priority,
        daysUntilDue: Int,
        dueFormatted: String
    ) {
        if (currentUser.value.role == UserRole.TEAM_MEMBER) {
            showToast("Unauthorized: Only Leads and Managers can assign tasks.", isError = true)
            return
        }
        repository.addTask(title, description, projectId, assignedMemberId, priority, daysUntilDue, dueFormatted)
        _showAddTaskDialog.value = false
        showToast("Task assigned to team member.")
    }

    fun updateTaskStatus(taskId: String, newStatus: TaskStatus) {
        repository.updateTaskStatus(taskId, newStatus)
        // Refresh selected task detail if open
        if (_selectedTaskDetail.value?.id == taskId) {
            _selectedTaskDetail.value = tasks.value.find { it.id == taskId }
        }
        showToast("Task status updated to ${newStatus.label}")
    }

    fun reassignTask(taskId: String, newMemberId: String) {
        repository.reassignTask(taskId, newMemberId)
        if (_selectedTaskDetail.value?.id == taskId) {
            _selectedTaskDetail.value = tasks.value.find { it.id == taskId }
        }
        showToast("Task reassigned.")
    }

    fun submitProof(taskId: String, notes: String, attachmentName: String, attachmentType: String, localMediaUri: String? = null) {
        repository.submitProof(taskId, notes, attachmentName, attachmentType, localMediaUri)
        _proofTaskToSubmit.value = null
        if (_selectedTaskDetail.value?.id == taskId) {
            _selectedTaskDetail.value = tasks.value.find { it.id == taskId }
        }
        showToast("Proof submitted! Sent to Team Lead for verification.")
    }

    fun reviewProof(taskId: String, approve: Boolean, feedback: String) {
        if (currentUser.value.role == UserRole.TEAM_MEMBER) {
            showToast("Unauthorized: Only Leads and Managers can review submissions.", isError = true)
            return
        }
        repository.reviewProof(taskId, approve, feedback)
        _proofTaskToReview.value = null
        if (_selectedTaskDetail.value?.id == taskId) {
            _selectedTaskDetail.value = tasks.value.find { it.id == taskId }
        }
        if (approve) {
            showToast("Submission approved! Task marked as Done.")
        } else {
            showToast("Revision requested. Task returned to In Progress.")
        }
    }

    fun markNotificationAsRead(notifId: String) {
        repository.markNotificationAsRead(notifId)
    }

    fun markAllNotificationsAsRead() {
        repository.markAllNotificationsAsRead()
        showToast("All notifications marked as read.")
    }

    fun resetDemoData() {
        repository.resetToDefaultSampleData()
        showToast("Demo dataset restored to initial state.")
    }

    fun getDeadlineAlerts(): List<DeadlineAlert> {
        return repository.getDeadlineAlertsForCurrentUser()
    }

    fun getDashboardMetrics(): DashboardMetrics {
        return repository.getDashboardMetrics()
    }
}
