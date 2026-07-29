package com.example.ui.screens

import android.content.Intent
import android.net.Uri
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.local.StudentEntity
import com.example.ui.components.StatusBadge
import com.example.ui.theme.*
import com.example.ui.viewmodel.WaqarViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudentManagementScreen(
    viewModel: WaqarViewModel,
    onOpenAddStudentDialog: () -> Unit
) {
    val context = LocalContext.current
    val students by viewModel.filteredStudents.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val selectedClass by viewModel.selectedClassFilter.collectAsState()
    val selectedStatus by viewModel.selectedStatusFilter.collectAsState()

    var selectedStudentForDetail by remember { mutableStateOf<StudentEntity?>(null) }
    var selectedStudentForEdit by remember { mutableStateOf<StudentEntity?>(null) }
    var selectedStudentForDelete by remember { mutableStateOf<StudentEntity?>(null) }

    val classOptions = listOf("All", "Class 10", "Class 11", "Class 12", "NEET Batch", "JEE Batch")

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(12.dp))

            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { viewModel.searchQuery.value = it },
                placeholder = { Text("Search student by Name, Mobile or ID...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { viewModel.searchQuery.value = "" }) {
                            Icon(Icons.Default.Close, contentDescription = null)
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Class Filter Chips
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(classOptions) { classItem ->
                    FilterChip(
                        selected = selectedClass == classItem,
                        onClick = { viewModel.selectedClassFilter.value = classItem },
                        label = { Text(classItem) },
                        leadingIcon = if (selectedClass == classItem) {
                            { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp)) }
                        } else null,
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Count header
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "${students.size} Students Found",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )

                // Active / Inactive Filter toggle
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = if (selectedStatus == "Active") "Active Only" else "All Status",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Switch(
                        checked = selectedStatus == "Active",
                        onCheckedChange = { isChecked ->
                            viewModel.selectedStatusFilter.value = if (isChecked) "Active" else "All"
                        },
                        modifier = Modifier.scale(0.8f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            if (students.isEmpty()) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.PersonOff,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "No student records found",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Try adjusting search query or click '+' to add a new student.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(bottom = 100.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    items(students, key = { it.id }) { student ->
                        StudentCardItem(
                            student = student,
                            onClick = { selectedStudentForDetail = student },
                            onEdit = { selectedStudentForEdit = student },
                            onDelete = { selectedStudentForDelete = student },
                            onCall = {
                                val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${student.mobileNumber}"))
                                context.startActivity(intent)
                            }
                        )
                    }
                }
            }
        }

        // Floating Action Button to Add Student
        FloatingActionButton(
            onClick = onOpenAddStudentDialog,
            containerColor = RoyalBluePrimary,
            contentColor = Color.White,
            shape = CircleShape,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(bottom = 90.dp, end = 20.dp)
        ) {
            Icon(Icons.Default.PersonAdd, contentDescription = "Add Student")
        }
    }

    // Student Detail Dialog
    selectedStudentForDetail?.let { student ->
        StudentDetailDialog(
            student = student,
            onDismiss = { selectedStudentForDetail = null },
            onEdit = {
                selectedStudentForEdit = student
                selectedStudentForDetail = null
            }
        )
    }

    // Edit Student Dialog
    selectedStudentForEdit?.let { student ->
        AddEditStudentDialog(
            studentToEdit = student,
            onDismiss = { selectedStudentForEdit = null },
            onSave = { name, className, courseName, parentName, address, mobile, monthlyFee, isActive ->
                viewModel.addOrUpdateStudent(
                    id = student.id,
                    name = name,
                    className = className,
                    courseName = courseName,
                    parentName = parentName,
                    address = address,
                    mobileNumber = mobile,
                    monthlyFee = monthlyFee,
                    isActive = isActive
                )
                selectedStudentForEdit = null
            }
        )
    }

    // Delete Student Dialog
    selectedStudentForDelete?.let { student ->
        AlertDialog(
            onDismissRequest = { selectedStudentForDelete = null },
            title = { Text("Delete Student Record?") },
            text = { Text("Are you sure you want to permanently remove ${student.name} (${student.studentCode})?") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteStudent(student)
                        selectedStudentForDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ErrorRed)
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { selectedStudentForDelete = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun StudentCardItem(
    student: StudentEntity,
    onClick: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onCall: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(14.dp)
        ) {
            // Student Photo / Avatar
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(CircleShape)
                    .background(RoyalBluePrimary.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.img_coaching_logo),
                    contentDescription = "Avatar",
                    modifier = Modifier.size(36.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = student.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    StatusBadge(
                        statusText = if (student.isActive) "Active" else "Inactive",
                        isPositive = student.isActive
                    )
                }

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = "${student.className} • ${student.courseName}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(4.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = BlueContainer
                    ) {
                        Text(
                            text = student.studentCode,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = RoyalBluePrimary,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Text(
                        text = "Ph: ${student.mobileNumber}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 11.sp
                    )
                }
            }

            Spacer(modifier = Modifier.width(6.dp))

            // Action Buttons
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                IconButton(onClick = onCall, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Default.Phone, contentDescription = "Call", tint = SuccessGreen)
                }
                IconButton(onClick = onEdit, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Default.Edit, contentDescription = "Edit", tint = RoyalBluePrimary)
                }
            }
        }
    }
}

@Composable
fun StudentDetailDialog(
    student: StudentEntity,
    onDismiss: () -> Unit,
    onEdit: () -> Unit
) {
    val context = LocalContext.current
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(text = "Student Profile", fontWeight = FontWeight.Bold)
                StatusBadge(statusText = if (student.isActive) "Active" else "Inactive", isPositive = student.isActive)
            }
        },
        text = {
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(vertical = 8.dp)
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.img_coaching_logo),
                        contentDescription = "Logo",
                        modifier = Modifier
                            .size(60.dp)
                            .clip(CircleShape)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(text = student.name, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                        Text(text = student.studentCode, style = MaterialTheme.typography.bodyMedium, color = RoyalBluePrimary, fontWeight = FontWeight.SemiBold)
                        Text(text = "Admitted: ${student.admissionDate}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }

                Divider(modifier = Modifier.padding(vertical = 10.dp))

                DetailItemRow("Class & Course", "${student.className} - ${student.courseName}")
                DetailItemRow("Parent / Guardian", student.parentName)
                DetailItemRow("Mobile Number", student.mobileNumber)
                DetailItemRow("Address", student.address)
                DetailItemRow("Monthly Fee", "₹${student.monthlyFee.toInt()}")

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${student.mobileNumber}"))
                        context.startActivity(intent)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = SuccessGreen)
                ) {
                    Icon(Icons.Default.Call, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Call Parent (${student.mobileNumber})")
                }
            }
        },
        confirmButton = {
            Button(onClick = onEdit) {
                Text("Edit Profile")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("Close")
            }
        }
    )
}

@Composable
fun DetailItemRow(label: String, value: String) {
    Column(modifier = Modifier.padding(vertical = 4.dp)) {
        Text(text = label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(text = value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditStudentDialog(
    studentToEdit: StudentEntity? = null,
    onDismiss: () -> Unit,
    onSave: (
        name: String,
        className: String,
        courseName: String,
        parentName: String,
        address: String,
        mobileNumber: String,
        monthlyFee: Double,
        isActive: Boolean
    ) -> Unit
) {
    var name by remember { mutableStateOf(studentToEdit?.name ?: "") }
    var className by remember { mutableStateOf(studentToEdit?.className ?: "Class 10") }
    var courseName by remember { mutableStateOf(studentToEdit?.courseName ?: "Science & Mathematics") }
    var parentName by remember { mutableStateOf(studentToEdit?.parentName ?: "") }
    var address by remember { mutableStateOf(studentToEdit?.address ?: "") }
    var mobileNumber by remember { mutableStateOf(studentToEdit?.mobileNumber ?: "") }
    var monthlyFeeText by remember { mutableStateOf(studentToEdit?.monthlyFee?.toInt()?.toString() ?: "3000") }
    var isActive by remember { mutableStateOf(studentToEdit?.isActive ?: true) }

    val classOptions = listOf("Class 10", "Class 11", "Class 12", "NEET Batch", "JEE Batch")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (studentToEdit == null) "Add New Student" else "Edit Student Profile",
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Student Full Name*") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                // Class Dropdown or Choice Buttons
                Text("Select Class*", style = MaterialTheme.typography.labelMedium)
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
                    value = courseName,
                    onValueChange = { courseName = it },
                    label = { Text("Course Name*") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = parentName,
                    onValueChange = { parentName = it },
                    label = { Text("Parent / Guardian Name*") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = mobileNumber,
                    onValueChange = { mobileNumber = it },
                    label = { Text("Mobile Number*") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = address,
                    onValueChange = { address = it },
                    label = { Text("Residential Address") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = monthlyFeeText,
                    onValueChange = { monthlyFeeText = it },
                    label = { Text("Monthly Fee (₹)*") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Active Admission Status", style = MaterialTheme.typography.bodyMedium)
                    Switch(checked = isActive, onCheckedChange = { isActive = it })
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank() && mobileNumber.isNotBlank()) {
                        val fee = monthlyFeeText.toDoubleOrNull() ?: 3000.0
                        onSave(name, className, courseName, parentName, address, mobileNumber, fee, isActive)
                    }
                },
                enabled = name.isNotBlank() && mobileNumber.isNotBlank()
            ) {
                Text("Save Record")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
