package com.example.ui.screens

import android.graphics.BitmapFactory
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Functions
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.RotateRight
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SecondaryTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.NoteEntity
import com.example.data.model.NoteFigureEntity
import com.example.ui.components.CameraCaptureView
import com.example.viewmodel.AppScreen
import com.example.viewmodel.AppViewModel
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoteEditorScreen(
    viewModel: AppViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val note by viewModel.selectedNoteEntity.collectAsState()
    val subject by viewModel.selectedSubjectEntity.collectAsState()
    val chapter by viewModel.selectedChapterEntity.collectAsState()
    val figures by viewModel.activeNoteFigures.collectAsState()

    if (note == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("No note selected", style = MaterialTheme.typography.bodyLarge)
        }
        return
    }

    val currentNote = note!!

    var titleText by remember(currentNote.id) { mutableStateOf(currentNote.title) }
    var topicText by remember(currentNote.id) { mutableStateOf(currentNote.topic) }
    var contentText by remember(currentNote.id) { mutableStateOf(currentNote.content) }
    var formulasText by remember(currentNote.id) { mutableStateOf(currentNote.formulas) }
    var importantPointsText by remember(currentNote.id) { mutableStateOf(currentNote.importantPoints) }
    var isBookmarked by remember(currentNote.id) { mutableStateOf(currentNote.isBookmarked) }

    var selectedTab by remember { mutableIntStateOf(0) }
    var showCameraView by remember { mutableStateOf(false) }
    var showDeleteConfirmDialog by remember { mutableStateOf(false) }
    var deletingFigure by remember { mutableStateOf<NoteFigureEntity?>(null) }
    var previewingFigure by remember { mutableStateOf<NoteFigureEntity?>(null) }
    var editingCaptionFigure by remember { mutableStateOf<NoteFigureEntity?>(null) }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            viewModel.addFigureFromUri(
                uri = uri,
                caption = "Engineering Diagram / Figure",
                noteId = currentNote.id,
                chapterId = currentNote.chapterId,
                subjectId = currentNote.subjectId
            )
            Toast.makeText(context, "Diagram added to note", Toast.LENGTH_SHORT).show()
        }
    }

    fun saveChanges() {
        val updated = currentNote.copy(
            title = titleText.trim().ifBlank { "Untitled Note" },
            topic = topicText.trim(),
            content = contentText,
            formulas = formulasText,
            importantPoints = importantPointsText,
            isBookmarked = isBookmarked,
            updatedAt = System.currentTimeMillis()
        )
        viewModel.updateNote(updated)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = titleText.ifBlank { "Note Editor" },
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "${subject?.name ?: "Subject"} • Ch ${chapter?.chapterNumber ?: 1}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = {
                            saveChanges()
                            viewModel.navigateTo(AppScreen.SUBJECT_DETAIL)
                        },
                        modifier = Modifier.testTag("back_to_subject_button")
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            isBookmarked = !isBookmarked
                            saveChanges()
                        },
                        modifier = Modifier.testTag("bookmark_toggle_button")
                    ) {
                        Icon(
                            imageVector = if (isBookmarked) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                            contentDescription = "Bookmark",
                            tint = if (isBookmarked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    IconButton(
                        onClick = {
                            saveChanges()
                            val updated = currentNote.copy(
                                title = titleText.trim().ifBlank { "Untitled Note" },
                                topic = topicText.trim(),
                                content = contentText,
                                formulas = formulasText,
                                importantPoints = importantPointsText,
                                isBookmarked = isBookmarked,
                                updatedAt = System.currentTimeMillis()
                            )
                            if (subject != null && chapter != null) {
                                viewModel.generateAndOpenPdf(updated, subject!!, chapter!!.name, context)
                            }
                        },
                        modifier = Modifier.testTag("export_pdf_top_button")
                    ) {
                        Icon(
                            Icons.Default.PictureAsPdf,
                            contentDescription = "Generate PDF",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }

                    IconButton(
                        onClick = {
                            saveChanges()
                            Toast.makeText(context, "Note saved", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.testTag("save_note_button")
                    ) {
                        Icon(Icons.Default.Save, contentDescription = "Save Note")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Tabs: Theory, Formulas, Figures, Exam Points
            SecondaryTabRow(
                selectedTabIndex = selectedTab,
                containerColor = MaterialTheme.colorScheme.surface
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("Theory & Notes") },
                    icon = { Icon(Icons.AutoMirrored.Filled.MenuBook, contentDescription = null, modifier = Modifier.size(18.dp)) },
                    modifier = Modifier.testTag("tab_theory")
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("Formulas") },
                    icon = { Icon(Icons.Default.Functions, contentDescription = null, modifier = Modifier.size(18.dp)) },
                    modifier = Modifier.testTag("tab_formulas")
                )
                Tab(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    text = { Text("Figures (${figures.size})") },
                    icon = { Icon(Icons.Default.Image, contentDescription = null, modifier = Modifier.size(18.dp)) },
                    modifier = Modifier.testTag("tab_figures")
                )
                Tab(
                    selected = selectedTab == 3,
                    onClick = { selectedTab = 3 },
                    text = { Text("Exam Tips") },
                    icon = { Icon(Icons.Default.VerifiedUser, contentDescription = null, modifier = Modifier.size(18.dp)) },
                    modifier = Modifier.testTag("tab_exam")
                )
            }

            // Tab Content
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f)
            ) {
                when (selectedTab) {
                    0 -> TheoryNotesTab(
                        title = titleText,
                        onTitleChange = { titleText = it },
                        topic = topicText,
                        onTopicChange = { topicText = it },
                        content = contentText,
                        onContentChange = { contentText = it }
                    )
                    1 -> FormulasTab(
                        formulas = formulasText,
                        onFormulasChange = { formulasText = it },
                        onInsertSymbol = { symbol ->
                            formulasText += symbol
                        }
                    )
                    2 -> FiguresTab(
                        figures = figures,
                        onTakePhoto = { showCameraView = true },
                        onPickGallery = { photoPickerLauncher.launch("image/*") },
                        onPreviewFigure = { previewingFigure = it },
                        onRotateFigure = { viewModel.rotateFigure(it) },
                        onEditCaption = { editingCaptionFigure = it },
                        onDeleteFigure = { deletingFigure = it },
                        onMoveOrder = { fig, up -> viewModel.moveFigureOrder(fig, up, currentNote.id) }
                    )
                    3 -> ExamTipsTab(
                        importantPoints = importantPointsText,
                        onImportantPointsChange = { importantPointsText = it },
                        onExportPdf = {
                            saveChanges()
                            val updated = currentNote.copy(
                                title = titleText.trim().ifBlank { "Untitled Note" },
                                topic = topicText.trim(),
                                content = contentText,
                                formulas = formulasText,
                                importantPoints = importantPointsText,
                                isBookmarked = isBookmarked,
                                updatedAt = System.currentTimeMillis()
                            )
                            if (subject != null && chapter != null) {
                                viewModel.generateAndOpenPdf(updated, subject!!, chapter!!.name, context)
                            }
                        },
                        onShareText = {
                            val textToShare = buildString {
                                append("Engineering Study Note: $titleText\n")
                                append("Subject: ${subject?.name ?: ""}\n")
                                append("Chapter: ${chapter?.name ?: ""}\n\n")
                                if (topicText.isNotBlank()) append("Topic: $topicText\n\n")
                                if (contentText.isNotBlank()) append("--- THEORY & CONCEPTS ---\n$contentText\n\n")
                                if (formulasText.isNotBlank()) append("--- GOVERNING FORMULAS ---\n$formulasText\n\n")
                                if (importantPointsText.isNotBlank()) append("--- EXAM & VIVA POINTS ---\n$importantPointsText\n\n")
                            }
                            val sendIntent = android.content.Intent().apply {
                                action = android.content.Intent.ACTION_SEND
                                putExtra(android.content.Intent.EXTRA_TEXT, textToShare)
                                type = "text/plain"
                            }
                            context.startActivity(android.content.Intent.createChooser(sendIntent, "Share Note"))
                        }
                    )
                }
            }
        }
    }

    // Fullscreen Camera Capture Dialog
    if (showCameraView) {
        Dialog(
            onDismissRequest = { showCameraView = false },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                CameraCaptureView(
                    modifier = Modifier.fillMaxSize(),
                    onPhotoCaptured = { bitmap, cap ->
                        viewModel.addFigureFromBitmap(
                            bitmap = bitmap,
                            caption = cap.ifBlank { "Blackboard / Textbook Diagram" },
                            noteId = currentNote.id,
                            chapterId = currentNote.chapterId,
                            subjectId = currentNote.subjectId
                        )
                        showCameraView = false
                        Toast.makeText(context, "Diagram captured with technical accuracy", Toast.LENGTH_SHORT).show()
                    },
                    onClose = { showCameraView = false }
                )
            }
        }
    }

    // Fullscreen Figure Image Zoom Dialog
    if (previewingFigure != null) {
        val targetFig = previewingFigure!!
        Dialog(
            onDismissRequest = { previewingFigure = null },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(androidx.compose.ui.graphics.Color.Black)
            ) {
                val file = File(targetFig.imagePath)
                val bitmap = remember(targetFig.imagePath, targetFig.rotationDegrees) {
                    if (file.exists()) {
                        BitmapFactory.decodeFile(file.absolutePath)
                    } else null
                }

                if (bitmap != null) {
                    Image(
                        bitmap = bitmap.asImageBitmap(),
                        contentDescription = targetFig.caption,
                        contentScale = ContentScale.Fit,
                        modifier = Modifier
                            .fillMaxSize()
                            .rotate(targetFig.rotationDegrees.toFloat())
                    )
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .align(Alignment.TopCenter),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = targetFig.caption.ifBlank { "Technical Figure" },
                        color = androidx.compose.ui.graphics.Color.White,
                        style = MaterialTheme.typography.titleMedium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(
                        onClick = { previewingFigure = null },
                        modifier = Modifier.background(androidx.compose.ui.graphics.Color.Black.copy(alpha = 0.5f), CircleShape)
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = androidx.compose.ui.graphics.Color.White)
                    }
                }
            }
        }
    }

    // Edit Figure Caption Dialog
    if (editingCaptionFigure != null) {
        val target = editingCaptionFigure!!
        var captionText by remember { mutableStateOf(target.caption) }

        AlertDialog(
            onDismissRequest = { editingCaptionFigure = null },
            title = { Text("Edit Diagram Caption") },
            text = {
                OutlinedTextField(
                    value = captionText,
                    onValueChange = { captionText = it },
                    label = { Text("Caption / Label") },
                    placeholder = { Text("e.g. P-V Diagram of Otto Cycle") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.updateFigureCaption(target.id, captionText)
                        editingCaptionFigure = null
                    }
                ) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { editingCaptionFigure = null }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Delete Figure Confirmation Dialog
    if (deletingFigure != null) {
        val target = deletingFigure!!
        AlertDialog(
            onDismissRequest = { deletingFigure = null },
            title = { Text("Delete Figure?") },
            text = {
                Text("Are you sure you want to delete this technical diagram? The image file will be permanently removed.")
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteFigure(target)
                        deletingFigure = null
                        Toast.makeText(context, "Figure deleted", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { deletingFigure = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun TheoryNotesTab(
    title: String,
    onTitleChange: (String) -> Unit,
    topic: String,
    onTopicChange: (String) -> Unit,
    content: String,
    onContentChange: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        OutlinedTextField(
            value = title,
            onValueChange = onTitleChange,
            label = { Text("Note Title", fontWeight = FontWeight.SemiBold) },
            placeholder = { Text("e.g. Air Standard Otto Cycle Derivation") },
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("input_note_title")
        )

        OutlinedTextField(
            value = topic,
            onValueChange = onTopicChange,
            label = { Text("Topic / Sub-heading") },
            placeholder = { Text("e.g. 4-Stroke Spark-Ignition Engine Benchmark") },
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("input_note_topic")
        )

        OutlinedTextField(
            value = content,
            onValueChange = onContentChange,
            label = { Text("Theory & Technical Notes", fontWeight = FontWeight.SemiBold) },
            placeholder = {
                Text("Enter complete engineering explanations, step-by-step processes, principles, and textbook derivations...")
            },
            minLines = 10,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("input_note_content")
        )

        Spacer(Modifier.height(40.dp))
    }
}

@Composable
fun FormulasTab(
    formulas: String,
    onFormulasChange: (String) -> Unit,
    onInsertSymbol: (String) -> Unit
) {
    val mathSymbols = listOf(
        "η", "γ", "σ", "ε", "π", "Δ", "∇", "∂", "∑", "√", "∫", "∮",
        "²", "³", "₁", "₂", "ᵢ", "ₙ", "±", "∝", "→", "∞", "θ", "ω", "λ", "μ", "ρ", "τ"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.4f)
            ),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("∑", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.tertiary)
                Spacer(Modifier.width(10.dp))
                Text(
                    text = "Engineering Formulas & Governing Equations are formatted cleanly in PDFs and exam previews.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onTertiaryContainer
                )
            }
        }

        Text(
            text = "Math & Engineering Symbols Keyboard:",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold
        )

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(mathSymbols) { symbol ->
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier
                        .clickable { onInsertSymbol(symbol) }
                        .border(0.5.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(8.dp))
                ) {
                    Text(
                        text = symbol,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }

        OutlinedTextField(
            value = formulas,
            onValueChange = onFormulasChange,
            label = { Text("Governing Formulas & Equations", fontWeight = FontWeight.SemiBold) },
            placeholder = {
                Text("• Compression Ratio: r = V1 / V2\n• Efficiency: η_otto = 1 - (1 / r^(γ - 1))\n• Heat Input: Qin = m · Cv · (T3 - T2)")
            },
            minLines = 10,
            textStyle = MaterialTheme.typography.bodyMedium.copy(fontFamily = FontFamily.Monospace),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("input_note_formulas")
        )

        Spacer(Modifier.height(40.dp))
    }
}

@Composable
fun FiguresTab(
    figures: List<NoteFigureEntity>,
    onTakePhoto: () -> Unit,
    onPickGallery: () -> Unit,
    onPreviewFigure: (NoteFigureEntity) -> Unit,
    onRotateFigure: (NoteFigureEntity) -> Unit,
    onEditCaption: (NoteFigureEntity) -> Unit,
    onDeleteFigure: (NoteFigureEntity) -> Unit,
    onMoveOrder: (NoteFigureEntity, Boolean) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Technical Accuracy Banner
        Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
            ),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.VerifiedUser,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(Modifier.width(10.dp))
                Column {
                    Text(
                        text = "Technical Accuracy Guardrail",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "Figures, diagrams, and formulas are preserved exactly as captured without modification or distortion.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
        }

        // Action Buttons: Camera & Gallery
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Button(
                onClick = onTakePhoto,
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp)
                    .testTag("btn_take_diagram_photo"),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.CameraAlt, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Take Photo", fontSize = 14.sp)
            }

            OutlinedButton(
                onClick = onPickGallery,
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp)
                    .testTag("btn_pick_diagram_gallery"),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.AddPhotoAlternate, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("From Gallery", fontSize = 14.sp)
            }
        }

        Text(
            text = "Attached Diagrams (${figures.size}):",
            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
        )

        if (figures.isEmpty()) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        Icons.Default.Image,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                        modifier = Modifier.size(40.dp)
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = "No figures attached yet",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "Snap a photo of the blackboard, P-V diagram, or circuit to embed in your exam notes.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                        textAlign = TextAlign.Center
                    )
                }
            }
        } else {
            figures.forEachIndexed { index, figure ->
                FigureCard(
                    figure = figure,
                    index = index,
                    totalCount = figures.size,
                    onPreview = { onPreviewFigure(figure) },
                    onRotate = { onRotateFigure(figure) },
                    onEditCaption = { onEditCaption(figure) },
                    onDelete = { onDeleteFigure(figure) },
                    onMoveUp = { onMoveOrder(figure, true) },
                    onMoveDown = { onMoveOrder(figure, false) }
                )
            }
        }

        Spacer(Modifier.height(40.dp))
    }
}

@Composable
fun FigureCard(
    figure: NoteFigureEntity,
    index: Int,
    totalCount: Int,
    onPreview: () -> Unit,
    onRotate: () -> Unit,
    onEditCaption: () -> Unit,
    onDelete: () -> Unit,
    onMoveUp: () -> Unit,
    onMoveDown: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Figure ${index + 1}: ${figure.caption.ifBlank { "Technical Diagram" }}",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    modifier = Modifier.weight(1f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                IconButton(onClick = onEditCaption, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Default.Edit, contentDescription = "Edit Caption", modifier = Modifier.size(16.dp))
                }
            }

            Spacer(Modifier.height(8.dp))

            // Thumbnail
            val file = File(figure.imagePath)
            val bitmap = remember(figure.imagePath, figure.rotationDegrees) {
                if (file.exists()) {
                    BitmapFactory.decodeFile(file.absolutePath)
                } else null
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    .clickable { onPreview() },
                contentAlignment = Alignment.Center
            ) {
                if (bitmap != null) {
                    Image(
                        bitmap = bitmap.asImageBitmap(),
                        contentDescription = figure.caption,
                        contentScale = ContentScale.Fit,
                        modifier = Modifier
                            .fillMaxSize()
                            .rotate(figure.rotationDegrees.toFloat())
                    )
                } else {
                    Text("Image not found", color = MaterialTheme.colorScheme.error)
                }
            }

            Spacer(Modifier.height(8.dp))

            // Action toolbar: Rotate, Move Up, Move Down, Delete
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    IconButton(onClick = onRotate, modifier = Modifier.size(36.dp)) {
                        Icon(Icons.Default.RotateRight, contentDescription = "Rotate 90°", modifier = Modifier.size(18.dp))
                    }
                    if (index > 0) {
                        IconButton(onClick = onMoveUp, modifier = Modifier.size(36.dp)) {
                            Icon(Icons.Default.ArrowUpward, contentDescription = "Move Up", modifier = Modifier.size(18.dp))
                        }
                    }
                    if (index < totalCount - 1) {
                        IconButton(onClick = onMoveDown, modifier = Modifier.size(36.dp)) {
                            Icon(Icons.Default.ArrowDownward, contentDescription = "Move Down", modifier = Modifier.size(18.dp))
                        }
                    }
                }

                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "Delete Figure",
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun ExamTipsTab(
    importantPoints: String,
    onImportantPointsChange: (String) -> Unit,
    onExportPdf: () -> Unit,
    onShareText: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        OutlinedTextField(
            value = importantPoints,
            onValueChange = onImportantPointsChange,
            label = { Text("High-Yield Exam Points & Viva Tips", fontWeight = FontWeight.SemiBold) },
            placeholder = {
                Text("• Crucial exam assumptions\n• Common calculation mistakes to avoid\n• Viva examiner trap questions\n• 2-mark and 5-mark answer pointers")
            },
            minLines = 8,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("input_note_exam_points")
        )

        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
            ),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Export & University PDF Sheet",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "Generate clean multi-page A4 PDF containing Theory, Governing Formulas, and technical figures formatted for university exam prep.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = onExportPdf,
                        modifier = Modifier
                            .weight(1f)
                            .height(46.dp)
                            .testTag("btn_export_pdf_full"),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.PictureAsPdf, contentDescription = null)
                        Spacer(Modifier.width(6.dp))
                        Text("Export PDF")
                    }

                    OutlinedButton(
                        onClick = onShareText,
                        modifier = Modifier
                            .weight(1f)
                            .height(46.dp)
                            .testTag("btn_share_text_full"),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.Share, contentDescription = null)
                        Spacer(Modifier.width(6.dp))
                        Text("Share Text")
                    }
                }
            }
        }

        Spacer(Modifier.height(40.dp))
    }
}
