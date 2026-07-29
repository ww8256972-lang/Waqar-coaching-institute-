package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import android.widget.Toast
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.StudentEntity
import com.example.ui.theme.*
import com.example.ui.viewmodel.WaqarViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationsScreen(viewModel: WaqarViewModel) {
    val context = LocalContext.current
    val notifications by viewModel.allNotifications.collectAsState()
    val students by viewModel.allStudentsRaw.collectAsState()

    var activeTab by remember { mutableStateOf(0) } // 0 = Alerts Log, 1 = Fee Due Auto-List

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        Spacer(modifier = Modifier.height(12.dp))

        TabRow(selectedTabIndex = activeTab, modifier = Modifier.clip(RoundedCornerShape(12.dp))) {
            Tab(
                selected = activeTab == 0,
                onClick = { activeTab = 0 },
                text = { Text("Activity Log") },
                icon = { Icon(Icons.Default.Notifications, contentDescription = null, modifier = Modifier.size(18.dp)) }
            )
            Tab(
                selected = activeTab == 1,
                onClick = { activeTab = 1 },
                text = { Text("Fee Due Reminders") },
                icon = { Icon(Icons.Default.Warning, contentDescription = null, modifier = Modifier.size(18.dp)) }
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        if (activeTab == 0) {
            if (notifications.isEmpty()) {
                Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                    Text("No notification alerts dispatched yet.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(bottom = 90.dp)
                ) {
                    items(notifications, key = { it.id }) { notif ->
                        Card(
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(14.dp)
                            ) {
                                Surface(
                                    shape = CircleShape,
                                    color = when (notif.type) {
                                        "Fee Due" -> ErrorRed.copy(alpha = 0.12f)
                                        "Attendance Alert" -> WarningOrange.copy(alpha = 0.12f)
                                        else -> BlueContainer
                                    },
                                    modifier = Modifier.size(42.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = when (notif.type) {
                                                "Fee Due" -> Icons.Default.NotificationsActive
                                                "Attendance Alert" -> Icons.Default.PersonOff
                                                else -> Icons.Default.Info
                                            },
                                            contentDescription = null,
                                            tint = when (notif.type) {
                                                "Fee Due" -> ErrorRed
                                                "Attendance Alert" -> WarningOrange
                                                else -> RoyalBluePrimary
                                            },
                                            modifier = Modifier.size(22.dp)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.width(12.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = notif.title,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = notif.message,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
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
        } else {
            // Fee Due Reminder Generator List
            val feeDueStudents = students.filter { it.isActive }
            if (feeDueStudents.isEmpty()) {
                Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                    Text("All student fees are currently up to date!", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(bottom = 90.dp)
                ) {
                    items(feeDueStudents) { student ->
                        FeeDueReminderCard(
                            student = student,
                            onSendSms = {
                                viewModel.sendFeeReminderAlert(student)
                                val uri = Uri.parse("smsto:${student.mobileNumber}")
                                val intent = Intent(Intent.ACTION_SENDTO, uri).apply {
                                    putExtra("sms_body", "Dear Parent, this is a friendly fee due reminder from Waqar Coaching Institute for ${student.name}. Pending fee: ₹${student.monthlyFee.toInt()}. Thank you.")
                                }
                                context.startActivity(intent)
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun FeeDueReminderCard(
    student: StudentEntity,
    onSendSms: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(14.dp)
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(student.name, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                Text("${student.studentCode} • ${student.className}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text("Parent: ${student.parentName} (${student.mobileNumber})", style = MaterialTheme.typography.bodySmall)

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "Monthly Due: ₹${student.monthlyFee.toInt()}",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = ErrorRed
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            Button(
                onClick = onSendSms,
                colors = ButtonDefaults.buttonColors(containerColor = ErrorRed)
            ) {
                Icon(Icons.Default.Send, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Send Alert", fontSize = 12.sp)
            }
        }
    }
}
