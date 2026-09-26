package com.example.absensisiswa.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.absensisiswa.util.LocationUtils

@Entity(tableName = "attendance_settings")
data class AttendanceSettingsEntity(
    @PrimaryKey
    val id: Long = 1,
    val schoolName: String = "SMK TRITECH INFORMATIKA MEDAN",
    val schoolStartTime: String = "07:15:00",
    val lateAfter: String = "07:16:00",
    val schoolAddress: String = "Jl. Bhayangkara No. 434, Medan",
    // Geofence & Location Configurations
    val schoolLatitude: Double = LocationUtils.DEFAULT_SCHOOL_LAT,
    val schoolLongitude: Double = LocationUtils.DEFAULT_SCHOOL_LON,
    val schoolRadiusMeters: Float = LocationUtils.DEFAULT_SCHOOL_RADIUS_METERS,
    val maxGpsAccuracyMeters: Float = LocationUtils.DEFAULT_MAX_ACCURACY_METERS,
    val monitoringStartTime: String = LocationUtils.DEFAULT_MONITORING_START,
    val monitoringEndTime: String = LocationUtils.DEFAULT_MONITORING_END,
    val requireLocationForAttendance: Boolean = true,
    val simulationModeEnabled: Boolean = false
)
