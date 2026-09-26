package com.example.absensisiswa.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.absensisiswa.data.dao.AttendanceDao
import com.example.absensisiswa.data.dao.AttendanceRecordDao
import com.example.absensisiswa.data.dao.AttendanceSessionDao
import com.example.absensisiswa.data.dao.ClassDao
import com.example.absensisiswa.data.dao.GeofenceEventDao
import com.example.absensisiswa.data.dao.ScheduleDao
import com.example.absensisiswa.data.dao.SettingsDao
import com.example.absensisiswa.data.dao.StudentDao
import com.example.absensisiswa.data.dao.SubjectDao
import com.example.absensisiswa.data.dao.TeacherDao
import com.example.absensisiswa.data.dao.UserDao
import com.example.absensisiswa.data.entity.AttendanceEntity
import com.example.absensisiswa.data.entity.AttendanceRecordEntity
import com.example.absensisiswa.data.entity.AttendanceSessionEntity
import com.example.absensisiswa.data.entity.AttendanceSettingsEntity
import com.example.absensisiswa.data.entity.ClassEntity
import com.example.absensisiswa.data.entity.GeofenceEventEntity
import com.example.absensisiswa.data.entity.ScheduleEntity
import com.example.absensisiswa.data.entity.StudentEntity
import com.example.absensisiswa.data.entity.SubjectEntity
import com.example.absensisiswa.data.entity.TeacherEntity
import com.example.absensisiswa.data.entity.UserEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        ClassEntity::class,
        StudentEntity::class,
        AttendanceEntity::class,
        AttendanceSettingsEntity::class,
        UserEntity::class,
        GeofenceEventEntity::class,
        TeacherEntity::class,
        SubjectEntity::class,
        ScheduleEntity::class,
        AttendanceSessionEntity::class,
        AttendanceRecordEntity::class
    ],
    version = 3,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun classDao(): ClassDao
    abstract fun studentDao(): StudentDao
    abstract fun attendanceDao(): AttendanceDao
    abstract fun settingsDao(): SettingsDao
    abstract fun userDao(): UserDao
    abstract fun geofenceEventDao(): GeofenceEventDao
    abstract fun teacherDao(): TeacherDao
    abstract fun subjectDao(): SubjectDao
    abstract fun scheduleDao(): ScheduleDao
    abstract fun attendanceSessionDao(): AttendanceSessionDao
    abstract fun attendanceRecordDao(): AttendanceRecordDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "absensi_siswa_db"
                )
                    .addCallback(DatabaseCallback(scope))
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }

    private class DatabaseCallback(
        private val scope: CoroutineScope
    ) : RoomDatabase.Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            INSTANCE?.let { database ->
                scope.launch(Dispatchers.IO) {
                    DatabaseInitializer.populateInitialData(database)
                }
            }
        }
    }
}
