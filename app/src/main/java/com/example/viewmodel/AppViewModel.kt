package com.example.viewmodel

import android.app.Application
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.audio.AudioPlayerManager
import com.example.audio.AudioRecorderManager
import com.example.audio.RecordingState
import com.example.data.gemini.GeminiService
import com.example.data.local.AppDatabase
import com.example.data.local.JsonUtils
import com.example.data.local.NoteStorageHelper
import com.example.data.local.SampleData
import com.example.data.model.BoardKeyFrame
import com.example.data.model.ChapterEntity
import com.example.data.model.ChatMessage
import com.example.data.model.Flashcard
import com.example.data.model.LectureEntity
import com.example.data.model.McqQuestion
import com.example.data.model.NoteEntity
import com.example.data.model.NoteFigureEntity
import com.example.data.model.NoteSearchResult
import com.example.data.model.NoteWithFigures
import com.example.data.model.PyqEntity
import com.example.data.model.PyqItem
import com.example.data.model.SubjectEntity
import com.example.data.model.SubjectWithStats
import com.example.data.model.SyllabusTopic
import com.example.data.model.SyllabusUnit
import com.example.data.model.TopicSection
import com.example.video.VideoRecorderManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.util.UUID

enum class AppScreen {
    HOME,
    RECORD,
    RECORD_VIDEO,
    UPLOAD_VIDEO,
    NOTES_DETAIL,
    EXAM_PREP,
    REVISION_QUIZ,
    PYQ_ANALYZER,
    SYLLABUS_VIEWER,
    SUBJECT_DETAIL,
    NOTE_EDITOR,
    PDF_PREVIEW,
    NOTES_SEARCH
}

class AppViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getDatabase(application)
    private val lectureDao = db.lectureDao()
    private val subjectDao = db.subjectDao()
    private val chapterDao = db.chapterDao()
    private val noteDao = db.noteDao()
    private val noteFigureDao = db.noteFigureDao()
    private val pyqDao = db.pyqDao()

    val audioRecorder = AudioRecorderManager(application)
    val videoRecorder = VideoRecorderManager(application)
    val audioPlayer = AudioPlayerManager(application)
    private val geminiService = GeminiService()

    // Navigation & Current Screen
    private val _currentScreen = MutableStateFlow(AppScreen.HOME)
    val currentScreen: StateFlow<AppScreen> = _currentScreen.asStateFlow()

    // Active Selected Lecture
    private val _selectedLecture = MutableStateFlow<LectureEntity?>(null)
    val selectedLecture: StateFlow<LectureEntity?> = _selectedLecture.asStateFlow()

    // Video Seek Synchronizer Target
    private val _videoSeekTargetSeconds = MutableStateFlow<Int?>(null)
    val videoSeekTargetSeconds: StateFlow<Int?> = _videoSeekTargetSeconds.asStateFlow()

    // Active Selected Subject & Chapter & Note for Notes Navigation
    private val _selectedSubjectEntity = MutableStateFlow<SubjectEntity?>(null)
    val selectedSubjectEntity: StateFlow<SubjectEntity?> = _selectedSubjectEntity.asStateFlow()

    private val _selectedChapterEntity = MutableStateFlow<ChapterEntity?>(null)
    val selectedChapterEntity: StateFlow<ChapterEntity?> = _selectedChapterEntity.asStateFlow()

    private val _selectedNoteEntity = MutableStateFlow<NoteEntity?>(null)
    val selectedNoteEntity: StateFlow<NoteEntity?> = _selectedNoteEntity.asStateFlow()

    private val _selectedSubject = MutableStateFlow("Engineering Thermodynamics")
    val selectedSubject: StateFlow<String> = _selectedSubject.asStateFlow()

    // Active PDF Preview File
    private val _activePdfPreviewFile = MutableStateFlow<File?>(null)
    val activePdfPreviewFile: StateFlow<File?> = _activePdfPreviewFile.asStateFlow()

    private val _activePdfNoteTitle = MutableStateFlow("")
    val activePdfNoteTitle: StateFlow<String> = _activePdfNoteTitle.asStateFlow()

    private val _activePdfSubjectName = MutableStateFlow("")
    val activePdfSubjectName: StateFlow<String> = _activePdfSubjectName.asStateFlow()

    // Search Query State
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    // Exam Preparation Mode (Toggle)
    private val _isExamPrepMode = MutableStateFlow(false)
    val isExamPrepMode: StateFlow<Boolean> = _isExamPrepMode.asStateFlow()

    // Processing / AI Generation State
    private val _isGeneratingAiNotes = MutableStateFlow(false)
    val isGeneratingAiNotes: StateFlow<Boolean> = _isGeneratingAiNotes.asStateFlow()

    private val _generationStatusMessage = MutableStateFlow("")
    val generationStatusMessage: StateFlow<String> = _generationStatusMessage.asStateFlow()

    // AI Chat Messages
    private val _chatMessages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val chatMessages: StateFlow<List<ChatMessage>> = _chatMessages.asStateFlow()

    private val _isChatAiThinking = MutableStateFlow(false)
    val isChatAiThinking: StateFlow<Boolean> = _isChatAiThinking.asStateFlow()

    // Quiz Score Tracker
    private val _quizScore = MutableStateFlow(0)
    val quizScore: StateFlow<Int> = _quizScore.asStateFlow()

    private val _currentQuizIndex = MutableStateFlow(0)
    val currentQuizIndex: StateFlow<Int> = _currentQuizIndex.asStateFlow()

    private val _selectedMcqAnswer = MutableStateFlow<Int?>(null)
    val selectedMcqAnswer: StateFlow<Int?> = _selectedMcqAnswer.asStateFlow()

    // Database Flows
    val allLectures: StateFlow<List<LectureEntity>> = lectureDao.getAllLectures()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allSubjects: StateFlow<List<SubjectEntity>> = subjectDao.getAllSubjects()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allChapters: StateFlow<List<ChapterEntity>> = chapterDao.getAllChapters()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allNotes: StateFlow<List<NoteEntity>> = noteDao.getAllNotes()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allFigures: StateFlow<List<NoteFigureEntity>> = noteFigureDao.getAllFigures()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Combined Flow: Subjects with Stats (Chapters count, Notes count)
    val subjectsWithStats: StateFlow<List<SubjectWithStats>> = combine(
        allSubjects,
        allChapters,
        allNotes
    ) { subjects, chapters, notes ->
        subjects.map { subj ->
            val chapCount = chapters.count { it.subjectId == subj.id }
            val notCount = notes.count { it.subjectId == subj.id }
            SubjectWithStats(
                subject = subj,
                chapterCount = chapCount,
                noteCount = notCount
            )
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Selected Note Figures Flow
    val activeNoteFigures: StateFlow<List<NoteFigureEntity>> = _selectedNoteEntity
        .flatMapLatest { note ->
            if (note == null) flowOf(emptyList())
            else noteFigureDao.getFiguresForNote(note.id)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Search Results Flow
    val searchResults: StateFlow<List<NoteSearchResult>> = combine(
        _searchQuery,
        allNotes,
        allSubjects,
        allChapters,
        allFigures
    ) { query, notes, subjects, chapters, figures ->
        if (query.isBlank()) {
            emptyList()
        } else {
            val q = query.trim().lowercase()
            val subjectMap = subjects.associateBy { it.id }
            val chapterMap = chapters.associateBy { it.id }

            notes.mapNotNull { note ->
                val subj = subjectMap[note.subjectId]
                val chap = chapterMap[note.chapterId]
                val subjName = subj?.name ?: "Unknown Subject"
                val chapName = chap?.name ?: "Unknown Chapter"
                val figCount = figures.count { it.noteId == note.id }

                val matchedField = when {
                    note.title.lowercase().contains(q) -> "Title: ${note.title}"
                    note.topic.lowercase().contains(q) -> "Topic: ${note.topic}"
                    subjName.lowercase().contains(q) -> "Subject: $subjName"
                    chapName.lowercase().contains(q) -> "Chapter: $chapName"
                    note.formulas.lowercase().contains(q) -> "Formula Match"
                    note.content.lowercase().contains(q) -> "Content Match"
                    note.importantPoints.lowercase().contains(q) -> "Exam Points Match"
                    else -> null
                }

                if (matchedField != null) {
                    NoteSearchResult(
                        note = note,
                        subjectName = subjName,
                        chapterName = chapName,
                        figureCount = figCount,
                        matchedField = matchedField
                    )
                } else null
            }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        purgeDemoDataAndStartClean()
    }

    private fun purgeDemoDataAndStartClean() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val context = getApplication<Application>()
                val prefs = context.getSharedPreferences("app_clean_reset_prefs", Context.MODE_PRIVATE)
                val isCleaned = prefs.getBoolean("clean_v5_executed", false)
                if (!isCleaned) {
                    // Purge any pre-existing fake notes, fake chapters, fake lectures
                    noteDao.clearAllNotes()
                    noteFigureDao.clearAllFigures()
                    lectureDao.clearAllLectures()
                    chapterDao.clearAllChapters()
                    subjectDao.clearAllSubjects()

                    prefs.edit().putBoolean("clean_v5_executed", true).apply()
                }
            } catch (e: Exception) {
                Log.e("AppViewModel", "Purge demo data error", e)
            }
        }
    }

    fun navigateTo(screen: AppScreen) {
        _currentScreen.value = screen
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    // ================= Subject, Chapter & Note Navigation =================

    fun openSubject(subject: SubjectEntity) {
        _selectedSubjectEntity.value = subject
        _selectedSubject.value = subject.name
        _currentScreen.value = AppScreen.SUBJECT_DETAIL
    }

    fun openChapter(chapter: ChapterEntity) {
        _selectedChapterEntity.value = chapter
    }

    fun openNoteEditor(note: NoteEntity?, chapter: ChapterEntity, subject: SubjectEntity) {
        _selectedSubjectEntity.value = subject
        _selectedChapterEntity.value = chapter
        _selectedNoteEntity.value = note
        _currentScreen.value = AppScreen.NOTE_EDITOR
    }

    fun openPdfPreview(pdfFile: File, noteTitle: String, subjectName: String) {
        _activePdfPreviewFile.value = pdfFile
        _activePdfNoteTitle.value = noteTitle
        _activePdfSubjectName.value = subjectName
        _currentScreen.value = AppScreen.PDF_PREVIEW
    }

    fun openNoteEditor(note: NoteEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            val subj = subjectDao.getSubjectById(note.subjectId)
            val chap = chapterDao.getChapterById(note.chapterId)
            withContext(Dispatchers.Main) {
                if (subj != null && chap != null) {
                    openNoteEditor(note, chap, subj)
                }
            }
        }
    }

    fun openNewNoteEditor(chapter: ChapterEntity, subject: SubjectEntity) {
        openNoteEditor(null, chapter, subject)
    }

    fun updateSubject(subject: SubjectEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            subjectDao.updateSubject(subject)
            if (_selectedSubjectEntity.value?.id == subject.id) {
                _selectedSubjectEntity.value = subject
                _selectedSubject.value = subject.name
            }
        }
    }

    fun deleteSubject(subject: SubjectEntity) {
        deleteSubject(subject.id)
    }

    fun updateChapter(chapter: ChapterEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            chapterDao.updateChapter(chapter)
            if (_selectedChapterEntity.value?.id == chapter.id) {
                _selectedChapterEntity.value = chapter
            }
        }
    }

    fun deleteChapter(chapter: ChapterEntity) {
        deleteChapter(chapter.id, chapter.subjectId)
    }

    fun updateNote(note: NoteEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            noteDao.insertNote(note)
            if (_selectedNoteEntity.value?.id == note.id) {
                _selectedNoteEntity.value = note
            }
        }
    }

    fun deleteNote(note: NoteEntity) {
        deleteNote(note.id, note.subjectId, note.chapterId)
    }

    fun toggleNoteBookmark(noteId: String) {
        toggleBookmarkNote(noteId)
    }

    fun generateAndOpenPdf(note: NoteEntity, subject: SubjectEntity, chapterName: String, context: Context) {
        viewModelScope.launch(Dispatchers.IO) {
            val figures = noteFigureDao.getFiguresListForNote(note.id)
            generateAndPreviewNotePdf(note, subject, chapterName, figures, context)
        }
    }

    fun shareNotePdf(context: Context, pdfFile: File, subjectName: String, noteTitle: String) {
        sharePdf(context, pdfFile, subjectName, noteTitle)
    }

    fun openNotePdfExternally(context: Context, pdfFile: File) {
        try {
            val uri = androidx.core.content.FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                pdfFile
            )
            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, "application/pdf")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(context, "Could not open PDF: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    // ================= Subject CRUD =================

    fun addNewSubject(name: String, code: String, semester: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val emptySyllabus = listOf(
                SyllabusUnit(
                    unitNumber = 1,
                    unitTitle = "Unit 1: Fundamentals",
                    weightageMarks = 20,
                    topics = listOf(
                        SyllabusTopic("custom_1", "Basic Principles & Governing Equations", "Introduction", false, false, "Pending")
                    )
                )
            )
            val newSubj = SubjectEntity(
                id = "subj_${UUID.randomUUID()}",
                name = name.trim(),
                code = code.trim(),
                semester = semester.trim(),
                syllabusJson = JsonUtils.syllabusListToJson(emptySyllabus),
                createdAt = System.currentTimeMillis()
            )
            subjectDao.insertSubject(newSubj)

            // Also create a default Chapter 1
            val defChapter = ChapterEntity(
                id = "chap_${UUID.randomUUID()}",
                subjectId = newSubj.id,
                name = "Chapter 1 – Introduction & Fundamentals",
                chapterNumber = 1,
                createdAt = System.currentTimeMillis()
            )
            chapterDao.insertChapter(defChapter)

            withContext(Dispatchers.Main) {
                _selectedSubjectEntity.value = newSubj
                _selectedSubject.value = newSubj.name
            }
        }
    }

    fun updateSubject(subjectId: String, name: String, code: String, semester: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val existing = subjectDao.getSubjectById(subjectId) ?: return@launch
            val updated = existing.copy(
                name = name.trim(),
                code = code.trim(),
                semester = semester.trim()
            )
            subjectDao.updateSubject(updated)
            if (_selectedSubjectEntity.value?.id == subjectId) {
                _selectedSubjectEntity.value = updated
                _selectedSubject.value = updated.name
            }
        }
    }

    fun deleteSubject(subjectId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            // Delete files on disk
            NoteStorageHelper.deleteSubjectDirectory(getApplication(), subjectId)
            // Delete DB records
            noteFigureDao.deleteFiguresBySubject(subjectId)
            noteDao.deleteNotesBySubject(subjectId)
            chapterDao.deleteChaptersBySubject(subjectId)
            subjectDao.deleteSubjectById(subjectId)

            withContext(Dispatchers.Main) {
                if (_selectedSubjectEntity.value?.id == subjectId) {
                    _selectedSubjectEntity.value = null
                    _currentScreen.value = AppScreen.HOME
                }
            }
        }
    }

    // ================= Chapter CRUD =================

    fun addNewChapter(subjectId: String, name: String, chapterNumber: Int = 1) {
        viewModelScope.launch(Dispatchers.IO) {
            val newChap = ChapterEntity(
                id = "chap_${UUID.randomUUID()}",
                subjectId = subjectId,
                name = name.trim(),
                chapterNumber = chapterNumber,
                createdAt = System.currentTimeMillis()
            )
            chapterDao.insertChapter(newChap)
        }
    }

    fun updateChapter(chapterId: String, name: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val existing = chapterDao.getChapterById(chapterId) ?: return@launch
            val updated = existing.copy(name = name.trim())
            chapterDao.updateChapter(updated)
            if (_selectedChapterEntity.value?.id == chapterId) {
                _selectedChapterEntity.value = updated
            }
        }
    }

    fun deleteChapter(chapterId: String, subjectId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            NoteStorageHelper.deleteChapterDirectory(getApplication(), subjectId, chapterId)
            noteFigureDao.deleteFiguresByChapter(chapterId)
            noteDao.deleteNotesByChapter(chapterId)
            chapterDao.deleteChapterById(chapterId)

            withContext(Dispatchers.Main) {
                if (_selectedChapterEntity.value?.id == chapterId) {
                    _selectedChapterEntity.value = null
                }
            }
        }
    }

    // ================= Note CRUD =================

    fun saveNote(
        noteId: String?,
        subjectId: String,
        chapterId: String,
        title: String,
        topic: String,
        content: String,
        formulas: String,
        importantPoints: String
    ): String {
        val id = noteId ?: "note_${UUID.randomUUID()}"
        viewModelScope.launch(Dispatchers.IO) {
            val existing = if (noteId != null) noteDao.getNoteById(noteId) else null
            val noteEntity = NoteEntity(
                id = id,
                subjectId = subjectId,
                chapterId = chapterId,
                title = title.trim().ifBlank { "Untitled Note" },
                topic = topic.trim(),
                content = content.trim(),
                formulas = formulas.trim(),
                importantPoints = importantPoints.trim(),
                isBookmarked = existing?.isBookmarked ?: false,
                createdAt = existing?.createdAt ?: System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis()
            )
            noteDao.insertNote(noteEntity)

            withContext(Dispatchers.Main) {
                _selectedNoteEntity.value = noteEntity
            }
        }
        return id
    }

    fun deleteNote(noteId: String, subjectId: String, chapterId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            NoteStorageHelper.deleteNoteDirectory(getApplication(), subjectId, chapterId, noteId)
            noteFigureDao.deleteFiguresByNote(noteId)
            noteDao.deleteNoteById(noteId)

            withContext(Dispatchers.Main) {
                if (_selectedNoteEntity.value?.id == noteId) {
                    _selectedNoteEntity.value = null
                    _currentScreen.value = AppScreen.SUBJECT_DETAIL
                }
            }
        }
    }

    fun toggleBookmarkNote(noteId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            noteDao.toggleBookmark(noteId)
            val updated = noteDao.getNoteById(noteId)
            if (_selectedNoteEntity.value?.id == noteId) {
                _selectedNoteEntity.value = updated
            }
        }
    }

    fun renameNote(noteId: String, newTitle: String) {
        viewModelScope.launch(Dispatchers.IO) {
            noteDao.renameNote(noteId, newTitle.trim())
            val updated = noteDao.getNoteById(noteId)
            if (_selectedNoteEntity.value?.id == noteId) {
                _selectedNoteEntity.value = updated
            }
        }
    }

    // ================= Figure / Image Management =================

    fun addFigureFromBitmap(
        bitmap: Bitmap,
        caption: String,
        noteId: String,
        chapterId: String,
        subjectId: String
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            val figureId = UUID.randomUUID().toString()
            val path = NoteStorageHelper.saveBitmapToNoteFigures(
                context = getApplication(),
                bitmap = bitmap,
                subjectId = subjectId,
                chapterId = chapterId,
                noteId = noteId,
                figureId = figureId
            )
            val currentFigs = noteFigureDao.getFiguresListForNote(noteId)
            val newFig = NoteFigureEntity(
                id = figureId,
                noteId = noteId,
                chapterId = chapterId,
                subjectId = subjectId,
                imagePath = path,
                caption = caption.trim(),
                rotationDegrees = 0,
                orderIndex = currentFigs.size,
                createdAt = System.currentTimeMillis()
            )
            noteFigureDao.insertFigure(newFig)
        }
    }

    fun addFigureFromUri(
        uri: Uri,
        caption: String,
        noteId: String,
        chapterId: String,
        subjectId: String
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            val figureId = UUID.randomUUID().toString()
            val path = NoteStorageHelper.saveUriToNoteFigures(
                context = getApplication(),
                sourceUri = uri,
                subjectId = subjectId,
                chapterId = chapterId,
                noteId = noteId,
                figureId = figureId
            ) ?: return@launch

            val currentFigs = noteFigureDao.getFiguresListForNote(noteId)
            val newFig = NoteFigureEntity(
                id = figureId,
                noteId = noteId,
                chapterId = chapterId,
                subjectId = subjectId,
                imagePath = path,
                caption = caption.trim(),
                rotationDegrees = 0,
                orderIndex = currentFigs.size,
                createdAt = System.currentTimeMillis()
            )
            noteFigureDao.insertFigure(newFig)
        }
    }

    fun rotateFigure(figure: NoteFigureEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            val newRot = (figure.rotationDegrees + 90) % 360
            noteFigureDao.updateFigureRotation(figure.id, newRot)
        }
    }

    fun updateFigureCaption(figureId: String, caption: String) {
        viewModelScope.launch(Dispatchers.IO) {
            noteFigureDao.updateFigureCaption(figureId, caption.trim())
        }
    }

    fun moveFigureOrder(figure: NoteFigureEntity, moveUp: Boolean, noteId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val figures = noteFigureDao.getFiguresListForNote(noteId).toMutableList()
            val index = figures.indexOfFirst { it.id == figure.id }
            if (index == -1) return@launch
            val targetIndex = if (moveUp) index - 1 else index + 1
            if (targetIndex in figures.indices) {
                val temp = figures[index]
                figures[index] = figures[targetIndex]
                figures[targetIndex] = temp
                figures.forEachIndexed { i, f ->
                    noteFigureDao.updateFigureOrder(f.id, i)
                }
            }
        }
    }

    fun deleteFigure(figure: NoteFigureEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val file = File(figure.imagePath)
                if (file.exists()) file.delete()
            } catch (e: Exception) {
                Log.e("AppViewModel", "Error deleting figure file", e)
            }
            noteFigureDao.deleteFigureById(figure.id)
        }
    }

    // ================= PDF Generation & Share =================

    fun generateAndPreviewNotePdf(
        note: NoteEntity,
        subject: SubjectEntity,
        chapterName: String,
        figures: List<NoteFigureEntity>,
        context: Context
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            val pdfFile = NoteStorageHelper.generateNotePdf(
                context = context,
                subject = subject,
                chapterName = chapterName,
                note = note,
                figures = figures
            )

            withContext(Dispatchers.Main) {
                if (pdfFile != null && pdfFile.exists()) {
                    openPdfPreview(pdfFile, note.title, subject.name)
                } else {
                    Toast.makeText(context, "Failed to generate PDF", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    fun sharePdf(context: Context, pdfFile: File, subjectName: String, noteTitle: String) {
        NoteStorageHelper.sharePdf(context, pdfFile, subjectName, noteTitle)
    }

    fun shareNoteText(context: Context, note: NoteEntity, subjectName: String, chapterName: String) {
        NoteStorageHelper.shareNoteText(context, note, subjectName, chapterName)
    }

    // ================= Lecture Selection & Management =================

    fun selectLecture(lecture: LectureEntity) {
        _selectedLecture.value = lecture
        _selectedSubject.value = lecture.subject
        _currentQuizIndex.value = 0
        _selectedMcqAnswer.value = null
        _quizScore.value = 0
        _chatMessages.value = listOf(
            ChatMessage(
                sender = "ai",
                message = "Hello! I am your AI Exam Tutor for '${lecture.title}'. Ask me to explain any topic, provide 2/3/5/7-mark answers, clarify engineering diagrams, or generate quick test questions!"
            )
        )
    }

    fun selectSubject(subjectName: String) {
        _selectedSubject.value = subjectName
    }

    fun toggleExamPrepMode() {
        _isExamPrepMode.value = !_isExamPrepMode.value
    }

    // ================= AI Lecture to Exam Notes Pipeline =================

    fun processLectureAndGenerateNotes(
        title: String,
        subject: String,
        unitName: String,
        rawNotesOrTranscript: String = "",
        spokenLanguage: String = "English",
        audioPath: String = "",
        durationSeconds: Int = 1200,
        onComplete: (LectureEntity) -> Unit = {}
    ) {
        viewModelScope.launch {
            _isGeneratingAiNotes.value = true
            _generationStatusMessage.value = "1/5: Transcribing & filtering noise in $spokenLanguage..."
            kotlinx.coroutines.delay(600)

            _generationStatusMessage.value = "2/5: Structuring into 2, 3, 5 & 7-mark exam answers..."
            kotlinx.coroutines.delay(600)

            _generationStatusMessage.value = "3/5: Rendering engineering diagrams & formulas..."
            kotlinx.coroutines.delay(500)

            _generationStatusMessage.value = "4/5: Running Technical Accuracy & Formula validation..."
            kotlinx.coroutines.delay(500)

            _generationStatusMessage.value = "5/5: Generating revision MCQs, flashcards and viva sheet..."

            try {
                val newLecture = geminiService.generateExamNotesFromLecture(
                    title = title.ifBlank { "Lecture on $subject" },
                    subject = subject,
                    unitName = unitName.ifBlank { "Unit 1: Fundamentals" },
                    rawTranscriptOrAudioNote = rawNotesOrTranscript.ifBlank { "Classroom lecture on $subject core principles" },
                    spokenLanguage = spokenLanguage,
                    audioPath = audioPath,
                    durationSec = durationSeconds
                )

                lectureDao.insertLecture(newLecture)
                synchronizeLectureToNotes(newLecture)
                _selectedLecture.value = newLecture
                _isGeneratingAiNotes.value = false
                _generationStatusMessage.value = ""
                onComplete(newLecture)
                _currentScreen.value = AppScreen.NOTES_DETAIL
            } catch (e: Exception) {
                Log.e("AppViewModel", "Failed to generate exam notes", e)
                _isGeneratingAiNotes.value = false
                _generationStatusMessage.value = ""
                Toast.makeText(getApplication(), "Note generation: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun processVideoLectureAndGenerateNotes(
        title: String,
        subject: String,
        unitName: String,
        rawNotesOrTranscript: String,
        spokenLanguage: String,
        videoPath: String,
        videoQuality: String,
        boardKeyFrames: List<BoardKeyFrame>,
        videoFileSizeMb: Double,
        durationSeconds: Int,
        onComplete: (LectureEntity) -> Unit = {}
    ) {
        viewModelScope.launch {
            _isGeneratingAiNotes.value = true
            _generationStatusMessage.value = "1/6: Extracting audio stream & reducing fan/murmur noise..."
            kotlinx.coroutines.delay(600)

            _generationStatusMessage.value = "2/6: Extracting key-frames & OCR text from blackboard/PPT..."
            kotlinx.coroutines.delay(600)

            _generationStatusMessage.value = "3/6: Filtering non-faculty chatter & transcribing in $spokenLanguage..."
            kotlinx.coroutines.delay(600)

            _generationStatusMessage.value = "4/6: Structuring 2, 3, 5 & 7-mark answers + timestamp links..."
            kotlinx.coroutines.delay(500)

            _generationStatusMessage.value = "5/6: Validating formulas against engineering syllabus..."
            kotlinx.coroutines.delay(500)

            _generationStatusMessage.value = "6/6: Generating flashcards, viva questions & exam notes..."

            try {
                val newLecture = geminiService.generateExamNotesFromVideoLecture(
                    title = title.ifBlank { "Video Lecture on $subject" },
                    subject = subject,
                    unitName = unitName.ifBlank { "Unit 1: Fundamentals" },
                    rawTranscriptOrAudioNote = rawNotesOrTranscript.ifBlank { "Classroom whiteboard lecture with derivations for $subject" },
                    spokenLanguage = spokenLanguage,
                    videoPath = videoPath,
                    videoQuality = videoQuality,
                    boardKeyFrames = boardKeyFrames,
                    videoFileSizeMb = videoFileSizeMb,
                    durationSec = durationSeconds
                )

                lectureDao.insertLecture(newLecture)
                synchronizeLectureToNotes(newLecture)
                _selectedLecture.value = newLecture
                _isGeneratingAiNotes.value = false
                _generationStatusMessage.value = ""
                onComplete(newLecture)
                _currentScreen.value = AppScreen.NOTES_DETAIL
            } catch (e: Exception) {
                Log.e("AppViewModel", "Failed to generate video exam notes", e)
                _isGeneratingAiNotes.value = false
                _generationStatusMessage.value = ""
                Toast.makeText(getApplication(), "Video note generation: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private suspend fun synchronizeLectureToNotes(lecture: LectureEntity) {
        withContext(Dispatchers.IO) {
            try {
                // Ensure Subject exists
                var subject = subjectDao.getSubjectByName(lecture.subject)
                if (subject == null) {
                    val newSubject = SubjectEntity(
                        id = "subj_" + UUID.randomUUID().toString().take(8),
                        name = lecture.subject.ifBlank { "General Engineering" },
                        code = "ENG-101",
                        semester = "Semester 1",
                        syllabusJson = "[]",
                        createdAt = System.currentTimeMillis()
                    )
                    subjectDao.insertSubject(newSubject)
                    subject = newSubject
                }

                // Ensure Chapter exists
                val chapters = chapterDao.getChaptersListForSubject(subject.id)
                var chapter = chapters.find { it.name.equals(lecture.unitName, ignoreCase = true) }
                if (chapter == null) {
                    val newChapter = ChapterEntity(
                        id = "chap_" + UUID.randomUUID().toString().take(8),
                        subjectId = subject.id,
                        name = lecture.unitName.ifBlank { "Chapter 1 – Lecture Concepts" },
                        chapterNumber = chapters.size + 1,
                        createdAt = System.currentTimeMillis()
                    )
                    chapterDao.insertChapter(newChapter)
                    chapter = newChapter
                }

                // Parse topics from lecture
                val topics = JsonUtils.topicListFromJson(lecture.topicsJson)
                val contentBuilder = StringBuilder()
                val formulaBuilder = StringBuilder()
                val importantPointsBuilder = StringBuilder()

                topics.forEach { t ->
                    val mins = t.timestampSeconds / 60
                    val secs = t.timestampSeconds % 60
                    val timeStr = String.format(java.util.Locale.getDefault(), "[%02d:%02d]", mins, secs)

                    contentBuilder.append("## ${t.topicName} $timeStr\n\n")
                    contentBuilder.append("**Definition:** ${t.definition}\n\n")
                    contentBuilder.append("${t.simpleExplanation}\n\n")
                    if (t.keyPoints.isNotEmpty()) {
                        contentBuilder.append("**Key Concepts:**\n")
                        t.keyPoints.forEach { pt -> contentBuilder.append("• $pt\n") }
                        contentBuilder.append("\n")
                    }
                    if (t.workingProcess.isNotBlank()) {
                        contentBuilder.append("**Working Process:**\n${t.workingProcess}\n\n")
                    }

                    if (t.formula.isNotBlank()) {
                        formulaBuilder.append("• ${t.topicName}: ${t.formula}\n")
                        t.variablesAndUnits.forEach { v -> formulaBuilder.append("   - $v\n") }
                    }

                    if (t.importantExamPoints.isNotEmpty()) {
                        t.importantExamPoints.forEach { pt -> importantPointsBuilder.append("• $pt\n") }
                    }
                }

                val noteEntity = NoteEntity(
                    id = "note_lec_${lecture.id}",
                    subjectId = subject.id,
                    chapterId = chapter.id,
                    title = lecture.title,
                    topic = lecture.unitName,
                    content = contentBuilder.toString().ifBlank { lecture.summary },
                    formulas = formulaBuilder.toString(),
                    importantPoints = importantPointsBuilder.toString(),
                    isBookmarked = false,
                    createdAt = lecture.dateEpoch,
                    updatedAt = System.currentTimeMillis()
                )
                noteDao.insertNote(noteEntity)
            } catch (e: Exception) {
                Log.e("AppViewModel", "Failed to sync lecture to note", e)
            }
        }
    }

    // ================= Delete Operations =================

    fun deleteLecture(id: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val lecture = lectureDao.getLectureById(id)
            if (lecture != null) {
                try {
                    if (lecture.audioFilePath.isNotBlank()) {
                        val f = File(lecture.audioFilePath)
                        if (f.exists()) f.delete()
                    }
                    if (lecture.videoFilePath != null && lecture.videoFilePath.isNotBlank()) {
                        val vf = File(lecture.videoFilePath)
                        if (vf.exists()) vf.delete()
                    }
                } catch (e: Exception) {
                    Log.e("AppViewModel", "File cleanup error", e)
                }
            }
            lectureDao.deleteLectureById(id)
            withContext(Dispatchers.Main) {
                if (_selectedLecture.value?.id == id) {
                    _selectedLecture.value = null
                    _currentScreen.value = AppScreen.HOME
                }
            }
        }
    }

    fun deleteLectureCompletely(lecture: LectureEntity) {
        deleteLecture(lecture.id)
    }

    fun deleteVideoOnly(lectureId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            lectureDao.deleteVideoOnly(lectureId)
            val updated = lectureDao.getLectureById(lectureId)
            withContext(Dispatchers.Main) {
                _selectedLecture.value = updated
            }
        }
    }

    fun deleteAudioOnly(lectureId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            lectureDao.deleteAudioOnly(lectureId)
            val updated = lectureDao.getLectureById(lectureId)
            withContext(Dispatchers.Main) {
                _selectedLecture.value = updated
            }
        }
    }

    fun deleteNotesOnly(lectureId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            lectureDao.deleteNotesOnly(lectureId)
            val updated = lectureDao.getLectureById(lectureId)
            withContext(Dispatchers.Main) {
                _selectedLecture.value = updated
            }
        }
    }

    fun renameLecture(lectureId: String, newTitle: String) {
        viewModelScope.launch(Dispatchers.IO) {
            lectureDao.renameLecture(lectureId, newTitle)
            val updated = lectureDao.getLectureById(lectureId)
            withContext(Dispatchers.Main) {
                _selectedLecture.value = updated
            }
        }
    }

    fun seekToVideoTimestamp(seconds: Int) {
        _videoSeekTargetSeconds.value = seconds
    }

    fun setVideoSeekTarget(seconds: Int) {
        _videoSeekTargetSeconds.value = seconds
    }

    fun clearVideoSeekTarget() {
        _videoSeekTargetSeconds.value = null
    }

    // ================= AI Tutor Chat =================

    fun sendAiChatMessage(query: String) {
        if (query.isBlank()) return

        val userMsg = ChatMessage(sender = "user", message = query)
        _chatMessages.value = _chatMessages.value + userMsg
        _isChatAiThinking.value = true

        viewModelScope.launch {
            val lectureContext = _selectedLecture.value?.let { lec ->
                "Lecture Title: ${lec.title}\nSubject: ${lec.subject}\nUnit: ${lec.unitName}\nSummary: ${lec.summary}\nClean Transcript: ${lec.cleanTranscript}"
            } ?: "General Engineering Subject: ${_selectedSubject.value}"

            val aiResponseText = geminiService.askAiChat(
                query = query,
                lectureContext = lectureContext,
                subject = _selectedSubject.value
            )

            val aiMsg = ChatMessage(sender = "ai", message = aiResponseText)
            _chatMessages.value = _chatMessages.value + aiMsg
            _isChatAiThinking.value = false
        }
    }

    fun sendChatMessage(userQuery: String) {
        sendAiChatMessage(userQuery)
    }

    // ================= Revision Quiz State =================

    fun submitMcqAnswer(answerIndex: Int, isCorrect: Boolean) {
        _selectedMcqAnswer.value = answerIndex
        if (isCorrect) {
            _quizScore.value += 1
        }
    }

    fun selectMcqOption(optionIndex: Int, correctIndex: Int) {
        submitMcqAnswer(optionIndex, optionIndex == correctIndex)
    }

    fun nextQuizQuestion(totalQuestions: Int) {
        if (_currentQuizIndex.value < totalQuestions - 1) {
            _currentQuizIndex.value += 1
            _selectedMcqAnswer.value = null
        }
    }

    fun resetQuiz() {
        _currentQuizIndex.value = 0
        _selectedMcqAnswer.value = null
        _quizScore.value = 0
    }

    // ================= Syllabus Progress =================

    fun toggleTopicCompletion(subjectId: String, topicId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val subjects = allSubjects.value
            val targetSubject = subjects.find { it.id == subjectId || it.name == _selectedSubject.value } ?: return@launch
            val units = JsonUtils.syllabusListFromJson(targetSubject.syllabusJson).toMutableList()

            val updatedUnits = units.map { unit ->
                val updatedTopics = unit.topics.map { topic ->
                    if (topic.id == topicId) {
                        topic.copy(
                            isCompleted = !topic.isCompleted,
                            revisionStatus = if (!topic.isCompleted) "Mastered" else "Pending"
                        )
                    } else {
                        topic
                    }
                }
                unit.copy(topics = updatedTopics)
            }

            val updatedSubject = targetSubject.copy(syllabusJson = JsonUtils.syllabusListToJson(updatedUnits))
            subjectDao.updateSubject(updatedSubject)
        }
    }

    // ================= Notes Export to Text / PDF =================

    fun exportNotes(context: Context, lecture: LectureEntity) {
        val topics = JsonUtils.topicListFromJson(lecture.topicsJson)
        val sb = StringBuilder()
        sb.append("====================================================\n")
        sb.append("      AI EXAM-READY STUDY NOTES (ENGINEERING)       \n")
        sb.append("====================================================\n\n")
        sb.append("Subject: ${lecture.subject}\n")
        sb.append("Unit: ${lecture.unitName}\n")
        sb.append("Lecture: ${lecture.title}\n")
        sb.append("Generated On: ${java.text.SimpleDateFormat("dd MMM yyyy, hh:mm a", java.util.Locale.getDefault()).format(java.util.Date(lecture.dateEpoch))}\n")
        sb.append("Quality Control: 100% Verified Technical Accuracy & Formulas\n\n")
        sb.append("----------------------------------------------------\n")
        sb.append("SUMMARY:\n${lecture.summary}\n")
        sb.append("----------------------------------------------------\n\n")

        topics.forEachIndexed { i, topic ->
            sb.append("${i + 1}. TOPIC: ${topic.topicName.uppercase()}\n\n")
            sb.append("• DEFINITION:\n  ${topic.definition}\n\n")
            sb.append("• SIMPLE EXPLANATION:\n  ${topic.simpleExplanation}\n\n")
            sb.append("• KEY POINTS:\n")
            topic.keyPoints.forEach { sb.append("  - $it\n") }
            sb.append("\n")
            sb.append("• WORKING PROCESS:\n  ${topic.workingProcess}\n\n")
            if (topic.formula.isNotBlank()) {
                sb.append("• FORMULA:\n  ${topic.formula}\n\n")
                sb.append("• VARIABLES & SI UNITS:\n")
                topic.variablesAndUnits.forEach { sb.append("  - $it\n") }
                sb.append("\n")
            }
            if (topic.exampleProblem.isNotBlank()) {
                sb.append("• PRACTICAL EXAMPLE / CALCULATION:\n  ${topic.exampleProblem}\n\n")
            }
            if (topic.advantages.isNotEmpty()) {
                sb.append("• ADVANTAGES:\n")
                topic.advantages.forEach { sb.append("  - $it\n") }
                sb.append("\n")
            }
            if (topic.disadvantages.isNotEmpty()) {
                sb.append("• DISADVANTAGES:\n")
                topic.disadvantages.forEach { sb.append("  - $it\n") }
                sb.append("\n")
            }
            if (topic.applications.isNotEmpty()) {
                sb.append("• PRACTICAL APPLICATIONS:\n")
                topic.applications.forEach { sb.append("  - $it\n") }
                sb.append("\n")
            }
            if (topic.importantExamPoints.isNotEmpty()) {
                sb.append("• HIGH-YIELD EXAM TIPS:\n")
                topic.importantExamPoints.forEach { sb.append("  🔥 $it\n") }
                sb.append("\n")
            }
            sb.append("----------------------------------------------------\n")
            sb.append("MODEL EXAM ANSWERS (UNIVERSITY FORMAT):\n")
            topic.answers.forEach { ans ->
                sb.append("[ ${ans.marks}-MARK ANSWER: ${ans.title} ]\n")
                sb.append("${ans.answerText}\n\n")
            }
            sb.append("====================================================\n\n")
        }

        val shareIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_SUBJECT, "Exam Notes: ${lecture.title}")
            putExtra(Intent.EXTRA_TEXT, sb.toString())
            type = "text/plain"
        }
        val chooser = Intent.createChooser(shareIntent, "Export & Share Engineering Notes")
        chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(chooser)
    }

    override fun onCleared() {
        super.onCleared()
        audioRecorder.reset()
        audioPlayer.stop()
    }
}
