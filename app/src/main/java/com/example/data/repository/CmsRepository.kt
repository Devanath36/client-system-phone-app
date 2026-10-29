package com.example.data.repository

import android.content.Context
import android.content.SharedPreferences
import com.example.data.model.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID

class CmsRepository(context: Context? = null) {

    private val prefs: SharedPreferences? = context?.getSharedPreferences("cms_database_store", Context.MODE_PRIVATE)

    val systemUsers = listOf(
        User(
            id = "usr_mgr_1",
            name = "Sarah Chen",
            email = "sarah.chen@enterprisecms.com",
            role = UserRole.MANAGER,
            title = "VP of Operations & Delivery",
            avatarInitials = "SC"
        ),
        User(
            id = "usr_lead_1",
            name = "Alex Morgan",
            email = "alex.morgan@enterprisecms.com",
            role = UserRole.TEAM_LEAD,
            title = "Engineering Lead · Alpha Team",
            avatarInitials = "AM",
            teamId = "team_alpha"
        ),
        User(
            id = "usr_lead_2",
            name = "Marcus Vance",
            email = "marcus.vance@enterprisecms.com",
            role = UserRole.TEAM_LEAD,
            title = "Product Design Lead · Studio",
            avatarInitials = "MV",
            teamId = "team_studio"
        ),
        User(
            id = "usr_mem_1",
            name = "Maya Lin",
            email = "maya.lin@enterprisecms.com",
            role = UserRole.TEAM_MEMBER,
            title = "Senior Full-Stack Engineer",
            avatarInitials = "ML",
            teamId = "team_alpha"
        ),
        User(
            id = "usr_mem_2",
            name = "David Kim",
            email = "david.kim@enterprisecms.com",
            role = UserRole.TEAM_MEMBER,
            title = "Cloud Infrastructure Specialist",
            avatarInitials = "DK",
            teamId = "team_alpha"
        ),
        User(
            id = "usr_mem_3",
            name = "Elena Rostova",
            email = "elena.r@enterprisecms.com",
            role = UserRole.TEAM_MEMBER,
            title = "UI/UX Product Designer",
            avatarInitials = "ER",
            teamId = "team_studio"
        )
    )

    private val _currentUser = MutableStateFlow<User>(systemUsers[0]) // Default Manager
    val currentUser: StateFlow<User> = _currentUser.asStateFlow()

    private val _clients = MutableStateFlow<List<Client>>(emptyList())
    val clients: StateFlow<List<Client>> = _clients.asStateFlow()

    private val _teams = MutableStateFlow<List<Team>>(emptyList())
    val teams: StateFlow<List<Team>> = _teams.asStateFlow()

    private val _projects = MutableStateFlow<List<Project>>(emptyList())
    val projects: StateFlow<List<Project>> = _projects.asStateFlow()

    private val _tasks = MutableStateFlow<List<TaskItem>>(emptyList())
    val tasks: StateFlow<List<TaskItem>> = _tasks.asStateFlow()

    private val _auditLogs = MutableStateFlow<List<AuditLogEntry>>(emptyList())
    val auditLogs: StateFlow<List<AuditLogEntry>> = _auditLogs.asStateFlow()

    private val _notifications = MutableStateFlow<List<AppNotification>>(emptyList())
    val notifications: StateFlow<List<AppNotification>> = _notifications.asStateFlow()

    init {
        loadData()
    }

    fun switchUser(userId: String) {
        val user = systemUsers.find { it.id == userId } ?: return
        _currentUser.value = user
        logAction(
            actorName = user.name,
            actorRole = user.role,
            action = "Role Switched",
            targetType = "SESSION",
            details = "Logged in as ${user.name} (${user.role.displayName})"
        )
    }

    private fun loadData() {
        val storedJson = prefs?.getString("cms_dataset_v2", null)
        if (storedJson != null) {
            try {
                resetToDefaultSampleData()
                return
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        resetToDefaultSampleData()
    }

    fun resetToDefaultSampleData() {
        val sampleClients = listOf(
            Client(
                id = "cli_1",
                name = "Elena Torres",
                company = "Apex Fintech Solutions",
                email = "etorres@apexfintech.io",
                phone = "+1 (415) 890-3321",
                industry = "Financial Services",
                contractValue = "$180,000",
                status = ClientStatus.ACTIVE,
                isDemo = true
            ),
            Client(
                id = "cli_2",
                name = "Dr. Robert Sterling",
                company = "Novasphere Health Systems",
                email = "r.sterling@novasphere.org",
                phone = "+1 (617) 555-0199",
                industry = "Healthcare Tech",
                contractValue = "$145,000",
                status = ClientStatus.ACTIVE,
                isDemo = true
            ),
            Client(
                id = "cli_3",
                name = "Miriam O'Connor",
                company = "Veritas Global Logistics",
                email = "moconnor@veritaslogistics.com",
                phone = "+1 (312) 440-8822",
                industry = "Supply Chain",
                contractValue = "$95,000",
                status = ClientStatus.ACTIVE,
                isDemo = true
            ),
            Client(
                id = "cli_4",
                name = "Arthur Pendelton",
                company = "Beacon Renewable Energy",
                email = "arthur@beaconpower.com",
                phone = "+1 (503) 789-1123",
                industry = "Clean Energy",
                contractValue = "$72,000",
                status = ClientStatus.IN_NEGOTIATION,
                isDemo = true
            )
        )

        val sampleTeams = listOf(
            Team(
                id = "team_alpha",
                name = "Alpha Engineering",
                leadId = "usr_lead_1",
                leadName = "Alex Morgan",
                memberIds = listOf("usr_mem_1", "usr_mem_2"),
                department = "Cloud & Core Platforms",
                isDemo = true
            ),
            Team(
                id = "team_studio",
                name = "Studio Design & UX",
                leadId = "usr_lead_2",
                leadName = "Marcus Vance",
                memberIds = listOf("usr_mem_3"),
                department = "Product Experience",
                isDemo = true
            )
        )

        val sampleProjects = listOf(
            Project(
                id = "proj_1",
                title = "Payment Gateway 2.0 & Fraud Detection",
                clientId = "cli_1",
                clientName = "Apex Fintech Solutions",
                teamId = "team_alpha",
                teamName = "Alpha Engineering",
                status = ProjectStatus.IN_PROGRESS,
                priority = Priority.URGENT,
                startDate = "Oct 01, 2026",
                dueDate = "Nov 15, 2026",
                dueEpochDays = 17,
                budget = "$120,000",
                progressPercent = 68,
                isDemo = true
            ),
            Project(
                id = "proj_2",
                title = "HIPAA Compliant Patient Telehealth Portal",
                clientId = "cli_2",
                clientName = "Novasphere Health Systems",
                teamId = "team_alpha",
                teamName = "Alpha Engineering",
                status = ProjectStatus.IN_PROGRESS,
                priority = Priority.HIGH,
                startDate = "Oct 10, 2026",
                dueDate = "Nov 02, 2026",
                dueEpochDays = 4,
                budget = "$95,000",
                progressPercent = 45,
                isDemo = true
            ),
            Project(
                id = "proj_3",
                title = "Global Fleet Fleet IoT Tracking Dashboard",
                clientId = "cli_3",
                clientName = "Veritas Global Logistics",
                teamId = "team_alpha",
                teamName = "Alpha Engineering",
                status = ProjectStatus.PLANNING,
                priority = Priority.MEDIUM,
                startDate = "Nov 01, 2026",
                dueDate = "Dec 20, 2026",
                dueEpochDays = 52,
                budget = "$80,000",
                progressPercent = 15,
                isDemo = true
            ),
            Project(
                id = "proj_4",
                title = "Clean Energy Brand System & Design Tokens",
                clientId = "cli_4",
                clientName = "Beacon Renewable Energy",
                teamId = "team_studio",
                teamName = "Studio Design & UX",
                status = ProjectStatus.IN_PROGRESS,
                priority = Priority.HIGH,
                startDate = "Sep 15, 2026",
                dueDate = "Oct 31, 2026",
                dueEpochDays = 2,
                budget = "$50,000",
                progressPercent = 85,
                isDemo = true
            )
        )

        val sampleTasks = listOf(
            // Overdue Task with rich audit history
            TaskItem(
                id = "task_1",
                title = "Audit OAuth2 Security Credentials & Token Rotation",
                description = "Ensure all microservices adhere to zero-trust token exchange and rate limiting protocols.",
                projectId = "proj_1",
                projectName = "Payment Gateway 2.0 & Fraud Detection",
                assignedMemberId = "usr_mem_2",
                assignedMemberName = "David Kim",
                teamLeadId = "usr_lead_1",
                status = TaskStatus.IN_PROGRESS,
                priority = Priority.URGENT,
                daysUntilDue = -1, // Overdue by 1 day
                dueFormatted = "Yesterday (Overdue)",
                auditHistory = listOf(
                    TaskAuditLog(
                        id = "tal_1_1",
                        timestamp = "Oct 20, 2026 · 10:00",
                        actorName = "Alex Morgan",
                        actorRole = UserRole.TEAM_LEAD,
                        changeType = "CREATED",
                        summary = "Task created and allocated to David Kim with Urgent priority."
                    ),
                    TaskAuditLog(
                        id = "tal_1_2",
                        timestamp = "Oct 22, 2026 · 14:15",
                        actorName = "David Kim",
                        actorRole = UserRole.TEAM_MEMBER,
                        changeType = "STATUS_CHANGED",
                        summary = "Status updated from New → In Progress.",
                        previousValue = "New",
                        newValue = "In Progress"
                    )
                ),
                isDemo = true
            ),
            // Due in 2 days (triggers Manager 2-day alert!)
            TaskItem(
                id = "task_2",
                title = "Design Tokens Export & Accessibility Compliance Audit",
                description = "Finalize WCAG AAA color contrast validation across mobile & web design system components.",
                projectId = "proj_4",
                projectName = "Clean Energy Brand System & Design Tokens",
                assignedMemberId = "usr_mem_3",
                assignedMemberName = "Elena Rostova",
                teamLeadId = "usr_lead_2",
                status = TaskStatus.IN_PROGRESS,
                priority = Priority.HIGH,
                daysUntilDue = 2, // 2 days left
                dueFormatted = "Due in 2 days",
                auditHistory = listOf(
                    TaskAuditLog(
                        id = "tal_2_1",
                        timestamp = "Oct 18, 2026 · 09:30",
                        actorName = "Marcus Vance",
                        actorRole = UserRole.TEAM_LEAD,
                        changeType = "CREATED",
                        summary = "Task created and assigned to Elena Rostova."
                    ),
                    TaskAuditLog(
                        id = "tal_2_2",
                        timestamp = "Oct 24, 2026 · 11:20",
                        actorName = "Elena Rostova",
                        actorRole = UserRole.TEAM_MEMBER,
                        changeType = "STATUS_CHANGED",
                        summary = "Started WCAG AAA color testing.",
                        previousValue = "New",
                        newValue = "In Progress"
                    )
                ),
                isDemo = true
            ),
            // Due in 5 days (triggers Team Lead 5-day alert!)
            TaskItem(
                id = "task_3",
                title = "Implement Telehealth WebRTC Signaling Server",
                description = "Build secure encrypted WebRTC peer connection relay with fallback TURN servers for video calls.",
                projectId = "proj_2",
                projectName = "HIPAA Compliant Patient Telehealth Portal",
                assignedMemberId = "usr_mem_1",
                assignedMemberName = "Maya Lin",
                teamLeadId = "usr_lead_1",
                status = TaskStatus.IN_PROGRESS,
                priority = Priority.HIGH,
                daysUntilDue = 5, // 5 days left
                dueFormatted = "Due in 5 days",
                auditHistory = listOf(
                    TaskAuditLog(
                        id = "tal_3_1",
                        timestamp = "Oct 21, 2026 · 13:00",
                        actorName = "Alex Morgan",
                        actorRole = UserRole.TEAM_LEAD,
                        changeType = "CREATED",
                        summary = "Signaling architecture assigned to Maya Lin."
                    ),
                    TaskAuditLog(
                        id = "tal_3_2",
                        timestamp = "Oct 25, 2026 · 16:45",
                        actorName = "Maya Lin",
                        actorRole = UserRole.TEAM_MEMBER,
                        changeType = "STATUS_CHANGED",
                        summary = "TURN servers configured in staging.",
                        previousValue = "New",
                        newValue = "In Progress"
                    )
                ),
                isDemo = true
            ),
            // Pending Review with proof attached
            TaskItem(
                id = "task_4",
                title = "Database Migration for Stripe Webhook Events",
                description = "Created idempotent table schemas and dead-letter queues for asynchronous payment events.",
                projectId = "proj_1",
                projectName = "Payment Gateway 2.0 & Fraud Detection",
                assignedMemberId = "usr_mem_1",
                assignedMemberName = "Maya Lin",
                teamLeadId = "usr_lead_1",
                status = TaskStatus.PENDING_REVIEW,
                priority = Priority.URGENT,
                daysUntilDue = 3,
                dueFormatted = "Due in 3 days",
                submission = SubmissionProof(
                    id = "sub_101",
                    taskId = "task_4",
                    memberId = "usr_mem_1",
                    memberName = "Maya Lin",
                    notes = "Completed migration scripts with unit tests and rollback plan. Load tested with 10k webhook events at 150ms p99 latency.",
                    attachmentName = "stripe_migration_audit_report.pdf",
                    attachmentType = "DOCUMENT",
                    submittedAt = "Today at 09:15 AM",
                    status = ProofStatus.PENDING
                ),
                auditHistory = listOf(
                    TaskAuditLog(
                        id = "tal_4_1",
                        timestamp = "Oct 15, 2026 · 11:00",
                        actorName = "Alex Morgan",
                        actorRole = UserRole.TEAM_LEAD,
                        changeType = "CREATED",
                        summary = "Migration task created."
                    ),
                    TaskAuditLog(
                        id = "tal_4_2",
                        timestamp = "Oct 16, 2026 · 14:00",
                        actorName = "Maya Lin",
                        actorRole = UserRole.TEAM_MEMBER,
                        changeType = "STATUS_CHANGED",
                        summary = "Status changed from New → In Progress.",
                        previousValue = "New",
                        newValue = "In Progress"
                    ),
                    TaskAuditLog(
                        id = "tal_4_3",
                        timestamp = "Today · 09:15",
                        actorName = "Maya Lin",
                        actorRole = UserRole.TEAM_MEMBER,
                        changeType = "PROOF_SUBMITTED",
                        summary = "Proof uploaded (stripe_migration_audit_report.pdf). Status set to Pending Review."
                    )
                ),
                isDemo = true
            ),
            // New task ready for assignment test
            TaskItem(
                id = "task_5",
                title = "Build Patient Appointment Push Notification Service",
                description = "Connect FCM push notification service with calendar event reminders and SMS fallback.",
                projectId = "proj_2",
                projectName = "HIPAA Compliant Patient Telehealth Portal",
                assignedMemberId = "usr_mem_1",
                assignedMemberName = "Maya Lin",
                teamLeadId = "usr_lead_1",
                status = TaskStatus.NEW,
                priority = Priority.MEDIUM,
                daysUntilDue = 7,
                dueFormatted = "Due in 7 days",
                auditHistory = listOf(
                    TaskAuditLog(
                        id = "tal_5_1",
                        timestamp = "Oct 26, 2026 · 15:30",
                        actorName = "Alex Morgan",
                        actorRole = UserRole.TEAM_LEAD,
                        changeType = "CREATED",
                        summary = "Notification service task created."
                    )
                ),
                isDemo = true
            ),
            // Completed task
            TaskItem(
                id = "task_6",
                title = "Cloud Infrastructure Terraform Provisioning",
                description = "Provisioned multi-region AWS ECS clusters with automated autoscaling and VPC peering.",
                projectId = "proj_1",
                projectName = "Payment Gateway 2.0 & Fraud Detection",
                assignedMemberId = "usr_mem_2",
                assignedMemberName = "David Kim",
                teamLeadId = "usr_lead_1",
                status = TaskStatus.DONE,
                priority = Priority.HIGH,
                daysUntilDue = 12,
                dueFormatted = "Completed",
                submission = SubmissionProof(
                    id = "sub_100",
                    taskId = "task_6",
                    memberId = "usr_mem_2",
                    memberName = "David Kim",
                    notes = "Terraform scripts validated and applied in production staging with zero drift.",
                    attachmentName = "infrastructure_graph.png",
                    attachmentType = "IMAGE",
                    submittedAt = "Oct 27, 2026",
                    status = ProofStatus.APPROVED,
                    reviewedBy = "Alex Morgan",
                    reviewerFeedback = "Excellent configuration. All IAM roles are properly scoped.",
                    reviewedAt = "Oct 28, 2026"
                ),
                auditHistory = listOf(
                    TaskAuditLog(
                        id = "tal_6_1",
                        timestamp = "Oct 12, 2026 · 09:00",
                        actorName = "Sarah Chen",
                        actorRole = UserRole.MANAGER,
                        changeType = "CREATED",
                        summary = "Infrastructure provisioning task assigned."
                    ),
                    TaskAuditLog(
                        id = "tal_6_2",
                        timestamp = "Oct 27, 2026 · 17:00",
                        actorName = "David Kim",
                        actorRole = UserRole.TEAM_MEMBER,
                        changeType = "PROOF_SUBMITTED",
                        summary = "Proof uploaded (infrastructure_graph.png)."
                    ),
                    TaskAuditLog(
                        id = "tal_6_3",
                        timestamp = "Oct 28, 2026 · 11:30",
                        actorName = "Alex Morgan",
                        actorRole = UserRole.TEAM_LEAD,
                        changeType = "REVIEW_APPROVED",
                        summary = "Proof approved by Alex Morgan: 'Excellent configuration.' Task marked Done.",
                        previousValue = "In Review",
                        newValue = "Done"
                    )
                ),
                isDemo = true
            )
        )

        val sampleLogs = listOf(
            AuditLogEntry(
                id = "log_1",
                timestamp = "Oct 28, 2026 · 16:45",
                actorName = "Alex Morgan",
                actorRole = UserRole.TEAM_LEAD,
                action = "Proof Approved",
                targetType = "TASK",
                details = "Approved task #task_6 (Cloud Infrastructure Terraform) submitted by David Kim."
            ),
            AuditLogEntry(
                id = "log_2",
                timestamp = "Oct 29, 2026 · 09:15",
                actorName = "Maya Lin",
                actorRole = UserRole.TEAM_MEMBER,
                action = "Proof Submitted",
                targetType = "TASK",
                details = "Submitted completion proof for #task_4 (Database Migration for Stripe)."
            ),
            AuditLogEntry(
                id = "log_3",
                timestamp = "Oct 29, 2026 · 10:30",
                actorName = "Sarah Chen",
                actorRole = UserRole.MANAGER,
                action = "Project Created",
                targetType = "PROJECT",
                details = "Approved budget of $120,000 for Apex Fintech Solutions."
            )
        )

        val initialNotifications = listOf(
            AppNotification(
                id = "notif_1",
                title = "Critical Overdue Alert",
                message = "Task 'Audit OAuth2 Security Credentials' is 1 day overdue! Immediate attention required.",
                category = NotificationCategory.DEADLINE_APPROACHING,
                timestamp = "10 mins ago",
                isRead = false,
                taskId = "task_1"
            ),
            AppNotification(
                id = "notif_2",
                title = "2-Day Manager Deadline Alert",
                message = "Task 'Design Tokens Export' is due in 2 days (Oct 31, 2026).",
                category = NotificationCategory.DEADLINE_APPROACHING,
                timestamp = "1 hour ago",
                isRead = false,
                taskId = "task_2",
                targetRole = UserRole.MANAGER
            ),
            AppNotification(
                id = "notif_3",
                title = "5-Day Team Lead Deadline Alert",
                message = "Task 'Implement Telehealth WebRTC Server' is due in 5 days.",
                category = NotificationCategory.DEADLINE_APPROACHING,
                timestamp = "3 hours ago",
                isRead = false,
                taskId = "task_3",
                targetRole = UserRole.TEAM_LEAD
            ),
            AppNotification(
                id = "notif_4",
                title = "Proof Awaiting Review",
                message = "Maya Lin submitted completion proof for 'Database Migration for Stripe Webhook Events'.",
                category = NotificationCategory.PROOF_SUBMISSION,
                timestamp = "4 hours ago",
                isRead = false,
                taskId = "task_4"
            )
        )

        _clients.value = sampleClients
        _teams.value = sampleTeams
        _projects.value = sampleProjects
        _tasks.value = sampleTasks
        _auditLogs.value = sampleLogs
        _notifications.value = initialNotifications
        persistData()
    }

    // Task CRUD with Per-Task Audit History and Real-Time Notifications
    fun addTask(
        title: String,
        description: String,
        projectId: String,
        assignedMemberId: String,
        priority: Priority,
        daysUntilDue: Int,
        dueFormatted: String
    ) {
        val project = _projects.value.find { it.id == projectId }
        val member = systemUsers.find { it.id == assignedMemberId }
        val lead = _teams.value.find { it.id == project?.teamId }?.let { team ->
            systemUsers.find { it.id == team.leadId }
        }

        val currentUser = _currentUser.value
        val creationAudit = TaskAuditLog(
            id = "tal_${UUID.randomUUID().toString().take(8)}",
            timestamp = getCurrentFormattedTimestamp(),
            actorName = currentUser.name,
            actorRole = currentUser.role,
            changeType = "CREATED",
            summary = "Task created by ${currentUser.name} (${currentUser.role.displayName}) and assigned to ${member?.name}."
        )

        val newTask = TaskItem(
            id = "task_${UUID.randomUUID().toString().take(8)}",
            title = title,
            description = description,
            projectId = projectId,
            projectName = project?.title ?: "Project",
            assignedMemberId = assignedMemberId,
            assignedMemberName = member?.name ?: "Assignee",
            teamLeadId = lead?.id ?: "usr_lead_1",
            status = TaskStatus.NEW,
            priority = priority,
            daysUntilDue = daysUntilDue,
            dueFormatted = dueFormatted,
            auditHistory = listOf(creationAudit),
            isDemo = false
        )

        _tasks.value = listOf(newTask) + _tasks.value
        recalculateProjectProgress(projectId)

        // Real-time notification for task assignment
        val assignmentNotif = AppNotification(
            id = "notif_${UUID.randomUUID().toString().take(8)}",
            title = "New Task Assigned",
            message = "${currentUser.name} assigned you: '$title' in ${project?.title}.",
            category = NotificationCategory.TASK_ASSIGNMENT,
            timestamp = getCurrentFormattedTimestamp(),
            taskId = newTask.id,
            targetUserId = assignedMemberId
        )
        _notifications.value = listOf(assignmentNotif) + _notifications.value

        logAction(
            actorName = currentUser.name,
            actorRole = currentUser.role,
            action = "Task Created",
            targetType = "TASK",
            details = "Created task '$title' assigned to ${member?.name}."
        )
        persistData()
    }

    fun updateTaskStatus(taskId: String, newStatus: TaskStatus) {
        val task = _tasks.value.find { it.id == taskId } ?: return
        val currentUser = _currentUser.value

        val statusAudit = TaskAuditLog(
            id = "tal_${UUID.randomUUID().toString().take(8)}",
            timestamp = getCurrentFormattedTimestamp(),
            actorName = currentUser.name,
            actorRole = currentUser.role,
            changeType = "STATUS_CHANGED",
            summary = "Status updated from '${task.status.label}' to '${newStatus.label}' by ${currentUser.name}.",
            previousValue = task.status.label,
            newValue = newStatus.label
        )

        _tasks.value = _tasks.value.map {
            if (it.id == taskId) {
                it.copy(
                    status = newStatus,
                    auditHistory = listOf(statusAudit) + it.auditHistory
                )
            } else it
        }

        recalculateProjectProgress(task.projectId)

        logAction(
            actorName = currentUser.name,
            actorRole = currentUser.role,
            action = "Task Status Changed",
            targetType = "TASK",
            details = "Task '${task.title}' updated to ${newStatus.label}."
        )
        persistData()
    }

    fun reassignTask(taskId: String, newMemberId: String) {
        val task = _tasks.value.find { it.id == taskId } ?: return
        val newMember = systemUsers.find { it.id == newMemberId } ?: return
        val currentUser = _currentUser.value

        val reassignAudit = TaskAuditLog(
            id = "tal_${UUID.randomUUID().toString().take(8)}",
            timestamp = getCurrentFormattedTimestamp(),
            actorName = currentUser.name,
            actorRole = currentUser.role,
            changeType = "REASSIGNED",
            summary = "Reassigned from '${task.assignedMemberName}' to '${newMember.name}' by ${currentUser.name}.",
            previousValue = task.assignedMemberName,
            newValue = newMember.name
        )

        _tasks.value = _tasks.value.map {
            if (it.id == taskId) {
                it.copy(
                    assignedMemberId = newMember.id,
                    assignedMemberName = newMember.name,
                    auditHistory = listOf(reassignAudit) + it.auditHistory
                )
            } else it
        }

        val reassignNotif = AppNotification(
            id = "notif_${UUID.randomUUID().toString().take(8)}",
            title = "Task Reassigned to You",
            message = "Task '${task.title}' was reassigned to you by ${currentUser.name}.",
            category = NotificationCategory.TASK_ASSIGNMENT,
            timestamp = getCurrentFormattedTimestamp(),
            taskId = taskId,
            targetUserId = newMemberId
        )
        _notifications.value = listOf(reassignNotif) + _notifications.value

        logAction(
            actorName = currentUser.name,
            actorRole = currentUser.role,
            action = "Task Reassigned",
            targetType = "TASK",
            details = "Task '${task.title}' reassigned to ${newMember.name}."
        )
        persistData()
    }

    fun submitProof(taskId: String, notes: String, attachmentName: String, attachmentType: String, localMediaUri: String? = null) {
        val task = _tasks.value.find { it.id == taskId } ?: return
        val user = _currentUser.value

        val proof = SubmissionProof(
            id = "sub_${UUID.randomUUID().toString().take(8)}",
            taskId = taskId,
            memberId = user.id,
            memberName = user.name,
            notes = notes,
            attachmentName = attachmentName.ifBlank { "work_completion_proof.pdf" },
            attachmentType = attachmentType,
            localMediaUri = localMediaUri,
            submittedAt = getCurrentFormattedTimestamp(),
            status = ProofStatus.PENDING
        )

        val proofAudit = TaskAuditLog(
            id = "tal_${UUID.randomUUID().toString().take(8)}",
            timestamp = getCurrentFormattedTimestamp(),
            actorName = user.name,
            actorRole = user.role,
            changeType = "PROOF_SUBMITTED",
            summary = "Completion proof submitted with attachment '${proof.attachmentName}' by ${user.name}. Status changed to Pending Review.",
            previousValue = task.status.label,
            newValue = TaskStatus.PENDING_REVIEW.label
        )

        _tasks.value = _tasks.value.map {
            if (it.id == taskId) {
                it.copy(
                    status = TaskStatus.PENDING_REVIEW,
                    submission = proof,
                    auditHistory = listOf(proofAudit) + it.auditHistory
                )
            } else it
        }

        // Send real-time notification to Team Lead & Manager
        val notif = AppNotification(
            id = "notif_${UUID.randomUUID().toString().take(8)}",
            title = "Proof Awaiting Verification",
            message = "${user.name} submitted completion proof for '${task.title}'.",
            category = NotificationCategory.PROOF_SUBMISSION,
            timestamp = getCurrentFormattedTimestamp(),
            taskId = taskId,
            targetRole = UserRole.TEAM_LEAD
        )
        _notifications.value = listOf(notif) + _notifications.value

        logAction(
            actorName = user.name,
            actorRole = user.role,
            action = "Proof Submitted",
            targetType = "SUBMISSION",
            details = "Submitted completion evidence for task '${task.title}'."
        )
        persistData()
    }

    fun reviewProof(taskId: String, approve: Boolean, feedback: String) {
        val task = _tasks.value.find { it.id == taskId } ?: return
        val currentProof = task.submission ?: return
        val reviewer = _currentUser.value

        val updatedProof = currentProof.copy(
            status = if (approve) ProofStatus.APPROVED else ProofStatus.REJECTED,
            reviewerFeedback = feedback,
            reviewedBy = reviewer.name,
            reviewedAt = getCurrentFormattedTimestamp()
        )

        val updatedStatus = if (approve) TaskStatus.DONE else TaskStatus.IN_PROGRESS

        val reviewAudit = TaskAuditLog(
            id = "tal_${UUID.randomUUID().toString().take(8)}",
            timestamp = getCurrentFormattedTimestamp(),
            actorName = reviewer.name,
            actorRole = reviewer.role,
            changeType = if (approve) "REVIEW_APPROVED" else "REVIEW_REJECTED",
            summary = if (approve) {
                "Work approved by ${reviewer.name}. Status set to Done. Feedback: '$feedback'"
            } else {
                "Revision requested by ${reviewer.name}. Status set to In Progress. Feedback: '$feedback'"
            },
            previousValue = task.status.label,
            newValue = updatedStatus.label
        )

        _tasks.value = _tasks.value.map {
            if (it.id == taskId) {
                it.copy(
                    status = updatedStatus,
                    submission = updatedProof,
                    auditHistory = listOf(reviewAudit) + it.auditHistory
                )
            } else it
        }

        recalculateProjectProgress(task.projectId)

        // Real-time notification to submitting member
        val decisionNotif = AppNotification(
            id = "notif_${UUID.randomUUID().toString().take(8)}",
            title = if (approve) "Task Approved! 🎉" else "Revision Requested ⚠️",
            message = if (approve) {
                "${reviewer.name} approved your submission for '${task.title}'."
            } else {
                "${reviewer.name} requested changes on '${task.title}': $feedback"
            },
            category = if (approve) NotificationCategory.TASK_APPROVAL else NotificationCategory.TASK_REVISION,
            timestamp = getCurrentFormattedTimestamp(),
            taskId = taskId,
            targetUserId = task.assignedMemberId
        )
        _notifications.value = listOf(decisionNotif) + _notifications.value

        logAction(
            actorName = reviewer.name,
            actorRole = reviewer.role,
            action = if (approve) "Proof Approved" else "Proof Rejected",
            targetType = "SUBMISSION",
            details = if (approve) {
                "Approved task '${task.title}' completed by ${task.assignedMemberName}."
            } else {
                "Requested revision on '${task.title}': '$feedback'."
            }
        )
        persistData()
    }

    fun markNotificationAsRead(notifId: String) {
        _notifications.value = _notifications.value.map {
            if (it.id == notifId) it.copy(isRead = true) else it
        }
    }

    fun markAllNotificationsAsRead() {
        _notifications.value = _notifications.value.map { it.copy(isRead = true) }
    }

    // CRUD for Clients
    fun addClient(name: String, company: String, email: String, phone: String, industry: String, contractValue: String, status: ClientStatus) {
        val newClient = Client(
            id = "cli_${UUID.randomUUID().toString().take(8)}",
            name = name,
            company = company,
            email = email,
            phone = phone,
            industry = industry,
            contractValue = contractValue,
            status = status,
            isDemo = false
        )
        _clients.value = listOf(newClient) + _clients.value
        persistData()

        logAction(
            actorName = _currentUser.value.name,
            actorRole = _currentUser.value.role,
            action = "Client Created",
            targetType = "CLIENT",
            details = "Added client '$company' ($name) with status ${status.label}."
        )
    }

    fun updateClient(client: Client) {
        _clients.value = _clients.value.map { if (it.id == client.id) client else it }
        persistData()
        logAction(
            actorName = _currentUser.value.name,
            actorRole = _currentUser.value.role,
            action = "Client Updated",
            targetType = "CLIENT",
            details = "Updated details for '${client.company}'."
        )
    }

    fun deleteClient(clientId: String) {
        val client = _clients.value.find { it.id == clientId }
        _clients.value = _clients.value.filter { it.id != clientId }
        persistData()
        logAction(
            actorName = _currentUser.value.name,
            actorRole = _currentUser.value.role,
            action = "Client Deleted",
            targetType = "CLIENT",
            details = "Removed client '${client?.company ?: clientId}'."
        )
    }

    // CRUD for Projects
    fun addProject(
        title: String,
        clientId: String,
        teamId: String,
        priority: Priority,
        startDate: String,
        dueDate: String,
        dueDays: Int,
        budget: String
    ) {
        val client = _clients.value.find { it.id == clientId }
        val team = _teams.value.find { it.id == teamId }
        val newProject = Project(
            id = "proj_${UUID.randomUUID().toString().take(8)}",
            title = title,
            clientId = clientId,
            clientName = client?.company ?: "Unknown Client",
            teamId = teamId,
            teamName = team?.name ?: "Unknown Team",
            status = ProjectStatus.PLANNING,
            priority = priority,
            startDate = startDate,
            dueDate = dueDate,
            dueEpochDays = dueDays.toLong(),
            budget = budget,
            progressPercent = 0,
            isDemo = false
        )
        _projects.value = listOf(newProject) + _projects.value
        persistData()

        logAction(
            actorName = _currentUser.value.name,
            actorRole = _currentUser.value.role,
            action = "Project Created",
            targetType = "PROJECT",
            details = "Created project '$title' assigned to ${team?.name}."
        )
    }

    private fun recalculateProjectProgress(projectId: String) {
        val projectTasks = _tasks.value.filter { it.projectId == projectId }
        if (projectTasks.isEmpty()) return
        val doneCount = projectTasks.count { it.status == TaskStatus.DONE }
        val newPercent = (doneCount * 100) / projectTasks.size

        _projects.value = _projects.value.map {
            if (it.id == projectId) {
                val newStatus = when {
                    newPercent == 100 -> ProjectStatus.COMPLETED
                    newPercent > 0 -> ProjectStatus.IN_PROGRESS
                    else -> it.status
                }
                it.copy(progressPercent = newPercent, status = newStatus)
            } else it
        }
    }

    fun getDeadlineAlertsForCurrentUser(): List<DeadlineAlert> {
        val user = _currentUser.value
        val allTasks = _tasks.value

        return when (user.role) {
            UserRole.MANAGER -> {
                allTasks.filter { it.status != TaskStatus.DONE && it.daysUntilDue <= 2 }
                    .map {
                        DeadlineAlert(
                            taskId = it.id,
                            taskTitle = it.title,
                            projectName = it.projectName,
                            assignedTo = it.assignedMemberName,
                            daysRemaining = it.daysUntilDue,
                            isOverdue = it.daysUntilDue < 0,
                            alertType = if (it.daysUntilDue < 0) AlertType.OVERDUE else AlertType.MANAGER_2_DAYS
                        )
                    }
            }
            UserRole.TEAM_LEAD -> {
                allTasks.filter { task ->
                    task.status != TaskStatus.DONE &&
                    (task.teamLeadId == user.id || user.teamId == null || _teams.value.find { it.leadId == user.id }?.memberIds?.contains(task.assignedMemberId) == true) &&
                    task.daysUntilDue <= 5
                }.map {
                    DeadlineAlert(
                        taskId = it.id,
                        taskTitle = it.title,
                        projectName = it.projectName,
                        assignedTo = it.assignedMemberName,
                        daysRemaining = it.daysUntilDue,
                        isOverdue = it.daysUntilDue < 0,
                        alertType = when {
                            it.daysUntilDue < 0 -> AlertType.OVERDUE
                            it.daysUntilDue <= 2 -> AlertType.MANAGER_2_DAYS
                            else -> AlertType.LEAD_5_DAYS
                        }
                    )
                }
            }
            UserRole.TEAM_MEMBER -> {
                allTasks.filter { it.assignedMemberId == user.id && it.status != TaskStatus.DONE && it.daysUntilDue <= 3 }
                    .map {
                        DeadlineAlert(
                            taskId = it.id,
                            taskTitle = it.title,
                            projectName = it.projectName,
                            assignedTo = it.assignedMemberName,
                            daysRemaining = it.daysUntilDue,
                            isOverdue = it.daysUntilDue < 0,
                            alertType = if (it.daysUntilDue < 0) AlertType.OVERDUE else AlertType.UPCOMING
                        )
                    }
            }
        }
    }

    fun getDashboardMetrics(): DashboardMetrics {
        val user = _currentUser.value
        val tasks = _tasks.value
        val total = tasks.size
        val done = tasks.count { it.status == TaskStatus.DONE }
        val completion = if (total > 0) (done * 100) / total else 0

        val reviewCount = when (user.role) {
            UserRole.MANAGER -> tasks.count { it.status == TaskStatus.PENDING_REVIEW }
            UserRole.TEAM_LEAD -> tasks.count { it.status == TaskStatus.PENDING_REVIEW && it.teamLeadId == user.id }
            UserRole.TEAM_MEMBER -> tasks.count { it.status == TaskStatus.PENDING_REVIEW && it.assignedMemberId == user.id }
        }

        val overdueCount = tasks.count { it.status != TaskStatus.DONE && it.daysUntilDue < 0 }

        return DashboardMetrics(
            totalClients = _clients.value.size,
            activeProjects = _projects.value.count { it.status == ProjectStatus.IN_PROGRESS || it.status == ProjectStatus.PLANNING },
            pendingTasks = tasks.count { it.status != TaskStatus.DONE },
            reviewQueueCount = reviewCount,
            overdueTasksCount = overdueCount,
            completionRatePercent = completion,
            activeAlertsCount = getDeadlineAlertsForCurrentUser().size
        )
    }

    private fun logAction(
        actorName: String,
        actorRole: UserRole,
        action: String,
        targetType: String,
        details: String
    ) {
        val entry = AuditLogEntry(
            id = "log_${UUID.randomUUID().toString().take(8)}",
            timestamp = getCurrentFormattedTimestamp(),
            actorName = actorName,
            actorRole = actorRole,
            action = action,
            targetType = targetType,
            details = details
        )
        _auditLogs.value = listOf(entry) + _auditLogs.value
    }

    private fun persistData() {
        if (prefs == null) return
        try {
            val root = JSONObject()
            val clientsArr = JSONArray()
            _clients.value.forEach { c ->
                clientsArr.put(JSONObject().apply {
                    put("id", c.id)
                    put("company", c.company)
                })
            }
            root.put("clients", clientsArr)
            prefs.edit().putString("cms_dataset_v2", root.toString()).apply()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
