package com.example.ui.components

import androidx.compose.foundation.background
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
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.User
import com.example.data.model.UserRole
import com.example.ui.theme.*
import com.example.ui.viewmodel.CmsScreen

data class NavItem(
    val screen: CmsScreen,
    val title: String,
    val icon: ImageVector,
    val badgeCount: Int? = null,
    val allowedRoles: List<UserRole> = listOf(UserRole.MANAGER, UserRole.TEAM_LEAD, UserRole.TEAM_MEMBER)
)

@Composable
fun NavDrawerContent(
    currentScreen: CmsScreen,
    currentUser: User,
    reviewQueueCount: Int,
    overdueCount: Int,
    onSelectScreen: (CmsScreen) -> Unit,
    onOpenRoleSwitcher: () -> Unit,
    onResetDemoData: () -> Unit,
    onCloseDrawer: () -> Unit
) {
    val navItems = listOf(
        NavItem(CmsScreen.DASHBOARD, "Dashboard", Icons.Default.Dashboard),
        NavItem(CmsScreen.CLIENTS, "Clients Directory", Icons.Default.Business),
        NavItem(CmsScreen.PROJECTS, "Projects & Delivery", Icons.Default.AccountTree),
        NavItem(
            CmsScreen.TASKS,
            "Tasks & Kanban",
            Icons.Default.Assignment,
            badgeCount = if (overdueCount > 0) overdueCount else null
        ),
        NavItem(
            CmsScreen.REVIEW_QUEUE,
            "Proof Review Queue",
            Icons.Default.Verified,
            badgeCount = if (reviewQueueCount > 0) reviewQueueCount else null
        ),
        NavItem(CmsScreen.TEAMS, "Teams & Staff", Icons.Default.Groups),
        NavItem(CmsScreen.ANALYTICS, "Analytics & Reports", Icons.Default.BarChart),
        NavItem(CmsScreen.AUDIT_LOG, "Audit History", Icons.Default.History)
    )

    Surface(
        color = MaterialTheme.colorScheme.surface,
        modifier = Modifier
            .fillMaxHeight()
            .width(310.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(vertical = 16.dp)
        ) {
            // Header: User identity card
            val (roleColor, roleBg) = when (currentUser.role) {
                UserRole.MANAGER -> Pair(RoleManagerColor, RoleManagerColor.copy(alpha = 0.12f))
                UserRole.TEAM_LEAD -> Pair(RoleTeamLeadColor, RoleTeamLeadColor.copy(alpha = 0.12f))
                UserRole.TEAM_MEMBER -> Pair(RoleMemberColor, RoleMemberColor.copy(alpha = 0.12f))
            }

            Surface(
                shape = RoundedCornerShape(14.dp),
                color = roleBg,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .clickable { onOpenRoleSwitcher() }
                    .testTag("nav_user_profile_card")
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .clip(CircleShape)
                            .background(roleColor),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = currentUser.avatarInitials,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = currentUser.name,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp
                            ),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = currentUser.title,
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(roleColor)
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = currentUser.role.displayName.uppercase(),
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }

                    Icon(
                        imageVector = Icons.Default.SwapHoriz,
                        contentDescription = "Switch User",
                        tint = roleColor,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))
            HorizontalDivider(
                modifier = Modifier.padding(horizontal = 16.dp),
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
            )
            Spacer(modifier = Modifier.height(10.dp))

            // Navigation Links
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 12.dp)
            ) {
                Text(
                    text = "NAVIGATION",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                )

                navItems.forEach { item ->
                    val isSelected = currentScreen == item.screen
                    val activeBg = if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.12f) else Color.Transparent
                    val activeColor = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface

                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = activeBg,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 2.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .clickable {
                                onSelectScreen(item.screen)
                                onCloseDrawer()
                            }
                            .testTag("nav_item_${item.screen.name}")
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 11.dp)
                        ) {
                            Icon(
                                imageVector = item.icon,
                                contentDescription = item.title,
                                tint = activeColor,
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(modifier = Modifier.width(14.dp))
                            Text(
                                text = item.title,
                                fontSize = 14.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = activeColor,
                                modifier = Modifier.weight(1f)
                            )

                            if (item.badgeCount != null && item.badgeCount > 0) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(
                                            if (item.screen == CmsScreen.TASKS) StatusOverdue else MaterialTheme.colorScheme.primary
                                        )
                                        .padding(horizontal = 7.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = item.badgeCount.toString(),
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Bottom System Actions
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp)
            ) {
                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                Spacer(modifier = Modifier.height(10.dp))

                OutlinedButton(
                    onClick = {
                        onResetDemoData()
                        onCloseDrawer()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("reset_demo_data_button"),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Reset Demo Data",
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Reset Demo Dataset", fontSize = 12.sp)
                }

                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Client Management System v2.4 · Build Mode",
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
            }
        }
    }
}
