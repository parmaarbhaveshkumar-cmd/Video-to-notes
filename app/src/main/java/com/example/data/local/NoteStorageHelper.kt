package com.example.data.local

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import android.util.Log
import androidx.core.content.FileProvider
import com.example.data.model.NoteEntity
import com.example.data.model.NoteFigureEntity
import com.example.data.model.SubjectEntity
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

object NoteStorageHelper {

    private const val TAG = "NoteStorageHelper"

    /**
     * Directory structure:
     * filesDir/subjects/{subjectId}/chapters/{chapterId}/notes/{noteId}/figures/
     * filesDir/subjects/{subjectId}/chapters/{chapterId}/notes/{noteId}/pdfs/
     */
    fun getNoteFiguresDir(context: Context, subjectId: String, chapterId: String, noteId: String): File {
        val dir = File(context.filesDir, "subjects/$subjectId/chapters/$chapterId/notes/$noteId/figures")
        if (!dir.exists()) {
            dir.mkdirs()
        }
        return dir
    }

    fun getNotePdfsDir(context: Context, subjectId: String, chapterId: String, noteId: String): File {
        val dir = File(context.filesDir, "subjects/$subjectId/chapters/$chapterId/notes/$noteId/pdfs")
        if (!dir.exists()) {
            dir.mkdirs()
        }
        return dir
    }

    fun saveBitmapToNoteFigures(
        context: Context,
        bitmap: Bitmap,
        subjectId: String,
        chapterId: String,
        noteId: String,
        figureId: String = UUID.randomUUID().toString()
    ): String {
        val figuresDir = getNoteFiguresDir(context, subjectId, chapterId, noteId)
        val file = File(figuresDir, "fig_$figureId.jpg")
        FileOutputStream(file).use { out ->
            bitmap.compress(Bitmap.CompressFormat.JPEG, 92, out)
        }
        return file.absolutePath
    }

    fun saveUriToNoteFigures(
        context: Context,
        sourceUri: Uri,
        subjectId: String,
        chapterId: String,
        noteId: String,
        figureId: String = UUID.randomUUID().toString()
    ): String? {
        return try {
            val figuresDir = getNoteFiguresDir(context, subjectId, chapterId, noteId)
            val file = File(figuresDir, "fig_$figureId.jpg")
            context.contentResolver.openInputStream(sourceUri)?.use { input: InputStream ->
                FileOutputStream(file).use { output ->
                    input.copyTo(output)
                }
            }
            file.absolutePath
        } catch (e: Exception) {
            Log.e(TAG, "Error copying uri to note figures", e)
            null
        }
    }

    fun rotateImageFile(filePath: String, degrees: Int): Boolean {
        return try {
            val file = File(filePath)
            if (!file.exists()) return false
            val bitmap = BitmapFactory.decodeFile(filePath) ?: return false
            val matrix = Matrix().apply { postRotate(degrees.toFloat()) }
            val rotated = Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
            FileOutputStream(file).use { out ->
                rotated.compress(Bitmap.CompressFormat.JPEG, 92, out)
            }
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error rotating image file", e)
            false
        }
    }

    fun deleteSubjectDirectory(context: Context, subjectId: String) {
        try {
            val dir = File(context.filesDir, "subjects/$subjectId")
            if (dir.exists()) {
                dir.deleteRecursively()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error deleting subject dir", e)
        }
    }

    fun deleteChapterDirectory(context: Context, subjectId: String, chapterId: String) {
        try {
            val dir = File(context.filesDir, "subjects/$subjectId/chapters/$chapterId")
            if (dir.exists()) {
                dir.deleteRecursively()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error deleting chapter dir", e)
        }
    }

    fun deleteNoteDirectory(context: Context, subjectId: String, chapterId: String, noteId: String) {
        try {
            val dir = File(context.filesDir, "subjects/$subjectId/chapters/$chapterId/notes/$noteId")
            if (dir.exists()) {
                dir.deleteRecursively()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error deleting note dir", e)
        }
    }

    // =========================================================================
    //  PDF GENERATION WITH FIGURES, FORMULAS, AND METADATA
    // =========================================================================

    /**
     * Generates a high-quality, multi-page PDF document including:
     * - Subject Name, Subject Code, Semester
     * - Chapter Name, Note Title, Topic, Date
     * - Written Notes / Content / Definitions
     * - Formulas with dedicated formula box
     * - Important Exam Points
     * - Engineering Figures & Images with figure captions directly below
     */
    fun generateNotePdf(
        context: Context,
        subject: SubjectEntity,
        chapterName: String,
        note: NoteEntity,
        figures: List<NoteFigureEntity>
    ): File? {
        val pdfDocument = PdfDocument()

        // Standard A4 dimensions in PostScript points: 595 x 842 pt
        val pageWidth = 595
        val pageHeight = 842
        val margin = 36f
        val contentWidth = (pageWidth - (margin * 2)).toInt()

        var pageNumber = 1
        var pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create()
        var page = pdfDocument.startPage(pageInfo)
        var canvas = page.canvas

        var cursorY = margin

        // Paints
        val primaryPaint = Paint().apply {
            color = Color.rgb(30, 58, 138) // Deep Blue #1E3A8A
            isAntiAlias = true
        }

        val textPaint = TextPaint().apply {
            color = Color.rgb(30, 41, 59) // Slate 800
            textSize = 11.5f
            isAntiAlias = true
        }

        val headingPaint = TextPaint().apply {
            color = Color.rgb(15, 23, 42) // Slate 900
            textSize = 15f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }

        val captionPaint = TextPaint().apply {
            color = Color.rgb(71, 85, 105) // Slate 600
            textSize = 10.5f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }

        val linePaint = Paint().apply {
            color = Color.rgb(226, 232, 240)
            strokeWidth = 1.2f
            isAntiAlias = true
        }

        fun drawHeader() {
            // Top colored header banner
            val headerHeight = 52f
            val headerRect = android.graphics.RectF(margin, cursorY, pageWidth - margin, cursorY + headerHeight)
            val headerBgPaint = Paint().apply {
                color = Color.rgb(238, 242, 255) // Light indigo
                isAntiAlias = true
            }
            canvas.drawRoundRect(headerRect, 6f, 6f, headerBgPaint)

            // Subject Name & Code
            val subNamePaint = TextPaint().apply {
                color = Color.rgb(30, 58, 138)
                textSize = 14f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                isAntiAlias = true
            }
            canvas.drawText(subject.name, margin + 12f, cursorY + 20f, subNamePaint)

            val metaText = buildString {
                if (subject.code.isNotBlank()) append("Code: ${subject.code}  •  ")
                if (subject.semester.isNotBlank()) append("${subject.semester}  •  ")
                append(chapterName)
            }
            val metaPaint = TextPaint().apply {
                color = Color.rgb(79, 70, 229)
                textSize = 9.5f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                isAntiAlias = true
            }
            canvas.drawText(metaText, margin + 12f, cursorY + 38f, metaPaint)

            cursorY += headerHeight + 14f

            // Note Title & Date Row
            val titlePaint = TextPaint().apply {
                color = Color.rgb(15, 23, 42)
                textSize = 18f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                isAntiAlias = true
            }
            canvas.drawText(note.title, margin, cursorY + 12f, titlePaint)

            val dateStr = SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date(note.updatedAt))
            val datePaint = TextPaint().apply {
                color = Color.rgb(100, 116, 139)
                textSize = 10f
                isAntiAlias = true
            }
            val dateWidth = datePaint.measureText(dateStr)
            canvas.drawText(dateStr, (pageWidth - margin) - dateWidth, cursorY + 10f, datePaint)

            cursorY += 22f

            if (note.topic.isNotBlank()) {
                val topicPaint = TextPaint().apply {
                    color = Color.rgb(71, 85, 105)
                    textSize = 11.5f
                    typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                    isAntiAlias = true
                }
                canvas.drawText("Topic: ${note.topic}", margin, cursorY + 8f, topicPaint)
                cursorY += 16f
            }

            // Divider line
            canvas.drawLine(margin, cursorY + 4f, pageWidth - margin, cursorY + 4f, linePaint)
            cursorY += 14f
        }

        fun checkPageBreak(requiredHeight: Float) {
            if (cursorY + requiredHeight > pageHeight - margin - 24f) {
                // Draw footer on current page
                val footerPaint = TextPaint().apply {
                    color = Color.rgb(148, 163, 184)
                    textSize = 9f
                    isAntiAlias = true
                }
                canvas.drawText("Page $pageNumber", pageWidth / 2f - 15f, pageHeight - margin / 2f, footerPaint)

                pdfDocument.finishPage(page)
                pageNumber++
                pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create()
                page = pdfDocument.startPage(pageInfo)
                canvas = page.canvas
                cursorY = margin
                drawHeader()
            }
        }

        try {
            // Draw Initial Header
            drawHeader()

            // 1. Written Notes / Content Section
            if (note.content.isNotBlank()) {
                checkPageBreak(30f)
                canvas.drawText("Notes & Explanation", margin, cursorY + 12f, headingPaint)
                cursorY += 18f

                val contentLayout = StaticLayout.Builder.obtain(
                    note.content,
                    0,
                    note.content.length,
                    textPaint,
                    contentWidth
                ).setAlignment(Layout.Alignment.ALIGN_NORMAL)
                    .setLineSpacing(3f, 1.15f)
                    .setIncludePad(false)
                    .build()

                checkPageBreak(contentLayout.height.toFloat() + 10f)
                canvas.save()
                canvas.translate(margin, cursorY)
                contentLayout.draw(canvas)
                canvas.restore()
                cursorY += contentLayout.height + 16f
            }

            // 2. Formulas Section
            if (note.formulas.isNotBlank()) {
                val formulaLayout = StaticLayout.Builder.obtain(
                    note.formulas,
                    0,
                    note.formulas.length,
                    TextPaint().apply {
                        color = Color.rgb(17, 24, 39)
                        textSize = 12f
                        typeface = Typeface.create(Typeface.MONOSPACE, Typeface.BOLD)
                        isAntiAlias = true
                    },
                    contentWidth - 24
                ).setAlignment(Layout.Alignment.ALIGN_NORMAL)
                    .setLineSpacing(2f, 1.2f)
                    .setIncludePad(false)
                    .build()

                val boxHeight = formulaLayout.height + 34f
                checkPageBreak(boxHeight + 10f)

                // Formula Box
                val formulaRect = android.graphics.RectF(margin, cursorY, pageWidth - margin, cursorY + boxHeight)
                val formulaBgPaint = Paint().apply {
                    color = Color.rgb(254, 243, 199) // Amber-100
                    isAntiAlias = true
                }
                val formulaBorderPaint = Paint().apply {
                    color = Color.rgb(217, 119, 6) // Amber-600
                    style = Paint.Style.STROKE
                    strokeWidth = 1.5f
                    isAntiAlias = true
                }
                canvas.drawRoundRect(formulaRect, 6f, 6f, formulaBgPaint)
                canvas.drawRoundRect(formulaRect, 6f, 6f, formulaBorderPaint)

                val formulaHeaderPaint = TextPaint().apply {
                    color = Color.rgb(180, 83, 9)
                    textSize = 10.5f
                    typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                    isAntiAlias = true
                }
                canvas.drawText("KEY ENGINEERING FORMULAS", margin + 12f, cursorY + 16f, formulaHeaderPaint)

                canvas.save()
                canvas.translate(margin + 12f, cursorY + 24f)
                formulaLayout.draw(canvas)
                canvas.restore()

                cursorY += boxHeight + 16f
            }

            // 3. Important Exam Points Section
            if (note.importantPoints.isNotBlank()) {
                checkPageBreak(30f)
                val impHeaderPaint = TextPaint().apply {
                    color = Color.rgb(185, 28, 28) // Red-700
                    textSize = 13.5f
                    typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                    isAntiAlias = true
                }
                canvas.drawText("Important Points for Exam", margin, cursorY + 12f, impHeaderPaint)
                cursorY += 18f

                val impLayout = StaticLayout.Builder.obtain(
                    note.importantPoints,
                    0,
                    note.importantPoints.length,
                    textPaint,
                    contentWidth
                ).setAlignment(Layout.Alignment.ALIGN_NORMAL)
                    .setLineSpacing(2f, 1.15f)
                    .setIncludePad(false)
                    .build()

                checkPageBreak(impLayout.height.toFloat() + 10f)
                canvas.save()
                canvas.translate(margin, cursorY)
                impLayout.draw(canvas)
                canvas.restore()
                cursorY += impLayout.height + 16f
            }

            // 4. Figures and Diagrams Section
            if (figures.isNotEmpty()) {
                checkPageBreak(30f)
                canvas.drawText("Engineering Figures & Diagrams", margin, cursorY + 12f, headingPaint)
                cursorY += 20f

                figures.forEachIndexed { index, figure ->
                    val file = File(figure.imagePath)
                    if (file.exists()) {
                        val bitmap = BitmapFactory.decodeFile(figure.imagePath)
                        if (bitmap != null) {
                            // Rotate if required
                            val finalBitmap = if (figure.rotationDegrees != 0) {
                                val matrix = Matrix().apply { postRotate(figure.rotationDegrees.toFloat()) }
                                Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
                            } else {
                                bitmap
                            }

                            // Calculate aspect fit dimensions for PDF page width
                            val maxImageWidth = contentWidth.toFloat()
                            val maxImageHeight = 260f // Max height on page to fit nicely
                            val scale = minOf(maxImageWidth / finalBitmap.width, maxImageHeight / finalBitmap.height)
                            val scaledWidth = finalBitmap.width * scale
                            val scaledHeight = finalBitmap.height * scale

                            val captionText = figure.caption.ifBlank { "Figure ${index + 1}: Engineering Diagram" }
                            val captionHeight = 22f
                            val totalItemHeight = scaledHeight + captionHeight + 18f

                            checkPageBreak(totalItemHeight)

                            // Centered Image position
                            val imageX = margin + ((contentWidth - scaledWidth) / 2f)
                            val destRect = android.graphics.RectF(imageX, cursorY, imageX + scaledWidth, cursorY + scaledHeight)

                            // Frame around figure
                            val framePaint = Paint().apply {
                                color = Color.rgb(203, 213, 225)
                                style = Paint.Style.STROKE
                                strokeWidth = 1f
                                isAntiAlias = true
                            }
                            canvas.drawBitmap(finalBitmap, null, destRect, null)
                            canvas.drawRect(destRect, framePaint)

                            cursorY += scaledHeight + 12f

                            // Figure Caption
                            val captionLayout = StaticLayout.Builder.obtain(
                                captionText,
                                0,
                                captionText.length,
                                captionPaint,
                                contentWidth
                            ).setAlignment(Layout.Alignment.ALIGN_CENTER)
                                .setIncludePad(false)
                                .build()

                            canvas.save()
                            canvas.translate(margin, cursorY)
                            captionLayout.draw(canvas)
                            canvas.restore()

                            cursorY += captionLayout.height + 16f
                        }
                    }
                }
            }

            // Draw footer on last page
            val footerPaint = TextPaint().apply {
                color = Color.rgb(148, 163, 184)
                textSize = 9f
                isAntiAlias = true
            }
            canvas.drawText("Page $pageNumber", pageWidth / 2f - 15f, pageHeight - margin / 2f, footerPaint)

            pdfDocument.finishPage(page)

            // Save PDF file to storage
            val pdfDir = getNotePdfsDir(context, subject.id, note.chapterId, note.id)
            val safeTitle = note.title.replace("[^a-zA-Z0-9_-]".toRegex(), "_")
            val pdfFile = File(pdfDir, "${safeTitle}_Notes.pdf")

            FileOutputStream(pdfFile).use { out ->
                pdfDocument.writeTo(out)
            }
            pdfDocument.close()

            return pdfFile
        } catch (e: Exception) {
            Log.e(TAG, "Error generating PDF", e)
            try {
                pdfDocument.close()
            } catch (ignored: Exception) {}
            return null
        }
    }

    fun sharePdf(context: Context, pdfFile: File, subjectName: String, noteTitle: String) {
        try {
            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                pdfFile
            )
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "application/pdf"
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra(Intent.EXTRA_SUBJECT, "$subjectName - $noteTitle (Notes)")
                putExtra(Intent.EXTRA_TEXT, "Here are the engineering notes and diagrams for '$noteTitle' from $subjectName.")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(Intent.createChooser(intent, "Share Note PDF via"))
        } catch (e: Exception) {
            Log.e(TAG, "Error sharing PDF", e)
        }
    }

    fun shareNoteText(context: Context, note: NoteEntity, subjectName: String, chapterName: String) {
        try {
            val text = buildString {
                appendLine("📚 $subjectName")
                appendLine("📂 $chapterName")
                appendLine("📝 ${note.title}")
                if (note.topic.isNotBlank()) appendLine("Topic: ${note.topic}")
                appendLine("--------------------------------")
                if (note.content.isNotBlank()) {
                    appendLine("NOTES:")
                    appendLine(note.content)
                    appendLine()
                }
                if (note.formulas.isNotBlank()) {
                    appendLine("FORMULAS:")
                    appendLine(note.formulas)
                    appendLine()
                }
                if (note.importantPoints.isNotBlank()) {
                    appendLine("IMPORTANT POINTS:")
                    appendLine(note.importantPoints)
                }
            }

            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_SUBJECT, "${note.title} - $subjectName")
                putExtra(Intent.EXTRA_TEXT, text)
            }
            context.startActivity(Intent.createChooser(intent, "Share Note via"))
        } catch (e: Exception) {
            Log.e(TAG, "Error sharing note text", e)
        }
    }
}
