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
import com.example.data.local.FeePaymentEntity
import com.example.data.local.StudentEntity
import com.example.ui.components.AutoReceiptDialog
import com.example.ui.theme.*
import com.example.ui.viewmodel.WaqarViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FeeManagementScreen(
    viewModel: WaqarViewModel,
    onOpenRecordFeeDialog: () -> Unit
) {
    val feePayments by viewModel.allFeePayments.collectAsState()
    val students by viewModel.allStudentsRaw.collectAsState()
    val config by viewModel.instituteConfig.collectAsState()

    val studentMap = remember(students) { students.associateBy { it.id } }

    val totalCollected by viewModel.totalFeesCollected.collectAsState()
    val pendingFees by viewModel.pendingFeesAmount.collectAsState()

    var selectedPaymentForReceipt by remember { mutableStateOf<FeePaymentEntity?>(null) }
    var paymentSearchQuery by remember { mutableStateOf("") }

    val filteredPayments = remember(feePayments, paymentSearchQuery, studentMap) {
        if (paymentSearchQuery.isBlank()) feePayments
        else {
            feePayments.filter { p ->
                val student = studentMap[p.studentId]
                val nameMatch = student?.name?.contains(paymentSearchQuery, ignoreCase = true) == true
                val receiptMatch = p.receiptNo.contains(paymentSearchQuery, ignoreCase = true)
                nameMatch || receiptMatch
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(12.dp))

            // Stats summary card
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = RoyalBluePrimary),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Fee Collection Summary",
                            style = MaterialTheme.typography.labelMedium,
                            color = Color.White.copy(alpha = 0.8f)
                        )
                        Text(
                            text = "₹${totalCollected.toInt()}",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = "Pending: ₹${pendingFees.toInt()}",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.9f)
                        )
                    }

                    Button(
                        onClick = onOpenRecordFeeDialog,
                        colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = RoyalBluePrimary)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Add Fee", fontWeight = FontWeight.Bold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Search text field
            OutlinedTextField(
                value = paymentSearchQuery,
                onValueChange = { paymentSearchQuery = it },
                placeholder = { Text("Search by Student Name or Receipt No...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                singleLine = true,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Payment History (${filteredPayments.size})",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            if (filteredPayments.isEmpty()) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                ) {
                    Text(
                        text = "No fee payment history found.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(bottom = 100.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    items(filteredPayments, key = { it.id }) { payment ->
                        val student = studentMap[payment.studentId]
                        PaymentHistoryCard(
                            payment = payment,
                            studentName = student?.name ?: "Student #${payment.studentId}",
                            studentClass = student?.className ?: "",
                            onViewReceipt = { selectedPaymentForReceipt = payment }
                        )
                    }
                }
            }
        }

        FloatingActionButton(
            onClick = onOpenRecordFeeDialog,
            containerColor = RoyalBluePrimary,
            contentColor = Color.White,
            shape = CircleShape,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(bottom = 90.dp, end = 20.dp)
        ) {
            Icon(Icons.Default.ReceiptLong, contentDescription = "Record Fee")
        }
    }

    // Auto Receipt Dialog Popup
    selectedPaymentForReceipt?.let { payment ->
        val student = studentMap[payment.studentId] ?: StudentEntity(
            studentCode = "WCI-000",
            name = "Unknown Student",
            admissionDate = "",
            className = "",
            courseName = "",
            parentName = "",
            address = "",
            mobileNumber = ""
        )
        AutoReceiptDialog(
            student = student,
            payment = payment,
            config = config,
            onDismiss = { selectedPaymentForReceipt = null }
        )
    }
}

@Composable
fun PaymentHistoryCard(
    payment: FeePaymentEntity,
    studentName: String,
    studentClass: String,
    onViewReceipt: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onViewReceipt() },
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(14.dp)
        ) {
            Surface(
                shape = CircleShape,
                color = SuccessGreen.copy(alpha = 0.12f),
                modifier = Modifier.size(46.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.Receipt,
                        contentDescription = null,
                        tint = SuccessGreen,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = studentName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    Text(
                        text = "₹${payment.amountPaid.toInt()}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = SuccessGreen
                    )
                }

                Spacer(modifier = Modifier.height(2.dp))

                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Receipt: ${payment.receiptNo} • ${payment.month}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = BlueContainer
                    ) {
                        Text(
                            text = payment.paymentMode,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = RoyalBluePrimary,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "Paid on ${payment.paymentDate} ${if (payment.dueAmount > 0) "• Due: ₹${payment.dueAmount.toInt()}" else ""}",
                    style = MaterialTheme.typography.labelSmall,
                    color = if (payment.dueAmount > 0) ErrorRed else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            IconButton(onClick = onViewReceipt) {
                Icon(Icons.Default.Visibility, contentDescription = "View Receipt", tint = RoyalBluePrimary)
            }
        }
    }
}

@Composable
fun RecordFeePaymentDialog(
    students: List<StudentEntity>,
    onDismiss: () -> Unit,
    onSave: (
        studentId: Long,
        amountPaid: Double,
        dueAmount: Double,
        paymentMode: String,
        month: String,
        notes: String
    ) -> Unit
) {
    if (students.isEmpty()) {
        AlertDialog(
            onDismissRequest = onDismiss,
            title = { Text("No Students Available") },
            text = { Text("Please add students first before recording fee payments.") },
            confirmButton = { Button(onClick = onDismiss) { Text("OK") } }
        )
        return
    }

    var selectedStudentId by remember { mutableStateOf(students.first().id) }
    val selectedStudent = students.find { it.id == selectedStudentId } ?: students.first()

    var amountPaidText by remember { mutableStateOf(selectedStudent.monthlyFee.toInt().toString()) }
    var dueAmountText by remember { mutableStateOf("0") }
    var paymentMode by remember { mutableStateOf("Cash") }
    var monthText by remember { mutableStateOf("July 2026") }
    var notes by remember { mutableStateOf("") }

    val modeOptions = listOf("Cash", "UPI", "Bank Transfer")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Record Fee Payment", fontWeight = FontWeight.Bold) },
        text = {
            Column(
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text("Select Student", style = MaterialTheme.typography.labelMedium)

                // Simple student choice list
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(students) { st ->
                        FilterChip(
                            selected = selectedStudentId == st.id,
                            onClick = {
                                selectedStudentId = st.id
                                amountPaidText = st.monthlyFee.toInt().toString()
                            },
                            label = { Text("${st.name} (${st.className})") }
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = BlueContainer.copy(alpha = 0.5f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Text("Expected Fee: ₹${selectedStudent.monthlyFee.toInt()}/month", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                        Text("Parent: ${selectedStudent.parentName} (${selectedStudent.mobileNumber})", style = MaterialTheme.typography.bodySmall)
                    }
                }

                OutlinedTextField(
                    value = amountPaidText,
                    onValueChange = { amountPaidText = it },
                    label = { Text("Amount Paid (₹)*") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = dueAmountText,
                    onValueChange = { dueAmountText = it },
                    label = { Text("Remaining Due Amount (₹)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Text("Payment Mode*", style = MaterialTheme.typography.labelMedium)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    modeOptions.forEach { mode ->
                        FilterChip(
                            selected = paymentMode == mode,
                            onClick = { paymentMode = mode },
                            label = { Text(mode) }
                        )
                    }
                }

                OutlinedTextField(
                    value = monthText,
                    onValueChange = { monthText = it },
                    label = { Text("Fee Month / Session") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Notes / Transaction ID") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val paid = amountPaidText.toDoubleOrNull() ?: 0.0
                    val due = dueAmountText.toDoubleOrNull() ?: 0.0
                    onSave(selectedStudentId, paid, due, paymentMode, monthText, notes)
                    onDismiss()
                }
            ) {
                Text("Generate Receipt")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
