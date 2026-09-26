package com.example.absensisiswa.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "attendances",
    foreignKeys = [
        ForeignKey(
            entity = StudentEntity::class,
            parentColumns = ["id"],
            childColumns = ["studentId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["studentId", "tanggal"], unique = true),
        Index(value = ["tanggal"]),
        Index(value = ["status"])
    ]
)
data class AttendanceEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val studentId: Long,
    val tanggal: String, // "YYYY-MM-DD"
    val jamMasuk: String, // "HH:mm:ss"
    val status: String, // "Hadir", "Terlambat", "Izin", "Sakit", "Alpa"
    val keterangan: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    // Geolocation Verification & Geofence Fields
    val latitude: Double? = null,
    val longitude: Double? = null,
    val accuracy: Float? = null,
    val distanceFromSchool: Float? = null,
    val locationVerified: Boolean = false,
    val geofenceStatus: String = "DI_SEKOLAH"
)
