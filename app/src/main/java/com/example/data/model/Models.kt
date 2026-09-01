package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.squareup.moshi.JsonClass

enum class DiagramType {
    PV_DIAGRAM_OTTO,
    PV_DIAGRAM_CARNOT,
    PV_DIAGRAM_DIESEL,
    TS_DIAGRAM_CYCLE,
    MOHR_CIRCLE,
    STRESS_STRAIN_CURVE,
    PROCESS_FLOW_CHART,
    HEAT_ENGINE_BLOCK,
    FOUR_STROKE_MECHANISM,
    GENERIC_GRAPH
}

@JsonClass(generateAdapter = true)
data class DiagramPoint(
    val x: Float,
    val y: Float,
    val label: String,
    val description: String = ""
)

@JsonClass(generateAdapter = true)
data class EngineeringDiagramData(
    val type: DiagramType = DiagramType.PV_DIAGRAM_OTTO,
    val title: String,
    val xAxisLabel: String = "Volume V (m³)",
    val yAxisLabel: String = "Pressure P (bar)",
    val points: List<DiagramPoint> = emptyList(),
    val processLabels: List<String> = emptyList(),
    val notes: String = "",
    val formula: String = ""
)

@JsonClass(generateAdapter = true)
data class MarkAnswer(
    val marks: Int, // 2, 3, 5, 7
    val title: String,
    val keyPoints: List<String>,
    val answerText: String,
    val diagramRequired: Boolean = false,
    val formulaRequired: Boolean = false
)

@JsonClass(generateAdapter = true)
data class TopicSection(
    val topicId: String,
    val topicName: String,
    val timestampSeconds: Int = 0, // for audio timestamp linking
    val definition: String,
    val simpleExplanation: String,
    val keyPoints: List<String>,
    val workingProcess: String,
    val formula: String = "",
    val variablesAndUnits: List<String> = emptyList(),
    val exampleProblem: String = "",
    val advantages: List<String> = emptyList(),
    val disadvantages: List<String> = emptyList(),
    val applications: List<String> = emptyList(),
    val importantExamPoints: List<String> = emptyList(),
    val answers: List<MarkAnswer> = emptyList(),
    val diagram: EngineeringDiagramData? = null
)

@JsonClass(generateAdapter = true)
data class QualityCheckReport(
    val isTechnicallyAccurate: Boolean = true,
    val formulasVerified: Boolean = true,
    val unitsStandardized: Boolean = true,
    val markAlignmentVerified: Boolean = true,
    val noHallucinationsDetected: Boolean = true,
    val sourcesSummary: String = "Verified from classroom lecture audio & engineering standard reference",
    val qualityScorePercent: Int = 98
)

@JsonClass(generateAdapter = true)
data class McqQuestion(
    val id: String,
    val question: String,
    val options: List<String>,
    val correctIndex: Int,
    val explanation: String,
    val topicName: String = "",
    val marksWeightage: Int = 1
)

@JsonClass(generateAdapter = true)
data class Flashcard(
    val id: String,
    val frontQuestion: String,
    val backAnswer: String,
    val topic: String,
    val formula: String = "",
    val isMastered: Boolean = false
)

@JsonClass(generateAdapter = true)
data class VivaQuestion(
    val question: String,
    val modelAnswer: String,
    val examinerTip: String
)

@JsonClass(generateAdapter = true)
data class PyqItem(
    val id: String,
    val question: String,
    val subject: String,
    val unit: String,
    val yearRepeated: List<String>, // e.g. ["Winter 2023", "Summer 2024", "Winter 2024"]
    val frequencyCount: Int,
    val marks: Int,
    val priorityTag: String, // "Very Important", "Important", "Useful for Revision"
    val solutionSummary: String
)

@JsonClass(generateAdapter = true)
data class SyllabusTopic(
    val id: String,
    val title: String,
    val chapter: String,
    val isCompleted: Boolean = false,
    val hasLectureCoverage: Boolean = false,
    val revisionStatus: String = "Pending" // "Pending", "Revised Once", "Mastered"
)

@JsonClass(generateAdapter = true)
data class SyllabusUnit(
    val unitNumber: Int,
    val unitTitle: String,
    val weightageMarks: Int,
    val topics: List<SyllabusTopic>
)

@JsonClass(generateAdapter = true)
data class ChatMessage(
    val id: String = java.util.UUID.randomUUID().toString(),
    val sender: String, // "user" or "ai"
    val message: String,
    val timestamp: Long = System.currentTimeMillis(),
    val quickAnswerMarks: Int? = null,
    val formulaCard: String? = null
)

@JsonClass(generateAdapter = true)
data class BoardKeyFrame(
    val id: String = java.util.UUID.randomUUID().toString(),
    val timestampSeconds: Int,
    val title: String,
    val visualType: String = "WHITEBOARD_WRITING", // "WHITEBOARD_WRITING", "BLACKBOARD_WRITING", "PPT_SLIDE", "ENGINEERING_DIAGRAM", "FORMULA_DERIVATION"
    val ocrExtractedContent: String,
    val figureDescription: String,
    val keyTakeaway: String,
    val imageUri: String? = null
)

@JsonClass(generateAdapter = true)
data class VideoQualityProfile(
    val id: String,
    val name: String,
    val resolution: String, // "720p", "1080p"
    val fps: Int = 30,
    val bitrateMbps: Float,
    val estimatedMbPerHour: Int,
    val description: String,
    val isRecommendedForLongLectures: Boolean = false
)

// Room Database Entities
@Entity(tableName = "lectures")
data class LectureEntity(
    @PrimaryKey val id: String,
    val title: String,
    val subject: String,
    val unitName: String,
    val dateEpoch: Long,
    val durationSeconds: Int,
    val audioFilePath: String,
    val originalTranscript: String,
    val cleanTranscript: String,
    val spokenLanguage: String, // "English", "Hindi", "Gujarati", "Mixed Hinglish/Gujlish"
    val summary: String,
    val topicsJson: String, // JSON serialized List<TopicSection>
    val qualityCheckJson: String, // JSON serialized QualityCheckReport
    val mcqsJson: String, // JSON serialized List<McqQuestion>
    val flashcardsJson: String, // JSON serialized List<Flashcard>
    val vivaJson: String, // JSON serialized List<VivaQuestion>
    val isExamPrepReady: Boolean = true,
    val videoFilePath: String? = null,
    val mediaType: String = "AUDIO", // "AUDIO" or "VIDEO"
    val videoQuality: String = "720p",
    val boardKeyFramesJson: String = "[]", // JSON serialized List<BoardKeyFrame>
    val videoFileSizeMb: Double = 0.0,
    val audioFileSizeMb: Double = 0.0,
    val hasVideo: Boolean = false,
    val hasAudio: Boolean = true,
    val hasNotes: Boolean = true
)

@Entity(tableName = "subjects")
data class SubjectEntity(
    @PrimaryKey val id: String,
    val name: String,
    val code: String,
    val semester: String,
    val syllabusJson: String // JSON serialized List<SyllabusUnit>
)

@Entity(tableName = "pyq_records")
data class PyqEntity(
    @PrimaryKey val id: String,
    val subjectName: String,
    val pyqListJson: String // JSON serialized List<PyqItem>
)
