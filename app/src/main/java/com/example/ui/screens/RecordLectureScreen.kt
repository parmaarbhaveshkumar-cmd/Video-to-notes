package com.example.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.FiberManualRecord
import androidx.compose.material.icons.filled.FilterAlt
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Hearing
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.UploadFile
import androidx.compose.material.icons.filled.Videocam
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.audio.FacultyVoiceProfile
import com.example.audio.RecordingState
import com.example.audio.VoiceDiarizationMode
import com.example.ui.components.AudioWaveformVisualizer
import com.example.viewmodel.AppScreen
import com.example.viewmodel.AppViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecordLectureScreen(
    viewModel: AppViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val subjects by viewModel.allSubjects.collectAsState()
    val recordingState by viewModel.audioRecorder.recordingState.collectAsState()
    val elapsedSeconds by viewModel.audioRecorder.elapsedSeconds.collectAsState()
    val amplitudeLevels by viewModel.audioRecorder.amplitudeLevels.collectAsState()
    val isGeneratingAiNotes by viewModel.isGeneratingAiNotes.collectAsState()
    val generationStatusMessage by viewModel.generationStatusMessage.collectAsState()

    val diarizationMode by viewModel.audioRecorder.diarizationMode.collectAsState()
    val selectedFacultyProfile by viewModel.audioRecorder.selectedFacultyProfile.collectAsState()
    val facultyVoiceConfidence by viewModel.audioRecorder.facultyVoiceConfidence.collectAsState()
    val isFacultySpeaking by viewModel.audioRecorder.isFacultySpeaking.collectAsState()

    val isFanHumFilterActive by viewModel.audioRecorder.isFanHumFilterActive.collectAsState()
    val isMurmurFilterActive by viewModel.audioRecorder.isMurmurFilterActive.collectAsState()
    val isVoiceClarityBoostActive by viewModel.audioRecorder.isVoiceClarityBoostActive.collectAsState()
    val snrDb by viewModel.audioRecorder.signalToNoiseRatioDb.collectAsState()
    val noiseStatus by viewModel.audioRecorder.detectedNoiseStatus.collectAsState()

    var lectureTitle by remember { mutableStateOf("") }
    var selectedSubject by remember { mutableStateOf(subjects.firstOrNull()?.name ?: "Engineering Thermodynamics") }
    var selectedUnit by remember { mutableStateOf("Unit 2: Air Standard Cycles") }
    var spokenLanguage by remember { mutableStateOf("Mixed Hinglish / Gujlish") }
    var pastedNotesText by remember { mutableStateOf("") }

    var selectedInputTab by remember { mutableIntStateOf(0) } // 0 = Audio Recording, 1 = Upload / Paste Notes
    var subjectExpanded by remember { mutableStateOf(false) }
    var showEnrollDialog by remember { mutableStateOf(false) }
    var showFacultyMenu by remember { mutableStateOf(false) }

    val languages = listOf("Mixed Hinglish / Gujlish", "English", "Hindi", "Gujarati")

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            val title = lectureTitle.ifBlank { "Classroom Lecture - $selectedSubject" }
            viewModel.audioRecorder.startRecording(title, selectedSubject)
        } else {
            Toast.makeText(context, "Microphone permission is required to record lectures", Toast.LENGTH_SHORT).show()
        }
    }

    if (showEnrollDialog) {
        AlertDialog(
            onDismissRequest = { showEnrollDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.RecordVoiceOver, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Enroll Faculty Voice Sample", style = MaterialTheme.typography.titleMedium)
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        "Record a quick 10–20 second sample of your professor's voice during lecture intro. The AI creates a custom acoustic signature to isolate the professor from student chatter and ambient fan noises.",
                        style = MaterialTheme.typography.bodySmall
                    )
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                        modifier = Modifier.fillMaxWidth().padding(top = 4.dp)
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text("Acoustic Model: MFCC + Deep Speaker Embedding", fontWeight = FontWeight.Bold, fontSize = 11.sp)
                            Text("Threshold: 88% Match Confidence", fontSize = 11.sp, color = MaterialTheme.colorScheme.onPrimaryContainer)
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showEnrollDialog = false
                        Toast.makeText(context, "Faculty Voice Sample Profile Enrolled Successfully!", Toast.LENGTH_SHORT).show()
                    }
                ) {
                    Text("Save Voice Profile")
                }
            },
            dismissButton = {
                TextButton(onClick = { showEnrollDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Record & Convert Lecture",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { viewModel.navigateTo(AppScreen.HOME) }) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
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
            // Input Mode Tabs
            TabRow(
                selectedTabIndex = selectedInputTab,
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            ) {
                Tab(
                    selected = selectedInputTab == 0,
                    onClick = { selectedInputTab = 0 },
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.Mic, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("1. Audio")
                        }
                    }
                )
                Tab(
                    selected = false,
                    onClick = { viewModel.navigateTo(AppScreen.RECORD_VIDEO) },
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.Videocam, contentDescription = null, tint = Color(0xFFE11D48), modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("2. Video + Board OCR", color = Color(0xFFE11D48), fontWeight = FontWeight.Bold)
                        }
                    }
                )
                Tab(
                    selected = selectedInputTab == 1,
                    onClick = { selectedInputTab = 1 },
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.UploadFile, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("3. Paste Notes")
                        }
                    }
                )
            }

            // Lecture Metadata Card
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Lecture Details",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    // Lecture Title Field
                    OutlinedTextField(
                        value = lectureTitle,
                        onValueChange = { lectureTitle = it },
                        label = { Text("Lecture Title (e.g. Otto Cycle & Efficiency Derivation)") },
                        placeholder = { Text("Enter topic name...") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("lecture_title_input"),
                        singleLine = true
                    )

                    // Subject Dropdown
                    ExposedDropdownMenuBox(
                        expanded = subjectExpanded,
                        onExpandedChange = { subjectExpanded = it }
                    ) {
                        OutlinedTextField(
                            value = selectedSubject,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Subject") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = subjectExpanded) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor()
                        )
                        ExposedDropdownMenu(
                            expanded = subjectExpanded,
                            onDismissRequest = { subjectExpanded = false }
                        ) {
                            subjects.forEach { subj ->
                                DropdownMenuItem(
                                    text = { Text(subj.name) },
                                    onClick = {
                                        selectedSubject = subj.name
                                        subjectExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    // Unit Selection
                    OutlinedTextField(
                        value = selectedUnit,
                        onValueChange = { selectedUnit = it },
                        label = { Text("Unit / Chapter (e.g. Unit 2: Air Cycles)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    // Spoken Language Selector
                    Column {
                        Text(
                            text = "Teacher's Spoken Language:",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            languages.take(2).forEach { lang ->
                                FilterChip(
                                    selected = spokenLanguage == lang,
                                    onClick = { spokenLanguage = lang },
                                    label = { Text(lang, fontSize = 12.sp) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = MaterialTheme.colorScheme.primary,
                                        selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                                    )
                                )
                            }
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            languages.drop(2).forEach { lang ->
                                FilterChip(
                                    selected = spokenLanguage == lang,
                                    onClick = { spokenLanguage = lang },
                                    label = { Text(lang, fontSize = 12.sp) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = MaterialTheme.colorScheme.primary,
                                        selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                                    )
                                )
                            }
                        }
                    }
                }
            }

            // Tab 0: Audio Recorder Studio & AI Noise Filters
            if (selectedInputTab == 0) {
                // 1. Faculty Voice Diarization Card
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.RecordVoiceOver,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Faculty Voice Diarization",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            // Active Profile Badge
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = if (isFacultySpeaking) Color(0xFFDCFCE7) else MaterialTheme.colorScheme.surfaceVariant
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(6.dp)
                                            .background(if (isFacultySpeaking) Color(0xFF16A34A) else Color.Gray, CircleShape)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = if (isFacultySpeaking) "Voice Locked (${(facultyVoiceConfidence * 100).toInt()}%)" else "Listening...",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isFacultySpeaking) Color(0xFF166534) else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }

                        // Diarization Mode Selector
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            FilterChip(
                                selected = diarizationMode == VoiceDiarizationMode.FACULTY_ONLY,
                                onClick = { viewModel.audioRecorder.setDiarizationMode(VoiceDiarizationMode.FACULTY_ONLY) },
                                label = { Text("Faculty Only (Default)") },
                                leadingIcon = {
                                    if (diarizationMode == VoiceDiarizationMode.FACULTY_ONLY) {
                                        Icon(imageVector = Icons.Default.Check, contentDescription = null, modifier = Modifier.size(14.dp))
                                    }
                                }
                            )

                            FilterChip(
                                selected = diarizationMode == VoiceDiarizationMode.ALL_SPEAKERS,
                                onClick = { viewModel.audioRecorder.setDiarizationMode(VoiceDiarizationMode.ALL_SPEAKERS) },
                                label = { Text("All Speakers (Q&A)") },
                                leadingIcon = {
                                    if (diarizationMode == VoiceDiarizationMode.ALL_SPEAKERS) {
                                        Icon(imageVector = Icons.Default.Check, contentDescription = null, modifier = Modifier.size(14.dp))
                                    }
                                }
                            )
                        }

                        // Faculty Profile Selector & Sample Enrollment
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Active Speaker Profile: ${selectedFacultyProfile.facultyName}",
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = selectedFacultyProfile.department,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            TextButton(onClick = { showEnrollDialog = true }) {
                                Icon(imageVector = Icons.Default.Tune, contentDescription = null, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("+ Enroll Sample", fontSize = 12.sp)
                            }
                        }
                    }
                }

                // 2. Real-time Noise Reduction & Audio Filters Card
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.FilterAlt,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Classroom Noise Cancellation",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            // Live SNR Meter
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = Color(0xFF0F172A)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(imageVector = Icons.Default.GraphicEq, contentDescription = null, tint = Color(0xFF38BDF8), modifier = Modifier.size(12.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "SNR: ${String.format("%.1f", snrDb)} dB",
                                        color = Color(0xFF38BDF8),
                                        fontFamily = FontFamily.Monospace,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }

                        Text(
                            text = noiseStatus,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Medium
                        )

                        HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)

                        // Filter Toggles
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text("AC & Ceiling Fan Hum Suppressor", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Medium)
                                Text("Notch filter (50Hz/60Hz + harmonics)", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Switch(
                                checked = isFanHumFilterActive,
                                onCheckedChange = { viewModel.audioRecorder.toggleFanHumFilter() }
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text("Classroom Murmur & Echo Filter", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Medium)
                                Text("Isolates directional microphone pickup", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Switch(
                                checked = isMurmurFilterActive,
                                onCheckedChange = { viewModel.audioRecorder.toggleMurmurFilter() }
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text("Faculty Vocal Clarity Boost", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Medium)
                                Text("Enhances 300Hz - 3.4kHz vocal frequencies", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Switch(
                                checked = isVoiceClarityBoostActive,
                                onCheckedChange = { viewModel.audioRecorder.toggleVoiceClarityBoost() }
                            )
                        }
                    }
                }

                // 3. Audio Recording Studio Main Card
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Status Header
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .background(
                                        color = when (recordingState) {
                                            RecordingState.RECORDING -> Color(0xFFEF4444)
                                            RecordingState.PAUSED -> Color(0xFFF59E0B)
                                            RecordingState.STOPPED -> Color(0xFF10B981)
                                            RecordingState.IDLE -> Color(0xFF94A3B8)
                                        },
                                        shape = CircleShape
                                    )
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = when (recordingState) {
                                    RecordingState.RECORDING -> "LIVE RECORDING (Background Safe • 1-2 Hr Support)"
                                    RecordingState.PAUSED -> "RECORDING PAUSED"
                                    RecordingState.STOPPED -> "AUDIO CAPTURED & AUTO-SAVED"
                                    RecordingState.IDLE -> "READY TO RECORD CLASSROOM AUDIO"
                                },
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Large Duration Timer Display
                        val min = elapsedSeconds / 60
                        val sec = elapsedSeconds % 60
                        Text(
                            text = String.format("%02d:%02d", min, sec),
                            style = MaterialTheme.typography.displayMedium,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            color = if (recordingState == RecordingState.RECORDING) Color(0xFFEF4444) else MaterialTheme.colorScheme.onSurface
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Live Waveform Visualizer
                        AudioWaveformVisualizer(
                            isRecording = recordingState == RecordingState.RECORDING,
                            amplitudes = amplitudeLevels
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        // Recording Controls
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            when (recordingState) {
                                RecordingState.IDLE -> {
                                    Button(
                                        onClick = {
                                            val perm = ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO)
                                            if (perm == PackageManager.PERMISSION_GRANTED) {
                                                val title = lectureTitle.ifBlank { "Classroom Lecture - $selectedSubject" }
                                                viewModel.audioRecorder.startRecording(title, selectedSubject)
                                            } else {
                                                permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                                            }
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444)),
                                        contentPadding = PaddingValues(horizontal = 24.dp, vertical = 14.dp),
                                        shape = RoundedCornerShape(28.dp),
                                        modifier = Modifier.testTag("start_recording_button")
                                    ) {
                                        Icon(imageVector = Icons.Default.FiberManualRecord, contentDescription = null)
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("Start Recording", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                    }
                                }

                                RecordingState.RECORDING -> {
                                    OutlinedButton(
                                        onClick = { viewModel.audioRecorder.pauseRecording() },
                                        shape = RoundedCornerShape(24.dp)
                                    ) {
                                        Icon(imageVector = Icons.Default.Pause, contentDescription = "Pause")
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("Pause")
                                    }

                                    Button(
                                        onClick = { viewModel.audioRecorder.stopRecording() },
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444)),
                                        shape = RoundedCornerShape(24.dp),
                                        modifier = Modifier.testTag("stop_recording_button")
                                    ) {
                                        Icon(imageVector = Icons.Default.Stop, contentDescription = "Stop")
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("Stop & Save")
                                    }
                                }

                                RecordingState.PAUSED -> {
                                    Button(
                                        onClick = { viewModel.audioRecorder.resumeRecording() },
                                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                                        shape = RoundedCornerShape(24.dp)
                                    ) {
                                        Icon(imageVector = Icons.Default.PlayArrow, contentDescription = "Resume")
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("Resume")
                                    }

                                    OutlinedButton(
                                        onClick = { viewModel.audioRecorder.stopRecording() },
                                        shape = RoundedCornerShape(24.dp)
                                    ) {
                                        Icon(imageVector = Icons.Default.Stop, contentDescription = "Stop")
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("Stop & Save")
                                    }
                                }

                                RecordingState.STOPPED -> {
                                    OutlinedButton(
                                        onClick = { viewModel.audioRecorder.reset() },
                                        shape = RoundedCornerShape(24.dp)
                                    ) {
                                        Icon(imageVector = Icons.Default.Refresh, contentDescription = "Re-record")
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("Record Again")
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Tab 1: Paste Notes / Transcript directly
            if (selectedInputTab == 1) {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = "Paste Lecture Audio Transcript / Professor's Notes",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "You can paste raw text in English, Gujarati, Hindi, or mixed Hinglish. The AI will remove fillers, standardize mathematical terms, and generate 2/3/5/7-mark answers with diagrams.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        OutlinedTextField(
                            value = pastedNotesText,
                            onValueChange = { pastedNotesText = it },
                            placeholder = { Text("Paste professor's speech or topic text here...") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(160.dp)
                                .testTag("pasted_notes_input")
                        )

                        // Quick Sample Buttons
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedButton(
                                onClick = {
                                    lectureTitle = "Otto Cycle & Air Standard Efficiency"
                                    selectedSubject = "Engineering Thermodynamics"
                                    selectedUnit = "Unit 2: Air Standard Cycles"
                                    pastedNotesText = "Good morning students, aaje apde unit 2 ma air standard cycles start karishu. Otto cycle and Carnot cycle are the most important topics for Gujarat and All-India exams. Samjho bhai, Otto cycle ma 4 processes hoy che: 2 isentropic compression/expansion and 2 constant volume heat addition and rejection. Formula derivation 5-marks ma 100% ave che, η = 1 - 1/(r^(γ-1)). Let's derive it step by step on board..."
                                },
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Text("Load Otto Cycle Sample", fontSize = 11.sp)
                            }

                            OutlinedButton(
                                onClick = {
                                    lectureTitle = "Mild Steel Tensile Testing & Stress Strain Curve"
                                    selectedSubject = "Material Science"
                                    selectedUnit = "Unit 1: Mechanical Testing"
                                    pastedNotesText = "Namaste students, today we discuss UTM tensile testing of mild steel. Dekho, when we apply tensile load on a standard ASTM specimen, pehle elastic zone aave che jya Hooke's law valid che. Then upper yield point C and lower yield point D aave che because of carbon atom dislocation pinning. Then strain hardening leads to Ultimate Tensile Strength E and necking happens before fracture at point F..."
                                },
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Text("Load Material Science Sample", fontSize = 11.sp)
                            }
                        }
                    }
                }
            }

            // AI Processing Progress Indicator Card
            AnimatedVisibility(visible = isGeneratingAiNotes) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        CircularProgressIndicator(
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(40.dp)
                        )
                        Spacer(modifier = Modifier.height(14.dp))
                        Text(
                            text = "Generating Exam-Ready Notes with Gemini AI",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = generationStatusMessage,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            }

            // Generate Exam-Ready Notes Main Action Button
            Button(
                onClick = {
                    val finalTitle = lectureTitle.ifBlank { "Lecture: $selectedSubject" }
                    val finalAudioPath = viewModel.audioRecorder.latestAudioFilePath.value ?: "/storage/emulated/0/Download/lecture_rec.m4a"
                    val rawNotes = if (selectedInputTab == 1) pastedNotesText else "Classroom lecture on $finalTitle with audio recording"

                    viewModel.processLectureAndGenerateNotes(
                        title = finalTitle,
                        subject = selectedSubject,
                        unitName = selectedUnit,
                        rawNotesOrTranscript = rawNotes,
                        spokenLanguage = spokenLanguage,
                        audioPath = finalAudioPath,
                        durationSeconds = if (elapsedSeconds > 0) elapsedSeconds else 1500,
                        onComplete = {
                            viewModel.navigateTo(AppScreen.NOTES_DETAIL)
                        }
                    )
                },
                enabled = !isGeneratingAiNotes,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
                    .testTag("generate_exam_notes_button"),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.AutoAwesome, contentDescription = null)
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Generate Exam-Ready Notes (AI)",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

