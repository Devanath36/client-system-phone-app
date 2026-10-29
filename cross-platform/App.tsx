import React, { useState, useMemo } from 'react';
import {
  StyleSheet,
  Text,
  View,
  ScrollView,
  TouchableOpacity,
  TextInput,
  Modal,
  Image,
  SafeAreaView,
  StatusBar,
  Platform,
  Alert
} from 'react-native';
import * as ImagePicker from 'expo-image-picker';
import {
  User,
  UserRole,
  Client,
  Project,
  TaskItem,
  TaskAuditLog,
  SubmissionProof,
  AppNotification,
  DateRangeFilter,
  TaskStatus,
  Priority
} from './src/types';

// Mock Profiles
const SYSTEM_USERS: User[] = [
  { id: 'usr_mgr_1', name: 'Sarah Chen', email: 'sarah.c@enterprisecms.com', role: 'MANAGER', title: 'VP of Delivery & Operations', avatarInitials: 'SC' },
  { id: 'usr_lead_1', name: 'Alex Morgan', email: 'alex.m@enterprisecms.com', role: 'TEAM_LEAD', title: 'Engineering Lead · Alpha Team', avatarInitials: 'AM', teamId: 'team_alpha' },
  { id: 'usr_mem_1', name: 'Maya Lin', email: 'maya.l@enterprisecms.com', role: 'TEAM_MEMBER', title: 'Senior Full-Stack Engineer', avatarInitials: 'ML', teamId: 'team_alpha' },
  { id: 'usr_mem_2', name: 'David Kim', email: 'david.k@enterprisecms.com', role: 'TEAM_MEMBER', title: 'Cloud Infrastructure Specialist', avatarInitials: 'DK', teamId: 'team_alpha' }
];

export default function App() {
  const [currentUser, setCurrentUser] = useState<User>(SYSTEM_USERS[0]);
  const [activeTab, setActiveTab] = useState<'DASHBOARD' | 'CLIENTS' | 'PROJECTS' | 'TASKS' | 'REVIEW_QUEUE'>('DASHBOARD');
  const [isDarkMode, setIsDarkMode] = useState(false);

  // Search & Filters
  const [searchQuery, setSearchQuery] = useState('');
  const [statusFilter, setStatusFilter] = useState<TaskStatus | null>(null);
  const [dateRangeFilter, setDateRangeFilter] = useState<DateRangeFilter>('ALL');

  // Modals
  const [selectedTaskDetail, setSelectedTaskDetail] = useState<TaskItem | null>(null);
  const [showRoleModal, setShowRoleModal] = useState(false);
  const [showNotifModal, setShowNotifModal] = useState(false);
  const [proofTaskToSubmit, setProofTaskToSubmit] = useState<TaskItem | null>(null);

  // Proof submission form states
  const [proofNotes, setProofNotes] = useState('');
  const [proofMediaUri, setProofMediaUri] = useState<string | null>(null);

  // Initial State
  const [tasks, setTasks] = useState<TaskItem[]>([
    {
      id: 'task_1',
      title: 'Audit OAuth2 Security Credentials & Token Rotation',
      description: 'Zero-trust token exchange and rate limiting verification across microservices.',
      projectId: 'proj_1',
      projectName: 'Payment Gateway 2.0',
      assignedMemberId: 'usr_mem_2',
      assignedMemberName: 'David Kim',
      teamLeadId: 'usr_lead_1',
      status: 'IN_PROGRESS',
      priority: 'URGENT',
      daysUntilDue: -1,
      dueFormatted: 'Yesterday (Overdue)',
      auditHistory: [
        { id: 'a1', timestamp: 'Oct 20, 2026', actorName: 'Alex Morgan', actorRole: 'TEAM_LEAD', changeType: 'CREATED', summary: 'Task created and allocated to David Kim.' },
        { id: 'a2', timestamp: 'Oct 22, 2026', actorName: 'David Kim', actorRole: 'TEAM_MEMBER', changeType: 'STATUS_CHANGED', summary: 'Status updated from New to In Progress.', previousValue: 'New', newValue: 'In Progress' }
      ]
    },
    {
      id: 'task_2',
      title: 'Design Tokens Export & Accessibility Compliance Audit',
      description: 'Finalize WCAG AAA color contrast validation across mobile & web design system.',
      projectId: 'proj_4',
      projectName: 'Clean Energy Design Tokens',
      assignedMemberId: 'usr_mem_1',
      assignedMemberName: 'Maya Lin',
      teamLeadId: 'usr_lead_1',
      status: 'IN_PROGRESS',
      priority: 'HIGH',
      daysUntilDue: 2,
      dueFormatted: 'Due in 2 days',
      auditHistory: [
        { id: 'a3', timestamp: 'Oct 24, 2026', actorName: 'Sarah Chen', actorRole: 'MANAGER', changeType: 'CREATED', summary: 'Task created with 2-day Manager deadline rule.' }
      ]
    },
    {
      id: 'task_4',
      title: 'Database Migration for Stripe Webhook Events',
      description: 'Idempotent table schemas and dead-letter queues for payment events.',
      projectId: 'proj_1',
      projectName: 'Payment Gateway 2.0',
      assignedMemberId: 'usr_mem_1',
      assignedMemberName: 'Maya Lin',
      teamLeadId: 'usr_lead_1',
      status: 'PENDING_REVIEW',
      priority: 'URGENT',
      daysUntilDue: 3,
      dueFormatted: 'Due in 3 days',
      submission: {
        id: 'sub_1',
        taskId: 'task_4',
        memberId: 'usr_mem_1',
        memberName: 'Maya Lin',
        notes: 'Migration scripts executed in staging with 150ms p99 latency test results.',
        attachmentName: 'stripe_migration_audit_report.pdf',
        attachmentType: 'DOCUMENT',
        submittedAt: 'Today at 09:15 AM',
        status: 'PENDING'
      },
      auditHistory: [
        { id: 'a4', timestamp: 'Oct 15, 2026', actorName: 'Alex Morgan', actorRole: 'TEAM_LEAD', changeType: 'CREATED', summary: 'Task created.' },
        { id: 'a5', timestamp: 'Today at 09:15 AM', actorName: 'Maya Lin', actorRole: 'TEAM_MEMBER', changeType: 'PROOF_SUBMITTED', summary: 'Proof submitted. Status set to Pending Review.' }
      ]
    }
  ]);

  const [notifications, setNotifications] = useState<AppNotification[]>([
    { id: 'n1', title: 'Critical Overdue Alert', message: "Task 'Audit OAuth2 Security Credentials' is 1 day overdue!", category: 'DEADLINE_APPROACHING', timestamp: '10m ago', isRead: false },
    { id: 'n2', title: 'Manager 2-Day Deadline Alert', message: "Task 'Design Tokens Export' is due in 2 days.", category: 'DEADLINE_APPROACHING', timestamp: '1h ago', isRead: false, targetRole: 'MANAGER' },
    { id: 'n3', title: 'Proof Awaiting Verification', message: "Maya Lin submitted completion proof for 'Database Migration for Stripe'.", category: 'PROOF_SUBMISSION', timestamp: '3h ago', isRead: false }
  ]);

  // Photo / Camera Proof Picker
  const pickProofImage = async () => {
    const permissionResult = await ImagePicker.requestMediaLibraryPermissionsAsync();
    if (!permissionResult.granted) {
      Alert.alert('Permission Required', 'Please enable gallery access to upload completion proof.');
      return;
    }

    const result = await ImagePicker.launchImageLibraryAsync({
      mediaTypes: ImagePicker.MediaTypeOptions.Images,
      allowsEditing: true,
      aspect: [4, 3],
      quality: 0.8
    });

    if (!result.canceled && result.assets && result.assets.length > 0) {
      setProofMediaUri(result.assets[0].uri);
    }
  };

  const handleCapturePhoto = async () => {
    const permissionResult = await ImagePicker.requestCameraPermissionsAsync();
    if (!permissionResult.granted) {
      Alert.alert('Permission Required', 'Please enable camera access to capture completion proof.');
      return;
    }

    const result = await ImagePicker.launchCameraAsync({
      allowsEditing: true,
      aspect: [4, 3],
      quality: 0.8
    });

    if (!result.canceled && result.assets && result.assets.length > 0) {
      setProofMediaUri(result.assets[0].uri);
    }
  };

  // Submit Proof Action with Per-Task Audit Entry
  const handleSubmitProof = () => {
    if (!proofTaskToSubmit || !proofNotes.trim()) return;

    const newProof: SubmissionProof = {
      id: `sub_${Date.now()}`,
      taskId: proofTaskToSubmit.id,
      memberId: currentUser.id,
      memberName: currentUser.name,
      notes: proofNotes,
      attachmentName: proofMediaUri ? 'camera_photo_proof.jpg' : 'deliverable_verification.pdf',
      attachmentType: proofMediaUri ? 'IMAGE' : 'DOCUMENT',
      mediaUri: proofMediaUri || undefined,
      submittedAt: 'Just now',
      status: 'PENDING'
    };

    const newAudit: TaskAuditLog = {
      id: `tal_${Date.now()}`,
      timestamp: 'Just now',
      actorName: currentUser.name,
      actorRole: currentUser.role,
      changeType: 'PROOF_SUBMITTED',
      summary: `Proof uploaded with notes by ${currentUser.name}. Status updated to Pending Review.`,
      previousValue: proofTaskToSubmit.status,
      newValue: 'In Review'
    };

    setTasks(prev => prev.map(t => {
      if (t.id === proofTaskToSubmit.id) {
        return {
          ...t,
          status: 'PENDING_REVIEW',
          submission: newProof,
          auditHistory: [newAudit, ...t.auditHistory]
        };
      }
      return t;
    }));

    // Dispatch real-time notification
    setNotifications(prev => [
      {
        id: `notif_${Date.now()}`,
        title: 'New Proof Submitted',
        message: `${currentUser.name} uploaded proof for '${proofTaskToSubmit.title}'.`,
        category: 'PROOF_SUBMISSION',
        timestamp: 'Just now',
        isRead: false
      },
      ...prev
    ]);

    setProofTaskToSubmit(null);
    setProofNotes('');
    setProofMediaUri(null);
    Alert.alert('Proof Submitted', 'Your evidence was uploaded and sent to the Team Lead for verification.');
  };

  // Review Proof (Approve / Reject) Action with Per-Task Audit Entry
  const handleReviewProof = (taskId: string, approve: boolean, feedback: string) => {
    setTasks(prev => prev.map(task => {
      if (task.id === taskId && task.submission) {
        const updatedStatus: TaskStatus = approve ? 'DONE' : 'IN_PROGRESS';
        const updatedProof = {
          ...task.submission,
          status: (approve ? 'APPROVED' : 'REJECTED') as any,
          reviewerFeedback: feedback,
          reviewedBy: currentUser.name,
          reviewedAt: 'Just now'
        };

        const reviewAudit: TaskAuditLog = {
          id: `tal_${Date.now()}`,
          timestamp: 'Just now',
          actorName: currentUser.name,
          actorRole: currentUser.role,
          changeType: approve ? 'REVIEW_APPROVED' : 'REVIEW_REJECTED',
          summary: approve
            ? `Approved by ${currentUser.name}. Task marked as Done.`
            : `Revision requested by ${currentUser.name}. Feedback: "${feedback}". Status returned to In Progress.`,
          previousValue: 'In Review',
          newValue: updatedStatus
        };

        return {
          ...task,
          status: updatedStatus,
          submission: updatedProof,
          auditHistory: [reviewAudit, ...task.auditHistory]
        };
      }
      return task;
    }));

    setNotifications(prev => [
      {
        id: `notif_${Date.now()}`,
        title: approve ? 'Task Approved! 🎉' : 'Revision Requested ⚠️',
        message: `${currentUser.name} ${approve ? 'approved' : 'requested changes on'} task submission.`,
        category: approve ? 'TASK_APPROVAL' : 'TASK_REVISION',
        timestamp: 'Just now',
        isRead: false
      },
      ...prev
    ]);

    setSelectedTaskDetail(null);
  };

  // Filtered Tasks
  const filteredTasks = useMemo(() => {
    return tasks.filter(task => {
      const matchesQuery = searchQuery === '' ||
        task.title.toLowerCase().includes(searchQuery.toLowerCase()) ||
        task.projectName.toLowerCase().includes(searchQuery.toLowerCase()) ||
        task.assignedMemberName.toLowerCase().includes(searchQuery.toLowerCase());

      const matchesStatus = statusFilter === null || task.status === statusFilter;

      const matchesDateRange =
        dateRangeFilter === 'ALL' ||
        (dateRangeFilter === 'OVERDUE' && task.daysUntilDue < 0 && task.status !== 'DONE') ||
        (dateRangeFilter === 'DUE_2_DAYS' && task.daysUntilDue >= 0 && task.daysUntilDue <= 2 && task.status !== 'DONE') ||
        (dateRangeFilter === 'DUE_5_DAYS' && task.daysUntilDue >= 0 && task.daysUntilDue <= 5 && task.status !== 'DONE') ||
        (dateRangeFilter === 'DUE_THIS_WEEK' && task.daysUntilDue >= 0 && task.daysUntilDue <= 7 && task.status !== 'DONE');

      return matchesQuery && matchesStatus && matchesDateRange;
    });
  }, [tasks, searchQuery, statusFilter, dateRangeFilter]);

  const themeColors = {
    bg: isDarkMode ? '#0B0F19' : '#F8FAFC',
    card: isDarkMode ? '#131B2E' : '#FFFFFF',
    text: isDarkMode ? '#F8FAFC' : '#0F172A',
    subtext: isDarkMode ? '#94A3B8' : '#64748B',
    primary: '#2563EB',
    border: isDarkMode ? '#1E293B' : '#E2E8F0'
  };

  return (
    <SafeAreaView style={[styles.container, { backgroundColor: themeColors.bg }]}>
      <StatusBar barStyle={isDarkMode ? 'light-content' : 'dark-content'} />

      {/* Header Bar */}
      <View style={[styles.header, { backgroundColor: themeColors.card, borderBottomColor: themeColors.border }]}>
        <View style={styles.brandRow}>
          <View style={styles.brandBadge}>
            <Text style={styles.brandBadgeText}>CF</Text>
          </View>
          <View>
            <Text style={[styles.brandTitle, { color: themeColors.text }]}>ClientFlow</Text>
            <Text style={[styles.brandSubtitle, { color: themeColors.subtext }]}>Cross-Platform CMS</Text>
          </View>
        </View>

        <View style={styles.headerRightActions}>
          <TouchableOpacity
            style={styles.notifButton}
            onPress={() => setShowNotifModal(true)}
          >
            <Text style={styles.notifIcon}>🔔</Text>
            {notifications.filter(n => !n.isRead).length > 0 && (
              <View style={styles.notifBadge}>
                <Text style={styles.notifBadgeText}>{notifications.filter(n => !n.isRead).length}</Text>
              </View>
            )}
          </TouchableOpacity>

          <TouchableOpacity
            style={[styles.roleCapsule, { borderColor: currentUser.role === 'MANAGER' ? '#8B5CF6' : '#0284C7' }]}
            onPress={() => setShowRoleModal(true)}
          >
            <Text style={styles.roleText}>{currentUser.role}</Text>
          </TouchableOpacity>
        </View>
      </View>

      {/* Main Tabs */}
      <View style={[styles.tabBar, { backgroundColor: themeColors.card, borderBottomColor: themeColors.border }]}>
        <TouchableOpacity
          style={[styles.tabItem, activeTab === 'DASHBOARD' && styles.activeTabItem]}
          onPress={() => setActiveTab('DASHBOARD')}
        >
          <Text style={[styles.tabText, activeTab === 'DASHBOARD' && styles.activeTabText]}>Dashboard</Text>
        </TouchableOpacity>
        <TouchableOpacity
          style={[styles.tabItem, activeTab === 'TASKS' && styles.activeTabItem]}
          onPress={() => setActiveTab('TASKS')}
        >
          <Text style={[styles.tabText, activeTab === 'TASKS' && styles.activeTabText]}>Tasks</Text>
        </TouchableOpacity>
        <TouchableOpacity
          style={[styles.tabItem, activeTab === 'REVIEW_QUEUE' && styles.activeTabItem]}
          onPress={() => setActiveTab('REVIEW_QUEUE')}
        >
          <Text style={[styles.tabText, activeTab === 'REVIEW_QUEUE' && styles.activeTabText]}>Review Queue</Text>
        </TouchableOpacity>
      </View>

      <ScrollView contentContainerStyle={styles.scrollContent}>
        {activeTab === 'DASHBOARD' && (
          <View>
            {/* Welcome banner */}
            <View style={[styles.bannerCard, { backgroundColor: '#DBEAFE' }]}>
              <Text style={styles.bannerRole}>{currentUser.role} OVERVIEW</Text>
              <Text style={styles.bannerTitle}>Welcome, {currentUser.name}</Text>
              <Text style={styles.bannerSubtext}>
                {currentUser.role === 'MANAGER'
                  ? 'All 4 clients active · 2-day deadline alert rule enabled'
                  : currentUser.role === 'TEAM_LEAD'
                  ? 'Supervising team deliverables · 5-day deadline alerts enabled'
                  : 'Assigned deliverer workspace · Upload proof upon completion'}
              </Text>
            </View>

            {/* KPI Cards */}
            <View style={styles.kpiGrid}>
              <View style={[styles.kpiCard, { backgroundColor: themeColors.card, borderColor: themeColors.border }]}>
                <Text style={[styles.kpiValue, { color: themeColors.primary }]}>4</Text>
                <Text style={[styles.kpiLabel, { color: themeColors.text }]}>Active Clients</Text>
              </View>
              <View style={[styles.kpiCard, { backgroundColor: themeColors.card, borderColor: themeColors.border }]}>
                <Text style={[styles.kpiValue, { color: '#0D9488' }]}>{tasks.length}</Text>
                <Text style={[styles.kpiLabel, { color: themeColors.text }]}>Total Tasks</Text>
              </View>
              <View style={[styles.kpiCard, { backgroundColor: themeColors.card, borderColor: themeColors.border }]}>
                <Text style={[styles.kpiValue, { color: '#8B5CF6' }]}>
                  {tasks.filter(t => t.status === 'PENDING_REVIEW').length}
                </Text>
                <Text style={[styles.kpiLabel, { color: themeColors.text }]}>In Review</Text>
              </View>
            </View>
          </View>
        )}

        {(activeTab === 'TASKS' || activeTab === 'DASHBOARD') && (
          <View style={styles.tasksSection}>
            <Text style={[styles.sectionTitle, { color: themeColors.text }]}>
              {activeTab === 'TASKS' ? 'Deliverable Management' : 'Recent Deliverables'}
            </Text>

            {activeTab === 'TASKS' && (
              <View style={styles.filterContainer}>
                {/* Search input */}
                <TextInput
                  style={[styles.searchInput, { backgroundColor: themeColors.card, color: themeColors.text, borderColor: themeColors.border }]}
                  placeholder="Search keywords, project, or assignee..."
                  placeholderTextColor={themeColors.subtext}
                  value={searchQuery}
                  onChangeText={setSearchQuery}
                />

                {/* Date range filter pills */}
                <ScrollView horizontal showsHorizontalScrollIndicator={false} style={styles.pillRow}>
                  {(['ALL', 'OVERDUE', 'DUE_2_DAYS', 'DUE_5_DAYS', 'DUE_THIS_WEEK'] as DateRangeFilter[]).map(range => (
                    <TouchableOpacity
                      key={range}
                      style={[styles.filterPill, dateRangeFilter === range && styles.activeFilterPill]}
                      onPress={() => setDateRangeFilter(range)}
                    >
                      <Text style={[styles.filterPillText, dateRangeFilter === range && styles.activeFilterPillText]}>
                        {range === 'ALL' ? 'All Dates' : range === 'OVERDUE' ? 'Overdue' : range === 'DUE_2_DAYS' ? '≤ 2 Days' : range === 'DUE_5_DAYS' ? '≤ 5 Days' : 'This Week'}
                      </Text>
                    </TouchableOpacity>
                  ))}
                </ScrollView>
              </View>
            )}

            {filteredTasks.map(task => (
              <TouchableOpacity
                key={task.id}
                style={[styles.taskCard, { backgroundColor: themeColors.card, borderColor: themeColors.border }]}
                onPress={() => setSelectedTaskDetail(task)}
              >
                <View style={styles.taskCardHeader}>
                  <Text style={styles.taskProjectName}>{task.projectName}</Text>
                  <View style={[styles.deadlinePill, { backgroundColor: task.daysUntilDue < 0 ? '#FEE2E2' : '#FEF3C7' }]}>
                    <Text style={[styles.deadlinePillText, { color: task.daysUntilDue < 0 ? '#EF4444' : '#D97706' }]}>
                      {task.daysUntilDue < 0 ? 'OVERDUE' : task.dueFormatted}
                    </Text>
                  </View>
                </View>

                <Text style={[styles.taskTitle, { color: themeColors.text }]}>{task.title}</Text>
                <Text style={[styles.taskDesc, { color: themeColors.subtext }]} numberOfLines={2}>{task.description}</Text>

                <View style={styles.taskCardFooter}>
                  <Text style={[styles.taskAssignee, { color: themeColors.text }]}>👤 {task.assignedMemberName}</Text>
                  <View style={[styles.statusBadge, { backgroundColor: task.status === 'DONE' ? '#D1FAE5' : task.status === 'PENDING_REVIEW' ? '#EDE9FE' : '#DBEAFE' }]}>
                    <Text style={styles.statusBadgeText}>{task.status}</Text>
                  </View>
                </View>

                {/* Audit history indicator */}
                <View style={styles.auditIndicator}>
                  <Text style={styles.auditIndicatorText}>🕒 {task.auditHistory.length} audit change events recorded</Text>
                  <Text style={styles.auditActionPrompt}>Tap for details →</Text>
                </View>
              </TouchableOpacity>
            ))}
          </View>
        )}

        {activeTab === 'REVIEW_QUEUE' && (
          <View>
            <Text style={[styles.sectionTitle, { color: themeColors.text }]}>Proof Verification Queue</Text>
            {tasks.filter(t => t.status === 'PENDING_REVIEW').length === 0 ? (
              <View style={styles.emptyState}>
                <Text style={[styles.emptyStateText, { color: themeColors.subtext }]}>All submissions verified!</Text>
              </View>
            ) : (
              tasks.filter(t => t.status === 'PENDING_REVIEW').map(task => (
                <View key={task.id} style={[styles.reviewCard, { backgroundColor: themeColors.card, borderColor: themeColors.border }]}>
                  <Text style={styles.taskProjectName}>{task.projectName}</Text>
                  <Text style={[styles.taskTitle, { color: themeColors.text }]}>{task.title}</Text>
                  <Text style={[styles.taskDesc, { color: themeColors.subtext }]}>Submitted by {task.submission?.memberName}: "{task.submission?.notes}"</Text>

                  {task.submission?.mediaUri && (
                    <Image source={{ uri: task.submission.mediaUri }} style={styles.proofThumbnail} />
                  )}

                  <View style={styles.reviewActionRow}>
                    <TouchableOpacity
                      style={[styles.reviewBtn, { backgroundColor: '#EF4444' }]}
                      onPress={() => handleReviewProof(task.id, false, 'Needs additional test coverage.')}
                    >
                      <Text style={styles.reviewBtnText}>Request Revision</Text>
                    </TouchableOpacity>
                    <TouchableOpacity
                      style={[styles.reviewBtn, { backgroundColor: '#10B981' }]}
                      onPress={() => handleReviewProof(task.id, true, 'Approved, verified proof.')}
                    >
                      <Text style={styles.reviewBtnText}>Approve & Complete</Text>
                    </TouchableOpacity>
                  </View>
                </View>
              ))
            )}
          </View>
        )}
      </ScrollView>

      {/* Task Detail & Audit Trail Modal */}
      {selectedTaskDetail && (
        <Modal visible={true} animationType="slide" transparent={true}>
          <View style={styles.modalBackdrop}>
            <View style={[styles.modalContent, { backgroundColor: themeColors.card }]}>
              <ScrollView>
                <View style={styles.modalHeader}>
                  <Text style={[styles.modalTitle, { color: themeColors.text }]}>{selectedTaskDetail.title}</Text>
                  <TouchableOpacity onPress={() => setSelectedTaskDetail(null)}>
                    <Text style={styles.closeBtn}>✕</Text>
                  </TouchableOpacity>
                </View>

                <Text style={[styles.modalDesc, { color: themeColors.subtext }]}>{selectedTaskDetail.description}</Text>
                <Text style={styles.metaLabel}>Assignee: {selectedTaskDetail.assignedMemberName}</Text>
                <Text style={styles.metaLabel}>Deadline: {selectedTaskDetail.dueFormatted}</Text>

                {/* Audit History Timeline */}
                <View style={styles.auditSection}>
                  <Text style={styles.auditSectionTitle}>AUDIT TRAIL & CHANGE LOG</Text>
                  {selectedTaskDetail.auditHistory.map(audit => (
                    <View key={audit.id} style={styles.auditItem}>
                      <View style={styles.auditDot} />
                      <View style={styles.auditItemContent}>
                        <Text style={styles.auditActor}>{audit.actorName} ({audit.actorRole}) · {audit.timestamp}</Text>
                        <Text style={[styles.auditSummary, { color: themeColors.text }]}>{audit.summary}</Text>
                        {audit.previousValue && audit.newValue && (
                          <Text style={styles.auditDelta}>{audit.previousValue} → {audit.newValue}</Text>
                        )}
                      </View>
                    </View>
                  ))}
                </View>

                {/* Submit Proof trigger for members */}
                {selectedTaskDetail.status !== 'DONE' && (
                  <TouchableOpacity
                    style={styles.submitProofBtn}
                    onPress={() => {
                      setProofTaskToSubmit(selectedTaskDetail);
                      setSelectedTaskDetail(null);
                    }}
                  >
                    <Text style={styles.submitProofBtnText}>Submit Work Proof</Text>
                  </TouchableOpacity>
                )}
              </ScrollView>
            </View>
          </View>
        </Modal>
      )}

      {/* Submit Proof Modal with Camera & Gallery */}
      {proofTaskToSubmit && (
        <Modal visible={true} animationType="slide" transparent={true}>
          <View style={styles.modalBackdrop}>
            <View style={[styles.modalContent, { backgroundColor: themeColors.card }]}>
              <Text style={[styles.modalTitle, { color: themeColors.text }]}>Upload Completion Proof</Text>
              <Text style={[styles.modalDesc, { color: themeColors.subtext }]}>Task: {proofTaskToSubmit.title}</Text>

              <TextInput
                style={[styles.textArea, { backgroundColor: isDarkMode ? '#1E293B' : '#F1F5F9', color: themeColors.text }]}
                placeholder="Describe your deliverable, outcomes, and test results..."
                placeholderTextColor={themeColors.subtext}
                multiline
                numberOfLines={3}
                value={proofNotes}
                onChangeText={setProofNotes}
              />

              <View style={styles.photoPickerRow}>
                <TouchableOpacity style={styles.mediaButton} onPress={handleCapturePhoto}>
                  <Text style={styles.mediaButtonText}>📷 Open Camera</Text>
                </TouchableOpacity>
                <TouchableOpacity style={styles.mediaButton} onPress={pickProofImage}>
                  <Text style={styles.mediaButtonText}>🖼️ Choose Photo</Text>
                </TouchableOpacity>
              </View>

              {proofMediaUri && (
                <Image source={{ uri: proofMediaUri }} style={styles.previewImage} />
              )}

              <View style={styles.modalFooterActions}>
                <TouchableOpacity style={styles.cancelBtn} onPress={() => setProofTaskToSubmit(null)}>
                  <Text style={styles.cancelBtnText}>Cancel</Text>
                </TouchableOpacity>
                <TouchableOpacity style={styles.confirmBtn} onPress={handleSubmitProof}>
                  <Text style={styles.confirmBtnText}>Submit to Lead</Text>
                </TouchableOpacity>
              </View>
            </View>
          </View>
        </Modal>
      )}

      {/* Role Switcher Modal */}
      {showRoleModal && (
        <Modal visible={true} animationType="fade" transparent={true}>
          <View style={styles.modalBackdrop}>
            <View style={[styles.modalContent, { backgroundColor: themeColors.card }]}>
              <Text style={[styles.modalTitle, { color: themeColors.text }]}>Switch Active User</Text>
              {SYSTEM_USERS.map(u => (
                <TouchableOpacity
                  key={u.id}
                  style={[styles.userOption, currentUser.id === u.id && styles.activeUserOption]}
                  onPress={() => {
                    setCurrentUser(u);
                    setShowRoleModal(false);
                  }}
                >
                  <Text style={styles.userName}>{u.name} ({u.role})</Text>
                  <Text style={styles.userTitle}>{u.title}</Text>
                </TouchableOpacity>
              ))}
              <TouchableOpacity style={styles.cancelBtn} onPress={() => setShowRoleModal(false)}>
                <Text style={styles.cancelBtnText}>Close</Text>
              </TouchableOpacity>
            </View>
          </View>
        </Modal>
      )}

      {/* Notifications Modal */}
      {showNotifModal && (
        <Modal visible={true} animationType="slide" transparent={true}>
          <View style={styles.modalBackdrop}>
            <View style={[styles.modalContent, { backgroundColor: themeColors.card }]}>
              <Text style={[styles.modalTitle, { color: themeColors.text }]}>Real-time Notifications</Text>
              <ScrollView style={{ maxHeight: 350 }}>
                {notifications.map(n => (
                  <View key={n.id} style={styles.notifItem}>
                    <Text style={styles.notifTitle}>{n.title} · <Text style={styles.notifTime}>{n.timestamp}</Text></Text>
                    <Text style={styles.notifMsg}>{n.message}</Text>
                  </View>
                ))}
              </ScrollView>
              <TouchableOpacity style={styles.cancelBtn} onPress={() => setShowNotifModal(false)}>
                <Text style={styles.cancelBtnText}>Close</Text>
              </TouchableOpacity>
            </View>
          </View>
        </Modal>
      )}
    </SafeAreaView>
  );
}

const styles = StyleSheet.create({
  container: { flex: 1 },
  header: {
    paddingHorizontal: 16,
    paddingVertical: 12,
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    borderBottomWidth: 1
  },
  brandRow: { flexDirection: 'row', alignItems: 'center' },
  brandBadge: {
    width: 34,
    height: 34,
    borderRadius: 8,
    backgroundColor: '#2563EB',
    justifyContent: 'center',
    alignItems: 'center',
    marginRight: 10
  },
  brandBadgeText: { color: '#FFF', fontWeight: 'bold' },
  brandTitle: { fontSize: 16, fontWeight: 'bold' },
  brandSubtitle: { fontSize: 11 },
  headerRightActions: { flexDirection: 'row', alignItems: 'center' },
  notifButton: { marginRight: 12, position: 'relative' },
  notifIcon: { fontSize: 20 },
  notifBadge: {
    position: 'absolute',
    top: -4,
    right: -4,
    backgroundColor: '#EF4444',
    borderRadius: 8,
    width: 16,
    height: 16,
    justifyContent: 'center',
    alignItems: 'center'
  },
  notifBadgeText: { color: '#FFF', fontSize: 9, fontWeight: 'bold' },
  roleCapsule: {
    paddingHorizontal: 10,
    paddingVertical: 4,
    borderRadius: 14,
    borderWidth: 1.5
  },
  roleText: { fontSize: 11, fontWeight: 'bold', color: '#2563EB' },
  tabBar: { flexDirection: 'row', borderBottomWidth: 1 },
  tabItem: { flex: 1, paddingVertical: 10, alignItems: 'center' },
  activeTabItem: { borderBottomWidth: 2, borderBottomColor: '#2563EB' },
  tabText: { fontSize: 12, fontWeight: '600', color: '#64748B' },
  activeTabText: { color: '#2563EB' },
  scrollContent: { padding: 16 },
  bannerCard: { padding: 16, borderRadius: 12, marginBottom: 16 },
  bannerRole: { fontSize: 10, fontWeight: 'bold', color: '#1E40AF', letterSpacing: 0.5 },
  bannerTitle: { fontSize: 18, fontWeight: 'bold', color: '#1E3A8A', marginVertical: 4 },
  bannerSubtext: { fontSize: 12, color: '#1E40AF' },
  kpiGrid: { flexDirection: 'row', justifyContent: 'space-between', marginBottom: 20 },
  kpiCard: { flex: 1, padding: 12, borderRadius: 10, borderWidth: 1, marginHorizontal: 4 },
  kpiValue: { fontSize: 20, fontWeight: 'bold' },
  kpiLabel: { fontSize: 11, marginTop: 4 },
  tasksSection: { marginTop: 10 },
  sectionTitle: { fontSize: 15, fontWeight: 'bold', marginBottom: 12 },
  filterContainer: { marginBottom: 12 },
  searchInput: { paddingHorizontal: 12, paddingVertical: 8, borderRadius: 8, borderWidth: 1, marginBottom: 8, fontSize: 13 },
  pillRow: { flexDirection: 'row', marginBottom: 6 },
  filterPill: { paddingHorizontal: 10, paddingVertical: 5, borderRadius: 14, backgroundColor: '#E2E8F0', marginRight: 6 },
  activeFilterPill: { backgroundColor: '#2563EB' },
  filterPillText: { fontSize: 11, color: '#475569' },
  activeFilterPillText: { color: '#FFFFFF', fontWeight: 'bold' },
  taskCard: { padding: 14, borderRadius: 12, borderWidth: 1, marginBottom: 12 },
  taskCardHeader: { flexDirection: 'row', justifyContent: 'space-between', marginBottom: 6 },
  taskProjectName: { fontSize: 11, fontWeight: 'bold', color: '#2563EB' },
  deadlinePill: { paddingHorizontal: 6, paddingVertical: 2, borderRadius: 4 },
  deadlinePillText: { fontSize: 9, fontWeight: 'bold' },
  taskTitle: { fontSize: 14, fontWeight: 'bold', marginBottom: 4 },
  taskDesc: { fontSize: 12, marginBottom: 10 },
  taskCardFooter: { flexDirection: 'row', justifyContent: 'space-between', alignItems: 'center' },
  taskAssignee: { fontSize: 11 },
  statusBadge: { paddingHorizontal: 8, paddingVertical: 3, borderRadius: 6 },
  statusBadgeText: { fontSize: 10, fontWeight: 'bold', color: '#1E3A8A' },
  auditIndicator: { marginTop: 8, paddingTop: 6, borderTopWidth: 1, borderTopColor: '#E2E8F0', flexDirection: 'row', justifyContent: 'space-between' },
  auditIndicatorText: { fontSize: 10, color: '#64748B' },
  auditActionPrompt: { fontSize: 10, color: '#2563EB', fontWeight: 'bold' },
  reviewCard: { padding: 14, borderRadius: 12, borderWidth: 1, marginBottom: 12 },
  proofThumbnail: { width: '100%', height: 120, borderRadius: 8, marginVertical: 8 },
  reviewActionRow: { flexDirection: 'row', justifyContent: 'space-between', marginTop: 10 },
  reviewBtn: { flex: 1, paddingVertical: 8, borderRadius: 6, alignItems: 'center', marginHorizontal: 4 },
  reviewBtnText: { color: '#FFF', fontSize: 11, fontWeight: 'bold' },
  emptyState: { padding: 30, alignItems: 'center' },
  emptyStateText: { fontSize: 13 },
  modalBackdrop: { flex: 1, backgroundColor: 'rgba(0,0,0,0.5)', justifyContent: 'center', padding: 20 },
  modalContent: { borderRadius: 16, padding: 20, maxHeight: '85%' },
  modalHeader: { flexDirection: 'row', justifyContent: 'space-between', alignItems: 'center', marginBottom: 10 },
  modalTitle: { fontSize: 16, fontWeight: 'bold' },
  closeBtn: { fontSize: 18, color: '#94A3B8' },
  modalDesc: { fontSize: 12, marginBottom: 8 },
  metaLabel: { fontSize: 11, color: '#64748B', marginBottom: 4 },
  auditSection: { marginTop: 14, paddingTop: 12, borderTopWidth: 1, borderTopColor: '#E2E8F0' },
  auditSectionTitle: { fontSize: 11, fontWeight: 'bold', color: '#2563EB', marginBottom: 10 },
  auditItem: { flexDirection: 'row', marginBottom: 10 },
  auditDot: { width: 8, height: 8, borderRadius: 4, backgroundColor: '#2563EB', marginTop: 4, marginRight: 8 },
  auditItemContent: { flex: 1 },
  auditActor: { fontSize: 10, color: '#64748B' },
  auditSummary: { fontSize: 11, fontWeight: '500' },
  auditDelta: { fontSize: 10, color: '#10B981', fontWeight: 'bold', marginTop: 2 },
  submitProofBtn: { backgroundColor: '#2563EB', paddingVertical: 10, borderRadius: 8, alignItems: 'center', marginTop: 14 },
  submitProofBtnText: { color: '#FFF', fontWeight: 'bold', fontSize: 12 },
  textArea: { padding: 10, borderRadius: 8, fontSize: 12, marginVertical: 10, textAlignVertical: 'top' },
  photoPickerRow: { flexDirection: 'row', justifyContent: 'space-between', marginVertical: 6 },
  mediaButton: { flex: 1, backgroundColor: '#E2E8F0', paddingVertical: 8, borderRadius: 6, alignItems: 'center', marginHorizontal: 4 },
  mediaButtonText: { fontSize: 11, fontWeight: '600' },
  previewImage: { width: '100%', height: 120, borderRadius: 8, marginVertical: 8 },
  modalFooterActions: { flexDirection: 'row', justifyContent: 'flex-end', marginTop: 12 },
  cancelBtn: { paddingVertical: 8, paddingHorizontal: 14 },
  cancelBtnText: { color: '#64748B', fontSize: 12 },
  confirmBtn: { backgroundColor: '#2563EB', paddingVertical: 8, paddingHorizontal: 14, borderRadius: 6 },
  confirmBtnText: { color: '#FFF', fontSize: 12, fontWeight: 'bold' },
  userOption: { padding: 10, borderRadius: 8, backgroundColor: '#F1F5F9', marginBottom: 8 },
  activeUserOption: { borderColor: '#2563EB', borderWidth: 1 },
  userName: { fontSize: 13, fontWeight: 'bold' },
  userTitle: { fontSize: 11, color: '#64748B' },
  notifItem: { padding: 10, borderRadius: 8, backgroundColor: '#F1F5F9', marginBottom: 8 },
  notifTitle: { fontSize: 12, fontWeight: 'bold' },
  notifTime: { fontSize: 10, color: '#64748B' },
  notifMsg: { fontSize: 11, marginTop: 2 }
});
