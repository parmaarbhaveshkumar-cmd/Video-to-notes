package com.example.data.local

import com.example.data.model.BoardKeyFrame
import com.example.data.model.Flashcard
import com.example.data.model.McqQuestion
import com.example.data.model.PyqItem
import com.example.data.model.QualityCheckReport
import com.example.data.model.SyllabusUnit
import com.example.data.model.TopicSection
import com.example.data.model.VivaQuestion
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory

object JsonUtils {
    private val moshi: Moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

    private val topicListType = Types.newParameterizedType(List::class.java, TopicSection::class.java)
    private val mcqListType = Types.newParameterizedType(List::class.java, McqQuestion::class.java)
    private val flashcardListType = Types.newParameterizedType(List::class.java, Flashcard::class.java)
    private val vivaListType = Types.newParameterizedType(List::class.java, VivaQuestion::class.java)
    private val pyqListType = Types.newParameterizedType(List::class.java, PyqItem::class.java)
    private val syllabusListType = Types.newParameterizedType(List::class.java, SyllabusUnit::class.java)
    private val boardKeyFrameListType = Types.newParameterizedType(List::class.java, BoardKeyFrame::class.java)

    fun boardKeyFrameListToJson(frames: List<BoardKeyFrame>): String {
        return moshi.adapter<List<BoardKeyFrame>>(boardKeyFrameListType).toJson(frames)
    }

    fun boardKeyFrameListFromJson(json: String): List<BoardKeyFrame> {
        return try {
            moshi.adapter<List<BoardKeyFrame>>(boardKeyFrameListType).fromJson(json) ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun topicListToJson(topics: List<TopicSection>): String {
        return moshi.adapter<List<TopicSection>>(topicListType).toJson(topics)
    }

    fun topicListFromJson(json: String): List<TopicSection> {
        return try {
            moshi.adapter<List<TopicSection>>(topicListType).fromJson(json) ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun qualityReportToJson(report: QualityCheckReport): String {
        return moshi.adapter(QualityCheckReport::class.java).toJson(report)
    }

    fun qualityReportFromJson(json: String): QualityCheckReport {
        return try {
            moshi.adapter(QualityCheckReport::class.java).fromJson(json) ?: QualityCheckReport()
        } catch (e: Exception) {
            QualityCheckReport()
        }
    }

    fun mcqListToJson(mcqs: List<McqQuestion>): String {
        return moshi.adapter<List<McqQuestion>>(mcqListType).toJson(mcqs)
    }

    fun mcqListFromJson(json: String): List<McqQuestion> {
        return try {
            moshi.adapter<List<McqQuestion>>(mcqListType).fromJson(json) ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun flashcardListToJson(cards: List<Flashcard>): String {
        return moshi.adapter<List<Flashcard>>(flashcardListType).toJson(cards)
    }

    fun flashcardListFromJson(json: String): List<Flashcard> {
        return try {
            moshi.adapter<List<Flashcard>>(flashcardListType).fromJson(json) ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun vivaListToJson(viva: List<VivaQuestion>): String {
        return moshi.adapter<List<VivaQuestion>>(vivaListType).toJson(viva)
    }

    fun vivaListFromJson(json: String): List<VivaQuestion> {
        return try {
            moshi.adapter<List<VivaQuestion>>(vivaListType).fromJson(json) ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun pyqListToJson(pyqs: List<PyqItem>): String {
        return moshi.adapter<List<PyqItem>>(pyqListType).toJson(pyqs)
    }

    fun pyqListFromJson(json: String): List<PyqItem> {
        return try {
            moshi.adapter<List<PyqItem>>(pyqListType).fromJson(json) ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun syllabusListToJson(syllabus: List<SyllabusUnit>): String {
        return moshi.adapter<List<SyllabusUnit>>(syllabusListType).toJson(syllabus)
    }

    fun syllabusListFromJson(json: String): List<SyllabusUnit> {
        return try {
            moshi.adapter<List<SyllabusUnit>>(syllabusListType).fromJson(json) ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }
}
