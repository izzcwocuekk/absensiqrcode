package com.example.absensisiswa.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.absensisiswa.data.entity.AttendanceSettingsEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SettingsDao {
    @Query("SELECT * FROM attendance_settings WHERE id = 1 LIMIT 1")
    fun getSettings(): Flow<AttendanceSettingsEntity?>

    @Query("SELECT * FROM attendance_settings WHERE id = 1 LIMIT 1")
    suspend fun getSettingsSync(): AttendanceSettingsEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSettings(settings: AttendanceSettingsEntity)

    @Update
    suspend fun updateSettings(settings: AttendanceSettingsEntity)
}
