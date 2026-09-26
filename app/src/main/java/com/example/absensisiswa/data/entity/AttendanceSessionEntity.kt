package com.example.absensisiswa.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "attendance_sessions",
    foreignKeys = [
        ForeignKey(
            entity = ScheduleEntity::class,
            parentColumns = ["id"],
            childColumns = ["scheduleId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = TeacherEntity::class,
            parentColumns = ["id"],
            childColumns = ["teacherId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = ClassEntity::class,
            parentColumns = ["id"],
            childColumns = ["classId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = SubjectEntity::class,
            parentColumns = ["id"],
            childColumns = ["subjectId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["scheduleId", "date"], unique = true),
        Index(value = ["date"]),
        Index(value = ["teacherId"]),
        Index(value = ["classId"]),
        Index(value = ["subjectId"]),
        Index(value = ["status"])
    ]
)
data class AttendanceSessionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val scheduleId: Long,
    val teacherId: Long,
    val classId: Long,
    val subjectId: Long,
    val date: String, // "YYYY-MM-DD"
    val lessonNumber: Int,
    val startTime: String,
    val endTime: String,
    val openedAt: String,
    val closedAt: String? = null,
    val status: String = "DIBUKA", // "DIBUKA", "DITUTUP"
    val latitude: Double? = null,
    val longitude: Double? = null,
    val distanceFromSchool: Float? = null,
    val locationVerified: Boolean = false,
    val notes: String? = null
)
