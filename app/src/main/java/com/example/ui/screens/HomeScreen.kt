package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.AudioFile
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.FormatListBulleted
import androidx.compose.material.icons.filled.Functions
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Quiz
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.UploadFile
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Badge
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ChapterEntity
import com.example.data.model.LectureEntity
import com.example.data.model.NoteEntity
import com.example.data.model.NoteFigureEntity
import com.example.data.model.SubjectEntity
import com.example.data.model.SubjectWithStats
import com.example.viewmodel.AppScreen
import com.example.viewmodel.AppViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: AppViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val lectures by viewModel.allLectures.collectAsState()
    val subjectsWithStats by viewModel.subjectsWithStats.collectAsState()
    val allNotes by viewModel.allNotes.collectAsState()
    val allFigures by viewModel.allFigures.collectAsState()
    val selectedSubject by viewModel.selectedSubject.collectAsState()
    val isExamPrepMode by viewModel.isExamPrepMode.collectAsState()

    var activeMainTab by remember { mutableIntStateOf(0) } // 0: Subject Notes, 1: AI Classroom, 2: Exam Hub

    var showAddSubjectDialog by remember { mutableStateOf(false) }
    var editingSubject by remember { mutableStateOf<SubjectEntity?>(null) }
    var deletingSubject by remember { mutableStateOf<SubjectEntity?>(null) }
    var lectureToDelete by remember { mutableStateOf<LectureEntity?>(null) }
    var lectureToRename by remember { mutableStateOf<LectureEntity?>(null) }
    var renameInputText by remember { mutableStateOf("") }

    val filteredLectures = remember(lectures, selectedSubject) {
        if (selectedSubject.isBlank() || selectedSubject == "All") {
            lectures
        } else {
            lectures.filter { it.subject.equals(selectedSubject, ignoreCase = true) }
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(MaterialTheme.colorScheme.primary),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.School,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Engineering Notes & Lectures",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = "Subject-Wise Notes, Figures & AI Prep",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                actions = {
                    IconButton(
                        onClick = { viewModel.navigateTo(AppScreen.NOTES_SEARCH) },
                        modifier = Modifier.testTag("home_search_notes_button")
                    ) {
                        Icon(Icons.Default.Search, contentDescription = "Search Notes")
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .background(
                                color = if (isExamPrepMode) Color(0xFFEF4444).copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant,
                                shape = RoundedCornerShape(20.dp)
                            )
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = if (isExamPrepMode) "🔥 Exam" else "Study",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = if (isExamPrepMode) Color(0xFFEF4444) else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Switch(
                            checked = isExamPrepMode,
                            onCheckedChange = { viewModel.toggleExamPrepMode() },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = Color(0xFFEF4444)
                            ),
                            modifier = Modifier.testTag("exam_mode_switch")
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 8.dp
            ) {
                NavigationBarItem(
                    selected = activeMainTab == 0,
                    onClick = { activeMainTab = 0 },
                    icon = { Icon(Icons.AutoMirrored.Filled.MenuBook, contentDescription = "Notes") },
                    label = { Text("Subject Notes", fontWeight = if (activeMainTab == 0) FontWeight.Bold else FontWeight.Normal) },
                    modifier = Modifier.testTag("nav_subject_notes")
                )
                NavigationBarItem(
                    selected = activeMainTab == 1,
                    onClick = { activeMainTab = 1 },
                    icon = { Icon(Icons.Default.Mic, contentDescription = "Lectures") },
                    label = { Text("AI Classroom", fontWeight = if (activeMainTab == 1) FontWeight.Bold else FontWeight.Normal) },
                    modifier = Modifier.testTag("nav_ai_lectures")
                )
                NavigationBarItem(
                    selected = activeMainTab == 2,
                    onClick = { activeMainTab = 2 },
                    icon = { Icon(Icons.Default.Psychology, contentDescription = "Exam Prep") },
                    label = { Text("Exam Hub", fontWeight = if (activeMainTab == 2) FontWeight.Bold else FontWeight.Normal) },
                    modifier = Modifier.testTag("nav_exam_hub")
                )
            }
        },
        floatingActionButton = {
            when (activeMainTab) {
                0 -> {
                    FloatingActionButton(
                        onClick = {
                            val firstSubj = subjectsWithStats.firstOrNull()?.subject
                            if (firstSubj != null) {
                                viewModel.openSubject(firstSubj)
                            } else {
                                showAddSubjectDialog = true
                            }
                        },
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.testTag("fab_add_subject_notes")
                    ) {
                        Row(modifier = Modifier.padding(horizontal = 16.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Add, contentDescription = null)
                            Spacer(Modifier.width(8.dp))
                            Text("Subject / Note", fontWeight = FontWeight.Bold)
                        }
                    }
                }
                1 -> {
                    FloatingActionButton(
                        onClick = { viewModel.navigateTo(AppScreen.RECORD) },
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.testTag("fab_record_lecture")
                    ) {
                        Row(modifier = Modifier.padding(horizontal = 16.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Mic, contentDescription = null)
                            Spacer(Modifier.width(8.dp))
                            Text("Record Audio", fontWeight = FontWeight.Bold)
                        }
                    }
                }
                else -> {}
            }
        }
    ) { innerPadding ->
        when (activeMainTab) {
            0 -> SubjectNotesTabContent(
                innerPadding = innerPadding,
                subjectsWithStats = subjectsWithStats,
                allNotes = allNotes,
                allFigures = allFigures,
                onAddSubject = { showAddSubjectDialog = true },
                onSelectSubject = { subj -> viewModel.openSubject(subj) },
                onEditSubject = { subj -> editingSubject = subj },
                onDeleteSubject = { subj -> deletingSubject = subj },
                onOpenNote = { note -> viewModel.openNoteEditor(note) },
                onSearchClick = { viewModel.navigateTo(AppScreen.NOTES_SEARCH) }
            )
            1 -> AiLecturesTabContent(
                innerPadding = innerPadding,
                lectures = filteredLectures,
                subjects = subjectsWithStats.map { it.subject },
                selectedSubject = selectedSubject,
                isExamPrepMode = isExamPrepMode,
                onSelectSubjectFilter = { viewModel.selectSubject(it) },
                onOpenLecture = { lecture ->
                    viewModel.selectLecture(lecture)
                    viewModel.navigateTo(AppScreen.NOTES_DETAIL)
                },
                onRenameLecture = { lecture ->
                    lectureToRename = lecture
                    renameInputText = lecture.title
                },
                onDeleteLecture = { lecture ->
                    lectureToDelete = lecture
                },
                onNavigate = { screen -> viewModel.navigateTo(screen) }
            )
            2 -> ExamHubTabContent(
                innerPadding = innerPadding,
                subjects = subjectsWithStats.map { it.subject },
                allNotes = allNotes,
                onNavigate = { screen -> viewModel.navigateTo(screen) }
            )
        }
    }

    // Add Subject Dialog
    if (showAddSubjectDialog) {
        var nameText by remember { mutableStateOf("") }
        var codeText by remember { mutableStateOf("") }
        var semesterText by remember { mutableStateOf("Semester 3") }

        AlertDialog(
            onDismissRequest = { showAddSubjectDialog = false },
            title = { Text("Add Engineering Subject") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = nameText,
                        onValueChange = { nameText = it },
                        label = { Text("Subject Name") },
                        placeholder = { Text("e.g. Fluid Mechanics") },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_new_subject_name")
                    )
                    OutlinedTextField(
                        value = codeText,
                        onValueChange = { codeText = it },
                        label = { Text("Subject Code") },
                        placeholder = { Text("e.g. ME302") },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_new_subject_code")
                    )
                    OutlinedTextField(
                        value = semesterText,
                        onValueChange = { semesterText = it },
                        label = { Text("Semester / Term") },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_new_subject_semester")
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (nameText.isNotBlank()) {
                            viewModel.addNewSubject(
                                name = nameText.trim(),
                                code = codeText.trim(),
                                semester = semesterText.trim()
                            )
                            showAddSubjectDialog = false
                        }
                    },
                    modifier = Modifier.testTag("btn_confirm_add_subject")
                ) {
                    Text("Add Subject")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddSubjectDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Edit Subject Dialog
    if (editingSubject != null) {
        val target = editingSubject!!
        var nameText by remember { mutableStateOf(target.name) }
        var codeText by remember { mutableStateOf(target.code) }
        var semesterText by remember { mutableStateOf(target.semester) }

        AlertDialog(
            onDismissRequest = { editingSubject = null },
            title = { Text("Edit Subject") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = nameText,
                        onValueChange = { nameText = it },
                        label = { Text("Subject Name") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = codeText,
                        onValueChange = { codeText = it },
                        label = { Text("Subject Code") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = semesterText,
                        onValueChange = { semesterText = it },
                        label = { Text("Semester / Term") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (nameText.isNotBlank()) {
                            viewModel.updateSubject(
                                target.copy(
                                    name = nameText.trim(),
                                    code = codeText.trim(),
                                    semester = semesterText.trim()
                                )
                            )
                            editingSubject = null
                        }
                    }
                ) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { editingSubject = null }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Delete Subject Dialog
    if (deletingSubject != null) {
        val target = deletingSubject!!
        AlertDialog(
            onDismissRequest = { deletingSubject = null },
            title = { Text("Delete '${target.name}'?") },
            text = { Text("Are you sure you want to delete this subject? All its chapters, notes, and attached figures will be permanently removed.") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteSubject(target)
                        deletingSubject = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Delete Subject")
                }
            },
            dismissButton = {
                TextButton(onClick = { deletingSubject = null }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Rename Lecture Dialog
    lectureToRename?.let { lecture ->
        AlertDialog(
            onDismissRequest = { lectureToRename = null },
            title = { Text("Rename Lecture") },
            text = {
                OutlinedTextField(
                    value = renameInputText,
                    onValueChange = { renameInputText = it },
                    label = { Text("Lecture Title") },
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (renameInputText.isNotBlank()) {
                            viewModel.renameLecture(lecture.id, renameInputText)
                            lectureToRename = null
                        }
                    }
                ) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { lectureToRename = null }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Delete Lecture Dialog
    lectureToDelete?.let { lecture ->
        AlertDialog(
            onDismissRequest = { lectureToDelete = null },
            title = { Text("Delete Lecture?") },
            text = { Text("Are you sure you want to delete '${lecture.title}' and its generated notes?") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteLecture(lecture.id)
                        lectureToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { lectureToDelete = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}

// ================= Tab 1: Subject Notes (Dedicated Notes Section) =================

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SubjectNotesTabContent(
    innerPadding: PaddingValues,
    subjectsWithStats: List<SubjectWithStats>,
    allNotes: List<NoteEntity>,
    allFigures: List<NoteFigureEntity>,
    onAddSubject: () -> Unit,
    onSelectSubject: (SubjectEntity) -> Unit,
    onEditSubject: (SubjectEntity) -> Unit,
    onDeleteSubject: (SubjectEntity) -> Unit,
    onOpenNote: (NoteEntity) -> Unit,
    onSearchClick: () -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(innerPadding)
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Quick Search Banner
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp)
                    .clickable { onSearchClick() }
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Search,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Search All Notes & Formulas",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                        )
                        Text(
                            text = "Search by Subject, Chapter, Title, Topic, or Formula symbols",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        // Subjects Section Header
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Subjects (${subjectsWithStats.size})",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    Text(
                        text = "Each subject has its own chapters, technical figures & PDFs",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Button(
                    onClick = onAddSubject,
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                    modifier = Modifier.testTag("btn_add_subject_notes_tab")
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Subject", fontSize = 13.sp)
                }
            }
        }

        // Subjects Grid / List
        if (subjectsWithStats.isEmpty()) {
            item {
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            Icons.Default.Folder,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(40.dp)
                        )
                        Spacer(Modifier.height(8.dp))
                        Text("No subjects created yet", fontWeight = FontWeight.Bold)
                        Spacer(Modifier.height(4.dp))
                        Text(
                            "Create your first engineering subject to organize notes chapter-wise.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(Modifier.height(12.dp))
                        Button(onClick = onAddSubject) {
                            Text("Create Engineering Subject")
                        }
                    }
                }
            }
        } else {
            items(subjectsWithStats, key = { it.subject.id }) { item ->
                SubjectCard(
                    subjectWithStats = item,
                    onClick = { onSelectSubject(item.subject) },
                    onEdit = { onEditSubject(item.subject) },
                    onDelete = { onDeleteSubject(item.subject) }
                )
            }
        }

        // Recent Notes Section
        if (allNotes.isEmpty()) {
            item {
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("empty_notes_container")
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            Icons.Default.Description,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(40.dp)
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            text = "No notes yet",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = "Upload a lecture video or select a subject above to add your first chapter notes.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        } else {
            item {
                Spacer(Modifier.height(8.dp))
                Text(
                    text = "Recent Study Notes",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
            }

            val recentNotes = allNotes.sortedByDescending { it.updatedAt }.take(5)
            items(recentNotes, key = { it.id }) { note ->
                val figCount = allFigures.count { it.noteId == note.id }
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surface,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onOpenNote(note) }
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = note.title,
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            if (note.topic.isNotBlank()) {
                                Text(
                                    text = note.topic,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.primary,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                            Spacer(Modifier.height(4.dp))
                            FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                if (note.formulas.isNotBlank()) {
                                    BadgeChip(label = "Formulas", icon = "∑", color = MaterialTheme.colorScheme.tertiary)
                                }
                                if (figCount > 0) {
                                    BadgeChip(label = "$figCount Figures", icon = "📷", color = MaterialTheme.colorScheme.secondary)
                                }
                                if (note.pdfPath != null) {
                                    BadgeChip(label = "PDF", icon = "📄", color = MaterialTheme.colorScheme.primary)
                                }
                            }
                        }

                        Icon(
                            Icons.Default.Edit,
                            contentDescription = "Edit Note",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SubjectCard(
    subjectWithStats: SubjectWithStats,
    onClick: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    var menuOpen by remember { mutableStateOf(false) }

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
            .clickable { onClick() }
            .testTag("subject_card_${subjectWithStats.subject.id}")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.MenuBook,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    Spacer(Modifier.width(12.dp))

                    Column {
                        Text(
                            text = subjectWithStats.subject.name,
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "${subjectWithStats.subject.code} • ${subjectWithStats.subject.semester}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Box {
                    IconButton(onClick = { menuOpen = true }) {
                        Icon(Icons.Default.MoreVert, contentDescription = "Subject Options")
                    }
                    DropdownMenu(
                        expanded = menuOpen,
                        onDismissRequest = { menuOpen = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Open Chapter Notes") },
                            onClick = {
                                menuOpen = false
                                onClick()
                            },
                            leadingIcon = { Icon(Icons.AutoMirrored.Filled.MenuBook, contentDescription = null) }
                        )
                        DropdownMenuItem(
                            text = { Text("Edit Subject") },
                            onClick = {
                                menuOpen = false
                                onEdit()
                            },
                            leadingIcon = { Icon(Icons.Default.Edit, contentDescription = null) }
                        )
                        DropdownMenuItem(
                            text = { Text("Delete Subject", color = MaterialTheme.colorScheme.error) },
                            onClick = {
                                menuOpen = false
                                onDelete()
                            },
                            leadingIcon = { Icon(Icons.Default.Delete, contentDescription = null, tint = MaterialTheme.colorScheme.error) }
                        )
                    }
                }
            }

            Spacer(Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f)
                    ) {
                        Text(
                            text = "${subjectWithStats.chapterCount} Chapters",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSecondaryContainer,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                    ) {
                        Text(
                            text = "${subjectWithStats.noteCount} Technical Notes",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }

                Text(
                    text = "Open Notes →",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

// ================= Tab 2: AI Classroom & Lectures =================

@Composable
fun AiLecturesTabContent(
    innerPadding: PaddingValues,
    lectures: List<LectureEntity>,
    subjects: List<SubjectEntity>,
    selectedSubject: String,
    isExamPrepMode: Boolean,
    onSelectSubjectFilter: (String) -> Unit,
    onOpenLecture: (LectureEntity) -> Unit,
    onRenameLecture: (LectureEntity) -> Unit,
    onDeleteLecture: (LectureEntity) -> Unit,
    onNavigate: (AppScreen) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(innerPadding)
            .testTag("home_screen_scroll"),
        contentPadding = PaddingValues(bottom = 96.dp)
    ) {
        // Hero Exam Mode Banner
        item {
            AnimatedVisibility(visible = isExamPrepMode) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    shape = RoundedCornerShape(14.dp),
                    color = Color(0xFF7F1D1D)
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = null,
                            tint = Color(0xFFFCA5A5),
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Exam Preparation Mode Active",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                text = "Prioritizing high-yield 7-mark derivations, formula cheat sheets, and repeated PYQs.",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFFFECACA)
                            )
                        }
                        Button(
                            onClick = { onNavigate(AppScreen.EXAM_PREP) },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color.White,
                                contentColor = Color(0xFF7F1D1D)
                            ),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("Open", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // Action Hub Grid
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Text(
                    text = "AI Classroom & Recording",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    ActionCard(
                        title = "Upload Video",
                        subtitle = "Select Video + AI Notes",
                        icon = Icons.Default.UploadFile,
                        badge = "📤 Upload",
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("action_upload_video_lecture"),
                        onClick = { onNavigate(AppScreen.UPLOAD_VIDEO) }
                    )

                    ActionCard(
                        title = "Record Video",
                        subtitle = "Camera + Board OCR & Notes",
                        icon = Icons.Default.Videocam,
                        badge = "🎥 HD + OCR",
                        color = Color(0xFFE11D48),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("action_record_video_lecture"),
                        onClick = { onNavigate(AppScreen.RECORD_VIDEO) }
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    ActionCard(
                        title = "Record Audio",
                        subtitle = "Multi-language Audio + AI",
                        icon = Icons.Default.Mic,
                        badge = "Live Audio",
                        color = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("action_record_lecture"),
                        onClick = { onNavigate(AppScreen.RECORD) }
                    )

                    ActionCard(
                        title = "PYQ Papers",
                        subtitle = "Repeated Questions & Trends",
                        icon = Icons.Default.MenuBook,
                        badge = "High Yield",
                        color = Color(0xFFD97706),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("action_pyq_papers"),
                        onClick = { onNavigate(AppScreen.PYQ_ANALYZER) }
                    )
                }
            }
        }

        // Subject Filter Chips
        item {
            Column(modifier = Modifier.padding(top = 16.dp)) {
                Text(
                    text = "Filter by Subject",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))

                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    item {
                        FilterChip(
                            selected = selectedSubject == "All",
                            onClick = { onSelectSubjectFilter("All") },
                            label = { Text("All Subjects") },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primary,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                            )
                        )
                    }

                    items(subjects) { subject ->
                        FilterChip(
                            selected = selectedSubject == subject.name,
                            onClick = { onSelectSubjectFilter(subject.name) },
                            label = { Text(subject.name) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primary,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                            )
                        )
                    }
                }
            }
        }

        // Lectures List Header
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, end = 16.dp, top = 20.dp, bottom = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "My Recorded Lectures (${lectures.size})",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }

        if (lectures.isEmpty()) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.AudioFile,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "No lectures yet",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Upload a lecture video or record a class to transcribe, extract formulas, detect key points, and generate PDF notes.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(14.dp))
                        Button(
                            onClick = { onNavigate(AppScreen.UPLOAD_VIDEO) },
                            modifier = Modifier.testTag("upload_video_empty_state_btn")
                        ) {
                            Icon(Icons.Default.UploadFile, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("Upload Lecture Video", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        } else {
            items(lectures, key = { it.id }) { lecture ->
                LectureItemCard(
                    lecture = lecture,
                    onOpen = { onOpenLecture(lecture) },
                    onRename = { onRenameLecture(lecture) },
                    onDelete = { onDeleteLecture(lecture) }
                )
            }
        }
    }
}

// ================= Tab 3: Exam Hub =================

@Composable
fun ExamHubTabContent(
    innerPadding: PaddingValues,
    subjects: List<SubjectEntity>,
    allNotes: List<NoteEntity>,
    onNavigate: (AppScreen) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(innerPadding)
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Spacer(Modifier.height(8.dp))
            Text(
                text = "Exam Preparation & Quick Revision",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
            )
            Text(
                text = "Master derivations, test yourself with interactive MCQs, and check syllabus progress",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        item {
            ActionCard(
                title = "Exam Prep & Formula Sheet",
                subtitle = "Step-by-step 2, 3, 5 & 7 mark model answers",
                icon = Icons.Default.Psychology,
                badge = "High Yield",
                color = Color(0xFFDC2626),
                modifier = Modifier.fillMaxWidth(),
                onClick = { onNavigate(AppScreen.EXAM_PREP) }
            )
        }

        item {
            ActionCard(
                title = "Revision Quiz & Flashcards",
                subtitle = "Self-assessment MCQs with instant explanations",
                icon = Icons.Default.Quiz,
                badge = "Test Yourself",
                color = Color(0xFF059669),
                modifier = Modifier.fillMaxWidth(),
                onClick = { onNavigate(AppScreen.REVISION_QUIZ) }
            )
        }

        item {
            ActionCard(
                title = "PYQ Repeated Questions Analyzer",
                subtitle = "Frequency analysis of 5-year university exam papers",
                icon = Icons.Default.Assignment,
                badge = "Exam Trends",
                color = Color(0xFFD97706),
                modifier = Modifier.fillMaxWidth(),
                onClick = { onNavigate(AppScreen.PYQ_ANALYZER) }
            )
        }

        item {
            ActionCard(
                title = "Syllabus Tracker",
                subtitle = "Track topic-wise completion across ${subjects.size} subjects",
                icon = Icons.Default.FormatListBulleted,
                badge = "${subjects.size} Subjects",
                color = Color(0xFF7C3AED),
                modifier = Modifier.fillMaxWidth(),
                onClick = { onNavigate(AppScreen.SYLLABUS_VIEWER) }
            )
        }
    }
}

@Composable
private fun ActionCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    badge: String,
    color: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    ElevatedCard(
        onClick = onClick,
        modifier = modifier.height(108.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .background(color.copy(alpha = 0.15f), RoundedCornerShape(10.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = color,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = color.copy(alpha = 0.12f)
                ) {
                    Text(
                        text = badge,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = color,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                        fontSize = 10.sp
                    )
                }
            }

            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    fontSize = 11.sp
                )
            }
        }
    }
}

@Composable
fun LectureItemCard(
    lecture: LectureEntity,
    onOpen: () -> Unit,
    onRename: () -> Unit,
    onDelete: () -> Unit
) {
    var menuExpanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .clickable { onOpen() }
            .testTag("lecture_item_${lecture.id}"),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .background(
                        color = if (lecture.hasVideo) Color(0xFFE11D48).copy(alpha = 0.12f) else MaterialTheme.colorScheme.primaryContainer,
                        shape = RoundedCornerShape(12.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (lecture.hasVideo) Icons.Default.Videocam else Icons.Default.AudioFile,
                    contentDescription = null,
                    tint = if (lecture.hasVideo) Color(0xFFE11D48) else MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = lecture.title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = "${lecture.subject} • ${lecture.unitName}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Medium
                )

                Spacer(modifier = Modifier.height(4.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    val dateFormatted = SimpleDateFormat("dd MMM", Locale.getDefault()).format(Date(lecture.dateEpoch))
                    val mins = lecture.durationSeconds / 60
                    Text(
                        text = "$dateFormatted • $mins mins",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.6f)
                    ) {
                        Text(
                            text = "Exam Ready",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSecondaryContainer,
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp),
                            fontSize = 10.sp
                        )
                    }
                }
            }

            Box {
                IconButton(onClick = { menuExpanded = true }) {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = "Options",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                DropdownMenu(
                    expanded = menuExpanded,
                    onDismissRequest = { menuExpanded = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("Open AI Exam Notes") },
                        onClick = {
                            menuExpanded = false
                            onOpen()
                        },
                        leadingIcon = {
                            Icon(Icons.Default.AutoAwesome, contentDescription = null)
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Rename Lecture") },
                        onClick = {
                            menuExpanded = false
                            onRename()
                        },
                        leadingIcon = {
                            Icon(Icons.Default.Edit, contentDescription = null)
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Delete Lecture", color = MaterialTheme.colorScheme.error) },
                        onClick = {
                            menuExpanded = false
                            onDelete()
                        },
                        leadingIcon = {
                            Icon(Icons.Default.Delete, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun BadgeChip(
    label: String,
    icon: String,
    color: Color
) {
    Surface(
        shape = RoundedCornerShape(6.dp),
        color = color.copy(alpha = 0.12f)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = icon, fontSize = 10.sp)
            Spacer(Modifier.width(3.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = color,
                fontWeight = FontWeight.Bold,
                fontSize = 10.sp
            )
        }
    }
}
