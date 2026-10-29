package com.example.data.model

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

enum class UserRole(val displayName: String) {
    MANAGER("Manager"),
    TEAM_LEAD("Team Lead"),
    TEAM_MEMBER("Team Member")
}

data class User(
    val id: String,
    val name: String,
    val email: String,
    val role: UserRole,
    val title: String,
    val avatarInitials: String,
    val teamId: String? = null
)

enum class ClientStatus(val label: String) {
    ACTIVE("Active"),
    IN_NEGOTIATION("In Review"),
    COMPLETED("Archived")
}

data class Client(
    val id: String,
    val name: String,
    val company: String,
    val email: String,
    val phone: String,
    val industry: String,
    val contractValue: String,
    val status: ClientStatus,
    val createdAt: Long = System.currentTimeMillis(),
    val isDemo: Boolean = false
)

data class Team(
    val id: String,
    val name: String,
    val leadId: String,
    val leadName: String,
    val memberIds: List<String>,
    val department: String,
    val isDemo: Boolean = false
)

enum class ProjectStatus(val label: String) {
    PLANNING("Planning"),
    IN_PROGRESS("In Progress"),
    ON_HOLD("On Hold"),
    COMPLETED("Completed")
}

enum class Priority(val label: String, val level: Int) {
    LOW("Low", 1),
    MEDIUM("Medium", 2),
    HIGH("High", 3),
    URGENT("Urgent", 4)
}

data class Project(
    val id: String,
    val title: String,
    val clientId: String,
    val clientName: String,
    val teamId: String,
    val teamName: String,
    val status: ProjectStatus,
    val priority: Priority,
    val startDate: String,
    val dueDate: String,
    val dueEpochDays: Long, // Days relative to current
    val budget: String,
    val progressPercent: Int,
    val isDemo: Boolean = false
)

enum class TaskStatus(val label: String) {
    NEW("New"),
    IN_PROGRESS("In Progress"),
    PENDING_REVIEW("In Review"),
    DONE("Done")
}

enum class ProofStatus(val label: String) {
    PENDING("Under Review"),
    APPROVED("Approved"),
    REJECTED("Revision Requested")
}

data class SubmissionProof(
    val id: String,
    val taskId: String,
    val memberId: String,
    val memberName: String,
    val notes: String,
    val attachmentName: String,
    val attachmentType: String = "DOCUMENT", // "IMAGE", "DOCUMENT", "CODE"
    val localMediaUri: String? = null,
    val submittedAt: String,
    val status: ProofStatus = ProofStatus.PENDING,
    val reviewerFeedback: String? = null,
    val reviewedBy: String? = null,
    val reviewedAt: String? = null
)

data class TaskAuditLog(
    val id: String,
    val timestamp: String,
    val actorName: String,
    val actorRole: UserRole,
    val changeType: String, // "CREATED", "STATUS_CHANGED", "REASSIGNED", "PROOF_SUBMITTED", "REVIEW_APPROVED", "REVIEW_REJECTED", "PRIORITY_CHANGED"
    val summary: String,
    val previousValue: String? = null,
    val newValue: String? = null
)

data class TaskItem(
    val id: String,
    val title: String,
    val description: String,
    val projectId: String,
    val projectName: String,
    val assignedMemberId: String,
    val assignedMemberName: String,
    val teamLeadId: String,
    val status: TaskStatus,
    val priority: Priority,
    val daysUntilDue: Int, // e.g. -1 overdue, 0 today, 2 days, 5 days
    val dueFormatted: String,
    val submission: SubmissionProof? = null,
    val auditHistory: List<TaskAuditLog> = emptyList(),
    val isDemo: Boolean = false
)

data class DeadlineAlert(
    val taskId: String,
    val taskTitle: String,
    val projectName: String,
    val assignedTo: String,
    val daysRemaining: Int,
    val isOverdue: Boolean,
    val alertType: AlertType
)

enum class AlertType {
    OVERDUE,
    MANAGER_2_DAYS,
    LEAD_5_DAYS,
    UPCOMING
}

enum class NotificationCategory(val title: String) {
    TASK_ASSIGNMENT("Task Assignment"),
    DEADLINE_APPROACHING("Approaching Deadline"),
    TASK_APPROVAL("Task Approved"),
    TASK_REVISION("Revision Requested"),
    PROOF_SUBMISSION("Proof Submitted")
}

data class AppNotification(
    val id: String,
    val title: String,
    val message: String,
    val category: NotificationCategory,
    val timestamp: String,
    val isRead: Boolean = false,
    val taskId: String? = null,
    val projectId: String? = null,
    val targetRole: UserRole? = null,
    val targetUserId: String? = null
)

enum class DateRangeFilter(val label: String) {
    ALL("All Timelines"),
    OVERDUE("Overdue Only"),
    DUE_2_DAYS("Due in ≤ 2 Days"),
    DUE_5_DAYS("Due in ≤ 5 Days"),
    DUE_THIS_WEEK("Due within 7 Days")
}

data class AuditLogEntry(
    val id: String,
    val timestamp: String,
    val actorName: String,
    val actorRole: UserRole,
    val action: String,
    val targetType: String,
    val details: String
)

data class DashboardMetrics(
    val totalClients: Int,
    val activeProjects: Int,
    val pendingTasks: Int,
    val reviewQueueCount: Int,
    val overdueTasksCount: Int,
    val completionRatePercent: Int,
    val activeAlertsCount: Int
)

fun getCurrentFormattedTimestamp(): String {
    val sdf = SimpleDateFormat("MMM dd, yyyy · HH:mm", Locale.getDefault())
    return sdf.format(Date())
}
