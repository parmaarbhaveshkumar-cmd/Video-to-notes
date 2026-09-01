package com.example.ui.screens

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Flip
import androidx.compose.material.icons.filled.Functions
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Quiz
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.JsonUtils
import com.example.data.model.Flashcard
import com.example.data.model.McqQuestion
import com.example.data.model.VivaQuestion
import com.example.viewmodel.AppScreen
import com.example.viewmodel.AppViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RevisionQuizScreen(
    viewModel: AppViewModel,
    modifier: Modifier = Modifier
) {
    val selectedLecture = viewModel.selectedLecture.collectAsState().value
    val allLectures by viewModel.allLectures.collectAsState()

    val currentQuizIndex by viewModel.currentQuizIndex.collectAsState()
    val selectedMcqAnswer by viewModel.selectedMcqAnswer.collectAsState()
    val quizScore by viewModel.quizScore.collectAsState()

    var selectedTab by remember { mutableIntStateOf(0) } // 0 = MCQ Test, 1 = Flashcards, 2 = Viva Defense

    val activeLecture = selectedLecture ?: allLectures.firstOrNull()

    val mcqs = remember(activeLecture?.mcqsJson) {
        activeLecture?.let { JsonUtils.mcqListFromJson(it.mcqsJson) } ?: emptyList()
    }
    val flashcards = remember(activeLecture?.flashcardsJson) {
        activeLecture?.let { JsonUtils.flashcardListFromJson(it.flashcardsJson) } ?: emptyList()
    }
    val vivaList = remember(activeLecture?.vivaJson) {
        activeLecture?.let { JsonUtils.vivaListFromJson(it.vivaJson) } ?: emptyList()
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Revision & Self-Test",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = activeLecture?.title ?: "Engineering Subjects",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1
                        )
                    }
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
        ) {
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = MaterialTheme.colorScheme.surface
            ) {
                Tab(selected = selectedTab == 0, onClick = { selectedTab = 0 }, text = { Text("MCQs (${mcqs.size})") })
                Tab(selected = selectedTab == 1, onClick = { selectedTab = 1 }, text = { Text("Flashcards (${flashcards.size})") })
                Tab(selected = selectedTab == 2, onClick = { selectedTab = 2 }, text = { Text("Viva Defense") })
            }

            when (selectedTab) {
                0 -> { // Interactive MCQ Quiz
                    if (mcqs.isEmpty()) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text("No MCQs available. Record a lecture to generate quiz questions.")
                        }
                    } else {
                        val currentQuestion = mcqs.getOrNull(currentQuizIndex) ?: mcqs.first()

                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                // Progress & Score Header
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Question ${currentQuizIndex + 1} of ${mcqs.size}",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = MaterialTheme.colorScheme.primary,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = Color(0xFF10B981)
                                    ) {
                                        Text(
                                            text = "Score: $quizScore / ${mcqs.size}",
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(16.dp))

                                // Question Card
                                Card(
                                    shape = RoundedCornerShape(16.dp),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                                ) {
                                    Column(modifier = Modifier.padding(16.dp)) {
                                        Text(
                                            text = currentQuestion.question,
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold,
                                            lineHeight = 24.sp
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(16.dp))

                                // Options List
                                currentQuestion.options.forEachIndexed { optIndex, optionText ->
                                    val isSelected = selectedMcqAnswer == optIndex
                                    val isAnswered = selectedMcqAnswer != null
                                    val isCorrectOption = optIndex == currentQuestion.correctIndex

                                    val containerColor = when {
                                        !isAnswered -> MaterialTheme.colorScheme.surface
                                        isCorrectOption -> Color(0xFFD1FAE5) // Light Emerald
                                        isSelected && !isCorrectOption -> Color(0xFFFEE2E2) // Light Red
                                        else -> MaterialTheme.colorScheme.surface
                                    }

                                    ElevatedCard(
                                        onClick = {
                                            if (selectedMcqAnswer == null) {
                                                viewModel.submitMcqAnswer(optIndex, optIndex == currentQuestion.correctIndex)
                                            }
                                        },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 4.dp),
                                        shape = RoundedCornerShape(12.dp),
                                        colors = CardDefaults.elevatedCardColors(containerColor = containerColor)
                                    ) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(14.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text(
                                                text = "${('A' + optIndex)}. $optionText",
                                                style = MaterialTheme.typography.bodyMedium,
                                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                                modifier = Modifier.weight(1f)
                                            )
                                            if (isAnswered) {
                                                if (isCorrectOption) {
                                                    Icon(imageVector = Icons.Default.Check, contentDescription = null, tint = Color(0xFF059669))
                                                } else if (isSelected) {
                                                    Icon(imageVector = Icons.Default.Close, contentDescription = null, tint = Color(0xFFDC2626))
                                                }
                                            }
                                        }
                                    }
                                }

                                // Explanation Card if answered
                                AnimatedVisibility(visible = selectedMcqAnswer != null) {
                                    Card(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(top = 12.dp),
                                        shape = RoundedCornerShape(12.dp),
                                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f))
                                    ) {
                                        Column(modifier = Modifier.padding(12.dp)) {
                                            Text(
                                                text = "Explanation & Key Concept:",
                                                style = MaterialTheme.typography.labelMedium,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.primary
                                            )
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(
                                                text = currentQuestion.explanation,
                                                style = MaterialTheme.typography.bodySmall
                                            )
                                        }
                                    }
                                }
                            }

                            // Next / Finish Button
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                OutlinedButton(
                                    onClick = { viewModel.resetQuiz() },
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Icon(imageVector = Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Reset")
                                }

                                Button(
                                    onClick = {
                                        viewModel.nextQuizQuestion(mcqs.size)
                                    },
                                    enabled = selectedMcqAnswer != null && currentQuizIndex < mcqs.size - 1,
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text(text = if (currentQuizIndex < mcqs.size - 1) "Next Question" else "Completed", fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }

                1 -> { // 3D Flippable Flashcards
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp),
                        contentPadding = PaddingValues(bottom = 24.dp)
                    ) {
                        items(flashcards) { card ->
                            FlashcardItem(card = card)
                        }
                    }
                }

                2 -> { // Oral Viva / Defense Questions
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp),
                        contentPadding = PaddingValues(bottom = 24.dp)
                    ) {
                        items(vivaList) { viva ->
                            VivaCard(viva = viva)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun FlashcardItem(card: Flashcard) {
    var isFlipped by remember { mutableStateOf(false) }

    ElevatedCard(
        onClick = { isFlipped = !isFlipped },
        modifier = Modifier
            .fillMaxWidth()
            .height(190.dp)
            .testTag("flashcard_item"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = if (isFlipped) Color(0xFF0F172A) else MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = if (isFlipped) Color(0xFF38BDF8).copy(alpha = 0.2f) else MaterialTheme.colorScheme.primaryContainer
                ) {
                    Text(
                        text = if (isFlipped) "ANSWER / FORMULA" else "QUESTION (Tap to Flip)",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = if (isFlipped) Color(0xFF38BDF8) else MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }

                Icon(
                    imageVector = Icons.Default.Flip,
                    contentDescription = "Flip",
                    tint = if (isFlipped) Color(0xFF38BDF8) else MaterialTheme.colorScheme.outline
                )
            }

            // Body
            if (!isFlipped) {
                Text(
                    text = card.frontQuestion,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            } else {
                Column {
                    Text(
                        text = card.backAnswer,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White,
                        lineHeight = 20.sp
                    )
                    if (card.formula.isNotBlank()) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Formula: ${card.formula}",
                            style = MaterialTheme.typography.bodySmall,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF38BDF8)
                        )
                    }
                }
            }

            Text(
                text = "Topic: ${card.topic}",
                style = MaterialTheme.typography.labelSmall,
                color = if (isFlipped) Color(0xFF94A3B8) else MaterialTheme.colorScheme.outline
            )
        }
    }
}

@Composable
private fun VivaCard(viva: VivaQuestion) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(imageVector = Icons.Default.RecordVoiceOver, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = "Viva Defense Question", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = viva.question, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)

            Spacer(modifier = Modifier.height(10.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Model Defense Answer:",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Text(text = viva.modelAnswer, style = MaterialTheme.typography.bodySmall, lineHeight = 18.sp)

            Spacer(modifier = Modifier.height(8.dp))
            Surface(
                shape = RoundedCornerShape(6.dp),
                color = MaterialTheme.colorScheme.surfaceVariant
            ) {
                Text(
                    text = "💡 Examiner Tip: ${viva.examinerTip}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(8.dp)
                )
            }
        }
    }
}
