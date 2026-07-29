package com.example.repository

import com.example.data.local.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class WaqarRepository(private val db: WaqarDatabase) {
    val studentDao = db.studentDao()
    val feePaymentDao = db.feePaymentDao()
    val attendanceDao = db.attendanceDao()
    val examDao = db.examDao()
    val examResultDao = db.examResultDao()
    val notificationLogDao = db.notificationLogDao()
    val instituteConfigDao = db.instituteConfigDao()

    val allStudents: Flow<List<StudentEntity>> = studentDao.getAllStudents()
    val allFeePayments: Flow<List<FeePaymentEntity>> = feePaymentDao.getAllFeePayments()
    val allExams: Flow<List<ExamEntity>> = examDao.getAllExams()
    val allNotifications: Flow<List<NotificationLogEntity>> = notificationLogDao.getAllNotifications()
    val instituteConfig: Flow<InstituteConfigEntity?> = instituteConfigDao.getConfig()

    fun searchStudents(query: String): Flow<List<StudentEntity>> = studentDao.searchStudents(query)

    fun getTodayDate(): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return sdf.format(Date())
    }

    suspend fun getStudentById(id: Long) = studentDao.getStudentById(id)

    suspend fun addStudent(student: StudentEntity): Long = studentDao.insertStudent(student)

    suspend fun updateStudent(student: StudentEntity) = studentDao.updateStudent(student)

    suspend fun deleteStudent(student: StudentEntity) = studentDao.deleteStudent(student)

    suspend fun addFeePayment(payment: FeePaymentEntity): Long {
        val id = feePaymentDao.insertFeePayment(payment)
        // Add a notification for payment
        val student = studentDao.getStudentById(payment.studentId)
        val studentName = student?.name ?: "Student #${payment.studentId}"
        notificationLogDao.insertNotification(
            NotificationLogEntity(
                title = "Fee Payment Received",
                message = "Received ₹${payment.amountPaid.toInt()} via ${payment.paymentMode} for $studentName (Receipt #${payment.receiptNo})",
                date = payment.paymentDate,
                type = "Fee Received",
                studentId = payment.studentId
            )
        )
        return id
    }

    fun getPaymentsForStudent(studentId: Long): Flow<List<FeePaymentEntity>> =
        feePaymentDao.getPaymentsForStudent(studentId)

    fun getAttendanceForDate(date: String): Flow<List<AttendanceEntity>> =
        attendanceDao.getAttendanceByDate(date)

    suspend fun saveAttendance(attendance: AttendanceEntity) =
        attendanceDao.insertOrUpdateAttendance(attendance)

    suspend fun saveBulkAttendance(list: List<AttendanceEntity>) =
        attendanceDao.insertBulkAttendance(list)

    fun getAttendanceForStudent(studentId: Long): Flow<List<AttendanceEntity>> =
        attendanceDao.getAttendanceForStudent(studentId)

    suspend fun addExam(exam: ExamEntity): Long = examDao.insertExam(exam)

    suspend fun deleteExam(exam: ExamEntity) = examDao.deleteExam(exam)

    fun getResultsForExam(examId: Long): Flow<List<ExamResultEntity>> =
        examResultDao.getResultsForExam(examId)

    fun getResultsForStudent(studentId: Long): Flow<List<ExamResultEntity>> =
        examResultDao.getResultsForStudent(studentId)

    suspend fun saveExamResult(result: ExamResultEntity) =
        examResultDao.insertOrUpdateResult(result)

    suspend fun markNotificationAsRead(id: Long) =
        notificationLogDao.markAsRead(id)

    suspend fun addNotification(notification: NotificationLogEntity) =
        notificationLogDao.insertNotification(notification)

    suspend fun saveInstituteConfig(config: InstituteConfigEntity) =
        instituteConfigDao.saveConfig(config)

    suspend fun seedInitialDataIfNeeded() {
        val existingConfig = instituteConfigDao.getConfigDirect()
        if (existingConfig == null) {
            instituteConfigDao.saveConfig(InstituteConfigEntity())
        }

        val students = studentDao.getAllStudents().first()
        if (students.isEmpty()) {
            val today = getTodayDate()

            // Seed Students
            val sampleStudents = listOf(
                StudentEntity(
                    studentCode = "WCI-2026-001",
                    name = "Aman Khan",
                    admissionDate = "2026-01-10",
                    className = "Class 12",
                    courseName = "Physics & Mathematics",
                    parentName = "Rashid Khan",
                    address = "Sector 4, Green Park, City",
                    mobileNumber = "9876543210",
                    isActive = true,
                    monthlyFee = 3500.0
                ),
                StudentEntity(
                    studentCode = "WCI-2026-002",
                    name = "Fatima Zohra",
                    admissionDate = "2026-01-15",
                    className = "NEET Batch",
                    courseName = "Biology & Chemistry",
                    parentName = "Tariq Zohra",
                    address = "Flat 202, Al-Noor Heights",
                    mobileNumber = "9812345678",
                    isActive = true,
                    monthlyFee = 4500.0
                ),
                StudentEntity(
                    studentCode = "WCI-2026-003",
                    name = "Rahul Sharma",
                    admissionDate = "2026-02-01",
                    className = "Class 10",
                    courseName = "All Subjects Science Foundation",
                    parentName = "Sanjay Sharma",
                    address = "House 45, Model Town",
                    mobileNumber = "9823456789",
                    isActive = true,
                    monthlyFee = 2800.0
                ),
                StudentEntity(
                    studentCode = "WCI-2026-004",
                    name = "Zaid Waqar",
                    admissionDate = "2026-02-10",
                    className = "JEE Batch",
                    courseName = "PCM Advanced",
                    parentName = "Waqar Ahmed",
                    address = "Civil Lines, Near University Gate",
                    mobileNumber = "9834567890",
                    isActive = true,
                    monthlyFee = 5000.0
                ),
                StudentEntity(
                    studentCode = "WCI-2026-005",
                    name = "Ananya Verma",
                    admissionDate = "2026-03-05",
                    className = "Class 11",
                    courseName = "Accountancy & Commerce",
                    parentName = "Rajesh Verma",
                    address = "Block C, Metro Enclave",
                    mobileNumber = "9845678901",
                    isActive = true,
                    monthlyFee = 3000.0
                ),
                StudentEntity(
                    studentCode = "WCI-2026-006",
                    name = "Mohammed Bilal",
                    admissionDate = "2026-03-12",
                    className = "Class 12",
                    courseName = "Physics & Mathematics",
                    parentName = "Salim Bilal",
                    address = "Line 3, Station Road",
                    mobileNumber = "9856789012",
                    isActive = true,
                    monthlyFee = 3500.0
                )
            )

            val studentIds = mutableListOf<Long>()
            for (st in sampleStudents) {
                val id = studentDao.insertStudent(st)
                studentIds.add(id)
            }

            // Seed Fee Payments
            if (studentIds.size >= 6) {
                feePaymentDao.insertFeePayment(
                    FeePaymentEntity(
                        studentId = studentIds[0],
                        receiptNo = "REC-1001",
                        amountPaid = 3500.0,
                        dueAmount = 0.0,
                        paymentDate = today,
                        paymentMode = "UPI",
                        month = "July 2026",
                        notes = "Full payment for July"
                    )
                )
                feePaymentDao.insertFeePayment(
                    FeePaymentEntity(
                        studentId = studentIds[1],
                        receiptNo = "REC-1002",
                        amountPaid = 3000.0,
                        dueAmount = 1500.0,
                        paymentDate = today,
                        paymentMode = "Cash",
                        month = "July 2026",
                        notes = "Partial payment, balance due next week"
                    )
                )
                feePaymentDao.insertFeePayment(
                    FeePaymentEntity(
                        studentId = studentIds[2],
                        receiptNo = "REC-1003",
                        amountPaid = 2800.0,
                        dueAmount = 0.0,
                        paymentDate = "2026-07-20",
                        paymentMode = "Bank Transfer",
                        month = "July 2026",
                        notes = "Online IMPS transfer"
                    )
                )
                feePaymentDao.insertFeePayment(
                    FeePaymentEntity(
                        studentId = studentIds[3],
                        receiptNo = "REC-1004",
                        amountPaid = 5000.0,
                        dueAmount = 0.0,
                        paymentDate = "2026-07-15",
                        paymentMode = "UPI",
                        month = "July 2026",
                        notes = "Paid via PhonePe"
                    )
                )
            }

            // Seed Attendance for Today
            val attendanceList = listOf(
                AttendanceEntity(studentId = studentIds[0], date = today, status = "Present", remark = "On time"),
                AttendanceEntity(studentId = studentIds[1], date = today, status = "Present", remark = "On time"),
                AttendanceEntity(studentId = studentIds[2], date = today, status = "Absent", remark = "Informed sick leave"),
                AttendanceEntity(studentId = studentIds[3], date = today, status = "Present", remark = "Late by 10 mins"),
                AttendanceEntity(studentId = studentIds[4], date = today, status = "Present", remark = "On time"),
                AttendanceEntity(studentId = studentIds[5], date = today, status = "Absent", remark = "Uninformed")
            )
            attendanceDao.insertBulkAttendance(attendanceList)

            // Seed Exams & Results
            val examId1 = examDao.insertExam(
                ExamEntity(
                    examName = "July Monthly Assessment",
                    className = "Class 12",
                    subject = "Physics",
                    maxMarks = 100,
                    examDate = "2026-07-25"
                )
            )
            val examId2 = examDao.insertExam(
                ExamEntity(
                    examName = "NEET Mock Test 1",
                    className = "NEET Batch",
                    subject = "Biology",
                    maxMarks = 100,
                    examDate = "2026-07-22"
                )
            )

            examResultDao.insertOrUpdateResult(ExamResultEntity(examId = examId1, studentId = studentIds[0], marksObtained = 92.0, remarks = "Excellent conceptual clarity"))
            examResultDao.insertOrUpdateResult(ExamResultEntity(examId = examId1, studentId = studentIds[5], marksObtained = 78.5, remarks = "Good effort"))
            examResultDao.insertOrUpdateResult(ExamResultEntity(examId = examId2, studentId = studentIds[1], marksObtained = 95.0, remarks = "Top Scorer in Biology"))

            // Seed Notifications
            notificationLogDao.insertNotification(
                NotificationLogEntity(
                    title = "System Setup Complete",
                    message = "Waqar Coaching Institute database initialized with student profiles and attendance records.",
                    date = today,
                    type = "General",
                    isRead = true
                )
            )
            notificationLogDao.insertNotification(
                NotificationLogEntity(
                    title = "Fee Reminder Generated",
                    message = "Fee due reminder pending for Ananya Verma (Class 11) - ₹3,000.",
                    date = today,
                    type = "Fee Due",
                    studentId = studentIds[4],
                    isRead = false
                )
            )
            notificationLogDao.insertNotification(
                NotificationLogEntity(
                    title = "Attendance Alert",
                    message = "Mohammed Bilal was absent today without prior notice.",
                    date = today,
                    type = "Attendance Alert",
                    studentId = studentIds[5],
                    isRead = false
                )
            )
        }
    }
}
