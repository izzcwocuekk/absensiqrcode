package com.example.absensisiswa.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.example.absensisiswa.data.entity.AttendanceSessionEntity
import com.example.absensisiswa.data.model.AttendanceSessionWithDetails
import kotlinx.coroutines.flow.Flow

@Dao
interface AttendanceSessionDao {
    @Transaction
    @Query("SELECT * FROM attendance_sessions ORDER BY date DESC, lessonNumber DESC")
    fun getAllSessionsWithDetails(): Flow<List<AttendanceSessionWithDetails>>

    @Transaction
    @Query("SELECT * FROM attendance_sessions WHERE date = :date ORDER BY lessonNumber ASC, classId ASC")
    fun getSessionsWithDetailsByDate(date: String): Flow<List<AttendanceSessionWithDetails>>

    @Transaction
    @Query("SELECT * FROM attendance_sessions WHERE teacherId = :teacherId ORDER BY date DESC, lessonNumber DESC")
    fun getSessionsWithDetailsByTeacher(teacherId: Long): Flow<List<AttendanceSessionWithDetails>>

    @Transaction
    @Query("SELECT * FROM attendance_sessions WHERE teacherId = :teacherId AND date = :date ORDER BY lessonNumber ASC")
    fun getSessionsWithDetailsByTeacherAndDate(teacherId: Long, date: String): Flow<List<AttendanceSessionWithDetails>>

    @Transaction
    @Query("SELECT * FROM attendance_sessions WHERE id = :id LIMIT 1")
    suspend fun getSessionWithDetailsById(id: Long): AttendanceSessionWithDetails?

    @Query("SELECT * FROM attendance_sessions WHERE id = :id LIMIT 1")
    suspend fun getSessionById(id: Long): AttendanceSessionEntity?

    @Query("SELECT * FROM attendance_sessions WHERE scheduleId = :scheduleId AND date = :date LIMIT 1")
    suspend fun findSessionByScheduleAndDate(scheduleId: Long, date: String): AttendanceSessionEntity?

    @Query("SELECT * FROM attendance_sessions WHERE teacherId = :teacherId AND date = :date AND status = 'DIBUKA' LIMIT 1")
    suspend fun getActiveSessionByTeacher(teacherId: Long, date: String): AttendanceSessionEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(session: AttendanceSessionEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(sessions: List<AttendanceSessionEntity>)

    @Update
    suspend fun updateSession(session: AttendanceSessionEntity)

    @Query("UPDATE attendance_sessions SET status = 'DITUTUP', closedAt = :closedAt, notes = :notes WHERE id = :sessionId")
    suspend fun closeSession(sessionId: Long, closedAt: String, notes: String?)

    @Delete
    suspend fun deleteSession(session: AttendanceSessionEntity)
}
