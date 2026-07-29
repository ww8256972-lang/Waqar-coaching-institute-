package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        StudentEntity::class,
        FeePaymentEntity::class,
        AttendanceEntity::class,
        ExamEntity::class,
        ExamResultEntity::class,
        NotificationLogEntity::class,
        InstituteConfigEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class WaqarDatabase : RoomDatabase() {
    abstract fun studentDao(): StudentDao
    abstract fun feePaymentDao(): FeePaymentDao
    abstract fun attendanceDao(): AttendanceDao
    abstract fun examDao(): ExamDao
    abstract fun examResultDao(): ExamResultDao
    abstract fun notificationLogDao(): NotificationLogDao
    abstract fun instituteConfigDao(): InstituteConfigDao

    companion object {
        @Volatile
        private var INSTANCE: WaqarDatabase? = null

        fun getDatabase(context: Context): WaqarDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    WaqarDatabase::class.java,
                    "waqar_coaching_db"
                ).fallbackToDestructiveMigration().build()
                INSTANCE = instance
                instance
            }
        }
    }
}
