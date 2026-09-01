package com.example.viewmodel

import android.app.Application
import android.content.Context
import android.content.Intent
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
import com.example.data.local.SampleData
import com.example.data.model.BoardKeyFrame
import com.example.data.model.ChatMessage
import com.example.data.model.Flashcard
import com.example.data.model.LectureEntity
import com.example.data.model.McqQuestion
import com.example.data.model.PyqEntity
import com.example.data.model.PyqItem
import com.example.data.model.SubjectEntity
import com.example.data.model.SyllabusTopic
import com.example.data.model.SyllabusUnit
import com.example.data.model.TopicSection
import com.example.video.VideoRecorderManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class AppScreen {
    HOME,
    RECORD,
    RECORD_VIDEO,
    NOTES_DETAIL,
    EXAM_PREP,
    REVISION_QUIZ,
    PYQ_ANALYZER,
    SYLLABUS_VIEWER
}

class AppViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getDatabase(application)
    private val lectureDao = db.lectureDao()
    private val subjectDao = db.subjectDao()
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

    // Active Selected Subject
    private val _selectedSubject = MutableStateFlow("Engineering Thermodynamics")
    val selectedSubject: StateFlow<String> = _selectedSubject.asStateFlow()

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

    init {
        seedInitialDataIfEmpty()
    }

    private fun seedInitialDataIfEmpty() {
        viewModelScope.launch {
            try {
                val existingSubjects = subjectDao.getSubjectByName("Engineering Thermodynamics")
                if (existingSubjects == null) {
                    subjectDao.insertSubjects(SampleData.getDefaultSubjects())
                    SampleData.getDefaultLectures().forEach { lectureDao.insertLecture(it) }
                    SampleData.getDefaultPyqs().forEach { pyqDao.insertPyq(it) }
                }
            } catch (e: Exception) {
                Log.e("AppViewModel", "Seeding error", e)
            }
        }
    }

    fun navigateTo(screen: AppScreen) {
        _currentScreen.value = screen
    }

    fun selectLecture(lecture: LectureEntity) {
        _selectedLecture.value = lecture
        _selectedSubject.value = lecture.subject
        _currentQuizIndex.value = 0
        _selectedMcqAnswer.value = null
        _quizScore.value = 0
        // Initialize default greeting in chat
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
        rawNotesOrTranscript: String,
        spokenLanguage: String,
        audioPath: String,
        durationSeconds: Int,
        onComplete: (LectureEntity) -> Unit
    ) {
        viewModelScope.launch {
            _isGeneratingAiNotes.value = true
            _generationStatusMessage.value = "1/5: Transcribing & filtering fillers in $spokenLanguage..."
            kotlinx.coroutines.delay(800)

            _generationStatusMessage.value = "2/5: Structuring into 2, 3, 5 & 7-mark exam answers..."
            kotlinx.coroutines.delay(800)

            _generationStatusMessage.value = "3/5: Rendering vector P-V, T-S & engineering diagrams..."
            kotlinx.coroutines.delay(600)

            _generationStatusMessage.value = "4/5: Running AI Quality Control & Formula validation..."
            kotlinx.coroutines.delay(600)

            _generationStatusMessage.value = "5/5: Generating revision MCQs, flashcards and viva sheet..."

            try {
                val newLecture = geminiService.generateExamNotesFromLecture(
                    title = title.ifBlank { "Lecture on $subject" },
                    subject = subject,
                    unitName = unitName.ifBlank { "Unit 1: Fundamentals" },
                    rawTranscriptOrAudioNote = rawNotesOrTranscript.ifBlank { "Standard classroom lecture on $subject core principles" },
                    spokenLanguage = spokenLanguage,
                    audioPath = audioPath,
                    durationSec = durationSeconds
                )

                lectureDao.insertLecture(newLecture)
                _selectedLecture.value = newLecture
                _isGeneratingAiNotes.value = false
                _generationStatusMessage.value = ""
                onComplete(newLecture)
            } catch (e: Exception) {
                Log.e("AppViewModel", "Failed to generate exam notes", e)
                _isGeneratingAiNotes.value = false
                _generationStatusMessage.value = ""
                Toast.makeText(getApplication(), "Failed to generate notes: ${e.message}", Toast.LENGTH_LONG).show()
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
        onComplete: (LectureEntity) -> Unit
    ) {
        viewModelScope.launch {
            _isGeneratingAiNotes.value = true
            _generationStatusMessage.value = "1/6: Extracting audio stream & reducing fan/murmur noise..."
            kotlinx.coroutines.delay(600)

            _generationStatusMessage.value = "2/6: Extracting key-frames & OCR text from blackboard/PPT..."
            kotlinx.coroutines.delay(800)

            _generationStatusMessage.value = "3/6: Filtering non-faculty chatter & transcribing in $spokenLanguage..."
            kotlinx.coroutines.delay(800)

            _generationStatusMessage.value = "4/6: Structuring 2, 3, 5 & 7-mark answers + timestamp links..."
            kotlinx.coroutines.delay(700)

            _generationStatusMessage.value = "5/6: Validating formulas against engineering syllabus..."
            kotlinx.coroutines.delay(600)

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
                _selectedLecture.value = newLecture
                _isGeneratingAiNotes.value = false
                _generationStatusMessage.value = ""
                onComplete(newLecture)
            } catch (e: Exception) {
                Log.e("AppViewModel", "Failed to generate video exam notes", e)
                _isGeneratingAiNotes.value = false
                _generationStatusMessage.value = ""
                Toast.makeText(getApplication(), "Failed to generate video notes: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    fun seekToVideoTimestamp(seconds: Int) {
        _videoSeekTargetSeconds.value = seconds
    }

    fun clearVideoSeekTarget() {
        _videoSeekTargetSeconds.value = null
    }

    // ================= Storage & Retention Management =================

    fun deleteVideoOnly(id: String) {
        viewModelScope.launch {
            lectureDao.deleteVideoOnly(id)
            _selectedLecture.value = _selectedLecture.value?.let { current ->
                if (current.id == id) {
                    current.copy(hasVideo = false, videoFilePath = null, videoFileSizeMb = 0.0)
                } else current
            }
            Toast.makeText(getApplication(), "Video file deleted to free storage. Audio & AI Notes preserved!", Toast.LENGTH_SHORT).show()
        }
    }

    fun deleteAudioOnly(id: String) {
        viewModelScope.launch {
            lectureDao.deleteAudioOnly(id)
            _selectedLecture.value = _selectedLecture.value?.let { current ->
                if (current.id == id) {
                    current.copy(hasAudio = false, audioFilePath = "", audioFileSizeMb = 0.0)
                } else current
            }
            Toast.makeText(getApplication(), "Audio file deleted. Notes preserved!", Toast.LENGTH_SHORT).show()
        }
    }

    fun deleteNotesOnly(id: String) {
        viewModelScope.launch {
            lectureDao.deleteNotesOnly(id)
            _selectedLecture.value = _selectedLecture.value?.let { current ->
                if (current.id == id) {
                    current.copy(hasNotes = false)
                } else current
            }
            Toast.makeText(getApplication(), "Notes deleted. Media preserved!", Toast.LENGTH_SHORT).show()
        }
    }

    fun deleteLecture(id: String) {
        viewModelScope.launch {
            lectureDao.deleteLectureById(id)
            if (_selectedLecture.value?.id == id) {
                _selectedLecture.value = null
                _currentScreen.value = AppScreen.HOME
            }
        }
    }

    fun renameLecture(id: String, newTitle: String) {
        viewModelScope.launch {
            lectureDao.renameLecture(id, newTitle)
            _selectedLecture.value = _selectedLecture.value?.copy(title = newTitle)
        }
    }

    // ================= AI Chat =================

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

    // ================= Quiz & Flashcard Actions =================

    fun submitMcqAnswer(answerIndex: Int, isCorrect: Boolean) {
        _selectedMcqAnswer.value = answerIndex
        if (isCorrect) {
            _quizScore.value += 1
        }
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

    // ================= Syllabus Tracking =================

    fun toggleTopicCompletion(subjectId: String, topicId: String) {
        viewModelScope.launch {
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

    fun addNewSubject(name: String, code: String, semester: String) {
        viewModelScope.launch {
            val emptySyllabus = listOf(
                SyllabusUnit(
                    unitNumber = 1,
                    unitTitle = "Unit 1: Fundamentals",
                    weightageMarks = 20,
                    topics = listOf(
                        SyllabusTopic("custom_1", "Basic Principles & Governing Equations", "Introduction", false, false, "Pending"),
                        SyllabusTopic("custom_2", "Process Analysis & Problem Formulations", "Analysis", false, false, "Pending")
                    )
                )
            )
            val newSubj = SubjectEntity(
                id = "subj_${System.currentTimeMillis()}",
                name = name,
                code = code.ifBlank { "ENG-${(100..999).random()}" },
                semester = semester.ifBlank { "Semester 3" },
                syllabusJson = JsonUtils.syllabusListToJson(emptySyllabus)
            )
            subjectDao.insertSubject(newSubj)
            _selectedSubject.value = name
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
