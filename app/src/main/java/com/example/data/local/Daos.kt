package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface StudentDao {
    @Query("SELECT * FROM students ORDER BY name ASC")
    fun getAllStudents(): Flow<List<StudentEntity>>

    @Query("SELECT * FROM students WHERE id = :id")
    suspend fun getStudentById(id: Long): StudentEntity?

    @Query("SELECT * FROM students WHERE name LIKE '%' || :query || '%' OR mobileNumber LIKE '%' || :query || '%' OR studentCode LIKE '%' || :query || '%' ORDER BY name ASC")
    fun searchStudents(query: String): Flow<List<StudentEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStudent(student: StudentEntity): Long

    @Update
    suspend fun updateStudent(student: StudentEntity)

    @Delete
    suspend fun deleteStudent(student: StudentEntity)

    @Query("SELECT COUNT(*) FROM students WHERE isActive = 1")
    fun getActiveStudentCount(): Flow<Int>
}

@Dao
interface FeePaymentDao {
    @Query("SELECT * FROM fee_payments ORDER BY paymentDate DESC, id DESC")
    fun getAllFeePayments(): Flow<List<FeePaymentEntity>>

    @Query("SELECT * FROM fee_payments WHERE studentId = :studentId ORDER BY paymentDate DESC")
    fun getPaymentsForStudent(studentId: Long): Flow<List<FeePaymentEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFeePayment(payment: FeePaymentEntity): Long

    @Query("SELECT SUM(amountPaid) FROM fee_payments")
    fun getTotalFeesCollected(): Flow<Double?>

    @Query("SELECT SUM(dueAmount) FROM fee_payments")
    fun getTotalFeesPending(): Flow<Double?>
}

@Dao
interface AttendanceDao {
    @Query("SELECT * FROM attendance WHERE date = :date")
    fun getAttendanceByDate(date: String): Flow<List<AttendanceEntity>>

    @Query("SELECT * FROM attendance WHERE studentId = :studentId ORDER BY date DESC")
    fun getAttendanceForStudent(studentId: Long): Flow<List<AttendanceEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateAttendance(attendance: AttendanceEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBulkAttendance(list: List<AttendanceEntity>)

    @Query("SELECT COUNT(*) FROM attendance WHERE date = :date AND status = 'Present'")
    fun getPresentCountForDate(date: String): Flow<Int>
}

@Dao
interface ExamDao {
    @Query("SELECT * FROM exams ORDER BY examDate DESC")
    fun getAllExams(): Flow<List<ExamEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExam(exam: ExamEntity): Long

    @Delete
    suspend fun deleteExam(exam: ExamEntity)
}

@Dao
interface ExamResultDao {
    @Query("SELECT * FROM exam_results WHERE examId = :examId")
    fun getResultsForExam(examId: Long): Flow<List<ExamResultEntity>>

    @Query("SELECT * FROM exam_results WHERE studentId = :studentId")
    fun getResultsForStudent(studentId: Long): Flow<List<ExamResultEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateResult(result: ExamResultEntity)
}

@Dao
interface NotificationLogDao {
    @Query("SELECT * FROM notifications ORDER BY id DESC")
    fun getAllNotifications(): Flow<List<NotificationLogEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotification(notification: NotificationLogEntity)

    @Query("UPDATE notifications SET isRead = 1 WHERE id = :id")
    suspend fun markAsRead(id: Long)
}

@Dao
interface InstituteConfigDao {
    @Query("SELECT * FROM institute_config WHERE id = 1")
    fun getConfig(): Flow<InstituteConfigEntity?>

    @Query("SELECT * FROM institute_config WHERE id = 1")
    suspend fun getConfigDirect(): InstituteConfigEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveConfig(config: InstituteConfigEntity)
}
