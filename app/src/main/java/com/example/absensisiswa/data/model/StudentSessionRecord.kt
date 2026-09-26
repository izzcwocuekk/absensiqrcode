package com.example.absensisiswa.data.model

import com.example.absensisiswa.data.entity.AttendanceRecordEntity
import com.example.absensisiswa.data.entity.StudentEntity

data class StudentSessionRecord(
    val student: StudentEntity,
    val record: AttendanceRecordEntity
)
