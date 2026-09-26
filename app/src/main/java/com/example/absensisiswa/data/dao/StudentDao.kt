package com.example.absensisiswa.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.example.absensisiswa.data.entity.StudentEntity
import com.example.absensisiswa.data.model.StudentWithClass
import kotlinx.coroutines.flow.Flow

@Dao
interface StudentDao {
    @Transaction
    @Query("SELECT * FROM students ORDER BY nama ASC")
    fun getAllStudentsWithClass(): Flow<List<StudentWithClass>>

    @Transaction
    @Query("SELECT * FROM students WHERE classId = :classId ORDER BY nama ASC")
    fun getStudentsByClass(classId: Long): Flow<List<StudentWithClass>>

    @Query("SELECT * FROM students WHERE classId = :classId ORDER BY nama ASC")
    suspend fun getStudentsByClassSync(classId: Long): List<StudentEntity>

    @Transaction
    @Query("SELECT * FROM students WHERE id = :id LIMIT 1")
    suspend fun getStudentWithClassById(id: Long): StudentWithClass?

    @Query("SELECT * FROM students WHERE id = :id LIMIT 1")
    suspend fun getStudentById(id: Long): StudentEntity?

    @Transaction
    @Query("SELECT * FROM students WHERE qrToken = :token OR nis = :token LIMIT 1")
    suspend fun findByTokenOrNis(token: String): StudentWithClass?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStudent(student: StudentEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(students: List<StudentEntity>)

    @Update
    suspend fun updateStudent(student: StudentEntity)

    @Delete
    suspend fun deleteStudent(student: StudentEntity)

    @Query("SELECT COUNT(*) FROM students")
    fun getTotalStudentCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM students WHERE isActive = 1")
    fun getActiveStudentCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM students WHERE classId = :classId")
    suspend fun getStudentCountByClass(classId: Long): Int
}
