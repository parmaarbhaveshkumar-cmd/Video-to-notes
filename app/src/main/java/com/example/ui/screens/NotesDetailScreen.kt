package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.DocumentScanner
import androidx.compose.material.icons.filled.FormatQuote
import androidx.compose.material.icons.filled.Functions
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Quiz
import androidx.compose.material.icons.filled.SdCard
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Badge
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.JsonUtils
import com.example.data.model.BoardKeyFrame
import com.example.data.model.QualityCheckReport
import com.example.data.model.TopicSection
import com.example.ui.components.AudioPlaybackController
import com.example.ui.components.EngineeringDiagramCanvas
import com.example.ui.components.MarkAnswerSection
import com.example.ui.components.QualityCheckBadge
import com.example.ui.components.VideoPlaybackPlayer
import com.example.viewmodel.AppScreen
import com.example.viewmodel.AppViewModel
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotesDetailScreen(
    viewModel: AppViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val lecture = viewModel.selectedLecture.collectAsState().value
    val isPlaying by viewModel.audioPlayer.isPlaying.collectAsState()
    val currentPositionMs by viewModel.audioPlayer.currentPositionMs.collectAsState()
    val durationMs by viewModel.audioPlayer.durationMs.collectAsState()
    val currentSpeed by viewModel.audioPlayer.currentSpeed.collectAsState()
    val videoSeekTarget by viewModel.videoSeekTargetSeconds.collectAsState()

    val chatMessages by viewModel.chatMessages.collectAsState()
    val isChatAiThinking by viewModel.isChatAiThinking.collectAsState()

    var selectedTab by remember { mutableIntStateOf(0) }
    var chatInputText by remember { mutableStateOf("") }
    var showMoreMenu by remember { mutableStateOf(false) }
    var showDeleteVideoDialog by remember { mutableStateOf(false) }
    var showDeleteAudioDialog by remember { mutableStateOf(false) }
    var isVideoPlayerExpanded by remember { mutableStateOf(true) }

    val scope = rememberCoroutineScope()
    val listState = rememberLazyListState()

    if (lecture == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("No lecture selected")
        }
        return
    }

    val topics = remember(lecture.topicsJson) { JsonUtils.topicListFromJson(lecture.topicsJson) }
    val boardFrames = remember(lecture.boardKeyFramesJson) { JsonUtils.boardKeyFrameListFromJson(lecture.boardKeyFramesJson) }
    val qualityReport = remember(lecture.qualityCheckJson) { JsonUtils.qualityReportFromJson(lecture.qualityCheckJson) }

    // Identify which topic matches the current playback position
    val currentPlayingTopic = remember(currentPositionMs, topics) {
        val currentSec = currentPositionMs / 1000
        topics.lastOrNull { it.timestampSeconds <= currentSec } ?: topics.firstOrNull()
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = lecture.title,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (lecture.hasVideo) {
                                Surface(
                                    shape = RoundedCornerShape(4.dp),
                                    color = Color(0xFFE11D48)
                                ) {
                                    Text(
                                        text = "VIDEO ${lecture.videoQuality}",
                                        color = Color.White,
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(6.dp))
                            }
                            Text(
                                text = "${lecture.subject} • ${lecture.unitName}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = { viewModel.navigateTo(AppScreen.HOME) },
                        modifier = Modifier.testTag("btn_back_notes_detail")
                    ) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.exportNotes(context, lecture) }) {
                        Icon(imageVector = Icons.Default.Share, contentDescription = "Export Notes")
                    }

                    Box {
                        IconButton(onClick = { showMoreMenu = true }) {
                            Icon(imageVector = Icons.Default.MoreVert, contentDescription = "More Options")
                        }

                        DropdownMenu(
                            expanded = showMoreMenu,
                            onDismissRequest = { showMoreMenu = false }
                        ) {
                            if (lecture.hasVideo) {
                                DropdownMenuItem(
                                    text = {
                                        Column {
                                            Text("Delete Video File Only")
                                            Text(
                                                "Saves ~${lecture.videoFileSizeMb.toInt()} MB (Keeps Audio & Notes)",
                                                fontSize = 11.sp,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    },
                                    leadingIcon = {
                                        Icon(imageVector = Icons.Default.SdCard, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                                    },
                                    onClick = {
                                        showMoreMenu = false
                                        showDeleteVideoDialog = true
                                    }
                                )
                            }

                            if (lecture.hasAudio) {
                                DropdownMenuItem(
                                    text = {
                                        Column {
                                            Text("Delete Audio File Only")
                                            Text("Keeps AI exam notes intact", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        }
                                    },
                                    leadingIcon = {
                                        Icon(imageVector = Icons.Default.DeleteSweep, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                                    },
                                    onClick = {
                                        showMoreMenu = false
                                        showDeleteAudioDialog = true
                                    }
                                )
                            }
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        bottomBar = {
            if (!lecture.hasVideo || !isVideoPlayerExpanded) {
                AudioPlaybackController(
                    isPlaying = isPlaying,
                    currentPositionMs = currentPositionMs,
                    durationMs = durationMs,
                    currentSpeed = currentSpeed,
                    activeTopicTitle = currentPlayingTopic?.topicName ?: "",
                    onTogglePlayPause = {
                        if (viewModel.audioPlayer.activeFilePath.value == null) {
                            viewModel.audioPlayer.loadAndPlay(lecture.audioFilePath, 0, lecture.durationSeconds)
                        } else {
                            viewModel.audioPlayer.togglePlayPause()
                        }
                    },
                    onSeek = { seconds ->
                        viewModel.audioPlayer.seekToSeconds(seconds)
                    },
                    onSpeedChange = { speed ->
                        viewModel.audioPlayer.setSpeed(speed)
                    }
                )
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // 1. If Video is present, render VideoPlaybackPlayer with keyframe strip
            if (lecture.hasVideo && isVideoPlayerExpanded) {
                VideoPlaybackPlayer(
                    videoFilePath = lecture.videoFilePath,
                    lectureTitle = lecture.title,
                    durationSeconds = lecture.durationSeconds,
                    boardKeyFrames = boardFrames,
                    seekToSeconds = videoSeekTarget,
                    onSeekHandled = {
                        viewModel.clearVideoSeekTarget()
                    },
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                )
            }

            // 2. Navigation Tabs (Tabs: Exam Notes, Whiteboard Frames, Model Answers, Formulas, Diagrams, AI Tutor, Transcript)
            ScrollableTabRow(
                selectedTabIndex = selectedTab,
                edgePadding = 12.dp,
                containerColor = MaterialTheme.colorScheme.surface
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("1. Exam Notes") }
                )
                if (boardFrames.isNotEmpty()) {
                    Tab(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        text = { Text("2. Board Frames (${boardFrames.size})") }
                    )
                }
                Tab(
                    selected = selectedTab == if (boardFrames.isNotEmpty()) 2 else 1,
                    onClick = { selectedTab = if (boardFrames.isNotEmpty()) 2 else 1 },
                    text = { Text("Model Answers (2/3/5/7M)") }
                )
                Tab(
                    selected = selectedTab == if (boardFrames.isNotEmpty()) 3 else 2,
                    onClick = { selectedTab = if (boardFrames.isNotEmpty()) 3 else 2 },
                    text = { Text("Formula Sheet") }
                )
                Tab(
                    selected = selectedTab == if (boardFrames.isNotEmpty()) 4 else 3,
                    onClick = { selectedTab = if (boardFrames.isNotEmpty()) 4 else 3 },
                    text = { Text("Diagrams") }
                )
                Tab(
                    selected = selectedTab == if (boardFrames.isNotEmpty()) 5 else 4,
                    onClick = { selectedTab = if (boardFrames.isNotEmpty()) 5 else 4 },
                    text = { Text("Ask AI Tutor") }
                )
                Tab(
                    selected = selectedTab == if (boardFrames.isNotEmpty()) 6 else 5,
                    onClick = { selectedTab = if (boardFrames.isNotEmpty()) 6 else 5 },
                    text = { Text("Transcript") }
                )
            }

            // Tab Content
            val effectiveTab = selectedTab
            val isBoardTabActive = boardFrames.isNotEmpty() && effectiveTab == 1

            if (isBoardTabActive) {
                // Whiteboard & Slide Snapshots Tab with OCR text
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    contentPadding = PaddingValues(top = 12.dp, bottom = 24.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    item {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(imageVector = Icons.Default.DocumentScanner, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Whiteboard & Presentation Key-Frames",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Extracted high-contrast blackboard writing, formulas, and diagrams with OCR text. Tap 'Jump to Video' to watch the teacher explain that exact frame.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    items(boardFrames) { frame ->
                        Card(
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = frame.title,
                                            style = MaterialTheme.typography.titleSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                        Text(
                                            text = frame.visualType.replace("_", " "),
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.outline
                                        )
                                    }

                                    Surface(
                                        onClick = {
                                            viewModel.seekToVideoTimestamp(frame.timestampSeconds)
                                            viewModel.audioPlayer.loadAndPlay(
                                                filePath = lecture.audioFilePath,
                                                startPositionSeconds = frame.timestampSeconds,
                                                totalFallbackDurationSeconds = lecture.durationSeconds
                                            )
                                        },
                                        shape = RoundedCornerShape(16.dp),
                                        color = MaterialTheme.colorScheme.primaryContainer
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(imageVector = Icons.Default.PlayCircle, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            val min = frame.timestampSeconds / 60
                                            val sec = frame.timestampSeconds % 60
                                            Text(
                                                text = "Jump to ${String.format("%02d:%02d", min, sec)}",
                                                style = MaterialTheme.typography.labelSmall,
                                                fontFamily = FontFamily.Monospace,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.onPrimaryContainer
                                            )
                                        }
                                    }
                                }

                                // Blueprint style OCR Content Box
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = Color(0xFF0F172A),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.padding(12.dp)) {
                                        Text(
                                            text = "OCR Whiteboard Transcript:",
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF38BDF8)
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = frame.ocrExtractedContent,
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontFamily = FontFamily.Monospace,
                                            color = Color.White
                                        )
                                    }
                                }

                                if (frame.figureDescription.isNotBlank()) {
                                    Text(
                                        text = "• Figure Analysis: ${frame.figureDescription}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }

                                if (frame.keyTakeaway.isNotBlank()) {
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = Color(0xFFFEF3C7)
                                    ) {
                                        Text(
                                            text = "💡 Exam Rule: ${frame.keyTakeaway}",
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF92400E),
                                            modifier = Modifier.padding(8.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            } else {
                val normalizedTab = if (boardFrames.isNotEmpty() && effectiveTab > 1) effectiveTab - 1 else effectiveTab

                when (normalizedTab) {
                    0 -> { // Comprehensive Exam Notes
                        var selectedTopicFilter by remember { mutableStateOf<String?>(null) }
                        val filteredTopics = remember(selectedTopicFilter, topics) {
                            if (selectedTopicFilter == null) topics else topics.filter { it.topicName == selectedTopicFilter }
                        }

                        LazyColumn(
                            state = listState,
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 16.dp),
                            contentPadding = PaddingValues(top = 12.dp, bottom = 24.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            // Quality Check Badge
                            item {
                                QualityCheckBadge(report = qualityReport)
                            }

                            // Chapter / Topic Navigation Chips
                            item {
                                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Text(
                                        text = "Topics in this Lecture (${topics.size}):",
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    androidx.compose.foundation.lazy.LazyRow(
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        item {
                                            androidx.compose.material3.FilterChip(
                                                selected = selectedTopicFilter == null,
                                                onClick = { selectedTopicFilter = null },
                                                label = { Text("All Topics") }
                                            )
                                        }
                                        items(topics) { topic ->
                                            androidx.compose.material3.FilterChip(
                                                selected = selectedTopicFilter == topic.topicName,
                                                onClick = {
                                                    selectedTopicFilter = if (selectedTopicFilter == topic.topicName) null else topic.topicName
                                                },
                                                label = { Text(topic.topicName, maxLines = 1) }
                                            )
                                        }
                                    }
                                }
                            }

                            // Summary Card
                            item {
                                Card(
                                    shape = RoundedCornerShape(12.dp),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                                ) {
                                    Column(modifier = Modifier.padding(14.dp)) {
                                        Text(
                                            text = "Executive Summary",
                                            style = MaterialTheme.typography.titleSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = lecture.summary,
                                            style = MaterialTheme.typography.bodyMedium,
                                            lineHeight = 20.sp
                                        )
                                    }
                                }
                            }

                            // Topics List
                            items(filteredTopics) { topic ->
                                TopicCard(
                                    topic = topic,
                                    isCurrentlyPlaying = currentPlayingTopic?.topicId == topic.topicId,
                                    onJumpToAudio = {
                                        viewModel.seekToVideoTimestamp(topic.timestampSeconds)
                                        viewModel.audioPlayer.loadAndPlay(
                                            filePath = lecture.audioFilePath,
                                            startPositionSeconds = topic.timestampSeconds,
                                            totalFallbackDurationSeconds = lecture.durationSeconds
                                        )
                                    }
                                )
                            }
                        }
                    }

                    1 -> { // 2, 3, 5, 7 Mark Answers
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 16.dp),
                            contentPadding = PaddingValues(top = 12.dp, bottom = 24.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            items(topics) { topic ->
                                Column {
                                    Text(
                                        text = "Topic: ${topic.topicName}",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.padding(bottom = 8.dp)
                                    )
                                    MarkAnswerSection(answers = topic.answers)
                                }
                            }
                        }
                    }

                    2 -> { // Formula Sheet
                        val formulasWithTopics = remember(topics) {
                            topics.filter { it.formula.isNotBlank() }
                        }

                        LazyColumn(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 16.dp),
                            contentPadding = PaddingValues(top = 12.dp, bottom = 24.dp),
                            verticalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            item {
                                Card(
                                    shape = RoundedCornerShape(12.dp),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f))
                                ) {
                                    Column(modifier = Modifier.padding(14.dp)) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(imageVector = Icons.Default.Functions, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(
                                                text = "Lecture Formula & Derivation Sheet",
                                                style = MaterialTheme.typography.titleSmall,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.primary
                                            )
                                        }
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = "All extracted equations, variable definitions, SI units, and timestamped audio explanation points.",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }

                            if (formulasWithTopics.isEmpty()) {
                                item {
                                    Box(
                                        modifier = Modifier.fillMaxWidth().padding(32.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text("No mathematical formulas extracted for this lecture.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                }
                            } else {
                                items(formulasWithTopics) { topic ->
                                    Card(
                                        shape = RoundedCornerShape(14.dp),
                                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                                    ) {
                                        Column(
                                            modifier = Modifier.fillMaxWidth().padding(16.dp),
                                            verticalArrangement = Arrangement.spacedBy(10.dp)
                                        ) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.SpaceBetween
                                            ) {
                                                Text(
                                                    text = topic.topicName,
                                                    style = MaterialTheme.typography.titleSmall,
                                                    fontWeight = FontWeight.Bold,
                                                    color = MaterialTheme.colorScheme.primary,
                                                    modifier = Modifier.weight(1f)
                                                )

                                                Surface(
                                                    onClick = {
                                                        viewModel.seekToVideoTimestamp(topic.timestampSeconds)
                                                        viewModel.audioPlayer.loadAndPlay(
                                                            filePath = lecture.audioFilePath,
                                                            startPositionSeconds = topic.timestampSeconds,
                                                            totalFallbackDurationSeconds = lecture.durationSeconds
                                                        )
                                                    },
                                                    shape = RoundedCornerShape(16.dp),
                                                    color = MaterialTheme.colorScheme.primaryContainer
                                                ) {
                                                    Row(
                                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                                        verticalAlignment = Alignment.CenterVertically
                                                    ) {
                                                        Icon(imageVector = Icons.Default.PlayCircle, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(14.dp))
                                                        Spacer(modifier = Modifier.width(4.dp))
                                                        val min = topic.timestampSeconds / 60
                                                        val sec = topic.timestampSeconds % 60
                                                        Text(
                                                            text = "Listen @ ${String.format("%02d:%02d", min, sec)}",
                                                            style = MaterialTheme.typography.labelSmall,
                                                            fontFamily = FontFamily.Monospace,
                                                            fontWeight = FontWeight.Bold,
                                                            color = MaterialTheme.colorScheme.onPrimaryContainer
                                                        )
                                                    }
                                                }
                                            }

                                            // Formula Blueprint Box
                                            Surface(
                                                shape = RoundedCornerShape(8.dp),
                                                color = Color(0xFF0F172A),
                                                modifier = Modifier.fillMaxWidth()
                                            ) {
                                                Column(modifier = Modifier.padding(12.dp)) {
                                                    Text(
                                                        text = topic.formula,
                                                        style = MaterialTheme.typography.titleMedium,
                                                        fontFamily = FontFamily.Monospace,
                                                        fontWeight = FontWeight.Bold,
                                                        color = Color.White
                                                    )
                                                }
                                            }

                                            if (topic.variablesAndUnits.isNotEmpty()) {
                                                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                                    Text(
                                                        text = "Variable Dictionary & Standard SI Units:",
                                                        style = MaterialTheme.typography.labelSmall,
                                                        fontWeight = FontWeight.Bold,
                                                        color = MaterialTheme.colorScheme.onSurface
                                                    )
                                                    topic.variablesAndUnits.forEach { v ->
                                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                                            Text("• ", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                                            Text(text = v, style = MaterialTheme.typography.bodySmall)
                                                        }
                                                    }
                                                }
                                            }

                                            if (topic.exampleProblem.isNotBlank()) {
                                                Surface(
                                                    shape = RoundedCornerShape(8.dp),
                                                    color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.4f),
                                                    modifier = Modifier.fillMaxWidth()
                                                ) {
                                                    Column(modifier = Modifier.padding(10.dp)) {
                                                        Text("Standard Numerical Application:", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSecondaryContainer)
                                                        Spacer(modifier = Modifier.height(2.dp))
                                                        Text(topic.exampleProblem, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSecondaryContainer)
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    3 -> { // Engineering Diagrams
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 16.dp),
                            contentPadding = PaddingValues(top = 12.dp, bottom = 24.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            items(topics) { topic ->
                                topic.diagram?.let { diagramData ->
                                    Column {
                                        Text(
                                            text = "Topic Diagram: ${topic.topicName}",
                                            style = MaterialTheme.typography.titleSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.padding(bottom = 6.dp)
                                        )
                                        EngineeringDiagramCanvas(diagramData = diagramData)
                                    }
                                }
                            }
                        }
                    }

                    4 -> { // Ask AI Chat
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 16.dp, vertical = 8.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                SuggestionChip(
                                    onClick = { viewModel.sendAiChatMessage("Give me a 5-mark answer for this lecture") },
                                    label = { Text("5-Mark Answer", fontSize = 11.sp) }
                                )
                                SuggestionChip(
                                    onClick = { viewModel.sendAiChatMessage("Explain the whiteboard derivations and diagram") },
                                    label = { Text("Explain Derivation", fontSize = 11.sp) }
                                )
                                SuggestionChip(
                                    onClick = { viewModel.sendAiChatMessage("What are the key formulas and SI units?") },
                                    label = { Text("Formulas", fontSize = 11.sp) }
                                )
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            LazyColumn(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxWidth(),
                                verticalArrangement = Arrangement.spacedBy(10.dp),
                                contentPadding = PaddingValues(bottom = 12.dp)
                            ) {
                                items(chatMessages) { msg ->
                                    val isUser = msg.sender == "user"
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
                                    ) {
                                        Surface(
                                            shape = RoundedCornerShape(12.dp),
                                            color = if (isUser) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                                            modifier = Modifier.fillMaxWidth(0.85f)
                                        ) {
                                            Column(modifier = Modifier.padding(12.dp)) {
                                                Text(
                                                    text = if (isUser) "You" else "AI Exam Tutor",
                                                    style = MaterialTheme.typography.labelSmall,
                                                    fontWeight = FontWeight.Bold,
                                                    color = if (isUser) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.primary
                                                )
                                                Spacer(modifier = Modifier.height(4.dp))
                                                Text(
                                                    text = msg.message,
                                                    style = MaterialTheme.typography.bodyMedium,
                                                    color = if (isUser) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                                                    lineHeight = 20.sp
                                                )
                                            }
                                        }
                                    }
                                }

                                if (isChatAiThinking) {
                                    item {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier.padding(8.dp)
                                        ) {
                                            CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text("AI Tutor is analyzing lecture notes...", style = MaterialTheme.typography.bodySmall)
                                        }
                                    }
                                }
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                OutlinedTextField(
                                    value = chatInputText,
                                    onValueChange = { chatInputText = it },
                                    placeholder = { Text("Ask anything about this lecture...") },
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("ai_chat_input"),
                                    singleLine = true
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                IconButton(
                                    onClick = {
                                        if (chatInputText.isNotBlank()) {
                                            viewModel.sendAiChatMessage(chatInputText)
                                            chatInputText = ""
                                        }
                                    },
                                    modifier = Modifier
                                        .size(48.dp)
                                        .background(MaterialTheme.colorScheme.primary, CircleShape)
                                ) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.Send,
                                        contentDescription = "Send",
                                        tint = MaterialTheme.colorScheme.onPrimary
                                    )
                                }
                            }
                        }
                    }

                    5 -> { // Raw & Clean Transcript
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp),
                            contentPadding = PaddingValues(bottom = 24.dp),
                            verticalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            item {
                                Card(
                                    shape = RoundedCornerShape(12.dp),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                                ) {
                                    Column(modifier = Modifier.padding(16.dp)) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(imageVector = Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF10B981))
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(
                                                text = "Clean Technical Transcript (Fillers Removed)",
                                                style = MaterialTheme.typography.titleSmall,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text(
                                            text = lecture.cleanTranscript,
                                            style = MaterialTheme.typography.bodyMedium,
                                            lineHeight = 22.sp
                                        )
                                    }
                                }
                            }

                            item {
                                Card(
                                    shape = RoundedCornerShape(12.dp),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                                ) {
                                    Column(modifier = Modifier.padding(16.dp)) {
                                        Text(
                                            text = "Original Audio Spoken Content (${lecture.spokenLanguage})",
                                            style = MaterialTheme.typography.titleSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text(
                                            text = lecture.originalTranscript,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            lineHeight = 20.sp
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Delete Video Only Confirmation Dialog
    if (showDeleteVideoDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteVideoDialog = false },
            title = { Text("Delete Video to Free Storage?") },
            text = {
                Text("This will delete the ${lecture.videoFileSizeMb.toInt()} MB video file from your device storage.\n\n✅ Your audio recording and all generated AI exam notes will be 100% preserved!")
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteVideoOnly(lecture.id)
                        showDeleteVideoDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Delete Video File")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteVideoDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Delete Audio Only Confirmation Dialog
    if (showDeleteAudioDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteAudioDialog = false },
            title = { Text("Delete Audio File?") },
            text = {
                Text("This will delete the audio file. Your generated AI exam notes and flashcards will remain saved.")
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteAudioOnly(lecture.id)
                        showDeleteAudioDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Delete Audio")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteAudioDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun TopicCard(
    topic: TopicSection,
    isCurrentlyPlaying: Boolean,
    onJumpToAudio: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isCurrentlyPlaying) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f) else MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = if (isCurrentlyPlaying) CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(MaterialTheme.colorScheme.primary)) else null
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Header: Topic Title & Timestamp Jump Button
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = topic.topicName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Surface(
                    onClick = onJumpToAudio,
                    shape = RoundedCornerShape(20.dp),
                    color = MaterialTheme.colorScheme.primaryContainer
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayCircle,
                            contentDescription = "Jump to audio",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        val min = topic.timestampSeconds / 60
                        val sec = topic.timestampSeconds % 60
                        Text(
                            text = String.format("%02d:%02d", min, sec),
                            style = MaterialTheme.typography.labelSmall,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            }

            // 1. Definition Section
            Column {
                Text(
                    text = "• Definition:",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = topic.definition,
                    style = MaterialTheme.typography.bodyMedium,
                    lineHeight = 20.sp
                )
            }

            // 2. Simple Explanation
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            ) {
                Row(modifier = Modifier.padding(10.dp)) {
                    Icon(
                        imageVector = Icons.Default.Lightbulb,
                        contentDescription = null,
                        tint = Color(0xFFF59E0B),
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = "Simple English Explanation:",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = topic.simpleExplanation,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // 3. Key Points
            if (topic.keyPoints.isNotEmpty()) {
                Column {
                    Text(
                        text = "• Key Points:",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    topic.keyPoints.forEach { pt ->
                        Row(
                            modifier = Modifier.padding(vertical = 2.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            Text("– ", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                            Text(text = pt, style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }
            }

            // 4. Working / Process
            if (topic.workingProcess.isNotBlank()) {
                Column {
                    Text(
                        text = "• Working Process:",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = topic.workingProcess,
                        style = MaterialTheme.typography.bodyMedium,
                        lineHeight = 20.sp
                    )
                }
            }

            // 5. Formula & Variables
            if (topic.formula.isNotBlank()) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFF0F172A) // Blueprint Dark
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.Functions, contentDescription = null, tint = Color(0xFF38BDF8))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Formula & Derivation Term:",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF38BDF8)
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = topic.formula,
                            style = MaterialTheme.typography.titleSmall,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        if (topic.variablesAndUnits.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(8.dp))
                            HorizontalDivider(color = Color(0xFF334155))
                            Spacer(modifier = Modifier.height(6.dp))
                            topic.variablesAndUnits.forEach { v ->
                                Text(
                                    text = v,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color(0xFF94A3B8),
                                    fontSize = 11.sp
                                )
                            }
                        }
                    }
                }
            }

            // 6. Vector Engineering Diagram (Embedded near explanation)
            topic.diagram?.let { diagramData ->
                Spacer(modifier = Modifier.height(4.dp))
                EngineeringDiagramCanvas(diagramData = diagramData)
            }

            // 7. Example Numerical / Calculation
            if (topic.exampleProblem.isNotBlank()) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f)
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Text(
                            text = "Practical Calculation Example:",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = topic.exampleProblem,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    }
                }
            }

            // 8. High Yield Exam Tips
            if (topic.importantExamPoints.isNotEmpty()) {
                Column {
                    topic.importantExamPoints.forEach { tip ->
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = Color(0xFFFEF2F2),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 2.dp)
                        ) {
                            Text(
                                text = tip,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF991B1B),
                                modifier = Modifier.padding(8.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
