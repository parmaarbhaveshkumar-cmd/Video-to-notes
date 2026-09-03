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
import com.example.data.model.ChapterEntity
import com.example.data.model.LectureEntity
import com.example.data.model.NoteEntity
import com.example.data.model.NoteFigureEntity
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

    @Query("DELETE FROM lectures")
    suspend fun clearAllLectures()
}

@Dao
interface SubjectDao {
    @Query("SELECT * FROM subjects ORDER BY createdAt ASC")
    fun getAllSubjects(): Flow<List<SubjectEntity>>

    @Query("SELECT * FROM subjects WHERE id = :id LIMIT 1")
    suspend fun getSubjectById(id: String): SubjectEntity?

    @Query("SELECT * FROM subjects WHERE name = :name LIMIT 1")
    suspend fun getSubjectByName(name: String): SubjectEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSubject(subject: SubjectEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSubjects(subjects: List<SubjectEntity>)

    @Update
    suspend fun updateSubject(subject: SubjectEntity)

    @Query("DELETE FROM subjects WHERE id = :id")
    suspend fun deleteSubjectById(id: String)

    @Query("DELETE FROM subjects")
    suspend fun clearAllSubjects()
}

@Dao
interface ChapterDao {
    @Query("SELECT * FROM chapters WHERE subjectId = :subjectId ORDER BY chapterNumber ASC, createdAt ASC")
    fun getChaptersForSubject(subjectId: String): Flow<List<ChapterEntity>>

    @Query("SELECT * FROM chapters WHERE subjectId = :subjectId ORDER BY chapterNumber ASC, createdAt ASC")
    suspend fun getChaptersListForSubject(subjectId: String): List<ChapterEntity>

    @Query("SELECT * FROM chapters")
    fun getAllChapters(): Flow<List<ChapterEntity>>

    @Query("SELECT * FROM chapters WHERE id = :id LIMIT 1")
    suspend fun getChapterById(id: String): ChapterEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChapter(chapter: ChapterEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChapters(chapters: List<ChapterEntity>)

    @Update
    suspend fun updateChapter(chapter: ChapterEntity)

    @Query("DELETE FROM chapters WHERE id = :id")
    suspend fun deleteChapterById(id: String)

    @Query("DELETE FROM chapters WHERE subjectId = :subjectId")
    suspend fun deleteChaptersBySubject(subjectId: String)

    @Query("DELETE FROM chapters")
    suspend fun clearAllChapters()
}

@Dao
interface NoteDao {
    @Query("SELECT * FROM notes WHERE chapterId = :chapterId ORDER BY updatedAt DESC")
    fun getNotesForChapter(chapterId: String): Flow<List<NoteEntity>>

    @Query("SELECT * FROM notes WHERE subjectId = :subjectId ORDER BY updatedAt DESC")
    fun getNotesForSubject(subjectId: String): Flow<List<NoteEntity>>

    @Query("SELECT * FROM notes ORDER BY updatedAt DESC")
    fun getAllNotes(): Flow<List<NoteEntity>>

    @Query("SELECT * FROM notes WHERE id = :id LIMIT 1")
    suspend fun getNoteById(id: String): NoteEntity?

    @Query("SELECT * FROM notes WHERE isBookmarked = 1 ORDER BY updatedAt DESC")
    fun getBookmarkedNotes(): Flow<List<NoteEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNote(note: NoteEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotes(notes: List<NoteEntity>)

    @Update
    suspend fun updateNote(note: NoteEntity)

    @Query("UPDATE notes SET title = :newTitle, updatedAt = :timestamp WHERE id = :id")
    suspend fun renameNote(id: String, newTitle: String, timestamp: Long = System.currentTimeMillis())

    @Query("UPDATE notes SET isBookmarked = NOT isBookmarked WHERE id = :id")
    suspend fun toggleBookmark(id: String)

    @Query("DELETE FROM notes WHERE id = :id")
    suspend fun deleteNoteById(id: String)

    @Query("DELETE FROM notes WHERE chapterId = :chapterId")
    suspend fun deleteNotesByChapter(chapterId: String)

    @Query("DELETE FROM notes WHERE subjectId = :subjectId")
    suspend fun deleteNotesBySubject(subjectId: String)

    @Query("DELETE FROM notes")
    suspend fun clearAllNotes()
}

@Dao
interface NoteFigureDao {
    @Query("SELECT * FROM note_figures WHERE noteId = :noteId ORDER BY orderIndex ASC, createdAt ASC")
    fun getFiguresForNote(noteId: String): Flow<List<NoteFigureEntity>>

    @Query("SELECT * FROM note_figures WHERE noteId = :noteId ORDER BY orderIndex ASC, createdAt ASC")
    suspend fun getFiguresListForNote(noteId: String): List<NoteFigureEntity>

    @Query("SELECT * FROM note_figures WHERE chapterId = :chapterId ORDER BY orderIndex ASC")
    fun getFiguresForChapter(chapterId: String): Flow<List<NoteFigureEntity>>

    @Query("SELECT * FROM note_figures")
    fun getAllFigures(): Flow<List<NoteFigureEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFigure(figure: NoteFigureEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFigures(figures: List<NoteFigureEntity>)

    @Update
    suspend fun updateFigure(figure: NoteFigureEntity)

    @Query("UPDATE note_figures SET caption = :caption WHERE id = :id")
    suspend fun updateFigureCaption(id: String, caption: String)

    @Query("UPDATE note_figures SET rotationDegrees = :degrees WHERE id = :id")
    suspend fun updateFigureRotation(id: String, degrees: Int)

    @Query("UPDATE note_figures SET orderIndex = :newIndex WHERE id = :id")
    suspend fun updateFigureOrder(id: String, newIndex: Int)

    @Query("DELETE FROM note_figures WHERE id = :id")
    suspend fun deleteFigureById(id: String)

    @Query("DELETE FROM note_figures WHERE noteId = :noteId")
    suspend fun deleteFiguresByNote(noteId: String)

    @Query("DELETE FROM note_figures WHERE chapterId = :chapterId")
    suspend fun deleteFiguresByChapter(chapterId: String)

    @Query("DELETE FROM note_figures WHERE subjectId = :subjectId")
    suspend fun deleteFiguresBySubject(subjectId: String)

    @Query("DELETE FROM note_figures")
    suspend fun clearAllFigures()
}

@Dao
interface PyqDao {
    @Query("SELECT * FROM pyq_records WHERE subjectName = :subjectName LIMIT 1")
    fun getPyqForSubject(subjectName: String): Flow<PyqEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPyq(pyq: PyqEntity)
}

@Database(
    entities = [
        LectureEntity::class,
        SubjectEntity::class,
        ChapterEntity::class,
        NoteEntity::class,
        NoteFigureEntity::class,
        PyqEntity::class
    ],
    version = 3,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun lectureDao(): LectureDao
    abstract fun subjectDao(): SubjectDao
    abstract fun chapterDao(): ChapterDao
    abstract fun noteDao(): NoteDao
    abstract fun noteFigureDao(): NoteFigureDao
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
