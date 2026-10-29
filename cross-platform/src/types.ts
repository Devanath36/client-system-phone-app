export type UserRole = 'MANAGER' | 'TEAM_LEAD' | 'TEAM_MEMBER';

export interface User {
  id: string;
  name: string;
  email: string;
  role: UserRole;
  title: string;
  avatarInitials: string;
  teamId?: string;
}

export type ClientStatus = 'ACTIVE' | 'IN_NEGOTIATION' | 'COMPLETED';

export interface Client {
  id: string;
  name: string;
  company: string;
  email: string;
  phone: string;
  industry: string;
  contractValue: string;
  status: ClientStatus;
  createdAt: number;
  isDemo?: boolean;
}

export interface Team {
  id: string;
  name: string;
  leadId: string;
  leadName: string;
  memberIds: string[];
  department: string;
  isDemo?: boolean;
}

export type Priority = 'LOW' | 'MEDIUM' | 'HIGH' | 'URGENT';
export type ProjectStatus = 'PLANNING' | 'IN_PROGRESS' | 'ON_HOLD' | 'COMPLETED';

export interface Project {
  id: string;
  title: string;
  clientId: string;
  clientName: string;
  teamId: string;
  teamName: string;
  status: ProjectStatus;
  priority: Priority;
  startDate: string;
  dueDate: string;
  budget: string;
  progressPercent: number;
  isDemo?: boolean;
}

export type TaskStatus = 'NEW' | 'IN_PROGRESS' | 'PENDING_REVIEW' | 'DONE';
export type ProofStatus = 'PENDING' | 'APPROVED' | 'REJECTED';

export interface SubmissionProof {
  id: string;
  taskId: string;
  memberId: string;
  memberName: string;
  notes: string;
  attachmentName: string;
  attachmentType: 'IMAGE' | 'DOCUMENT' | 'CODE';
  mediaUri?: string;
  submittedAt: string;
  status: ProofStatus;
  reviewerFeedback?: string;
  reviewedBy?: string;
  reviewedAt?: string;
}

export interface TaskAuditLog {
  id: string;
  timestamp: string;
  actorName: string;
  actorRole: UserRole;
  changeType: 'CREATED' | 'STATUS_CHANGED' | 'REASSIGNED' | 'PROOF_SUBMITTED' | 'REVIEW_APPROVED' | 'REVIEW_REJECTED';
  summary: string;
  previousValue?: string;
  newValue?: string;
}

export interface TaskItem {
  id: string;
  title: string;
  description: string;
  projectId: string;
  projectName: string;
  assignedMemberId: string;
  assignedMemberName: string;
  teamLeadId: string;
  status: TaskStatus;
  priority: Priority;
  daysUntilDue: number;
  dueFormatted: string;
  submission?: SubmissionProof;
  auditHistory: TaskAuditLog[];
  isDemo?: boolean;
}

export type NotificationCategory =
  | 'TASK_ASSIGNMENT'
  | 'DEADLINE_APPROACHING'
  | 'TASK_APPROVAL'
  | 'TASK_REVISION'
  | 'PROOF_SUBMISSION';

export interface AppNotification {
  id: string;
  title: string;
  message: string;
  category: NotificationCategory;
  timestamp: string;
  isRead: boolean;
  taskId?: string;
  targetRole?: UserRole;
  targetUserId?: string;
}

export type DateRangeFilter = 'ALL' | 'OVERDUE' | 'DUE_2_DAYS' | 'DUE_5_DAYS' | 'DUE_THIS_WEEK';
