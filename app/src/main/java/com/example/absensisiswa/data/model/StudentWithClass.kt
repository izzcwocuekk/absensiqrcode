package com.example.absensisiswa.data.model

import androidx.room.Embedded
import androidx.room.Relation
import com.example.absensisiswa.data.entity.ClassEntity
import com.example.absensisiswa.data.entity.StudentEntity

data class StudentWithClass(
    @Embedded
    val student: StudentEntity,
    @Relation(
        parentColumn = "classId",
        entityColumn = "id"
    )
    val classEntity: ClassEntity?
)
