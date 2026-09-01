package com.example.data.local

import android.content.Context
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.Update
import com.example.data.model.LectureEntity
import com.example.data.model.PyqEntity
import com.example.data.model.SubjectEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface LectureDao {
    @Query("SELECT * FROM lectures ORDER BY dateEpoch DESC")
    fun getAllLectures(): Flow<List<LectureEntity>>

    @Query("SELECT * FROM lectures WHERE id = :id LIMIT 1")
    suspend fun getLectureById(id: String): LectureEntity?

    @Query("SELECT * FROM lectures WHERE subject = :subject ORDER BY dateEpoch DESC")
    fun getLecturesBySubject(subject: String): Flow<List<LectureEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLecture(lecture: LectureEntity)

    @Update
    suspend fun updateLecture(lecture: LectureEntity)

    @Query("UPDATE lectures SET title = :newTitle WHERE id = :id")
    suspend fun renameLecture(id: String, newTitle: String)

    @Query("UPDATE lectures SET hasVideo = 0, videoFilePath = NULL, videoFileSizeMb = 0.0 WHERE id = :id")
    suspend fun deleteVideoOnly(id: String)

    @Query("UPDATE lectures SET hasAudio = 0, audioFilePath = '', audioFileSizeMb = 0.0 WHERE id = :id")
    suspend fun deleteAudioOnly(id: String)

    @Query("UPDATE lectures SET hasNotes = 0, summary = '', topicsJson = '[]', cleanTranscript = '' WHERE id = :id")
    suspend fun deleteNotesOnly(id: String)

    @Query("DELETE FROM lectures WHERE id = :id")
    suspend fun deleteLectureById(id: String)
}

@Dao
interface SubjectDao {
    @Query("SELECT * FROM subjects")
    fun getAllSubjects(): Flow<List<SubjectEntity>>

    @Query("SELECT * FROM subjects WHERE name = :name LIMIT 1")
    suspend fun getSubjectByName(name: String): SubjectEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSubject(subject: SubjectEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSubjects(subjects: List<SubjectEntity>)

    @Update
    suspend fun updateSubject(subject: SubjectEntity)
}

@Dao
interface PyqDao {
    @Query("SELECT * FROM pyq_records WHERE subjectName = :subjectName LIMIT 1")
    fun getPyqForSubject(subjectName: String): Flow<PyqEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPyq(pyq: PyqEntity)
}

@Database(
    entities = [LectureEntity::class, SubjectEntity::class, PyqEntity::class],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun lectureDao(): LectureDao
    abstract fun subjectDao(): SubjectDao
    abstract fun pyqDao(): PyqDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "lecture_exam_db"
                ).fallbackToDestructiveMigration().build()
                INSTANCE = instance
                instance
            }
        }
    }
}
