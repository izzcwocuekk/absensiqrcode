package com.example.absensisiswa.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "geofence_events",
    foreignKeys = [
        ForeignKey(
            entity = StudentEntity::class,
            parentColumns = ["id"],
            childColumns = ["studentId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["studentId"]),
        Index(value = ["eventTime"])
    ]
)
data class GeofenceEventEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val studentId: Long,
    val eventType: String, // "ENTER", "EXIT", "UNKNOWN"
    val eventTime: String, // "YYYY-MM-DD HH:mm:ss"
    val notes: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)
