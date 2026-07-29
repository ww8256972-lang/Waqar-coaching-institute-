package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "students")
data class StudentEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val studentCode: String,
    val name: String,
    val photoUri: String? = null,
    val admissionDate: String,
    val className: String, // e.g. "Class 10", "Class 12", "NEET Batch"
    val courseName: String, // e.g. "Mathematics & Science", "Physics", "Full Course"
    val parentName: String,
    val address: String,
    val mobileNumber: String,
    val isActive: Boolean = true,
    val monthlyFee: Double = 3000.0
)

@Entity(tableName = "fee_payments")
data class FeePaymentEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val studentId: Long,
    val receiptNo: String,
    val amountPaid: Double,
    val dueAmount: Double,
    val paymentDate: String, // YYYY-MM-DD
    val paymentMode: String, // "Cash", "UPI", "Bank Transfer"
    val month: String, // "July 2026"
    val notes: String = ""
)

@Entity(tableName = "attendance")
data class AttendanceEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val studentId: Long,
    val date: String, // YYYY-MM-DD
    val status: String, // "Present", "Absent", "Late"
    val remark: String = ""
)

@Entity(tableName = "exams")
data class ExamEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val examName: String, // e.g., "Monthly Unit Test - July"
    val className: String,
    val subject: String,
    val maxMarks: Int = 100,
    val examDate: String
)

@Entity(tableName = "exam_results")
data class ExamResultEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val examId: Long,
    val studentId: Long,
    val marksObtained: Double,
    val remarks: String = ""
)

@Entity(tableName = "notifications")
data class NotificationLogEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val message: String,
    val date: String,
    val type: String, // "Fee Due", "Attendance Alert", "General"
    val studentId: Long? = null,
    val isRead: Boolean = false
)

@Entity(tableName = "institute_config")
data class InstituteConfigEntity(
    @PrimaryKey val id: Int = 1,
    val instituteName: String = "Waqar Coaching Institute",
    val tagline: String = "Excellence in Education & Competitive Prep",
    val address: String = "123 Academic Block, Knowledge Park, City",
    val phone: String = "+91 98765 43210",
    val email: String = "info@waqarcoaching.edu",
    val adminPin: String = "1234"
)
