package com.example.absensisiswa.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val username: String,
    val password: String, // simple plain or hash matching original
    val nama: String,
    val role: String, // "admin", "guru", "siswa"
    val studentId: Long? = null,
    val teacherId: Long? = null
)
