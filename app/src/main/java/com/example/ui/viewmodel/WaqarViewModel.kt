package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.*
import com.example.repository.WaqarRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class WaqarViewModel(application: Application) : AndroidViewModel(application) {
    private val db = WaqarDatabase.getDatabase(application)
    val repository = WaqarRepository(db)

    // Filtering states
    val searchQuery = MutableStateFlow("")
    val selectedClassFilter = MutableStateFlow("All")
    val selectedStatusFilter = MutableStateFlow("All")

    // Attendance Date & Class Filter
    val selectedAttendanceDate = MutableStateFlow(repository.getTodayDate())
    val selectedAttendanceClass = MutableStateFlow("All")

    // Admin Auth State
    val isAdminAuthenticated = MutableStateFlow(false)
    val adminAuthError = MutableStateFlow<String?>(null)

    // UI Toast or SnackBar Feedback Message
    val toastMessage = MutableStateFlow<String?>(null)

    val instituteConfig: StateFlow<InstituteConfigEntity?> = repository.instituteConfig
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val allStudentsRaw: StateFlow<List<StudentEntity>> = repository.allStudents
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val filteredStudents: StateFlow<List<StudentEntity>> = combine(
        allStudentsRaw,
        searchQuery,
        selectedClassFilter,
        selectedStatusFilter
    ) { students, query, classF, statusF ->
        students.filter { student ->
            val matchesQuery = query.isBlank() ||
                    student.name.contains(query, ignoreCase = true) ||
                    student.mobileNumber.contains(query, ignoreCase = true) ||
                    student.studentCode.contains(query, ignoreCase = true)

            val matchesClass = classF == "All" || student.className == classF
            val matchesStatus = when (statusF) {
                "Active" -> student.isActive
                "Inactive" -> !student.isActive
                else -> true
            }

            matchesQuery && matchesClass && matchesStatus
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allFeePayments: StateFlow<List<FeePaymentEntity>> = repository.allFeePayments
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val attendanceForSelectedDate: StateFlow<List<AttendanceEntity>> = selectedAttendanceDate
        .flatMapLatest { date -> repository.getAttendanceForDate(date) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allExams: StateFlow<List<ExamEntity>> = repository.allExams
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allNotifications: StateFlow<List<NotificationLogEntity>> = repository.allNotifications
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Calculations for Dashboard Stats
    val totalStudentsCount: StateFlow<Int> = allStudentsRaw
        .map { list -> list.count { it.isActive } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val totalFeesCollected: StateFlow<Double> = allFeePayments
        .map { list -> list.sumOf { it.amountPaid } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val pendingFeesAmount: StateFlow<Double> = combine(allStudentsRaw, allFeePayments) { students, payments ->
        // Total monthly expected fees minus paid
        val activeStudents = students.filter { it.isActive }
        val totalExpected = activeStudents.sumOf { it.monthlyFee }
        val totalPaid = payments.sumOf { it.amountPaid }
        val pending = totalExpected - totalPaid
        if (pending < 0) 0.0 else pending
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val todayAttendanceCount: StateFlow<Pair<Int, Int>> = combine(
        allStudentsRaw,
        attendanceForSelectedDate
    ) { students, attendance ->
        val activeCount = students.count { it.isActive }
        val presentCount = attendance.count { it.status == "Present" }
        Pair(presentCount, activeCount)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), Pair(0, 0))

    init {
        viewModelScope.launch {
            repository.seedInitialDataIfNeeded()
        }
    }

    fun showToast(msg: String) {
        toastMessage.value = msg
    }

    fun clearToast() {
        toastMessage.value = null
    }

    // Student Actions
    fun addOrUpdateStudent(
        id: Long = 0,
        name: String,
        className: String,
        courseName: String,
        parentName: String,
        address: String,
        mobileNumber: String,
        monthlyFee: Double,
        isActive: Boolean = true
    ) {
        viewModelScope.launch {
            val code = if (id == 0L) {
                val nextNum = (allStudentsRaw.value.size + 1)
                String.format(Locale.getDefault(), "WCI-2026-%03d", nextNum)
            } else ""

            val admission = repository.getTodayDate()
            val student = StudentEntity(
                id = id,
                studentCode = if (id == 0L) code else allStudentsRaw.value.find { it.id == id }?.studentCode ?: "WCI-2026-000",
                name = name,
                admissionDate = if (id == 0L) admission else allStudentsRaw.value.find { it.id == id }?.admissionDate ?: admission,
                className = className,
                courseName = courseName,
                parentName = parentName,
                address = address,
                mobileNumber = mobileNumber,
                isActive = isActive,
                monthlyFee = monthlyFee
            )

            if (id == 0L) {
                repository.addStudent(student)
                showToast("Student '$name' added successfully!")
            } else {
                repository.updateStudent(student)
                showToast("Student '$name' updated successfully!")
            }
        }
    }

    fun deleteStudent(student: StudentEntity) {
        viewModelScope.launch {
            repository.deleteStudent(student)
            showToast("Student '${student.name}' deleted.")
        }
    }

    // Fee Actions
    fun recordFeePayment(
        studentId: Long,
        amountPaid: Double,
        dueAmount: Double,
        paymentMode: String,
        month: String,
        notes: String
    ) {
        viewModelScope.launch {
            val nextReceipt = "REC-" + (1000 + allFeePayments.value.size + 1)
            val payment = FeePaymentEntity(
                studentId = studentId,
                receiptNo = nextReceipt,
                amountPaid = amountPaid,
                dueAmount = dueAmount,
                paymentDate = repository.getTodayDate(),
                paymentMode = paymentMode,
                month = month,
                notes = notes
            )
            repository.addFeePayment(payment)
            showToast("Fee Payment Recorded! Receipt #$nextReceipt")
        }
    }

    // Attendance Actions
    fun markStudentAttendance(studentId: Long, status: String, remark: String = "") {
        viewModelScope.launch {
            val date = selectedAttendanceDate.value
            val existing = attendanceForSelectedDate.value.find { it.studentId == studentId }
            val record = AttendanceEntity(
                id = existing?.id ?: 0,
                studentId = studentId,
                date = date,
                status = status,
                remark = remark
            )
            repository.saveAttendance(record)
        }
    }

    fun markAllAttendance(status: String) {
        viewModelScope.launch {
            val date = selectedAttendanceDate.value
            val active = allStudentsRaw.value.filter { it.isActive }
            val currentMap = attendanceForSelectedDate.value.associateBy { it.studentId }

            val bulk = active.map { st ->
                val existing = currentMap[st.id]
                AttendanceEntity(
                    id = existing?.id ?: 0,
                    studentId = st.id,
                    date = date,
                    status = status,
                    remark = if (status == "Present") "All Marked Present" else "All Marked Absent"
                )
            }
            repository.saveBulkAttendance(bulk)
            showToast("Marked all ${active.size} students as $status for $date")
        }
    }

    // Exam Actions
    fun addExam(examName: String, className: String, subject: String, maxMarks: Int, examDate: String) {
        viewModelScope.launch {
            val exam = ExamEntity(
                examName = examName,
                className = className,
                subject = subject,
                maxMarks = maxMarks,
                examDate = examDate
            )
            repository.addExam(exam)
            showToast("Exam '$examName' created successfully!")
        }
    }

    fun saveStudentMarks(examId: Long, studentId: Long, marks: Double, remark: String) {
        viewModelScope.launch {
            val result = ExamResultEntity(
                examId = examId,
                studentId = studentId,
                marksObtained = marks,
                remarks = remark
            )
            repository.saveExamResult(result)
            showToast("Marks updated!")
        }
    }

    // Admin Auth Actions
    fun authenticateAdmin(pin: String): Boolean {
        val currentPin = instituteConfig.value?.adminPin ?: "1234"
        if (pin == currentPin) {
            isAdminAuthenticated.value = true
            adminAuthError.value = null
            return true
        } else {
            adminAuthError.value = "Invalid PIN code. Default is 1234."
            return false
        }
    }

    fun changeAdminPin(newPin: String) {
        viewModelScope.launch {
            val current = instituteConfig.value ?: InstituteConfigEntity()
            val updated = current.copy(adminPin = newPin)
            repository.saveInstituteConfig(updated)
            showToast("Admin PIN updated successfully!")
        }
    }

    fun updateInstituteInfo(name: String, tagline: String, address: String, phone: String, email: String) {
        viewModelScope.launch {
            val current = instituteConfig.value ?: InstituteConfigEntity()
            val updated = current.copy(
                instituteName = name,
                tagline = tagline,
                address = address,
                phone = phone,
                email = email
            )
            repository.saveInstituteConfig(updated)
            showToast("Institute profile details saved!")
        }
    }

    fun sendFeeReminderAlert(student: StudentEntity) {
        viewModelScope.launch {
            val alert = NotificationLogEntity(
                title = "Fee Due Reminder Sent",
                message = "Reminder sent to ${student.name} (${student.mobileNumber}) for pending monthly fee ₹${student.monthlyFee.toInt()}.",
                date = repository.getTodayDate(),
                type = "Fee Due",
                studentId = student.id
            )
            repository.addNotification(alert)
            showToast("Reminder alert dispatched to ${student.name}'s mobile!")
        }
    }

    fun sendAttendanceAlert(student: StudentEntity) {
        viewModelScope.launch {
            val alert = NotificationLogEntity(
                title = "Absence Alert Dispatched",
                message = "Absence alert SMS/Notification sent to parent ${student.parentName} (${student.mobileNumber}).",
                date = repository.getTodayDate(),
                type = "Attendance Alert",
                studentId = student.id
            )
            repository.addNotification(alert)
            showToast("Attendance alert sent to parent of ${student.name}!")
        }
    }
}
