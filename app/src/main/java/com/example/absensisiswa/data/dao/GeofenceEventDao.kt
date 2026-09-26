package com.example.absensisiswa.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.absensisiswa.data.entity.GeofenceEventEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface GeofenceEventDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEvent(event: GeofenceEventEntity): Long

    @Query("SELECT * FROM geofence_events ORDER BY createdAt DESC")
    fun getAllEvents(): Flow<List<GeofenceEventEntity>>

    @Query("SELECT * FROM geofence_events WHERE studentId = :studentId ORDER BY createdAt DESC")
    fun getEventsForStudent(studentId: Long): Flow<List<GeofenceEventEntity>>

    @Query("SELECT * FROM geofence_events WHERE eventTime LIKE :datePrefix || '%' ORDER BY createdAt DESC")
    fun getEventsForDate(datePrefix: String): Flow<List<GeofenceEventEntity>>

    @Query("DELETE FROM geofence_events")
    suspend fun clearAllEvents()
}
