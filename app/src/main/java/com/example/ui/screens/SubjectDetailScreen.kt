package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ChapterEntity
import com.example.data.model.NoteEntity
import com.example.data.model.SubjectEntity
import com.example.viewmodel.AppScreen
import com.example.viewmodel.AppViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SubjectDetailScreen(
    viewModel: AppViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val subject by viewModel.selectedSubjectEntity.collectAsState()
    val allChapters by viewModel.allChapters.collectAsState()
    val allNotes by viewModel.allNotes.collectAsState()
    val allFigures by viewModel.allFigures.collectAsState()

    if (subject == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("No subject selected", style = MaterialTheme.typography.bodyLarge)
        }
        return
    }

    val currentSubj = subject!!
    val chapters = allChapters.filter { it.subjectId == currentSubj.id }.sortedBy { it.chapterNumber }
    val notes = allNotes.filter { it.subjectId == currentSubj.id }

    var selectedChapterId by remember { mutableStateOf<String?>(null) }
    var showAddChapterDialog by remember { mutableStateOf(false) }
    var showEditSubjectDialog by remember { mutableStateOf(false) }
    var showDeleteSubjectDialog by remember { mutableStateOf(false) }
    var editingChapter by remember { mutableStateOf<ChapterEntity?>(null) }
    var deletingChapter by remember { mutableStateOf<ChapterEntity?>(null) }
    var deletingNote by remember { mutableStateOf<NoteEntity?>(null) }
    var renamingNote by remember { mutableStateOf<NoteEntity?>(null) }
    var showMoreMenu by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = currentSubj.name,
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "${currentSubj.code} • ${currentSubj.semester}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = { viewModel.navigateTo(AppScreen.HOME) },
                        modifier = Modifier.testTag("back_button")
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back to Home")
                    }
                },
                actions = {
                    IconButton(
                        onClick = { viewModel.navigateTo(AppScreen.NOTES_SEARCH) },
                        modifier = Modifier.testTag("search_notes_button")
                    ) {
                        Icon(Icons.Default.Search, contentDescription = "Search Notes")
                    }
                    Box {
                        IconButton(
                            onClick = { showMoreMenu = true },
                            modifier = Modifier.testTag("subject_more_menu")
                        ) {
                            Icon(Icons.Default.MoreVert, contentDescription = "Subject Options")
                        }
                        DropdownMenu(
                            expanded = showMoreMenu,
                            onDismissRequest = { showMoreMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Edit Subject Details") },
                                onClick = {
                                    showMoreMenu = false
                                    showEditSubjectDialog = true
                                },
                                leadingIcon = { Icon(Icons.Default.Edit, contentDescription = null) }
                            )
                            DropdownMenuItem(
                                text = { Text("Delete Subject", color = MaterialTheme.colorScheme.error) },
                                onClick = {
                                    showMoreMenu = false
                                    showDeleteSubjectDialog = true
                                },
                                leadingIcon = { Icon(Icons.Default.Delete, contentDescription = null, tint = MaterialTheme.colorScheme.error) }
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    val defaultChap = chapters.firstOrNull { it.id == selectedChapterId } ?: chapters.firstOrNull()
                    if (defaultChap == null) {
                        Toast.makeText(context, "Please create a Chapter first", Toast.LENGTH_SHORT).show()
                        showAddChapterDialog = true
                    } else {
                        viewModel.openNewNoteEditor(
                            chapter = defaultChap,
                            subject = currentSubj
                        )
                    }
                },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.testTag("fab_create_note")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("New Note", fontWeight = FontWeight.Bold)
                }
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Subject Overview Banner
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Subject Overview",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.SemiBold
                            )
                            Spacer(Modifier.height(4.dp))
                            Text(
                                text = "${chapters.size} Chapters • ${notes.size} Technical Notes",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                            )
                            Text(
                                text = "Technical diagrams, formulas, and PDFs organized chapter-wise",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Button(
                            onClick = { showAddChapterDialog = true },
                            shape = RoundedCornerShape(12.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                            modifier = Modifier.testTag("add_chapter_banner_button")
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("Chapter", fontSize = 13.sp)
                        }
                    }
                }
            }

            // Chapter Filter Chips
            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Chapters",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        TextButton(
                            onClick = { showAddChapterDialog = true },
                            modifier = Modifier.testTag("add_chapter_text_button")
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("Add Chapter")
                        }
                    }

                    if (chapters.isEmpty()) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "No chapters created yet",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                OutlinedButton(
                                    onClick = { showAddChapterDialog = true },
                                    modifier = Modifier.testTag("create_first_chapter_btn")
                                ) {
                                    Text("Create Chapter 1")
                                }
                            }
                        }
                    } else {
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            item {
                                FilterChip(
                                    selected = selectedChapterId == null,
                                    onClick = { selectedChapterId = null },
                                    label = { Text("All Chapters (${notes.size})") },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = MaterialTheme.colorScheme.primary,
                                        selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                                    ),
                                    modifier = Modifier.testTag("filter_all_chapters")
                                )
                            }
                            items(chapters, key = { it.id }) { chap ->
                                val chapNotesCount = notes.count { it.chapterId == chap.id }
                                FilterChip(
                                    selected = selectedChapterId == chap.id,
                                    onClick = { selectedChapterId = chap.id },
                                    label = { Text("Ch ${chap.chapterNumber}: ${chap.name} ($chapNotesCount)") },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = MaterialTheme.colorScheme.primary,
                                        selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                                    ),
                                    modifier = Modifier.testTag("filter_chapter_${chap.id}")
                                )
                            }
                        }
                    }
                }
            }

            // Chapters and their Notes list
            val displayChapters = if (selectedChapterId == null) {
                chapters
            } else {
                chapters.filter { it.id == selectedChapterId }
            }

            if (displayChapters.isEmpty() && chapters.isNotEmpty()) {
                item {
                    Text(
                        text = "No notes in this chapter.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                items(displayChapters, key = { it.id }) { chapter ->
                    val chapterNotes = notes.filter { it.chapterId == chapter.id }

                    ChapterSection(
                        chapter = chapter,
                        notes = chapterNotes,
                        figures = allFigures,
                        onEditChapter = { editingChapter = chapter },
                        onDeleteChapter = { deletingChapter = chapter },
                        onAddNote = {
                            viewModel.openNewNoteEditor(
                                chapter = chapter,
                                subject = currentSubj
                            )
                        },
                        onNoteClick = { note ->
                            viewModel.openNoteEditor(note)
                        },
                        onToggleBookmark = { note ->
                            viewModel.toggleNoteBookmark(note.id)
                        },
                        onRenameNote = { note ->
                            renamingNote = note
                        },
                        onDeleteNote = { note ->
                            deletingNote = note
                        },
                        onExportPdf = { note ->
                            viewModel.generateAndOpenPdf(note, currentSubj, chapter.name, context)
                        }
                    )
                }
            }

            item {
                Spacer(Modifier.height(80.dp))
            }
        }
    }

    // ================= Dialogs =================

    // Add Chapter Dialog
    if (showAddChapterDialog) {
        var chapNumberText by remember { mutableStateOf("${chapters.size + 1}") }
        var chapNameText by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showAddChapterDialog = false },
            title = { Text("Add New Chapter") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = chapNumberText,
                        onValueChange = { chapNumberText = it },
                        label = { Text("Chapter Number") },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_chapter_number")
                    )
                    OutlinedTextField(
                        value = chapNameText,
                        onValueChange = { chapNameText = it },
                        label = { Text("Chapter Name") },
                        placeholder = { Text("e.g. First Law of Thermodynamics") },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_chapter_name")
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val num = chapNumberText.toIntOrNull() ?: (chapters.size + 1)
                        if (chapNameText.isNotBlank()) {
                            viewModel.addNewChapter(
                                subjectId = currentSubj.id,
                                chapterNumber = num,
                                name = chapNameText.trim()
                            )
                            showAddChapterDialog = false
                        } else {
                            Toast.makeText(context, "Please enter chapter name", Toast.LENGTH_SHORT).show()
                        }
                    },
                    modifier = Modifier.testTag("dialog_confirm_add_chapter")
                ) {
                    Text("Add Chapter")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddChapterDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Edit Chapter Dialog
    if (editingChapter != null) {
        val target = editingChapter!!
        var chapNumberText by remember { mutableStateOf("${target.chapterNumber}") }
        var chapNameText by remember { mutableStateOf(target.name) }

        AlertDialog(
            onDismissRequest = { editingChapter = null },
            title = { Text("Edit Chapter") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = chapNumberText,
                        onValueChange = { chapNumberText = it },
                        label = { Text("Chapter Number") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = chapNameText,
                        onValueChange = { chapNameText = it },
                        label = { Text("Chapter Name") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val num = chapNumberText.toIntOrNull() ?: target.chapterNumber
                        if (chapNameText.isNotBlank()) {
                            viewModel.updateChapter(
                                target.copy(
                                    chapterNumber = num,
                                    name = chapNameText.trim()
                                )
                            )
                            editingChapter = null
                        }
                    }
                ) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { editingChapter = null }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Delete Chapter Confirmation Dialog
    if (deletingChapter != null) {
        val target = deletingChapter!!
        val chapterNotesCount = notes.count { it.chapterId == target.id }

        AlertDialog(
            onDismissRequest = { deletingChapter = null },
            title = { Text("Delete Chapter ${target.chapterNumber}?") },
            text = {
                Text("Are you sure you want to delete '${target.name}'? This will permanently delete all $chapterNotesCount notes and technical figures under this chapter.")
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteChapter(target)
                        deletingChapter = null
                        Toast.makeText(context, "Chapter deleted", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                    modifier = Modifier.testTag("confirm_delete_chapter_button")
                ) {
                    Text("Delete Permanently")
                }
            },
            dismissButton = {
                TextButton(onClick = { deletingChapter = null }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Edit Subject Dialog
    if (showEditSubjectDialog) {
        var nameText by remember { mutableStateOf(currentSubj.name) }
        var codeText by remember { mutableStateOf(currentSubj.code) }
        var semesterText by remember { mutableStateOf(currentSubj.semester) }

        AlertDialog(
            onDismissRequest = { showEditSubjectDialog = false },
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
                                currentSubj.copy(
                                    name = nameText.trim(),
                                    code = codeText.trim(),
                                    semester = semesterText.trim()
                                )
                            )
                            showEditSubjectDialog = false
                        }
                    }
                ) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditSubjectDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Delete Subject Confirmation Dialog
    if (showDeleteSubjectDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteSubjectDialog = false },
            title = { Text("Delete '${currentSubj.name}'?") },
            text = {
                Text("Are you sure you want to delete this subject? All ${chapters.size} chapters, ${notes.size} notes, and attached engineering figures will be permanently removed.")
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteSubject(currentSubj)
                        showDeleteSubjectDialog = false
                        Toast.makeText(context, "Subject deleted", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                    modifier = Modifier.testTag("confirm_delete_subject_button")
                ) {
                    Text("Delete Subject")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteSubjectDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Rename Note Dialog
    if (renamingNote != null) {
        val target = renamingNote!!
        var newTitle by remember { mutableStateOf(target.title) }

        AlertDialog(
            onDismissRequest = { renamingNote = null },
            title = { Text("Rename Note") },
            text = {
                OutlinedTextField(
                    value = newTitle,
                    onValueChange = { newTitle = it },
                    label = { Text("Note Title") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newTitle.isNotBlank()) {
                            viewModel.updateNote(target.copy(title = newTitle.trim(), updatedAt = System.currentTimeMillis()))
                            renamingNote = null
                        }
                    }
                ) {
                    Text("Rename")
                }
            },
            dismissButton = {
                TextButton(onClick = { renamingNote = null }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Delete Note Confirmation Dialog
    if (deletingNote != null) {
        val target = deletingNote!!
        AlertDialog(
            onDismissRequest = { deletingNote = null },
            title = { Text("Delete Note?") },
            text = {
                Text("Are you sure you want to delete '${target.title}'? All technical notes, formulas, and attached diagrams will be permanently deleted.")
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteNote(target)
                        deletingNote = null
                        Toast.makeText(context, "Note deleted", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                    modifier = Modifier.testTag("confirm_delete_note_button")
                ) {
                    Text("Delete Permanently")
                }
            },
            dismissButton = {
                TextButton(onClick = { deletingNote = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ChapterSection(
    chapter: ChapterEntity,
    notes: List<NoteEntity>,
    figures: List<com.example.data.model.NoteFigureEntity>,
    onEditChapter: () -> Unit,
    onDeleteChapter: () -> Unit,
    onAddNote: () -> Unit,
    onNoteClick: (NoteEntity) -> Unit,
    onToggleBookmark: (NoteEntity) -> Unit,
    onRenameNote: (NoteEntity) -> Unit,
    onDeleteNote: (NoteEntity) -> Unit,
    onExportPdf: (NoteEntity) -> Unit
) {
    var isExpanded by remember { mutableStateOf(true) }
    var chapterMenuOpen by remember { mutableStateOf(false) }

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(
                1.dp,
                MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                RoundedCornerShape(16.dp)
            )
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
            // Chapter Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .weight(1f)
                        .clickable { isExpanded = !isExpanded }
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "${chapter.chapterNumber}",
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            fontSize = 15.sp
                        )
                    }

                    Spacer(Modifier.width(12.dp))

                    Column {
                        Text(
                            text = "Chapter ${chapter.chapterNumber}: ${chapter.name}",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "${notes.size} Notes",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = onAddNote,
                        modifier = Modifier.testTag("add_note_to_chapter_${chapter.id}")
                    ) {
                        Icon(
                            Icons.Default.Add,
                            contentDescription = "Add Note",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }

                    Box {
                        IconButton(onClick = { chapterMenuOpen = true }) {
                            Icon(Icons.Default.MoreVert, contentDescription = "Chapter Options")
                        }
                        DropdownMenu(
                            expanded = chapterMenuOpen,
                            onDismissRequest = { chapterMenuOpen = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Edit Chapter") },
                                onClick = {
                                    chapterMenuOpen = false
                                    onEditChapter()
                                },
                                leadingIcon = { Icon(Icons.Default.Edit, contentDescription = null) }
                            )
                            DropdownMenuItem(
                                text = { Text("Delete Chapter", color = MaterialTheme.colorScheme.error) },
                                onClick = {
                                    chapterMenuOpen = false
                                    onDeleteChapter()
                                },
                                leadingIcon = { Icon(Icons.Default.Delete, contentDescription = null, tint = MaterialTheme.colorScheme.error) }
                            )
                        }
                    }
                }
            }

            AnimatedVisibility(
                visible = isExpanded,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (notes.isEmpty()) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    Icons.Default.Description,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                                    modifier = Modifier.size(32.dp)
                                )
                                Spacer(Modifier.height(6.dp))
                                Text(
                                    text = "No notes yet in Chapter ${chapter.chapterNumber}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(Modifier.height(8.dp))
                                OutlinedButton(
                                    onClick = onAddNote,
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.testTag("create_note_cta_${chapter.id}")
                                ) {
                                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(Modifier.width(4.dp))
                                    Text("Add Technical Note")
                                }
                            }
                        }
                    } else {
                        notes.forEach { note ->
                            val figCount = figures.count { it.noteId == note.id }
                            NoteListItem(
                                note = note,
                                figureCount = figCount,
                                onClick = { onNoteClick(note) },
                                onToggleBookmark = { onToggleBookmark(note) },
                                onRename = { onRenameNote(note) },
                                onDelete = { onDeleteNote(note) },
                                onExportPdf = { onExportPdf(note) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun NoteListItem(
    note: NoteEntity,
    figureCount: Int,
    onClick: () -> Unit,
    onToggleBookmark: () -> Unit,
    onRename: () -> Unit,
    onDelete: () -> Unit,
    onExportPdf: () -> Unit
) {
    var noteMenuOpen by remember { mutableStateOf(false) }

    Surface(
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .testTag("note_card_${note.id}")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
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
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = onToggleBookmark,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = if (note.isBookmarked) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                            contentDescription = "Bookmark",
                            tint = if (note.isBookmarked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Box {
                        IconButton(
                            onClick = { noteMenuOpen = true },
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                Icons.Default.MoreVert,
                                contentDescription = "Note Options",
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        DropdownMenu(
                            expanded = noteMenuOpen,
                            onDismissRequest = { noteMenuOpen = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Export & Preview PDF") },
                                onClick = {
                                    noteMenuOpen = false
                                    onExportPdf()
                                },
                                leadingIcon = { Icon(Icons.Default.PictureAsPdf, contentDescription = null) }
                            )
                            DropdownMenuItem(
                                text = { Text("Rename Note") },
                                onClick = {
                                    noteMenuOpen = false
                                    onRename()
                                },
                                leadingIcon = { Icon(Icons.Default.Edit, contentDescription = null) }
                            )
                            DropdownMenuItem(
                                text = { Text("Delete Note", color = MaterialTheme.colorScheme.error) },
                                onClick = {
                                    noteMenuOpen = false
                                    onDelete()
                                },
                                leadingIcon = { Icon(Icons.Default.Delete, contentDescription = null, tint = MaterialTheme.colorScheme.error) }
                            )
                        }
                    }
                }
            }

            if (note.content.isNotBlank()) {
                Spacer(Modifier.height(4.dp))
                Text(
                    text = note.content,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(Modifier.height(8.dp))

            // Badges for formulas, figures, and PDF
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                if (note.formulas.isNotBlank()) {
                    BadgeChip(
                        label = "Formulas",
                        icon = "∑",
                        color = MaterialTheme.colorScheme.tertiary
                    )
                }

                if (figureCount > 0) {
                    BadgeChip(
                        label = "$figureCount Figures",
                        icon = "📷",
                        color = MaterialTheme.colorScheme.secondary
                    )
                }

                if (note.pdfPath != null && note.pdfPath.isNotBlank()) {
                    BadgeChip(
                        label = "PDF Ready",
                        icon = "📄",
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                val dateStr = java.text.SimpleDateFormat("dd MMM", java.util.Locale.getDefault())
                    .format(java.util.Date(note.updatedAt))
                Text(
                    text = "Updated $dateStr",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                    modifier = Modifier.align(Alignment.CenterVertically)
                )
            }
        }
    }
}
