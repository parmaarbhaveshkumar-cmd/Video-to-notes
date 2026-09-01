package com.example.ui.components

import android.media.MediaPlayer
import android.net.Uri
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.VideoView
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AspectRatio
import androidx.compose.material.icons.filled.Draw
import androidx.compose.material.icons.filled.Forward10
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.Hd
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Replay10
import androidx.compose.material.icons.filled.Slideshow
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.data.model.BoardKeyFrame
import kotlinx.coroutines.delay
import java.io.File
import java.util.Locale

@Composable
fun VideoPlaybackPlayer(
    videoFilePath: String?,
    lectureTitle: String,
    durationSeconds: Int,
    boardKeyFrames: List<BoardKeyFrame>,
    seekToSeconds: Int? = null,
    onSeekHandled: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var isPlaying by remember { mutableStateOf(false) }
    var currentPositionMs by remember { mutableIntStateOf(0) }
    var totalDurationMs by remember { mutableIntStateOf(durationSeconds * 1000) }
    var videoViewRef by remember { mutableStateOf<VideoView?>(null) }
    var playbackSpeed by remember { mutableFloatStateOf(1.0f) }
    var isTheaterMode by remember { mutableStateOf(false) }
    var selectedKeyFrame by remember { mutableStateOf<BoardKeyFrame?>(null) }

    // Handle external seek requests (e.g. user taps a timestamp in the notes or formulas)
    LaunchedEffect(seekToSeconds) {
        if (seekToSeconds != null) {
            val targetMs = seekToSeconds * 1000
            videoViewRef?.seekTo(targetMs)
            currentPositionMs = targetMs
            if (!isPlaying) {
                videoViewRef?.start()
                isPlaying = true
            }
            onSeekHandled()
        }
    }

    // Tracker coroutine for current position
    LaunchedEffect(isPlaying) {
        while (isPlaying) {
            videoViewRef?.let { vv ->
                currentPositionMs = vv.currentPosition
                if (vv.duration > 0) {
                    totalDurationMs = vv.duration
                }
            }
            delay(250)
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            videoViewRef?.stopPlayback()
        }
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("video_player_card"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // 1. Video Surface or Animated Canvas Frame
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(if (isTheaterMode) 1.85f else 1.6f)
                    .background(Color.Black),
                contentAlignment = Alignment.Center
            ) {
                val hasValidFile = remember(videoFilePath) {
                    videoFilePath != null && File(videoFilePath).exists()
                }

                if (hasValidFile && videoFilePath != null) {
                    AndroidView(
                        factory = { ctx ->
                            VideoView(ctx).apply {
                                layoutParams = FrameLayout.LayoutParams(
                                    ViewGroup.LayoutParams.MATCH_PARENT,
                                    ViewGroup.LayoutParams.MATCH_PARENT
                                )
                                setVideoPath(videoFilePath)
                                setOnPreparedListener { mp ->
                                    mp.isLooping = false
                                    totalDurationMs = mp.duration
                                }
                                setOnCompletionListener {
                                    isPlaying = false
                                    currentPositionMs = totalDurationMs
                                }
                                videoViewRef = this
                            }
                        },
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    // Smart Visual Simulation for Classroom Presentation & Board
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(Color(0xFF0F2027), Color(0xFF203A43), Color(0xFF2C5364))
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Videocam,
                                contentDescription = null,
                                tint = Color(0xFF38BDF8),
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = lectureTitle,
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.titleSmall
                            )
                            Text(
                                text = "Classroom Video Stream • ${if (durationSeconds > 0) "${durationSeconds / 60}m ${durationSeconds % 60}s" else "1h 02m"} • 720p HD",
                                color = Color(0xFF94A3B8),
                                style = MaterialTheme.typography.labelSmall
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = Color(0xFF1E293B)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(imageVector = Icons.Default.Draw, contentDescription = null, tint = Color(0xFFFBBF24), modifier = Modifier.size(12.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Whiteboard & Formula Capture Active",
                                        color = Color(0xFFFBBF24),
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        }
                    }
                }

                // Top Overlay with Resolution & Mode
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.TopCenter)
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(Color.Black.copy(alpha = 0.7f), Color.Transparent)
                            )
                        )
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = Color(0xFFDC2626)
                        ) {
                            Text(
                                text = "VIDEO",
                                color = Color.White,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "720p @ 30 FPS",
                            color = Color.White,
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }

                    IconButton(
                        onClick = { isTheaterMode = !isTheaterMode },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = if (isTheaterMode) Icons.Default.AspectRatio else Icons.Default.Fullscreen,
                            contentDescription = "Toggle Aspect Ratio",
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            // 2. Playback Controls & Timeline
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 10.dp)
            ) {
                // Seekbar Slider
                val maxDur = if (totalDurationMs > 0) totalDurationMs.toFloat() else 3600000f
                val curPos = currentPositionMs.toFloat().coerceIn(0f, maxDur)

                Slider(
                    value = curPos,
                    onValueChange = { newPos ->
                        currentPositionMs = newPos.toInt()
                        videoViewRef?.seekTo(newPos.toInt())
                    },
                    valueRange = 0f..maxDur,
                    colors = SliderDefaults.colors(
                        thumbColor = Color(0xFF38BDF8),
                        activeTrackColor = Color(0xFF38BDF8),
                        inactiveTrackColor = Color(0xFF334155)
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(20.dp)
                )

                // Timer & Control Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = formatTimeMs(currentPositionMs),
                        color = Color(0xFF94A3B8),
                        fontSize = 12.sp,
                        fontFamily = FontFamily.Monospace
                    )

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        IconButton(
                            onClick = {
                                val target = (currentPositionMs - 10000).coerceAtLeast(0)
                                currentPositionMs = target
                                videoViewRef?.seekTo(target)
                            },
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Replay10,
                                contentDescription = "Rewind 10s",
                                tint = Color.White
                            )
                        }

                        IconButton(
                            onClick = {
                                if (isPlaying) {
                                    videoViewRef?.pause()
                                    isPlaying = false
                                } else {
                                    videoViewRef?.start()
                                    isPlaying = true
                                }
                            },
                            modifier = Modifier
                                .size(44.dp)
                                .background(Color(0xFF38BDF8), CircleShape)
                        ) {
                            Icon(
                                imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                contentDescription = if (isPlaying) "Pause" else "Play",
                                tint = Color(0xFF0F172A),
                                modifier = Modifier.size(24.dp)
                            )
                        }

                        IconButton(
                            onClick = {
                                val target = (currentPositionMs + 10000).coerceAtMost(totalDurationMs)
                                currentPositionMs = target
                                videoViewRef?.seekTo(target)
                            },
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Forward10,
                                contentDescription = "Forward 10s",
                                tint = Color.White
                            )
                        }
                    }

                    // Speed Selector Chips
                    val speeds = listOf(1.0f, 1.25f, 1.5f, 2.0f)
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        speeds.forEach { spd ->
                            Surface(
                                onClick = { playbackSpeed = spd },
                                shape = RoundedCornerShape(4.dp),
                                color = if (playbackSpeed == spd) Color(0xFF38BDF8) else Color(0xFF1E293B)
                            ) {
                                Text(
                                    text = "${spd}x",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (playbackSpeed == spd) Color(0xFF0F172A) else Color(0xFF94A3B8),
                                    modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }
                }

                // 3. Extracted Board & PPT Snapshots Strip
                if (boardKeyFrames.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(10.dp))
                    HorizontalDivider(color = Color(0xFF1E293B))
                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Slideshow,
                                contentDescription = null,
                                tint = Color(0xFF38BDF8),
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Board & PPT Keyframes (${boardKeyFrames.size})",
                                color = Color.White,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Text(
                            text = "Tap to jump in video",
                            color = Color(0xFF64748B),
                            fontSize = 10.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    LazyRow(
                        contentPadding = PaddingValues(end = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(boardKeyFrames) { frame ->
                            Surface(
                                onClick = {
                                    val targetMs = frame.timestampSeconds * 1000
                                    videoViewRef?.seekTo(targetMs)
                                    currentPositionMs = targetMs
                                    selectedKeyFrame = frame
                                    if (!isPlaying) {
                                        videoViewRef?.start()
                                        isPlaying = true
                                    }
                                },
                                shape = RoundedCornerShape(8.dp),
                                color = if (selectedKeyFrame?.id == frame.id) Color(0xFF1E3A8A) else Color(0xFF1E293B),
                                modifier = Modifier.width(180.dp)
                            ) {
                                Column(modifier = Modifier.padding(8.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Surface(
                                            shape = RoundedCornerShape(4.dp),
                                            color = Color(0xFF0F172A)
                                        ) {
                                            Text(
                                                text = formatTimeMs(frame.timestampSeconds * 1000),
                                                color = Color(0xFF38BDF8),
                                                fontSize = 9.sp,
                                                fontFamily = FontFamily.Monospace,
                                                fontWeight = FontWeight.Bold,
                                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                            )
                                        }

                                        Text(
                                            text = if (frame.visualType == "WHITEBOARD_WRITING") "Board" else "Diagram",
                                            color = Color(0xFFFBBF24),
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = frame.title,
                                        color = Color.White,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        maxLines = 1
                                    )
                                    Text(
                                        text = frame.ocrExtractedContent,
                                        color = Color(0xFF94A3B8),
                                        fontSize = 9.sp,
                                        fontFamily = FontFamily.Monospace,
                                        maxLines = 1
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

private fun formatTimeMs(ms: Int): String {
    val totalSeconds = ms / 1000
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds)
}
