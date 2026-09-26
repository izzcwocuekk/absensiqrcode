package com.example.absensisiswa

import android.app.Application
import com.example.absensisiswa.data.AppDatabase
import com.example.absensisiswa.data.AttendanceRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob

class AbsensiApplication : Application() {
    private val applicationScope = CoroutineScope(SupervisorJob())

    val database by lazy { AppDatabase.getDatabase(this, applicationScope) }
    val repository by lazy {
        AttendanceRepository(
            database.classDao(),
            database.studentDao(),
            database.attendanceDao(),
            database.settingsDao(),
            database.userDao(),
            database
        )
    }
}
