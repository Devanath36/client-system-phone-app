package com.example.ui.components

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import coil.compose.AsyncImage
import com.example.data.model.*
import com.example.ui.theme.*

@Composable
fun TaskDetailDialog(
    task: TaskItem,
    currentUser: User,
    allUsers: List<User>,
    onDismiss: () -> Unit,
    onUpdateStatus: (TaskStatus) -> Unit,
    onReassign: (String) -> Unit,
    onOpenSubmitProof: () -> Unit,
    onOpenReviewProof: () -> Unit
) {
    var showReassignMenu by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(18.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp,
            modifier = Modifier
                .fillMaxWidth()
                .padding(4.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // Header: Project + Priority + Close
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = task.projectName,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = task.title,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            ),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    IconButton(onClick = onDismiss) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close")
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Meta row: Priority, Status, Due date
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val priorityColor = when (task.priority) {
                        Priority.URGENT -> StatusOverdue
                        Priority.HIGH -> StatusInProgress
                        Priority.MEDIUM -> MaterialTheme.colorScheme.primary
                        Priority.LOW -> MaterialTheme.colorScheme.onSurfaceVariant
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(priorityColor.copy(alpha = 0.15f))
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = "${task.priority.label.uppercase()} PRIORITY",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = priorityColor
                        )
                    }

                    val (statusColor, statusBg) = when (task.status) {
                        TaskStatus.NEW -> Pair(StatusNew, StatusNew.copy(alpha = 0.15f))
                        TaskStatus.IN_PROGRESS -> Pair(StatusInProgress, StatusInProgress.copy(alpha = 0.15f))
                        TaskStatus.PENDING_REVIEW -> Pair(StatusReview, StatusReview.copy(alpha = 0.15f))
                        TaskStatus.DONE -> Pair(StatusDone, StatusDone.copy(alpha = 0.15f))
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(statusBg)
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = task.status.label.uppercase(),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = statusColor
                        )
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = task.dueFormatted,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Description
                Text(
                    text = "SPECIFICATIONS",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = task.description.ifBlank { "No detailed specifications provided for this task deliverable." },
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Assignee Info + Reassign button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "ASSIGNED DELIVERER",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = task.assignedMemberName,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    if (currentUser.role in listOf(UserRole.MANAGER, UserRole.TEAM_LEAD)) {
                        TextButton(
                            onClick = { showReassignMenu = !showReassignMenu },
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Icon(Icons.Default.SwapHoriz, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Reassign", fontSize = 11.sp)
                        }
                    }
                }

                if (showReassignMenu) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(8.dp)) {
                            Text("Select new team member:", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            val members = allUsers.filter { it.role == UserRole.TEAM_MEMBER }
                            members.forEach { m ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            onReassign(m.id)
                                            showReassignMenu = false
                                        }
                                        .padding(vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    RadioButton(
                                        selected = task.assignedMemberId == m.id,
                                        onClick = {
                                            onReassign(m.id)
                                            showReassignMenu = false
                                        }
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("${m.name} (${m.title})", fontSize = 12.sp)
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Submission proof preview if available
                task.submission?.let { proof ->
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "SUBMITTED PROOF",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )

                                val proofBg = when (proof.status) {
                                    ProofStatus.APPROVED -> StatusDone
                                    ProofStatus.REJECTED -> StatusOverdue
                                    ProofStatus.PENDING -> StatusReview
                                }

                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(proofBg.copy(alpha = 0.15f))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = proof.status.label,
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = proofBg
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(6.dp))
                            Text(text = proof.notes, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface)

                            if (proof.localMediaUri != null) {
                                Spacer(modifier = Modifier.height(8.dp))
                                AsyncImage(
                                    model = proof.localMediaUri,
                                    contentDescription = "Uploaded proof photo",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(140.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                )
                            }

                            Spacer(modifier = Modifier.height(6.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Attachment, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(text = proof.attachmentName, fontSize = 11.sp, color = MaterialTheme.colorScheme.primary)
                            }

                            if (!proof.reviewerFeedback.isNullOrBlank()) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Reviewer Feedback (${proof.reviewedBy ?: "Lead"}):",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (proof.status == ProofStatus.APPROVED) StatusDone else StatusOverdue
                                )
                                Text(
                                    text = proof.reviewerFeedback,
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(14.dp))
                }

                // ============================================
                // AUDIT TRAIL & CHANGE HISTORY (TASK SPECIFIC)
                // ============================================
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "AUDIT TRAIL & CHANGE HISTORY",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "${task.auditHistory.size} events",
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                if (task.auditHistory.isEmpty()) {
                    Text(
                        text = "No change events recorded yet.",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        task.auditHistory.forEach { audit ->
                            val auditRoleColor = when (audit.actorRole) {
                                UserRole.MANAGER -> RoleManagerColor
                                UserRole.TEAM_LEAD -> RoleTeamLeadColor
                                UserRole.TEAM_MEMBER -> RoleMemberColor
                            }

                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.15f)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(10.dp),
                                    verticalAlignment = Alignment.Top
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(26.dp)
                                            .clip(CircleShape)
                                            .background(auditRoleColor.copy(alpha = 0.2f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = audit.actorName.take(1),
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = auditRoleColor
                                        )
                                    }

                                    Spacer(modifier = Modifier.width(10.dp))

                                    Column(modifier = Modifier.weight(1f)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text(
                                                text = "${audit.actorName} (${audit.actorRole.displayName})",
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                            Text(
                                                text = audit.timestamp,
                                                fontSize = 9.sp,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }

                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = audit.summary,
                                            fontSize = 11.sp,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )

                                        if (audit.previousValue != null && audit.newValue != null) {
                                            Spacer(modifier = Modifier.height(2.dp))
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Text(
                                                    text = audit.previousValue,
                                                    fontSize = 10.sp,
                                                    color = StatusOverdue,
                                                    fontWeight = FontWeight.Medium
                                                )
                                                Text(text = " → ", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                                Text(
                                                    text = audit.newValue,
                                                    fontSize = 10.sp,
                                                    color = StatusDone,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Action buttons inside Detail Modal
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (task.status == TaskStatus.PENDING_REVIEW && currentUser.role in listOf(UserRole.MANAGER, UserRole.TEAM_LEAD)) {
                        Button(
                            onClick = {
                                onOpenReviewProof()
                                onDismiss()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = StatusReview),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.Verified, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Review Proof", fontSize = 12.sp)
                        }
                    } else if (task.status != TaskStatus.DONE && (task.assignedMemberId == currentUser.id || currentUser.role == UserRole.TEAM_MEMBER)) {
                        Button(
                            onClick = {
                                onOpenSubmitProof()
                                onDismiss()
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.CloudUpload, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Submit Proof", fontSize = 12.sp)
                        }
                    } else if (task.status != TaskStatus.DONE) {
                        Button(
                            onClick = {
                                onUpdateStatus(TaskStatus.DONE)
                                onDismiss()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = StatusDone),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Mark Complete", fontSize = 12.sp)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SubmitProofDialog(
    task: TaskItem,
    onDismiss: () -> Unit,
    onSubmit: (notes: String, attachmentName: String, attachmentType: String, localMediaUri: String?) -> Unit
) {
    var notes by remember { mutableStateOf("") }
    var selectedAttachment by remember { mutableStateOf("deployment_verification_log.pdf") }
    var selectedType by remember { mutableStateOf("DOCUMENT") }
    var pickedMediaUri by remember { mutableStateOf<Uri?>(null) }

    // Modern Zero-Permission Photo / Media Picker
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            pickedMediaUri = uri
            selectedAttachment = "captured_proof_${System.currentTimeMillis().toString().takeLast(4)}.jpg"
            selectedType = "IMAGE"
        }
    }

    val mockFiles = listOf(
        Pair("deployment_verification_log.pdf", "DOCUMENT"),
        Pair("unit_and_e2e_test_results.json", "CODE"),
        Pair("client_signoff_receipt.pdf", "DOCUMENT")
    )

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.CloudUpload,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Submit Completion Proof",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        Text(
                            text = "Attach photo or documents for Lead verification",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Text(text = "Task: ${task.title}", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        Text(text = "Project: ${task.projectName}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Completion Summary & Notes") },
                    placeholder = { Text("Describe what was delivered, test results, architecture outcomes...") },
                    minLines = 3,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("proof_notes_input")
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Mobile Camera / Gallery Picker Button
                Text("Capture or Upload Photo Evidence", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(6.dp))

                OutlinedButton(
                    onClick = {
                        photoPickerLauncher.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("pick_photo_proof_button"),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(imageVector = Icons.Default.PhotoCamera, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Select Photo from Gallery or Camera", fontSize = 12.sp)
                }

                if (pickedMediaUri != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(130.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .border(1.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(8.dp))
                    ) {
                        AsyncImage(
                            model = pickedMediaUri,
                            contentDescription = "Selected Proof",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                        Box(
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .padding(6.dp)
                                .background(Color.Black.copy(alpha = 0.7f), RoundedCornerShape(4.dp))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text("Ready to Upload", fontSize = 10.sp, color = Color.White)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))
                Text("Or Select Sample Deliverable Document", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(6.dp))

                mockFiles.forEach { (fileName, type) ->
                    val isPicked = selectedAttachment == fileName && pickedMediaUri == null
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (isPicked) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (isPicked) MaterialTheme.colorScheme.primary else Color.Transparent
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 3.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .clickable {
                                selectedAttachment = fileName
                                selectedType = type
                                pickedMediaUri = null
                            }
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Description,
                                contentDescription = null,
                                tint = if (isPicked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = fileName,
                                fontSize = 12.sp,
                                fontWeight = if (isPicked) FontWeight.Bold else FontWeight.Normal,
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.weight(1f)
                            )
                            if (isPicked) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            if (notes.isNotBlank()) {
                                onSubmit(notes, selectedAttachment, selectedType, pickedMediaUri?.toString())
                            }
                        },
                        enabled = notes.isNotBlank(),
                        modifier = Modifier.testTag("submit_proof_button")
                    ) {
                        Icon(imageVector = Icons.Default.Send, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Submit for Review")
                    }
                }
            }
        }
    }
}

@Composable
fun RealtimeNotificationSheet(
    notifications: List<AppNotification>,
    alerts: List<DeadlineAlert>,
    userRole: UserRole,
    onDismiss: () -> Unit,
    onMarkAllRead: () -> Unit,
    onInspectTask: (String) -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(18.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp,
            modifier = Modifier
                .fillMaxWidth()
                .padding(4.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.NotificationsActive,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Notification Center",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                            )
                            Text(
                                text = "Real-time deadline and assignment alerts",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    IconButton(onClick = onDismiss) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close")
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onMarkAllRead) {
                        Text("Mark all read", fontSize = 11.sp)
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (notifications.isEmpty() && alerts.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(30.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("No notifications at this time.", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    } else {
                        // Render real-time notifications
                        notifications.forEach { notif ->
                            val (catColor, catIcon) = when (notif.category) {
                                NotificationCategory.TASK_ASSIGNMENT -> Pair(BrandPrimaryLight, Icons.Default.AssignmentInd)
                                NotificationCategory.DEADLINE_APPROACHING -> Pair(StatusOverdue, Icons.Default.AccessAlarm)
                                NotificationCategory.TASK_APPROVAL -> Pair(StatusDone, Icons.Default.CheckCircle)
                                NotificationCategory.TASK_REVISION -> Pair(StatusInProgress, Icons.Default.ErrorOutline)
                                NotificationCategory.PROOF_SUBMISSION -> Pair(StatusReview, Icons.Default.UploadFile)
                            }

                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = catColor.copy(alpha = 0.08f),
                                border = androidx.compose.foundation.BorderStroke(1.dp, catColor.copy(alpha = 0.25f)),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        notif.taskId?.let { onInspectTask(it) }
                                        onDismiss()
                                    }
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(imageVector = catIcon, contentDescription = null, tint = catColor, modifier = Modifier.size(22.dp))
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text(
                                                text = notif.title,
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = catColor
                                            )
                                            Text(
                                                text = notif.timestamp,
                                                fontSize = 9.sp,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = notif.message,
                                            fontSize = 11.sp,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AdvancedFilterSheet(
    dateRangeFilter: DateRangeFilter,
    assignedMemberFilter: String?,
    priorityFilter: Priority?,
    allMembers: List<User>,
    onDateRangeChange: (DateRangeFilter) -> Unit,
    onAssignedMemberChange: (String?) -> Unit,
    onPriorityChange: (Priority?) -> Unit,
    onClearAll: () -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(18.dp),
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier
                .fillMaxWidth()
                .padding(6.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Advanced Search & Filters",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close")
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Timeline Filter
                Text("TIMELINE & DEADLINE FILTER", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.height(6.dp))

                DateRangeFilter.values().forEach { range ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onDateRangeChange(range) }
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = dateRangeFilter == range,
                            onClick = { onDateRangeChange(range) }
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(range.label, fontSize = 12.sp)
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Assigned User Filter
                Text("ASSIGNED USER FILTER", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onAssignedMemberChange(null) }
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = assignedMemberFilter == null,
                        onClick = { onAssignedMemberChange(null) }
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("All Deliverers", fontSize = 12.sp)
                }

                val members = allMembers.filter { it.role == UserRole.TEAM_MEMBER }
                members.forEach { m ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onAssignedMemberChange(m.id) }
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = assignedMemberFilter == m.id,
                            onClick = { onAssignedMemberChange(m.id) }
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("${m.name} (${m.title})", fontSize = 12.sp)
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Priority Filter
                Text("PRIORITY TIER", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    FilterChip(
                        selected = priorityFilter == null,
                        onClick = { onPriorityChange(null) },
                        label = { Text("Any", fontSize = 11.sp) }
                    )
                    FilterChip(
                        selected = priorityFilter == Priority.URGENT,
                        onClick = { onPriorityChange(Priority.URGENT) },
                        label = { Text("Urgent", fontSize = 11.sp) }
                    )
                    FilterChip(
                        selected = priorityFilter == Priority.HIGH,
                        onClick = { onPriorityChange(Priority.HIGH) },
                        label = { Text("High", fontSize = 11.sp) }
                    )
                }

                Spacer(modifier = Modifier.height(18.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    OutlinedButton(onClick = {
                        onClearAll()
                        onDismiss()
                    }) {
                        Text("Reset All", fontSize = 12.sp)
                    }

                    Button(onClick = onDismiss) {
                        Text("Apply Filters", fontSize = 12.sp)
                    }
                }
            }
        }
    }
}

@Composable
fun RoleSwitcherDialog(
    users: List<User>,
    currentUser: User,
    onSelectUser: (String) -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(18.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp,
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column {
                        Text(
                            text = "Switch Active User",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp
                            ),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Test permissions & workflows across roles",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    IconButton(onClick = onDismiss) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close")
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    users.forEach { user ->
                        val isSelected = user.id == currentUser.id
                        val (roleColor, roleBg) = when (user.role) {
                            UserRole.MANAGER -> Pair(RoleManagerColor, RoleManagerColor.copy(alpha = 0.12f))
                            UserRole.TEAM_LEAD -> Pair(RoleTeamLeadColor, RoleTeamLeadColor.copy(alpha = 0.12f))
                            UserRole.TEAM_MEMBER -> Pair(RoleMemberColor, RoleMemberColor.copy(alpha = 0.12f))
                        }

                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (isSelected) roleBg else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            border = androidx.compose.foundation.BorderStroke(
                                if (isSelected) 2.dp else 1.dp,
                                if (isSelected) roleColor else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .clickable { onSelectUser(user.id) }
                                .testTag("select_user_${user.id}")
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(42.dp)
                                        .clip(CircleShape)
                                        .background(roleColor),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = user.avatarInitials,
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                }

                                Spacer(modifier = Modifier.width(12.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = user.name,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 14.sp,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(4.dp))
                                                .background(roleColor.copy(alpha = 0.2f))
                                                .padding(horizontal = 6.dp, vertical = 2.dp)
                                        ) {
                                            Text(
                                                text = user.role.displayName,
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = roleColor
                                            )
                                        }
                                    }

                                    Text(
                                        text = user.title,
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )

                                    val roleCapabilities = when (user.role) {
                                        UserRole.MANAGER -> "All Clients · Assign Teams · Overdue & 2-Day Alerts · Financials"
                                        UserRole.TEAM_LEAD -> "Assigned Projects · Create Tasks · Review Proofs · 5-Day Alerts"
                                        UserRole.TEAM_MEMBER -> "Assigned Tasks · Update Status · Upload Proofs · Deadlines"
                                    }
                                    Text(
                                        text = roleCapabilities,
                                        fontSize = 10.sp,
                                        color = roleColor,
                                        modifier = Modifier.padding(top = 2.dp)
                                    )
                                }

                                if (isSelected) {
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = "Active User",
                                        tint = roleColor,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ReviewProofDialog(
    task: TaskItem,
    onDismiss: () -> Unit,
    onReview: (approve: Boolean, feedback: String) -> Unit
) {
    val proof = task.submission
    var feedback by remember { mutableStateOf("") }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(RoleTeamLeadColor.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.RateReview,
                            contentDescription = null,
                            tint = RoleTeamLeadColor
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Review Work Submission",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        Text(
                            text = "Verify completion standards & quality",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Text(text = task.title, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        Text(text = "Project: ${task.projectName}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(text = "Submitted by: ${proof?.memberName ?: task.assignedMemberName} (${proof?.submittedAt})", fontSize = 11.sp, color = MaterialTheme.colorScheme.primary)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text("Submitted Member Notes:", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = proof?.notes ?: "No notes provided.",
                        fontSize = 12.sp,
                        modifier = Modifier.padding(10.dp)
                    )
                }

                if (proof?.localMediaUri != null) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Text("Photo Attachment:", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(4.dp))
                    AsyncImage(
                        model = proof.localMediaUri,
                        contentDescription = "Uploaded Proof Photo",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(140.dp)
                            .clip(RoundedCornerShape(8.dp))
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Attachment,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Attachment: ${proof?.attachmentName ?: "verified_artifact.pdf"}",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                OutlinedTextField(
                    value = feedback,
                    onValueChange = { feedback = it },
                    label = { Text("Lead Reviewer Feedback (Optional)") },
                    placeholder = { Text("Add constructive feedback or reason for revision...") },
                    minLines = 2,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("reviewer_feedback_input")
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            val msg = feedback.ifBlank { "Revisions requested. Please review instructions and re-submit." }
                            onReview(false, msg)
                        },
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = StatusOverdue),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("reject_submission_button")
                    ) {
                        Icon(imageVector = Icons.Default.ThumbDown, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Request Revision", fontSize = 11.sp)
                    }

                    Button(
                        onClick = {
                            val msg = feedback.ifBlank { "Approved. Great execution and verified proof." }
                            onReview(true, msg)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = StatusDone),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("approve_submission_button")
                    ) {
                        Icon(imageVector = Icons.Default.ThumbUp, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Approve Work", fontSize = 11.sp)
                    }
                }
            }
        }
    }
}

@Composable
fun AddClientDialog(
    onDismiss: () -> Unit,
    onConfirm: (name: String, company: String, email: String, phone: String, industry: String, contractValue: String, status: ClientStatus) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var company by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var industry by remember { mutableStateOf("Technology") }
    var contractValue by remember { mutableStateOf("$100,000") }
    var status by remember { mutableStateOf(ClientStatus.ACTIVE) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    text = "Add New Client",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                )
                Text(
                    text = "Register a client organization in the system",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(14.dp))

                OutlinedTextField(
                    value = company,
                    onValueChange = { company = it },
                    label = { Text("Company Name") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("client_company_input")
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Primary Contact Person") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("client_name_input")
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Contact Email") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("Phone Number") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = industry,
                    onValueChange = { industry = it },
                    label = { Text("Industry / Sector") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = contractValue,
                    onValueChange = { contractValue = it },
                    label = { Text("Contract Value ($)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            if (company.isNotBlank() && name.isNotBlank()) {
                                onConfirm(name, company, email, phone, industry, contractValue, status)
                            }
                        },
                        enabled = company.isNotBlank() && name.isNotBlank(),
                        modifier = Modifier.testTag("save_client_button")
                    ) {
                        Text("Save Client")
                    }
                }
            }
        }
    }
}

@Composable
fun AddProjectDialog(
    clients: List<Client>,
    teams: List<Team>,
    onDismiss: () -> Unit,
    onConfirm: (title: String, clientId: String, teamId: String, priority: Priority, startDate: String, dueDate: String, dueDays: Int, budget: String) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var selectedClientId by remember { mutableStateOf(clients.firstOrNull()?.id ?: "") }
    var selectedTeamId by remember { mutableStateOf(teams.firstOrNull()?.id ?: "") }
    var priority by remember { mutableStateOf(Priority.HIGH) }
    var budget by remember { mutableStateOf("$85,000") }
    var dueDays by remember { mutableStateOf("14") }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    text = "Initiate New Project",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                )
                Text(
                    text = "Assign client deliverable to a specialized team",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(14.dp))

                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Project Title") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("project_title_input")
                )

                Spacer(modifier = Modifier.height(10.dp))

                Text("Client", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                clients.forEach { c ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { selectedClientId = c.id }
                            .padding(vertical = 4.dp)
                    ) {
                        RadioButton(
                            selected = selectedClientId == c.id,
                            onClick = { selectedClientId = c.id }
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(c.company, fontSize = 13.sp)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text("Assigned Team", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                teams.forEach { t ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { selectedTeamId = t.id }
                            .padding(vertical = 4.dp)
                    ) {
                        RadioButton(
                            selected = selectedTeamId == t.id,
                            onClick = { selectedTeamId = t.id }
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("${t.name} (Lead: ${t.leadName})", fontSize = 13.sp)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = budget,
                    onValueChange = { budget = it },
                    label = { Text("Budget Allocation") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = dueDays,
                    onValueChange = { dueDays = it },
                    label = { Text("Timeline (Days from today)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            if (title.isNotBlank()) {
                                onConfirm(
                                    title,
                                    selectedClientId,
                                    selectedTeamId,
                                    priority,
                                    "Today",
                                    "In $dueDays days",
                                    dueDays.toIntOrNull() ?: 14,
                                    budget
                                )
                            }
                        },
                        enabled = title.isNotBlank(),
                        modifier = Modifier.testTag("save_project_button")
                    ) {
                        Text("Create Project")
                    }
                }
            }
        }
    }
}

@Composable
fun AddTaskDialog(
    projects: List<Project>,
    members: List<User>,
    onDismiss: () -> Unit,
    onConfirm: (title: String, description: String, projectId: String, assignedMemberId: String, priority: Priority, daysUntilDue: Int, dueFormatted: String) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var selectedProjectId by remember { mutableStateOf(projects.firstOrNull()?.id ?: "") }
    var selectedMemberId by remember { mutableStateOf(members.firstOrNull { it.role == UserRole.TEAM_MEMBER }?.id ?: "") }
    var priority by remember { mutableStateOf(Priority.HIGH) }
    var daysUntilDue by remember { mutableStateOf("4") }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    text = "Assign New Task",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                )
                Text(
                    text = "Create actionable deliverable for team member",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(14.dp))

                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Task Title") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("task_title_input")
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Instructions & Acceptance Criteria") },
                    minLines = 3,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(10.dp))

                Text("Target Project", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                projects.forEach { p ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { selectedProjectId = p.id }
                            .padding(vertical = 3.dp)
                    ) {
                        RadioButton(
                            selected = selectedProjectId == p.id,
                            onClick = { selectedProjectId = p.id }
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(p.title, fontSize = 12.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text("Assign to Team Member", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                val memberUsers = members.filter { it.role == UserRole.TEAM_MEMBER }
                memberUsers.forEach { m ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { selectedMemberId = m.id }
                            .padding(vertical = 3.dp)
                    ) {
                        RadioButton(
                            selected = selectedMemberId == m.id,
                            onClick = { selectedMemberId = m.id }
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("${m.name} (${m.title})", fontSize = 12.sp)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = daysUntilDue,
                    onValueChange = { daysUntilDue = it },
                    label = { Text("Due In (Days)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            if (title.isNotBlank() && selectedProjectId.isNotBlank() && selectedMemberId.isNotBlank()) {
                                val days = daysUntilDue.toIntOrNull() ?: 3
                                onConfirm(
                                    title,
                                    description,
                                    selectedProjectId,
                                    selectedMemberId,
                                    priority,
                                    days,
                                    "Due in $days days"
                                )
                            }
                        },
                        enabled = title.isNotBlank(),
                        modifier = Modifier.testTag("save_task_button")
                    ) {
                        Text("Assign Task")
                    }
                }
            }
        }
    }
}
