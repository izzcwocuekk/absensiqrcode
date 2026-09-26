package com.example.absensisiswa.data.model

import androidx.room.Embedded
import androidx.room.Relation
import com.example.absensisiswa.data.entity.AttendanceEntity
import com.example.absensisiswa.data.entity.StudentEntity

data class AttendanceWithDetails(
    @Embedded
    val attendance: AttendanceEntity,
    @Relation(
        entity = StudentEntity::class,
        parentColumn = "studentId",
        entityColumn = "id"
    )
    val studentWithClass: StudentWithClass?
)
