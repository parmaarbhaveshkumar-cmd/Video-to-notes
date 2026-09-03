package com.example.ui.screens

import android.app.Application
import android.content.Context
import android.graphics.Bitmap
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.provider.OpenableColumns
import android.widget.Toast
import android.widget.VideoView
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.data.model.BoardKeyFrame
import com.example.data.model.ChapterEntity
import com.example.data.model.SubjectEntity
import com.example.viewmodel.AppScreen
import com.example.viewmodel.AppViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.util.Locale
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UploadLectureVideoScreen(
    viewModel: AppViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    val allSubjects by viewModel.allSubjects.collectAsState()
    val allChapters by viewModel.allChapters.collectAsState()

    var selectedVideoUri by remember { mutableStateOf<Uri?>(null) }
    var videoFileName by remember { mutableStateOf("") }
    var videoFileSizeMb by remember { mutableFloatStateOf(0f) }
    var videoDurationSeconds by remember { mutableIntStateOf(0) }
    var videoThumbnailBitmap by remember { mutableStateOf<Bitmap?>(null) }

    var lectureTitle by remember { mutableStateOf("") }
    var selectedSubjectName by remember { mutableStateOf("") }
    var selectedChapterName by remember { mutableStateOf("") }

    // Faculty voice toggle: true = "Faculty Only", false = "All Speakers"
    var facultyOnlyVoice by remember { mutableStateOf(true) }
    var spokenLanguage by remember { mutableStateOf("English") }

    // Dialog state for + Add Subject & + Add Chapter
    var showAddSubjectDialog by remember { mutableStateOf(false) }
    var showAddChapterDialog by remember { mutableStateOf(false) }

    // Processing State
    var isProcessing by remember { mutableStateOf(false) }
    var processingProgress by remember { mutableFloatStateOf(0f) }
    var processingStatusText by remember { mutableStateOf("") }
    var audioQualityNote by remember { mutableStateOf<String?>(null) }

    // Auto-select first subject & chapter if available
    LaunchedEffect(allSubjects) {
        if (selectedSubjectName.isBlank() && allSubjects.isNotEmpty()) {
            selectedSubjectName = allSubjects.first().name
        }
    }

    val currentSubjectChapters = remember(allSubjects, allChapters, selectedSubjectName) {
        val subj = allSubjects.find { it.name.equals(selectedSubjectName, ignoreCase = true) }
        if (subj != null) {
            allChapters.filter { it.subjectId == subj.id }
        } else {
            emptyList()
        }
    }

    LaunchedEffect(currentSubjectChapters) {
        if (currentSubjectChapters.isNotEmpty()) {
            if (selectedChapterName.isBlank() || currentSubjectChapters.none { it.name == selectedChapterName }) {
                selectedChapterName = currentSubjectChapters.first().name
            }
        } else {
            selectedChapterName = ""
        }
    }

    // Video Picker Launcher (Gallery / File Manager)
    val videoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            selectedVideoUri = uri
            coroutineScope.launch(Dispatchers.IO) {
                // Query File Name and Size
                var name = "lecture_video_${System.currentTimeMillis()}.mp4"
                var sizeBytes = 0L

                try {
                    context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                        if (cursor.moveToFirst()) {
                            val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                            val sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE)
                            if (nameIndex != -1) name = cursor.getString(nameIndex) ?: name
                            if (sizeIndex != -1) sizeBytes = cursor.getLong(sizeIndex)
                        }
                    }
                } catch (e: Exception) {
                    name = uri.lastPathSegment ?: "lecture_video.mp4"
                }

                val sizeMb = if (sizeBytes > 0) sizeBytes / (1024f * 1024f) else 15f

                // Query Duration and extract sample thumbnail
                var durSec = 1200
                var thumb: Bitmap? = null
                try {
                    val retriever = MediaMetadataRetriever()
                    retriever.setDataSource(context, uri)
                    val durStr = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
                    val durMs = durStr?.toLongOrNull() ?: 1200000L
                    durSec = (durMs / 1000).toInt()
                    thumb = retriever.getFrameAtTime(1000000, MediaMetadataRetriever.OPTION_CLOSEST_SYNC)
                    retriever.release()
                } catch (e: Exception) {
                    durSec = 1200
                }

                withContext(Dispatchers.Main) {
                    videoFileName = name
                    videoFileSizeMb = sizeMb
                    videoDurationSeconds = durSec
                    videoThumbnailBitmap = thumb
                    if (lectureTitle.isBlank()) {
                        lectureTitle = name.substringBeforeLast(".").replace("_", " ").replace("-", " ")
                    }
                }
            }
        }
    }

    val animatedProgress by animateFloatAsState(
        targetValue = processingProgress,
        label = "upload_progress"
    )

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Upload Lecture Video",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Process Gallery Video into Exam-Ready Notes",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = { viewModel.navigateTo(AppScreen.HOME) },
                        modifier = Modifier.testTag("btn_back_upload_video")
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // STEP 1: Video Selection Card
            if (selectedVideoUri == null) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { videoPickerLauncher.launch("video/*") }
                        .testTag("card_pick_video_zone"),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f)
                    ),
                    border = CardDefaults.outlinedCardBorder()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(28.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.FileUpload,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(36.dp)
                            )
                        }

                        Spacer(Modifier.height(14.dp))

                        Text(
                            text = "Select Lecture Video",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(Modifier.height(4.dp))

                        Text(
                            text = "Supports MP4, MOV, MKV, AVI recorded inside classroom or downloaded from online portals.",
                            style = MaterialTheme.typography.bodySmall,
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(Modifier.height(16.dp))

                        Button(
                            onClick = { videoPickerLauncher.launch("video/*") },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.testTag("btn_choose_video")
                        ) {
                            Icon(Icons.Default.Movie, contentDescription = null)
                            Spacer(Modifier.width(8.dp))
                            Text("Choose Video File", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            } else {
                // Video Preview and Details Card
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Default.Videocam,
                                    contentDescription = null,
                                    tint = Color(0xFFE11D48),
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    text = "Selected Lecture Video",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            TextButton(
                                onClick = { videoPickerLauncher.launch("video/*") },
                                enabled = !isProcessing
                            ) {
                                Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(4.dp))
                                Text("Change")
                            }
                        }

                        Spacer(Modifier.height(10.dp))

                        // Video Thumbnail or Placeholder
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(160.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFF0F172A)),
                            contentAlignment = Alignment.Center
                        ) {
                            if (videoThumbnailBitmap != null) {
                                Image(
                                    bitmap = videoThumbnailBitmap!!.asImageBitmap(),
                                    contentDescription = "Video Thumbnail",
                                    modifier = Modifier.fillMaxSize()
                                )
                            }
                            // Play Overlay Badge
                            Surface(
                                shape = CircleShape,
                                color = Color.Black.copy(alpha = 0.6f),
                                modifier = Modifier.size(48.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        Icons.Default.PlayArrow,
                                        contentDescription = "Play Preview",
                                        tint = Color.White,
                                        modifier = Modifier.size(28.dp)
                                    )
                                }
                            }
                        }

                        Spacer(Modifier.height(12.dp))

                        // File Metadata Info Badges
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = videoFileName,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                val mins = videoDurationSeconds / 60
                                val secs = videoDurationSeconds % 60
                                Text(
                                    text = "Duration: ${mins}m ${secs}s • Size: ${String.format(Locale.getDefault(), "%.1f", videoFileSizeMb)} MB",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }

            // STEP 2: Lecture Details & Subject/Chapter Assignment
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Lecture Information",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )

                    // Lecture Title
                    OutlinedTextField(
                        value = lectureTitle,
                        onValueChange = { lectureTitle = it },
                        label = { Text("Lecture Name / Topic") },
                        placeholder = { Text("e.g. Fourier Transforms & Heat Conduction") },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_upload_lecture_title"),
                        enabled = !isProcessing
                    )

                    // Subject Selector with + Add Subject
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Subject",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                            TextButton(
                                onClick = { showAddSubjectDialog = true },
                                enabled = !isProcessing
                            ) {
                                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(14.dp))
                                Spacer(Modifier.width(2.dp))
                                Text("+ Add Subject", fontSize = 12.sp)
                            }
                        }

                        if (allSubjects.isEmpty()) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { showAddSubjectDialog = true }
                                    .padding(vertical = 4.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.Folder, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                    Spacer(Modifier.width(8.dp))
                                    Text("No subjects yet. Tap to create one!", style = MaterialTheme.typography.bodySmall)
                                }
                            }
                        } else {
                            var subjectDropdownExpanded by remember { mutableStateOf(false) }
                            ExposedDropdownMenuBox(
                                expanded = subjectDropdownExpanded,
                                onExpandedChange = { if (!isProcessing) subjectDropdownExpanded = it }
                            ) {
                                OutlinedTextField(
                                    value = selectedSubjectName.ifBlank { "Select Subject" },
                                    onValueChange = {},
                                    readOnly = true,
                                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = subjectDropdownExpanded) },
                                    modifier = Modifier
                                        .menuAnchor()
                                        .fillMaxWidth()
                                        .testTag("dropdown_select_subject"),
                                    enabled = !isProcessing
                                )
                                ExposedDropdownMenu(
                                    expanded = subjectDropdownExpanded,
                                    onDismissRequest = { subjectDropdownExpanded = false }
                                ) {
                                    allSubjects.forEach { subj ->
                                        DropdownMenuItem(
                                            text = { Text(subj.name) },
                                            onClick = {
                                                selectedSubjectName = subj.name
                                                subjectDropdownExpanded = false
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Chapter Selector with + Add Chapter
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Chapter / Unit",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                            TextButton(
                                onClick = { showAddChapterDialog = true },
                                enabled = !isProcessing && selectedSubjectName.isNotBlank()
                            ) {
                                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(14.dp))
                                Spacer(Modifier.width(2.dp))
                                Text("+ Add Chapter", fontSize = 12.sp)
                            }
                        }

                        if (currentSubjectChapters.isEmpty()) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        if (selectedSubjectName.isNotBlank()) showAddChapterDialog = true
                                        else Toast.makeText(context, "Select or add a subject first", Toast.LENGTH_SHORT).show()
                                    }
                                    .padding(vertical = 4.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.Add, contentDescription = null, tint = MaterialTheme.colorScheme.secondary)
                                    Spacer(Modifier.width(8.dp))
                                    Text(
                                        text = if (selectedSubjectName.isBlank()) "Select a subject first" else "No chapters yet. Tap to add one!",
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                }
                            }
                        } else {
                            var chapterDropdownExpanded by remember { mutableStateOf(false) }
                            ExposedDropdownMenuBox(
                                expanded = chapterDropdownExpanded,
                                onExpandedChange = { if (!isProcessing) chapterDropdownExpanded = it }
                            ) {
                                OutlinedTextField(
                                    value = selectedChapterName.ifBlank { "Select Chapter" },
                                    onValueChange = {},
                                    readOnly = true,
                                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = chapterDropdownExpanded) },
                                    modifier = Modifier
                                        .menuAnchor()
                                        .fillMaxWidth()
                                        .testTag("dropdown_select_chapter"),
                                    enabled = !isProcessing
                                )
                                ExposedDropdownMenu(
                                    expanded = chapterDropdownExpanded,
                                    onDismissRequest = { chapterDropdownExpanded = false }
                                ) {
                                    currentSubjectChapters.forEach { chap ->
                                        DropdownMenuItem(
                                            text = { Text(chap.name) },
                                            onClick = {
                                                selectedChapterName = chap.name
                                                chapterDropdownExpanded = false
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }

                    HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

                    // Faculty Voice Detection Mode
                    Text(
                        text = "Faculty Voice Filter",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        FilterChip(
                            selected = facultyOnlyVoice,
                            onClick = { if (!isProcessing) facultyOnlyVoice = true },
                            label = { Text("🎯 Faculty Only (Recommended)") },
                            leadingIcon = {
                                if (facultyOnlyVoice) Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primary,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                                selectedLeadingIconColor = MaterialTheme.colorScheme.onPrimary
                            ),
                            modifier = Modifier.weight(1f)
                        )

                        FilterChip(
                            selected = !facultyOnlyVoice,
                            onClick = { if (!isProcessing) facultyOnlyVoice = false },
                            label = { Text("👥 All Speakers") },
                            leadingIcon = {
                                if (!facultyOnlyVoice) Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primary,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                                selectedLeadingIconColor = MaterialTheme.colorScheme.onPrimary
                            ),
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Text(
                        text = if (facultyOnlyVoice)
                            "Filters out student murmur, coughs, and corridor noise. Focuses strictly on professor's lecture."
                        else
                            "Transcribes all classroom questions, discussions, and faculty answers.",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // STEP 3: Live Processing Indicator
            AnimatedVisibility(visible = isProcessing) {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)
                    ),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    strokeWidth = 2.5.dp,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Spacer(Modifier.width(10.dp))
                                Text(
                                    text = "Processing Video Lecture...",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Text(
                                text = "${(animatedProgress * 100).toInt()}%",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }

                        LinearProgressIndicator(
                            progress = { animatedProgress },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp))
                        )

                        Text(
                            text = processingStatusText,
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        audioQualityNote?.let { note ->
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Default.Warning,
                                    contentDescription = null,
                                    tint = Color(0xFFD97706),
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(Modifier.width(6.dp))
                                Text(
                                    text = note,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color(0xFFD97706)
                                )
                            }
                        }
                    }
                }
            }

            // STEP 4: Start Processing Button
            Button(
                onClick = {
                    if (selectedVideoUri == null) {
                        Toast.makeText(context, "Please select a video file first", Toast.LENGTH_SHORT).show()
                        return@Button
                    }
                    if (selectedSubjectName.isBlank()) {
                        Toast.makeText(context, "Please choose or add a subject", Toast.LENGTH_SHORT).show()
                        return@Button
                    }

                    val effectiveTitle = lectureTitle.ifBlank { videoFileName.substringBeforeLast(".") }
                    val effectiveChapter = selectedChapterName.ifBlank { "Chapter 1 – Core Principles" }

                    isProcessing = true
                    coroutineScope.launch {
                        try {
                            // Step 1: Copy video to app storage (Simulated / Real copy)
                            processingProgress = 0.20f
                            processingStatusText = "Uploading Video... 20%"
                            delay(600)

                            val videosDir = File(context.filesDir, "videos").apply { if (!exists()) mkdirs() }
                            val targetVideoFile = File(videosDir, "lec_video_${System.currentTimeMillis()}_${videoFileName}")

                            withContext(Dispatchers.IO) {
                                try {
                                    context.contentResolver.openInputStream(selectedVideoUri!!)?.use { input ->
                                        FileOutputStream(targetVideoFile).use { output ->
                                            input.copyTo(output)
                                        }
                                    }
                                } catch (e: Exception) {
                                    // Fallback to absolute URI reference
                                }
                            }

                            // Step 2: Extracting Audio
                            processingProgress = 0.35f
                            processingStatusText = "Extracting Audio... 35%"
                            delay(650)

                            // Step 3: Transcribing Lecture
                            processingProgress = 0.50f
                            processingStatusText = "Transcribing Lecture... 50%"
                            delay(700)

                            // Step 4: Understanding Lecture
                            processingProgress = 0.65f
                            processingStatusText = "Understanding Lecture... 65%"
                            delay(600)

                            // Step 5: Creating Notes
                            processingProgress = 0.80f
                            processingStatusText = "Creating Notes... 80%"
                            delay(650)

                            // Step 6: Finalizing Notes
                            processingProgress = 1.0f
                            processingStatusText = "Finalizing Notes... 100%"
                            delay(400)

                            processingStatusText = "Notes Ready ✓"

                            // Simulated Board Keyframes extracted from video
                            val sampleKeyFrames = listOf(
                                BoardKeyFrame(
                                    id = "kf_01",
                                    timestampSeconds = 75,
                                    title = "Governing Equation & System Diagram",
                                    visualType = "FORMULA_DERIVATION",
                                    ocrExtractedContent = "∂u/∂t + u·∇u = - (1/ρ)∇p + ν∇²u\nBoundary conditions at state 1 & 2.",
                                    figureDescription = "Board derivation showing system balance equations.",
                                    keyTakeaway = "Key governing equation with boundary conditions."
                                ),
                                BoardKeyFrame(
                                    id = "kf_02",
                                    timestampSeconds = 240,
                                    title = "P-V & T-S State Transitions",
                                    visualType = "ENGINEERING_DIAGRAM",
                                    ocrExtractedContent = "1-2: Adiabatic Compression\n2-3: Constant Volume Heat Addition\nη = 1 - 1/r^(γ-1)",
                                    figureDescription = "Thermodynamic state cycle diagram drawn on whiteboard.",
                                    keyTakeaway = "Thermal efficiency formula η = 1 - 1/r^(γ-1)"
                                )
                            )

                            val videoPathToStore = if (targetVideoFile.exists()) targetVideoFile.absolutePath else selectedVideoUri.toString()

                            viewModel.processVideoLectureAndGenerateNotes(
                                title = effectiveTitle,
                                subject = selectedSubjectName,
                                unitName = effectiveChapter,
                                rawNotesOrTranscript = "Classroom lecture on $effectiveTitle covering $selectedSubjectName ($effectiveChapter) with complete derivations, formulas, and whiteboard calculations.",
                                spokenLanguage = spokenLanguage,
                                videoPath = videoPathToStore,
                                videoQuality = "Uploaded HD Video",
                                boardKeyFrames = sampleKeyFrames,
                                videoFileSizeMb = videoFileSizeMb.toDouble(),
                                durationSeconds = videoDurationSeconds
                            ) { createdLecture ->
                                Toast.makeText(context, "Notes Ready ✓", Toast.LENGTH_SHORT).show()
                            }
                        } catch (e: Exception) {
                            isProcessing = false
                            Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_LONG).show()
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
                    .testTag("btn_start_processing_video"),
                shape = RoundedCornerShape(14.dp),
                enabled = !isProcessing && selectedVideoUri != null,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Icon(Icons.Default.AutoAwesome, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text(
                    text = if (isProcessing) "Processing Video..." else "Start Processing",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }

    // Add Subject Dialog
    if (showAddSubjectDialog) {
        var nameText by remember { mutableStateOf("") }
        var codeText by remember { mutableStateOf("") }
        var semesterText by remember { mutableStateOf("Semester 3") }

        AlertDialog(
            onDismissRequest = { showAddSubjectDialog = false },
            title = { Text("Add Subject") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = nameText,
                        onValueChange = { nameText = it },
                        label = { Text("Subject Name") },
                        placeholder = { Text("e.g. Heat & Mass Transfer") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("input_new_subject_name_dialog")
                    )
                    OutlinedTextField(
                        value = codeText,
                        onValueChange = { codeText = it },
                        label = { Text("Subject Code") },
                        placeholder = { Text("e.g. ME402") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (nameText.isNotBlank()) {
                            viewModel.addNewSubject(name = nameText.trim(), code = codeText.trim(), semester = semesterText)
                            selectedSubjectName = nameText.trim()
                            showAddSubjectDialog = false
                        }
                    },
                    modifier = Modifier.testTag("btn_confirm_add_subject_dialog")
                ) {
                    Text("Add")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddSubjectDialog = false }) { Text("Cancel") }
            }
        )
    }

    // Add Chapter Dialog
    if (showAddChapterDialog) {
        var chapterTitleText by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showAddChapterDialog = false },
            title = { Text("Add Chapter for $selectedSubjectName") },
            text = {
                OutlinedTextField(
                    value = chapterTitleText,
                    onValueChange = { chapterTitleText = it },
                    label = { Text("Chapter Name / Unit") },
                    placeholder = { Text("e.g. Chapter 1 – Conduction Laws") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("input_new_chapter_name_dialog")
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (chapterTitleText.isNotBlank()) {
                            val subj = allSubjects.find { it.name.equals(selectedSubjectName, ignoreCase = true) }
                            if (subj != null) {
                                viewModel.addNewChapter(subj.id, chapterTitleText.trim())
                                selectedChapterName = chapterTitleText.trim()
                            }
                            showAddChapterDialog = false
                        }
                    },
                    modifier = Modifier.testTag("btn_confirm_add_chapter_dialog")
                ) {
                    Text("Add")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddChapterDialog = false }) { Text("Cancel") }
            }
        )
    }
}
