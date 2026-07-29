package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import com.example.ui.components.BarChartCanvas
import com.example.ui.theme.*
import com.example.ui.viewmodel.WaqarViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportsScreen(viewModel: WaqarViewModel) {
    val context = LocalContext.current
    val students by viewModel.allStudentsRaw.collectAsState()
    val feePayments by viewModel.allFeePayments.collectAsState()
    val totalCollected by viewModel.totalFeesCollected.collectAsState()
    val pendingFees by viewModel.pendingFeesAmount.collectAsState()

    val cashTotal = feePayments.filter { it.paymentMode == "Cash" }.sumOf { it.amountPaid }
    val upiTotal = feePayments.filter { it.paymentMode == "UPI" }.sumOf { it.amountPaid }
    val bankTotal = feePayments.filter { it.paymentMode == "Bank Transfer" }.sumOf { it.amountPaid }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(bottom = 90.dp, top = 12.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Top Banner with Export buttons
        item {
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = RoyalBluePrimary),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Institute Analytics & Financial Reports",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = "Export consolidated reports for administrative auditing",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.85f)
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Button(
                            onClick = {
                                Toast.makeText(context, "Exporting Consolidated Report to Excel (.xlsx)...", Toast.LENGTH_LONG).show()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = AccentGold, contentColor = Color.Black),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.TableChart, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Export Excel", fontWeight = FontWeight.Bold)
                        }

                        Button(
                            onClick = {
                                Toast.makeText(context, "Generating Institute Summary PDF...", Toast.LENGTH_LONG).show()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = RoyalBluePrimary),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.PictureAsPdf, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Export PDF", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // Student Enrollment Breakdown
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("1. Student Enrollment Report", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(10.dp))

                    ReportMetricRow("Total Active Enrolled", "${students.count { it.isActive }} Students")
                    ReportMetricRow("Class 10 Foundation", "${students.count { it.className == "Class 10" }} Students")
                    ReportMetricRow("Class 11 Science/Commerce", "${students.count { it.className == "Class 11" }} Students")
                    ReportMetricRow("Class 12 Board Prep", "${students.count { it.className == "Class 12" }} Students")
                    ReportMetricRow("NEET / JEE Competitive Batches", "${students.count { it.className.contains("NEET") || it.className.contains("JEE") }} Students")
                }
            }
        }

        // Income & Mode Breakdown
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("2. Income & Payment Mode Breakdown", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(10.dp))

                    ReportMetricRow("Total Fees Collected", "₹${totalCollected.toInt()}", isHighlight = true, color = SuccessGreen)
                    ReportMetricRow("Pending Fee Dues", "₹${pendingFees.toInt()}", isHighlight = true, color = ErrorRed)

                    Divider(modifier = Modifier.padding(vertical = 8.dp))

                    ReportMetricRow("Collected via Cash", "₹${cashTotal.toInt()}")
                    ReportMetricRow("Collected via UPI (GPay/PhonePe)", "₹${upiTotal.toInt()}")
                    ReportMetricRow("Collected via Bank Transfer", "₹${bankTotal.toInt()}")
                }
            }
        }

        // Visual Graph
        item {
            BarChartCanvas(
                dataPoints = listOf(
                    Pair("Cash", cashTotal.toFloat()),
                    Pair("UPI", upiTotal.toFloat()),
                    Pair("Bank", bankTotal.toFloat()),
                    Pair("Due", pendingFees.toFloat())
                ),
                maxVal = (totalCollected + pendingFees).toFloat().coerceAtLeast(10000f),
                barColor = RoyalBluePrimary
            )
        }
    }
}

@Composable
private fun ReportMetricRow(
    label: String,
    value: String,
    isHighlight: Boolean = false,
    color: Color = MaterialTheme.colorScheme.onSurface
) {
    Row(
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = if (isHighlight) FontWeight.Bold else FontWeight.SemiBold,
            color = color
        )
    }
}
