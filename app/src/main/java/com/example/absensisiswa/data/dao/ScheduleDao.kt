package com.example.absensisiswa.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.example.absensisiswa.data.entity.ScheduleEntity
import com.example.absensisiswa.data.model.ScheduleWithDetails
import kotlinx.coroutines.flow.Flow

@Dao
interface ScheduleDao {
    @Transaction
    @Query("SELECT * FROM schedules ORDER BY day ASC, lessonNumber ASC")
    fun getAllSchedulesWithDetails(): Flow<List<ScheduleWithDetails>>

    @Transaction
    @Query("SELECT * FROM schedules WHERE teacherId = :teacherId AND day = :day ORDER BY lessonNumber ASC")
    fun getSchedulesByTeacherAndDay(teacherId: Long, day: String): Flow<List<ScheduleWithDetails>>

    @Transaction
    @Query("SELECT * FROM schedules WHERE teacherId = :teacherId ORDER BY day ASC, lessonNumber ASC")
    fun getSchedulesByTeacher(teacherId: Long): Flow<List<ScheduleWithDetails>>

    @Transaction
    @Query("SELECT * FROM schedules WHERE day = :day ORDER BY lessonNumber ASC, classId ASC")
    fun getSchedulesByDay(day: String): Flow<List<ScheduleWithDetails>>

    @Transaction
    @Query("SELECT * FROM schedules WHERE classId = :classId AND day = :day ORDER BY lessonNumber ASC")
    fun getSchedulesByClassAndDay(classId: Long, day: String): Flow<List<ScheduleWithDetails>>

    @Transaction
    @Query("SELECT * FROM schedules WHERE id = :id LIMIT 1")
    suspend fun getScheduleWithDetailsById(id: Long): ScheduleWithDetails?

    @Query("SELECT * FROM schedules WHERE id = :id LIMIT 1")
    suspend fun getScheduleById(id: Long): ScheduleEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSchedule(schedule: ScheduleEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(schedules: List<ScheduleEntity>)

    @Update
    suspend fun updateSchedule(schedule: ScheduleEntity)

    @Delete
    suspend fun deleteSchedule(schedule: ScheduleEntity)
}
