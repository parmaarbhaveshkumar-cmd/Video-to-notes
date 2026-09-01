package com.example.audio

import android.content.Context
import android.media.MediaPlayer
import android.media.PlaybackParams
import android.net.Uri
import android.os.Build
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.io.File

class AudioPlayerManager(private val context: Context) {
    private var mediaPlayer: MediaPlayer? = null
    private var progressJob: Job? = null
    private val scope = CoroutineScope(Dispatchers.Main)

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    private val _currentPositionMs = MutableStateFlow(0)
    val currentPositionMs: StateFlow<Int> = _currentPositionMs.asStateFlow()

    private val _durationMs = MutableStateFlow(0)
    val durationMs: StateFlow<Int> = _durationMs.asStateFlow()

    private val _currentSpeed = MutableStateFlow(1.0f)
    val currentSpeed: StateFlow<Float> = _currentSpeed.asStateFlow()

    private val _activeFilePath = MutableStateFlow<String?>(null)
    val activeFilePath: StateFlow<String?> = _activeFilePath.asStateFlow()

    fun loadAndPlay(filePath: String, startPositionSeconds: Int = 0, totalFallbackDurationSeconds: Int = 1800) {
        try {
            stop()
            _activeFilePath.value = filePath
            val file = File(filePath)

            if (file.exists()) {
                mediaPlayer = MediaPlayer().apply {
                    setDataSource(context, Uri.fromFile(file))
                    prepare()
                    seekTo(startPositionSeconds * 1000)
                    start()
                    setSpeed(_currentSpeed.value)
                    setOnCompletionListener {
                        _isPlaying.value = false
                        _currentPositionMs.value = 0
                    }
                }
                _durationMs.value = mediaPlayer?.duration ?: (totalFallbackDurationSeconds * 1000)
                _isPlaying.value = true
                startProgressTracker(isSimulated = false)
            } else {
                // If audio file is simulated / not on disk, run simulated player so timestamps work seamlessly
                _durationMs.value = totalFallbackDurationSeconds * 1000
                _currentPositionMs.value = startPositionSeconds * 1000
                _isPlaying.value = true
                startProgressTracker(isSimulated = true)
            }
        } catch (e: Exception) {
            Log.e("AudioPlayer", "Error loading audio file, falling back to simulated playback", e)
            _durationMs.value = totalFallbackDurationSeconds * 1000
            _currentPositionMs.value = startPositionSeconds * 1000
            _isPlaying.value = true
            startProgressTracker(isSimulated = true)
        }
    }

    fun togglePlayPause() {
        if (_isPlaying.value) {
            pause()
        } else {
            resume()
        }
    }

    fun pause() {
        try {
            mediaPlayer?.pause()
        } catch (e: Exception) {
            Log.e("AudioPlayer", "Pause error", e)
        }
        _isPlaying.value = false
    }

    fun resume() {
        try {
            mediaPlayer?.start()
            setSpeed(_currentSpeed.value)
        } catch (e: Exception) {
            Log.e("AudioPlayer", "Resume error", e)
        }
        _isPlaying.value = true
    }

    fun seekToSeconds(seconds: Int) {
        val targetMs = seconds * 1000
        _currentPositionMs.value = targetMs
        try {
            mediaPlayer?.seekTo(targetMs)
        } catch (e: Exception) {
            Log.e("AudioPlayer", "Seek error", e)
        }
        if (!_isPlaying.value) {
            resume()
        }
    }

    fun setSpeed(speed: Float) {
        _currentSpeed.value = speed
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            try {
                mediaPlayer?.let { player ->
                    if (player.isPlaying) {
                        player.playbackParams = PlaybackParams().apply { this.speed = speed }
                    }
                }
            } catch (e: Exception) {
                Log.e("AudioPlayer", "Set speed error", e)
            }
        }
    }

    fun stop() {
        progressJob?.cancel()
        progressJob = null
        try {
            mediaPlayer?.apply {
                if (isPlaying) {
                    stop()
                }
                release()
            }
        } catch (e: Exception) {
            Log.e("AudioPlayer", "Stop error", e)
        }
        mediaPlayer = null
        _isPlaying.value = false
    }

    private fun startProgressTracker(isSimulated: Boolean) {
        progressJob?.cancel()
        progressJob = scope.launch {
            while (isActive) {
                delay(200)
                if (_isPlaying.value) {
                    if (!isSimulated && mediaPlayer != null) {
                        try {
                            _currentPositionMs.value = mediaPlayer?.currentPosition ?: _currentPositionMs.value
                        } catch (e: Exception) {
                            // ignore
                        }
                    } else {
                        val newPos = (_currentPositionMs.value + (200 * _currentSpeed.value).toInt())
                        if (newPos >= _durationMs.value) {
                            _currentPositionMs.value = _durationMs.value
                            _isPlaying.value = false
                        } else {
                            _currentPositionMs.value = newPos
                        }
                    }
                }
            }
        }
    }
}
