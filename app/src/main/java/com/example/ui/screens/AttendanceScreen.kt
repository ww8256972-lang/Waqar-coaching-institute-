package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.StudentEntity
import com.example.ui.components.StatusBadge
import com.example.ui.theme.*
import com.example.ui.viewmodel.WaqarViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AttendanceScreen(viewModel: WaqarViewModel) {
    val context = LocalContext.current
    val students by viewModel.allStudentsRaw.collectAsState()
    val attendanceList by viewModel.attendanceForSelectedDate.collectAsState()
    val selectedDate by viewModel.selectedAttendanceDate.collectAsState()
    val selectedClass by viewModel.selectedAttendanceClass.collectAsState()

    var activeTab by remember { mutableStateOf(0) } // 0 = Daily Register, 1 = Monthly Summary Report

    val attendanceMap = remember(attendanceList) { attendanceList.associateBy { it.studentId } }

    val filteredStudents = remember(students, selectedClass) {
        if (selectedClass == "All") students.filter { it.isActive }
        else students.filter { it.isActive && it.className == selectedClass }
    }

    val presentCount = attendanceList.count { it.status == "Present" }
    val absentCount = attendanceList.count { it.status == "Absent" }
    val totalActive = filteredStudents.size

    val classOptions = listOf("All", "Class 10", "Class 11", "Class 12", "NEET Batch", "JEE Batch")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        Spacer(modifier = Modifier.height(12.dp))

        // Tab Selector
        TabRow(selectedTabIndex = activeTab, modifier = Modifier.clip(RoundedCornerShape(12.dp))) {
            Tab(
                selected = activeTab == 0,
                onClick = { activeTab = 0 },
                text = { Text("Daily Register", fontWeight = FontWeight.Bold) },
                icon = { Icon(Icons.Default.FactCheck, contentDescription = null, modifier = Modifier.size(18.dp)) }
            )
            Tab(
                selected = activeTab == 1,
                onClick = { activeTab = 1 },
                text = { Text("Monthly Report", fontWeight = FontWeight.Bold) },
                icon = { Icon(Icons.Default.Assessment, contentDescription = null, modifier = Modifier.size(18.dp)) }
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (activeTab == 0) {
            // Date & Summary Row
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column {
                            Text(text = "Attendance Date", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(text = selectedDate, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = RoyalBluePrimary)
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = SuccessGreen.copy(alpha = 0.15f)
                            ) {
                                Text(
                                    text = "$presentCount Present",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = SuccessGreen,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }

                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = ErrorRed.copy(alpha = 0.15f)
                            ) {
                                Text(
                                    text = "$absentCount Absent",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = ErrorRed,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Bulk Action Buttons
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Button(
                            onClick = { viewModel.markAllAttendance("Present") },
                            colors = ButtonDefaults.buttonColors(containerColor = SuccessGreen),
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.Default.DoneAll, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("All Present", fontSize = 12.sp)
                        }

                        OutlinedButton(
                            onClick = { viewModel.markAllAttendance("Absent") },
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = ErrorRed),
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.Default.Close, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("All Absent", fontSize = 12.sp)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Class Filter Chips
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(classOptions) { classItem ->
                    FilterChip(
                        selected = selectedClass == classItem,
                        onClick = { viewModel.selectedAttendanceClass.value = classItem },
                        label = { Text(classItem) },
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Attendance Student List
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(bottom = 90.dp),
                modifier = Modifier.weight(1f)
            ) {
                items(filteredStudents, key = { it.id }) { student ->
                    val record = attendanceMap[student.id]
                    val currentStatus = record?.status ?: "Unmarked"

                    AttendanceStudentRow(
                        student = student,
                        currentStatus = currentStatus,
                        onStatusChange = { newStatus ->
                            viewModel.markStudentAttendance(student.id, newStatus)
                        }
                    )
                }
            }
        } else {
            // Monthly Attendance Report Summary Table
            MonthlyAttendanceReportView(students = students)
        }
    }
}

@Composable
fun AttendanceStudentRow(
    student: StudentEntity,
    currentStatus: String,
    onStatusChange: (String) -> Unit
) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(12.dp)
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = student.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "${student.studentCode} • ${student.className}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                // Present Button
                SelectableChip(
                    label = "Present",
                    isSelected = currentStatus == "Present",
                    selectedColor = SuccessGreen,
                    onClick = { onStatusChange("Present") }
                )

                // Absent Button
                SelectableChip(
                    label = "Absent",
                    isSelected = currentStatus == "Absent",
                    selectedColor = ErrorRed,
                    onClick = { onStatusChange("Absent") }
                )

                // Late Button
                SelectableChip(
                    label = "Late",
                    isSelected = currentStatus == "Late",
                    selectedColor = WarningOrange,
                    onClick = { onStatusChange("Late") }
                )
            }
        }
    }
}

@Composable
private fun SelectableChip(
    label: String,
    isSelected: Boolean,
    selectedColor: Color,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = if (isSelected) selectedColor else MaterialTheme.colorScheme.surfaceVariant,
        border = BorderStroke(1.dp, if (isSelected) selectedColor else Color.Transparent),
        modifier = Modifier.clickable { onClick() }
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
        )
    }
}

@Composable
fun MonthlyAttendanceReportView(students: List<StudentEntity>) {
    val context = LocalContext.current
    var searchReportQuery by remember { mutableStateOf("") }

    val filtered = remember(students, searchReportQuery) {
        if (searchReportQuery.isBlank()) students
        else students.filter { it.name.contains(searchReportQuery, ignoreCase = true) || it.studentCode.contains(searchReportQuery, ignoreCase = true) }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        OutlinedTextField(
            value = searchReportQuery,
            onValueChange = { searchReportQuery = it },
            placeholder = { Text("Search student attendance report...") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(12.dp))

        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Monthly Attendance Summary (July 2026)", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)

                    IconButton(onClick = {
                        Toast.makeText(context, "Exporting Attendance CSV to Downloads...", Toast.LENGTH_SHORT).show()
                    }) {
                        Icon(Icons.Default.Download, contentDescription = "Export CSV", tint = RoyalBluePrimary)
                    }
                }

                Divider(modifier = Modifier.padding(vertical = 8.dp))

                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(bottom = 90.dp)
                ) {
                    items(filtered) { st ->
                        // Simulated high sample attendance percentage for demonstration
                        val simulatedPct = when (st.id % 4) {
                            0L -> 95
                            1L -> 88
                            2L -> 72
                            else -> 91
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 6.dp)
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(st.name, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                                Text("${st.studentCode} • ${st.className}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                LinearProgressIndicator(
                                    progress = { simulatedPct / 100f },
                                    modifier = Modifier
                                        .width(70.dp)
                                        .height(8.dp)
                                        .clip(RoundedCornerShape(4.dp)),
                                    color = if (simulatedPct >= 75) SuccessGreen else ErrorRed
                                )

                                Spacer(modifier = Modifier.width(8.dp))

                                Text(
                                    text = "$simulatedPct%",
                                    fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = if (simulatedPct >= 75) SuccessGreen else ErrorRed
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
