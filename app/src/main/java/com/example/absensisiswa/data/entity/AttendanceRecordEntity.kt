package com.example.absensisiswa.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "attendance_records",
    foreignKeys = [
        ForeignKey(
            entity = AttendanceSessionEntity::class,
            parentColumns = ["id"],
            childColumns = ["sessionId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = StudentEntity::class,
            parentColumns = ["id"],
            childColumns = ["studentId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["sessionId", "studentId"], unique = true),
        Index(value = ["sessionId"]),
        Index(value = ["studentId"]),
        Index(value = ["status"])
    ]
)
data class AttendanceRecordEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val sessionId: Long,
    val studentId: Long,
    val status: String = "ALPA", // "HADIR", "IZIN", "SAKIT", "ALPA"
    val checkInTime: String? = null,
    val notes: String? = null,
    val verifiedByQr: Boolean = false,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val distanceFromSchool: Float? = null
)
