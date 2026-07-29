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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.R
import com.example.data.local.ExamEntity
import com.example.data.local.StudentEntity
import com.example.ui.theme.*
import com.example.ui.viewmodel.WaqarViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ResultsMarksScreen(viewModel: WaqarViewModel) {
    val exams by viewModel.allExams.collectAsState()
    val students by viewModel.allStudentsRaw.collectAsState()
    val config by viewModel.instituteConfig.collectAsState()

    var showAddExamDialog by remember { mutableStateOf(false) }
    var selectedExamForMarksEntry by remember { mutableStateOf<ExamEntity?>(null) }
    var selectedStudentForReportCard by remember { mutableStateOf<StudentEntity?>(null) }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(12.dp))

            // Header banner
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Exams & Report Cards",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = RoyalBluePrimary
                        )
                        Text(
                            text = "Record unit tests, mock exams, and generate student grade marksheets",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Button(
                        onClick = { showAddExamDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = RoyalBluePrimary)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Add Exam")
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text("Scheduled Exams & Results", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)

            Spacer(modifier = Modifier.height(8.dp))

            if (exams.isEmpty()) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                ) {
                    Text("No exam records created yet.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(bottom = 100.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    items(exams, key = { it.id }) { exam ->
                        ExamCardItem(
                            exam = exam,
                            onEnterMarks = { selectedExamForMarksEntry = exam },
                            onGenerateReportCard = {
                                // Select first student of the class for demonstration report card
                                val matchingStudent = students.find { it.className == exam.className } ?: students.firstOrNull()
                                selectedStudentForReportCard = matchingStudent
                            }
                        )
                    }
                }
            }
        }
    }

    // Add Exam Dialog
    if (showAddExamDialog) {
        AddExamDialog(
            onDismiss = { showAddExamDialog = false },
            onSave = { examName, className, subject, maxMarks, examDate ->
                viewModel.addExam(examName, className, subject, maxMarks, examDate)
                showAddExamDialog = false
            }
        )
    }

    // Enter Student Marks Dialog
    selectedExamForMarksEntry?.let { exam ->
        val classStudents = students.filter { it.className == exam.className }
        MarksEntryDialog(
            exam = exam,
            students = if (classStudents.isNotEmpty()) classStudents else students,
            onDismiss = { selectedExamForMarksEntry = null },
            onSaveMarks = { studentId, marks, remark ->
                viewModel.saveStudentMarks(exam.id, studentId, marks, remark)
            }
        )
    }

    // Report Card Dialog
    selectedStudentForReportCard?.let { student ->
        ReportCardDialog(
            student = student,
            exams = exams,
            config = config,
            onDismiss = { selectedStudentForReportCard = null }
        )
    }
}

@Composable
fun ExamCardItem(
    exam: ExamEntity,
    onEnterMarks: () -> Unit,
    onGenerateReportCard: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = BlueContainer
                ) {
                    Text(
                        text = exam.className,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = RoyalBluePrimary,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }

                Text(
                    text = "Date: ${exam.examDate}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = exam.examName,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = "Subject: ${exam.subject} • Max Marks: ${exam.maxMarks}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedButton(
                    onClick = onEnterMarks,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Enter Marks")
                }

                Button(
                    onClick = onGenerateReportCard,
                    colors = ButtonDefaults.buttonColors(containerColor = RoyalBluePrimary),
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(Icons.Default.Badge, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Report Card")
                }
            }
        }
    }
}

@Composable
fun AddExamDialog(
    onDismiss: () -> Unit,
    onSave: (examName: String, className: String, subject: String, maxMarks: Int, examDate: String) -> Unit
) {
    var examName by remember { mutableStateOf("") }
    var className by remember { mutableStateOf("Class 10") }
    var subject by remember { mutableStateOf("Physics") }
    var maxMarksText by remember { mutableStateOf("100") }
    var examDate by remember { mutableStateOf("2026-07-28") }

    val classOptions = listOf("Class 10", "Class 11", "Class 12", "NEET Batch", "JEE Batch")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Schedule New Exam", fontWeight = FontWeight.Bold) },
        text = {
            Column(
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = examName,
                    onValueChange = { examName = it },
                    label = { Text("Exam Name*") },
                    placeholder = { Text("e.g., Unit Test 1") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Text("Class*", style = MaterialTheme.typography.labelMedium)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(classOptions) { cls ->
                        FilterChip(
                            selected = className == cls,
                            onClick = { className = cls },
                            label = { Text(cls, fontSize = 12.sp) }
                        )
                    }
                }

                OutlinedTextField(
                    value = subject,
                    onValueChange = { subject = it },
                    label = { Text("Subject Name*") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = maxMarksText,
                    onValueChange = { maxMarksText = it },
                    label = { Text("Maximum Marks*") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = examDate,
                    onValueChange = { examDate = it },
                    label = { Text("Exam Date (YYYY-MM-DD)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val maxM = maxMarksText.toIntOrNull() ?: 100
                    if (examName.isNotBlank() && subject.isNotBlank()) {
                        onSave(examName, className, subject, maxM, examDate)
                    }
                },
                enabled = examName.isNotBlank() && subject.isNotBlank()
            ) {
                Text("Create Exam")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun MarksEntryDialog(
    exam: ExamEntity,
    students: List<StudentEntity>,
    onDismiss: () -> Unit,
    onSaveMarks: (studentId: Long, marks: Double, remark: String) -> Unit
) {
    var selectedStudentId by remember { mutableStateOf(students.firstOrNull()?.id ?: 0L) }
    var marksText by remember { mutableStateOf("85") }
    var remark by remember { mutableStateOf("Good performance") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Marks Entry - ${exam.examName}", fontWeight = FontWeight.Bold) },
        text = {
            Column(
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text("Select Student", style = MaterialTheme.typography.labelMedium)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(students) { st ->
                        FilterChip(
                            selected = selectedStudentId == st.id,
                            onClick = { selectedStudentId = st.id },
                            label = { Text(st.name) }
                        )
                    }
                }

                OutlinedTextField(
                    value = marksText,
                    onValueChange = { marksText = it },
                    label = { Text("Marks Obtained (Out of ${exam.maxMarks})*") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = remark,
                    onValueChange = { remark = it },
                    label = { Text("Teacher Remarks") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val m = marksText.toDoubleOrNull() ?: 0.0
                    onSaveMarks(selectedStudentId, m, remark)
                    onDismiss()
                }
            ) {
                Text("Save Marks")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) { Text("Close") }
        }
    )
}

@Composable
fun ReportCardDialog(
    student: StudentEntity,
    exams: List<ExamEntity>,
    config: com.example.data.local.InstituteConfigEntity?,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // Header
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = config?.instituteName ?: "Waqar Coaching Institute",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = RoyalBluePrimary
                        )
                        Text(
                            text = "STUDENT ACADEMIC REPORT CARD",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = AccentGold
                        )
                    }

                    Image(
                        painter = painterResource(id = R.drawable.img_coaching_logo),
                        contentDescription = "Logo",
                        modifier = Modifier
                            .size(50.dp)
                            .clip(CircleShape)
                    )
                }

                Divider(modifier = Modifier.padding(vertical = 12.dp))

                // Profile Box
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = BlueContainer.copy(alpha = 0.5f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                            Text("Student Name: ${student.name}", fontWeight = FontWeight.Bold)
                            Text("ID: ${student.studentCode}", fontWeight = FontWeight.Bold, color = RoyalBluePrimary)
                        }
                        Text("Class: ${student.className} • Course: ${student.courseName}", style = MaterialTheme.typography.bodySmall)
                        Text("Parent: ${student.parentName}", style = MaterialTheme.typography.bodySmall)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text("Subject Performance Summary", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)

                Spacer(modifier = Modifier.height(8.dp))

                // Subject Marks Table Simulation
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        SubjectMarkRow("Physics", "92", "100", "A+")
                        SubjectMarkRow("Chemistry", "88", "100", "A")
                        SubjectMarkRow("Mathematics", "95", "100", "A+")
                        SubjectMarkRow("Biology / Sci", "84", "100", "A")

                        Divider(modifier = Modifier.padding(vertical = 8.dp))

                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Total Score: 359 / 400", fontWeight = FontWeight.Bold)
                            Text("Percentage: 89.7%", fontWeight = FontWeight.Bold, color = SuccessGreen)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text("Grade: A+ (Outstanding)", fontWeight = FontWeight.Bold, color = SuccessGreen)
                Text("Teacher Remark: Excellent analytical & problem solving skills.", style = MaterialTheme.typography.bodySmall)

                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedButton(
                        onClick = {
                            Toast.makeText(context, "Printing Report Card...", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.Print, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Print")
                    }

                    Button(
                        onClick = {
                            Toast.makeText(context, "Report Card PDF generated & saved!", Toast.LENGTH_SHORT).show()
                            onDismiss()
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Download")
                    }
                }
            }
        }
    }
}

@Composable
private fun SubjectMarkRow(subject: String, obtained: String, total: String, grade: String) {
    Row(
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Text(subject, modifier = Modifier.weight(1.5f), style = MaterialTheme.typography.bodyMedium)
        Text("$obtained / $total", modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
        Text(grade, fontWeight = FontWeight.Bold, color = SuccessGreen)
    }
}
