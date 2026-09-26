package com.example.absensisiswa.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "teachers")
data class TeacherEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val userId: Long? = null,
    val nama: String,
    val nip: String? = null,
    val noHp: String? = null,
    val mataPelajaranUtama: String? = null,
    val email: String? = null
)
