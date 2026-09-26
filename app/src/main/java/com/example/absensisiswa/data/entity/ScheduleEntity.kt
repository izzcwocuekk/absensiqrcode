package com.example.absensisiswa.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "schedules",
    foreignKeys = [
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
        Index(value = ["teacherId"]),
        Index(value = ["classId"]),
        Index(value = ["subjectId"]),
        Index(value = ["day", "lessonNumber", "classId"])
    ]
)
data class ScheduleEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val teacherId: Long,
    val classId: Long,
    val subjectId: Long,
    val day: String, // "Senin", "Selasa", "Rabu", "Kamis", "Jumat", "Sabtu"
    val startTime: String, // "07:00"
    val endTime: String, // "07:45"
    val lessonNumber: Int, // 1, 2, 3...
    val room: String = "Lab Komputer"
)
