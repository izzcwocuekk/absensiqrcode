package com.example.absensisiswa.data.model

import androidx.room.Embedded
import androidx.room.Relation
import com.example.absensisiswa.data.entity.AttendanceSessionEntity
import com.example.absensisiswa.data.entity.ClassEntity
import com.example.absensisiswa.data.entity.ScheduleEntity
import com.example.absensisiswa.data.entity.SubjectEntity
import com.example.absensisiswa.data.entity.TeacherEntity

data class AttendanceSessionWithDetails(
    @Embedded
    val session: AttendanceSessionEntity,

    @Relation(
        parentColumn = "scheduleId",
        entityColumn = "id"
    )
    val schedule: ScheduleEntity?,

    @Relation(
        parentColumn = "teacherId",
        entityColumn = "id"
    )
    val teacher: TeacherEntity?,

    @Relation(
        parentColumn = "classId",
        entityColumn = "id"
    )
    val classEntity: ClassEntity?,

    @Relation(
        parentColumn = "subjectId",
        entityColumn = "id"
    )
    val subject: SubjectEntity?
)
