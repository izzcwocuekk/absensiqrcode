package com.example.absensisiswa.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "classes")
data class ClassEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val namaKelas: String,
    val jurusan: String,
    val tingkat: String,
    val createdAt: Long = System.currentTimeMillis()
)
