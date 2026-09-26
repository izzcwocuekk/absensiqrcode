package com.example.absensisiswa.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "subjects")
data class SubjectEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val kode: String, // e.g. "INF", "PBO", "BIND", "MTK", "BD"
    val namaMataPelajaran: String,
    val deskripsi: String? = null
)
