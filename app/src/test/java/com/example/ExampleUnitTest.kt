package com.example

import com.example.data.model.*
import com.example.data.repository.CmsRepository
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class ExampleUnitTest {

    private lateinit var repository: CmsRepository

    @Before
    fun setup() {
        repository = CmsRepository(context = null)
        repository.resetToDefaultSampleData()
    }

    @Test
    fun testInitialRoleIsManager() {
        val user = repository.currentUser.value
        assertEquals(UserRole.MANAGER, user.role)
        assertEquals("Sarah Chen", user.name)
    }

    @Test
    fun testRoleSwitchingToTeamLead() {
        repository.switchUser("usr_lead_1")
        val user = repository.currentUser.value
        assertEquals(UserRole.TEAM_LEAD, user.role)
        assertEquals("Alex Morgan", user.name)
    }

    @Test
    fun testRoleSwitchingToTeamMember() {
        repository.switchUser("usr_mem_1")
        val user = repository.currentUser.value
        assertEquals(UserRole.TEAM_MEMBER, user.role)
        assertEquals("Maya Lin", user.name)
    }

    @Test
    fun testManagerDeadlineAlertsTwoDaysAndOverdue() {
        repository.switchUser("usr_mgr_1")
        val alerts = repository.getDeadlineAlertsForCurrentUser()

        assertTrue("Manager must receive deadline alerts", alerts.isNotEmpty())
        alerts.forEach { alert ->
            assertTrue("Manager alert days remaining must be <= 2", alert.daysRemaining <= 2)
        }
    }

    @Test
    fun testTeamLeadDeadlineAlertsFiveDaysAndOverdue() {
        repository.switchUser("usr_lead_1")
        val alerts = repository.getDeadlineAlertsForCurrentUser()

        assertTrue("Team Lead must receive deadline alerts", alerts.isNotEmpty())
        alerts.forEach { alert ->
            assertTrue("Lead alert days remaining must be <= 5", alert.daysRemaining <= 5)
        }
    }

    @Test
    fun testProofSubmissionWorkflow() {
        // Switch to Maya Lin (Team Member)
        repository.switchUser("usr_mem_1")

        repository.submitProof(
            taskId = "task_5",
            notes = "Push notification service tested and verified with APNs and FCM sandbox.",
            attachmentName = "fcm_test_payload.json",
            attachmentType = "CODE"
        )

        val updatedTask = repository.tasks.value.find { it.id == "task_5" }
        assertNotNull(updatedTask)
        assertEquals(TaskStatus.PENDING_REVIEW, updatedTask?.status)
        assertEquals("Maya Lin", updatedTask?.submission?.memberName)
        assertEquals(ProofStatus.PENDING, updatedTask?.submission?.status)

        // Switch to Alex Morgan (Team Lead) to review
        repository.switchUser("usr_lead_1")
        repository.reviewProof(
            taskId = "task_5",
            approve = true,
            feedback = "Verified and meets all acceptance criteria."
        )

        val approvedTask = repository.tasks.value.find { it.id == "task_5" }
        assertNotNull(approvedTask)
        assertEquals(TaskStatus.DONE, approvedTask?.status)
        assertEquals(ProofStatus.APPROVED, approvedTask?.submission?.status)
        assertEquals("Alex Morgan", approvedTask?.submission?.reviewedBy)

        val auditLogs = repository.auditLogs.value
        assertTrue("Audit log should contain Proof Approved", auditLogs.any { it.action == "Proof Approved" })
    }

    @Test
    fun testProofRejectionWorkflow() {
        repository.switchUser("usr_lead_1")
        repository.reviewProof(
            taskId = "task_4",
            approve = false,
            feedback = "Rollback plan lacks dry run metrics. Please re-run."
        )

        val rejectedTask = repository.tasks.value.find { it.id == "task_4" }
        assertNotNull(rejectedTask)
        assertEquals(TaskStatus.IN_PROGRESS, rejectedTask?.status)
        assertEquals(ProofStatus.REJECTED, rejectedTask?.submission?.status)
        assertEquals("Rollback plan lacks dry run metrics. Please re-run.", rejectedTask?.submission?.reviewerFeedback)
    }

    @Test
    fun testClientCreationAndAuditTrail() {
        val initialCount = repository.clients.value.size
        repository.addClient(
            name = "Jordan Blake",
            company = "Quantum Nexus Labs",
            email = "jordan@quantumnexus.io",
            phone = "+1 (800) 555-0144",
            industry = "Quantum Computing",
            contractValue = "$210,000",
            status = ClientStatus.ACTIVE
        )

        assertEquals(initialCount + 1, repository.clients.value.size)
        val created = repository.clients.value.find { it.company == "Quantum Nexus Labs" }
        assertNotNull(created)
        assertFalse(created!!.isDemo)

        val audit = repository.auditLogs.value
        assertTrue("Audit log recorded client creation", audit.any { it.action == "Client Created" })
    }

    @Test
    fun testPerTaskAuditTrailRecordsStateTransitions() {
        val task = repository.tasks.value.find { it.id == "task_1" }
        assertNotNull(task)
        assertTrue("Task 1 must have audit history entries", task!!.auditHistory.isNotEmpty())

        // Update status and verify per-task audit history records transition
        repository.switchUser("usr_lead_1")
        repository.updateTaskStatus("task_1", TaskStatus.DONE)

        val updatedTask = repository.tasks.value.find { it.id == "task_1" }
        assertNotNull(updatedTask)
        val latestAudit = updatedTask!!.auditHistory.first()
        assertEquals("STATUS_CHANGED", latestAudit.changeType)
        assertEquals("Alex Morgan", latestAudit.actorName)
        assertEquals("In Progress", latestAudit.previousValue)
        assertEquals("Done", latestAudit.newValue)
    }

    @Test
    fun testRealtimeNotificationsOnTaskAssignmentAndApproval() {
        val initialNotifsCount = repository.notifications.value.size

        // Assign task as Lead
        repository.switchUser("usr_lead_1")
        repository.addTask(
            title = "API Rate Limiter Implementation",
            description = "Token bucket algorithm with Redis store.",
            projectId = "proj_1",
            assignedMemberId = "usr_mem_1",
            priority = Priority.HIGH,
            daysUntilDue = 4,
            dueFormatted = "Due in 4 days"
        )

        val afterAssignment = repository.notifications.value
        assertTrue("Notifications must increase on task assignment", afterAssignment.size > initialNotifsCount)
        val assignNotif = afterAssignment.first()
        assertEquals(NotificationCategory.TASK_ASSIGNMENT, assignNotif.category)
        assertEquals("usr_mem_1", assignNotif.targetUserId)
    }
}
