package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.screens.ExamPrepScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.NoteEditorScreen
import com.example.ui.screens.NotesDetailScreen
import com.example.ui.screens.NotesSearchScreen
import com.example.ui.screens.PdfPreviewScreen
import com.example.ui.screens.PyqAnalyzerScreen
import com.example.ui.screens.RecordLectureScreen
import com.example.ui.screens.RecordVideoLectureScreen
import com.example.ui.screens.RevisionQuizScreen
import com.example.ui.screens.SubjectDetailScreen
import com.example.ui.screens.SyllabusViewerScreen
import com.example.ui.screens.UploadLectureVideoScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.viewmodel.AppScreen
import com.example.viewmodel.AppViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val appViewModel: AppViewModel = viewModel()
                    MainAppContent(viewModel = appViewModel)
                }
            }
        }
    }
}

@Composable
fun MainAppContent(viewModel: AppViewModel) {
    val currentScreen by viewModel.currentScreen.collectAsState()

    // Handle Android system back press with intelligent parent navigation
    BackHandler(enabled = currentScreen != AppScreen.HOME) {
        when (currentScreen) {
            AppScreen.PDF_PREVIEW -> viewModel.navigateTo(AppScreen.NOTE_EDITOR)
            AppScreen.NOTE_EDITOR -> viewModel.navigateTo(AppScreen.SUBJECT_DETAIL)
            AppScreen.SUBJECT_DETAIL, AppScreen.NOTES_SEARCH -> viewModel.navigateTo(AppScreen.HOME)
            else -> viewModel.navigateTo(AppScreen.HOME)
        }
    }

    AnimatedContent(
        targetState = currentScreen,
        transitionSpec = { fadeIn() togetherWith fadeOut() },
        label = "screen_transition"
    ) { screen ->
        when (screen) {
            AppScreen.HOME -> HomeScreen(viewModel = viewModel)
            AppScreen.RECORD -> RecordLectureScreen(viewModel = viewModel)
            AppScreen.RECORD_VIDEO -> RecordVideoLectureScreen(viewModel = viewModel)
            AppScreen.UPLOAD_VIDEO -> UploadLectureVideoScreen(viewModel = viewModel)
            AppScreen.NOTES_DETAIL -> NotesDetailScreen(viewModel = viewModel)
            AppScreen.EXAM_PREP -> ExamPrepScreen(viewModel = viewModel)
            AppScreen.REVISION_QUIZ -> RevisionQuizScreen(viewModel = viewModel)
            AppScreen.PYQ_ANALYZER -> PyqAnalyzerScreen(viewModel = viewModel)
            AppScreen.SYLLABUS_VIEWER -> SyllabusViewerScreen(viewModel = viewModel)
            AppScreen.SUBJECT_DETAIL -> SubjectDetailScreen(viewModel = viewModel)
            AppScreen.NOTE_EDITOR -> NoteEditorScreen(viewModel = viewModel)
            AppScreen.PDF_PREVIEW -> PdfPreviewScreen(viewModel = viewModel)
            AppScreen.NOTES_SEARCH -> NotesSearchScreen(viewModel = viewModel)
        }
    }
}
