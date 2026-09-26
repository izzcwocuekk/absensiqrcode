package com.example.absensisiswa.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "students",
    foreignKeys = [
        ForeignKey(
            entity = ClassEntity::class,
            parentColumns = ["id"],
            childColumns = ["classId"],
            onDelete = ForeignKey.RESTRICT
        )
    ],
    indices = [
        Index(value = ["nis"], unique = true),
        Index(value = ["qrToken"], unique = true),
        Index(value = ["classId"])
    ]
)
data class StudentEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val nis: String,
    val nisn: String? = null,
    val nama: String,
    val classId: Long,
    val jenisKelamin: String, // "L" or "P"
    val qrToken: String,
    val foto: String? = null,
    val isActive: Boolean = true,
    val createdAt: Long = System.currentTimeMillis()
)
