package com.example.absensisiswa.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.example.absensisiswa.data.entity.AttendanceEntity
import com.example.absensisiswa.data.model.AttendanceWithDetails
import kotlinx.coroutines.flow.Flow

@Dao
interface AttendanceDao {
    @Transaction
    @Query("SELECT * FROM attendances WHERE tanggal = :tanggal ORDER BY jamMasuk DESC")
    fun getAttendancesByDate(tanggal: String): Flow<List<AttendanceWithDetails>>

    @Transaction
    @Query("SELECT * FROM attendances ORDER BY tanggal DESC, jamMasuk DESC LIMIT :limit")
    fun getRecentAttendances(limit: Int = 10): Flow<List<AttendanceWithDetails>>

    @Transaction
    @Query("SELECT * FROM attendances WHERE studentId = :studentId ORDER BY tanggal DESC")
    fun getAttendancesByStudent(studentId: Long): Flow<List<AttendanceWithDetails>>

    @Transaction
    @Query("""
        SELECT * FROM attendances 
        WHERE tanggal BETWEEN :startDate AND :endDate 
        ORDER BY tanggal DESC, jamMasuk DESC
    """)
    fun getAttendancesByDateRange(startDate: String, endDate: String): Flow<List<AttendanceWithDetails>>

    @Query("SELECT * FROM attendances WHERE studentId = :studentId AND tanggal = :tanggal LIMIT 1")
    suspend fun getAttendanceForStudentOnDate(studentId: Long, tanggal: String): AttendanceEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAttendance(attendance: AttendanceEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(attendances: List<AttendanceEntity>)

    @Update
    suspend fun updateAttendance(attendance: AttendanceEntity)

    @Delete
    suspend fun deleteAttendance(attendance: AttendanceEntity)

    @Query("SELECT COUNT(*) FROM attendances WHERE tanggal = :tanggal AND status = 'Hadir'")
    fun getHadirCountForDate(tanggal: String): Flow<Int>

    @Query("SELECT COUNT(*) FROM attendances WHERE tanggal = :tanggal AND status = 'Terlambat'")
    fun getTerlambatCountForDate(tanggal: String): Flow<Int>

    @Query("SELECT COUNT(*) FROM attendances WHERE tanggal = :tanggal AND status = 'Izin'")
    fun getIzinCountForDate(tanggal: String): Flow<Int>

    @Query("SELECT COUNT(*) FROM attendances WHERE tanggal = :tanggal AND status = 'Sakit'")
    fun getSakitCountForDate(tanggal: String): Flow<Int>

    @Query("SELECT COUNT(*) FROM attendances WHERE tanggal = :tanggal AND (status = 'Alpa' OR status = 'Alfa')")
    fun getAlpaCountForDate(tanggal: String): Flow<Int>

    @Query("DELETE FROM attendances")
    suspend fun clearAllAttendances()
}
