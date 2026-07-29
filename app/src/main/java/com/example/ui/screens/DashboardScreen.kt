package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.*
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.components.BarChartCanvas
import com.example.ui.components.SectionHeader
import com.example.ui.components.StatCard
import com.example.ui.navigation.Screen
import com.example.ui.theme.*
import com.example.ui.viewmodel.WaqarViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    viewModel: WaqarViewModel,
    onNavigateTo: (String) -> Unit,
    onOpenAddStudentDialog: () -> Unit,
    onOpenRecordFeeDialog: () -> Unit
) {
    val totalStudents by viewModel.totalStudentsCount.collectAsState()
    val totalFees by viewModel.totalFeesCollected.collectAsState()
    val pendingFees by viewModel.pendingFeesAmount.collectAsState()
    val attendancePair by viewModel.todayAttendanceCount.collectAsState()
    val feePayments by viewModel.allFeePayments.collectAsState()
    val notifications by viewModel.allNotifications.collectAsState()
    val students by viewModel.allStudentsRaw.collectAsState()

    val attendancePct = if (attendancePair.second > 0) {
        ((attendancePair.first.toFloat() / attendancePair.second) * 100).toInt()
    } else 0

    val chartData = remember(feePayments) {
        listOf(
            Pair("Jan", 42000f),
            Pair("Feb", 48000f),
            Pair("Mar", 55000f),
            Pair("Apr", 51000f),
            Pair("May", 62000f),
            Pair("Jun", 68000f),
            Pair("Jul", totalFees.toFloat().coerceAtLeast(10000f))
        )
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(bottom = 90.dp, top = 12.dp)
    ) {
        // Hero Header Card with Logo and Institute Title
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Box(modifier = Modifier.fillMaxWidth()) {
                    Image(
                        painter = painterResource(id = R.drawable.img_hero_dashboard),
                        contentDescription = "Institute Banner",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(130.dp)
                    )

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(130.dp)
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(
                                        Color.Black.copy(alpha = 0.3f),
                                        RoyalBlueDark.copy(alpha = 0.85f)
                                    )
                                )
                            )
                    )

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .padding(16.dp)
                            .align(Alignment.BottomStart)
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.img_coaching_logo),
                            contentDescription = "Logo",
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Waqar Coaching Institute",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                text = "Dashboard Overview • Academic Session 2026",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.White.copy(alpha = 0.85f),
                                fontSize = 11.sp
                            )
                        }
                    }
                }
            }
        }

        // 4 Key Stats Grid
        item {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    StatCard(
                        title = "Active Students",
                        value = "$totalStudents",
                        subtitle = "${students.size} enrolled in total",
                        icon = Icons.Default.People,
                        containerColor = BlueContainer,
                        iconColor = RoyalBluePrimary,
                        modifier = Modifier.weight(1f),
                        onClick = { onNavigateTo(Screen.Students.route) }
                    )

                    StatCard(
                        title = "Fees Collected",
                        value = "₹${totalFees.toInt()}",
                        subtitle = "Total income recorded",
                        icon = Icons.Default.AccountBalanceWallet,
                        containerColor = SuccessGreen.copy(alpha = 0.12f),
                        iconColor = SuccessGreen,
                        modifier = Modifier.weight(1f),
                        onClick = { onNavigateTo(Screen.FeeManagement.route) }
                    )
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    StatCard(
                        title = "Pending Fees",
                        value = "₹${pendingFees.toInt()}",
                        subtitle = "Due from current students",
                        icon = Icons.Default.Warning,
                        containerColor = ErrorRed.copy(alpha = 0.12f),
                        iconColor = ErrorRed,
                        modifier = Modifier.weight(1f),
                        onClick = { onNavigateTo(Screen.Notifications.route) }
                    )

                    StatCard(
                        title = "Today's Attendance",
                        value = "$attendancePct%",
                        subtitle = "${attendancePair.first}/${attendancePair.second} Present Today",
                        icon = Icons.Default.HowToReg,
                        containerColor = AccentGold.copy(alpha = 0.15f),
                        iconColor = WarningOrange,
                        modifier = Modifier.weight(1f),
                        onClick = { onNavigateTo(Screen.Attendance.route) }
                    )
                }
            }
        }

        // Quick Actions Row
        item {
            Spacer(modifier = Modifier.height(16.dp))
            SectionHeader(title = "Quick Actions")

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
            ) {
                QuickActionChip(
                    label = "Add Student",
                    icon = Icons.Default.PersonAdd,
                    onClick = onOpenAddStudentDialog
                )
                QuickActionChip(
                    label = "Collect Fee",
                    icon = Icons.Default.Receipt,
                    onClick = onOpenRecordFeeDialog
                )
                QuickActionChip(
                    label = "Mark Attendance",
                    icon = Icons.Default.FactCheck,
                    onClick = { onNavigateTo(Screen.Attendance.route) }
                )
                QuickActionChip(
                    label = "Exam & Marks",
                    icon = Icons.Default.Grade,
                    onClick = { onNavigateTo(Screen.Results.route) }
                )
            }
        }

        // Performance Chart
        item {
            Spacer(modifier = Modifier.height(16.dp))
            BarChartCanvas(
                dataPoints = chartData,
                maxVal = 80000f,
                barColor = RoyalBluePrimary
            )
        }

        // Recent Activity Feed
        item {
            Spacer(modifier = Modifier.height(16.dp))
            SectionHeader(
                title = "Recent Activities",
                subtitle = "Latest transactions and alert dispatches",
                actionText = "View All",
                onActionClick = { onNavigateTo(Screen.Notifications.route) }
            )
        }

        val recentNotifs = notifications.take(4)
        if (recentNotifs.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Text(
                        text = "No recent activity recorded yet.",
                        modifier = Modifier.padding(16.dp),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        } else {
            items(recentNotifs) { notif ->
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(12.dp)
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = when (notif.type) {
                                "Fee Received" -> SuccessGreen.copy(alpha = 0.15f)
                                "Fee Due" -> ErrorRed.copy(alpha = 0.15f)
                                else -> BlueContainer
                            },
                            modifier = Modifier.size(38.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = when (notif.type) {
                                        "Fee Received" -> Icons.Default.CheckCircle
                                        "Fee Due" -> Icons.Default.NotificationsActive
                                        else -> Icons.Default.Info
                                    },
                                    contentDescription = null,
                                    tint = when (notif.type) {
                                        "Fee Received" -> SuccessGreen
                                        "Fee Due" -> ErrorRed
                                        else -> RoyalBluePrimary
                                    },
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = notif.title,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = notif.message,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        Text(
                            text = notif.date,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 10.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun QuickActionChip(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, RoyalBlueLight.copy(alpha = 0.3f)),
        modifier = Modifier.clickable { onClick() }
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = RoyalBluePrimary,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}
