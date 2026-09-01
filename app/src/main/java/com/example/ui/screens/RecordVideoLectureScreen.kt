package com.example.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.BatteryChargingFull
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Cameraswitch
import androidx.compose.material.icons.filled.DocumentScanner
import androidx.compose.material.icons.filled.FiberManualRecord
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SdCard
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Stop
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
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.example.audio.RecordingState
import com.example.data.model.BoardKeyFrame
import com.example.data.model.VideoQualityProfile
import com.example.video.CameraFacing
import com.example.viewmodel.AppScreen
import com.example.viewmodel.AppViewModel
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecordVideoLectureScreen(
    viewModel: AppViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    val subjects by viewModel.allSubjects.collectAsState()
    val recordingState by viewModel.videoRecorder.recordingState.collectAsState()
    val elapsedSeconds by viewModel.videoRecorder.elapsedSeconds.collectAsState()
    val capturedKeyFrames by viewModel.videoRecorder.capturedBoardFrames.collectAsState()
    val selectedQuality by viewModel.videoRecorder.selectedQuality.collectAsState()
    val cameraFacing by viewModel.videoRecorder.cameraFacing.collectAsState()
    val estimatedSizeMb by viewModel.videoRecorder.latestVideoFileSizeMb.collectAsState()
    val availableStorageGb by viewModel.videoRecorder.availableStorageGb.collectAsState()
    val batteryPercent by viewModel.videoRecorder.batteryPercentage.collectAsState()
    val isFacultySpeaking by viewModel.videoRecorder.isFacultySpeaking.collectAsState()
    val facultyVoiceConfidence by viewModel.videoRecorder.facultyVoiceConfidence.collectAsState()

    val isGeneratingAiNotes by viewModel.isGeneratingAiNotes.collectAsState()
    val generationStatusMessage by viewModel.generationStatusMessage.collectAsState()

    var lectureTitle by remember { mutableStateOf("") }
    var selectedSubject by remember { mutableStateOf(subjects.firstOrNull()?.name ?: "Engineering Thermodynamics") }
    var selectedUnit by remember { mutableStateOf("Unit 2: Air Standard Cycles") }
    var spokenLanguage by remember { mutableStateOf("Mixed Hinglish / Gujlish") }
    var subjectExpanded by remember { mutableStateOf(false) }
    var showPrivacyDialog by remember { mutableStateOf(false) }
    var isAutoKeyframeActive by remember { mutableStateOf(true) }

    val languages = listOf("Mixed Hinglish / Gujlish", "English", "Hindi", "Gujarati")

    // Multiple permissions launcher (Camera + Audio)
    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
        )
    }
    var hasAudioPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionsLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        hasCameraPermission = permissions[Manifest.permission.CAMERA] ?: hasCameraPermission
        hasAudioPermission = permissions[Manifest.permission.RECORD_AUDIO] ?: hasAudioPermission

        if (hasCameraPermission && hasAudioPermission) {
            Toast.makeText(context, "Camera & Audio permissions granted for lecture recording", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(context, "Camera and Audio permissions are required to record video lectures", Toast.LENGTH_LONG).show()
        }
    }

    LaunchedEffect(Unit) {
        if (!hasCameraPermission || !hasAudioPermission) {
            permissionsLauncher.launch(
                arrayOf(Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO)
            )
        }
    }

    // Processing overlay dialog
    if (isGeneratingAiNotes) {
        AlertDialog(
            onDismissRequest = {},
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.AutoAwesome, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("AI Video & Board Engine")
                }
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    CircularProgressIndicator(modifier = Modifier.size(48.dp))
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = generationStatusMessage,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Extracting OCR formulas, board derivations, and generating 2/3/5/7-mark answers...",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            },
            confirmButton = {}
        )
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .background(Color(0xFFE11D48), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Videocam,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Video Lecture Recorder",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Whiteboard OCR & Derivation Capture",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = {
                            if (recordingState == RecordingState.RECORDING || recordingState == RecordingState.PAUSED) {
                                viewModel.videoRecorder.stopRecording()
                            }
                            viewModel.navigateTo(AppScreen.HOME)
                        },
                        modifier = Modifier.testTag("btn_back_video_recorder")
                    ) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { showPrivacyDialog = true }) {
                        Icon(
                            imageVector = Icons.Default.Security,
                            contentDescription = "Privacy Policy",
                            tint = MaterialTheme.colorScheme.primary
                        )
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
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .testTag("record_video_scroll_container")
        ) {
            // 1. Live Camera Viewport / Preview
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(280.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .testTag("video_camera_preview_card"),
                colors = CardDefaults.cardColors(containerColor = Color.Black),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Box(modifier = Modifier.fillMaxSize()) {
                    if (hasCameraPermission) {
                        AndroidView(
                            factory = { ctx ->
                                PreviewView(ctx).apply {
                                    scaleType = PreviewView.ScaleType.FILL_CENTER
                                    viewModel.videoRecorder.bindCameraPreview(lifecycleOwner, this)
                                }
                            },
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Videocam,
                                contentDescription = null,
                                tint = Color.LightGray,
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text("Camera permission required", color = Color.White, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(8.dp))
                            Button(
                                onClick = {
                                    permissionsLauncher.launch(
                                        arrayOf(Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO)
                                    )
                                }
                            ) {
                                Text("Grant Permissions")
                            }
                        }
                    }

                    // Top Overlays on Video Preview (Status, Quality, Battery, Storage)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp)
                            .align(Alignment.TopCenter),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Recording Indicator & Timer Badge
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = when (recordingState) {
                                RecordingState.RECORDING -> Color(0xFFDC2626)
                                RecordingState.PAUSED -> Color(0xFFD97706)
                                else -> Color.Black.copy(alpha = 0.6f)
                            }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = if (recordingState == RecordingState.RECORDING) Icons.Default.FiberManualRecord else Icons.Default.Videocam,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                val minutes = elapsedSeconds / 60
                                val seconds = elapsedSeconds % 60
                                Text(
                                    text = String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds),
                                    color = Color.White,
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                )
                            }
                        }

                        // Right side info: Quality, Storage, Battery
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = Color.Black.copy(alpha = 0.6f)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.SdCard, contentDescription = null, tint = Color.LightGray, modifier = Modifier.size(12.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "${availableStorageGb.toInt()} GB Free",
                                        color = Color.White,
                                        fontSize = 10.sp
                                    )
                                }
                            }

                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = Color.Black.copy(alpha = 0.6f)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.BatteryChargingFull, contentDescription = null, tint = Color(0xFF4ADE80), modifier = Modifier.size(12.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "$batteryPercent%",
                                        color = Color.White,
                                        fontSize = 10.sp
                                    )
                                }
                            }

                            // Camera Switcher Button
                            IconButton(
                                onClick = { viewModel.videoRecorder.switchCamera() },
                                modifier = Modifier
                                    .size(32.dp)
                                    .background(Color.Black.copy(alpha = 0.6f), CircleShape)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Cameraswitch,
                                    contentDescription = "Switch Camera",
                                    tint = Color.White,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }

                    // Bottom Floating Overlays (Live OCR & Snapshots Count)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp)
                            .align(Alignment.BottomCenter),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color.Black.copy(alpha = 0.7f)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.DocumentScanner,
                                    contentDescription = null,
                                    tint = Color(0xFF38BDF8),
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (isFacultySpeaking) "Faculty Speaking (${(facultyVoiceConfidence * 100).toInt()}%)" else "Listening...",
                                    color = Color(0xFFE0F2FE),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color(0xFF0F172A).copy(alpha = 0.8f)
                        ) {
                            Text(
                                text = "📸 ${capturedKeyFrames.size} Board Frames",
                                color = Color(0xFFFDE047),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 2. Video Controls & Manual Board Snapshot Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Manual Snapshot Button
                OutlinedButton(
                    onClick = {
                        viewModel.videoRecorder.captureManualBoardSnapshot(
                            title = "Board Snapshot",
                            note = "Salient diagram / derivation marked at ${elapsedSeconds}s"
                        )
                        Toast.makeText(context, "📸 Board Snapshot Captured at ${elapsedSeconds}s!", Toast.LENGTH_SHORT).show()
                    },
                    enabled = recordingState == RecordingState.RECORDING || recordingState == RecordingState.PAUSED,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.testTag("btn_capture_board_snapshot")
                ) {
                    Icon(imageVector = Icons.Default.CameraAlt, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Snap Board")
                }

                // Main Record / Pause / Resume / Stop Buttons
                when (recordingState) {
                    RecordingState.IDLE, RecordingState.STOPPED -> {
                        Button(
                            onClick = {
                                val title = lectureTitle.ifBlank { "Classroom Video - $selectedSubject" }
                                viewModel.videoRecorder.startRecording(
                                    lectureTitle = title,
                                    subject = selectedSubject
                                )
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE11D48)),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier
                                .height(50.dp)
                                .testTag("btn_start_video_record")
                        ) {
                            Icon(imageVector = Icons.Default.FiberManualRecord, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Start Recording", fontWeight = FontWeight.Bold)
                        }
                    }

                    RecordingState.RECORDING -> {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Button(
                                onClick = { viewModel.videoRecorder.pauseRecording() },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD97706)),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.testTag("btn_pause_video_record")
                            ) {
                                Icon(imageVector = Icons.Default.Pause, contentDescription = "Pause")
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Pause")
                            }

                            Button(
                                onClick = {
                                    val videoPath = viewModel.videoRecorder.stopRecording() ?: ""
                                    triggerVideoNoteGeneration(
                                        viewModel = viewModel,
                                        title = lectureTitle,
                                        subject = selectedSubject,
                                        unitName = selectedUnit,
                                        language = spokenLanguage,
                                        videoPath = videoPath,
                                        quality = selectedQuality.resolution,
                                        keyFrames = capturedKeyFrames,
                                        videoSizeMb = estimatedSizeMb,
                                        duration = elapsedSeconds
                                    )
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.testTag("btn_stop_video_record")
                            ) {
                                Icon(imageVector = Icons.Default.Stop, contentDescription = "Stop & Generate Notes")
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Stop & Notes", fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    RecordingState.PAUSED -> {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Button(
                                onClick = { viewModel.videoRecorder.resumeRecording() },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF059669)),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.testTag("btn_resume_video_record")
                            ) {
                                Icon(imageVector = Icons.Default.PlayArrow, contentDescription = "Resume")
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Resume")
                            }

                            Button(
                                onClick = {
                                    val videoPath = viewModel.videoRecorder.stopRecording() ?: ""
                                    triggerVideoNoteGeneration(
                                        viewModel = viewModel,
                                        title = lectureTitle,
                                        subject = selectedSubject,
                                        unitName = selectedUnit,
                                        language = spokenLanguage,
                                        videoPath = videoPath,
                                        quality = selectedQuality.resolution,
                                        keyFrames = capturedKeyFrames,
                                        videoSizeMb = estimatedSizeMb,
                                        duration = elapsedSeconds
                                    )
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.testTag("btn_stop_paused_video_record")
                            ) {
                                Icon(imageVector = Icons.Default.Stop, contentDescription = "Finish")
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Finish", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            // 3. Captured Key-Frames Thumbnail Strip
            if (capturedKeyFrames.isNotEmpty()) {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Extracted Whiteboard Snapshots (${capturedKeyFrames.size})",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(vertical = 4.dp)
                ) {
                    items(capturedKeyFrames) { frame ->
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier
                                .width(160.dp)
                                .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(12.dp))
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    val min = frame.timestampSeconds / 60
                                    val sec = frame.timestampSeconds % 60
                                    Text(
                                        text = String.format(Locale.getDefault(), "%02d:%02d", min, sec),
                                        style = MaterialTheme.typography.labelSmall,
                                        fontFamily = FontFamily.Monospace,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    Surface(
                                        shape = RoundedCornerShape(4.dp),
                                        color = MaterialTheme.colorScheme.primaryContainer
                                    ) {
                                        Text(
                                            text = "OCR",
                                            style = MaterialTheme.typography.labelSmall,
                                            fontSize = 9.sp,
                                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = frame.title,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = frame.ocrExtractedContent,
                                    style = MaterialTheme.typography.bodySmall,
                                    fontSize = 10.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 2
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
            Spacer(modifier = Modifier.height(12.dp))

            // 4. Video Recording Settings (Quality, Audio Background, Storage)
            Text(
                text = "Recording & Video Optimization",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))

            // Quality Selection Chips
            val qualityProfiles = viewModel.videoRecorder.qualityProfiles
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                qualityProfiles.forEach { quality ->
                    FilterChip(
                        selected = selectedQuality.id == quality.id,
                        onClick = { viewModel.videoRecorder.setVideoQuality(quality) },
                        label = {
                            Column {
                                Text(quality.name, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                Text("${quality.resolution} • ${quality.estimatedMbPerHour} MB/hr", fontSize = 9.sp)
                            }
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Auto-OCR Board change detection toggle
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Automatic Whiteboard / Slide Snapping",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Detects faculty writing new derivations and captures crisp key-frames without manual tapping.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 11.sp
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Switch(
                    checked = isAutoKeyframeActive,
                    onCheckedChange = { isAutoKeyframeActive = it }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 5. Lecture Metadata Setup
            Text(
                text = "Lecture Details & Exam Unit",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = lectureTitle,
                onValueChange = { lectureTitle = it },
                label = { Text("Lecture Title (e.g. Otto vs Carnot Efficiency)") },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("input_video_lecture_title"),
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Subject Selector Dropdown
            ExposedDropdownMenuBox(
                expanded = subjectExpanded,
                onExpandedChange = { subjectExpanded = it }
            ) {
                OutlinedTextField(
                    value = selectedSubject,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Engineering Subject") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = subjectExpanded) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(),
                    shape = RoundedCornerShape(12.dp)
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

            Spacer(modifier = Modifier.height(10.dp))

            OutlinedTextField(
                value = selectedUnit,
                onValueChange = { selectedUnit = it },
                label = { Text("Syllabus Unit Name") },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("input_video_unit_name"),
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Spoken Language Selector
            Text(
                text = "Classroom Spoken Language",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(6.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(languages) { lang ->
                    FilterChip(
                        selected = spokenLanguage == lang,
                        onClick = { spokenLanguage = lang },
                        label = { Text(lang) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primary,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }

    // Privacy & Classroom Consent Dialog
    if (showPrivacyDialog) {
        AlertDialog(
            onDismissRequest = { showPrivacyDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.Lock, contentDescription = null, tint = Color(0xFF059669))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Classroom Video Privacy Notice")
                }
            },
            text = {
                Column {
                    Text(
                        text = "🔒 Safe & Private Classroom Recording",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "• Video recording captures classroom surroundings and faculty board presentations.\n• All recorded videos and audio are stored securely on your local device storage.\n• No video is uploaded to cloud servers without your explicit consent.\n• You can delete video files at any time to free device storage while keeping AI exam notes.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            },
            confirmButton = {
                Button(onClick = { showPrivacyDialog = false }) {
                    Text("Understood")
                }
            }
        )
    }
}

private fun triggerVideoNoteGeneration(
    viewModel: AppViewModel,
    title: String,
    subject: String,
    unitName: String,
    language: String,
    videoPath: String,
    quality: String,
    keyFrames: List<BoardKeyFrame>,
    videoSizeMb: Double,
    duration: Int
) {
    val finalTitle = title.ifBlank { "Classroom Video Lecture - $subject" }
    viewModel.processVideoLectureAndGenerateNotes(
        title = finalTitle,
        subject = subject,
        unitName = unitName,
        rawNotesOrTranscript = "Blackboard derivations, formula explanations, and classroom questions for $subject ($unitName)",
        spokenLanguage = language,
        videoPath = videoPath,
        videoQuality = quality,
        boardKeyFrames = keyFrames,
        videoFileSizeMb = if (videoSizeMb > 0) videoSizeMb else 250.0,
        durationSeconds = duration
    ) { createdLecture ->
        viewModel.selectLecture(createdLecture)
        viewModel.navigateTo(AppScreen.NOTES_DETAIL)
    }
}
