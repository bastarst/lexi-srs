package com.bastarst.lexisrs.ui.study

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.TextAutoSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.bastarst.lexisrs.data.local.entity.Word
import com.bastarst.lexisrs.data.repository.StudyMode

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudyScreen(
    viewModel: StudyViewModel,
    deckName: String,
    mode: StudyMode,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text(deckName) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            when {
                state.isLoading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
                state.phase == StudyPhase.MATCH -> {
                    StudyBoard(state = state, viewModel = viewModel)
                }
                state.phase == StudyPhase.QUIZ -> {
                    QuizBoard(state = state, viewModel = viewModel)
                }
                state.phase == StudyPhase.RECALL -> {
                    RecallBoard(state = state, viewModel = viewModel)
                }
                state.phase == StudyPhase.WRITING -> {
                    WritingBoard(state = state, viewModel = viewModel)
                }
                state.phase == StudyPhase.DONE -> {
                    DoneScreen(onBack = onBack)
                }
            }
        }
    }
}

@Composable
private fun StudyBoard(
    state: StudyUiState,
    viewModel: StudyViewModel
) {
    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val outerPadding = 12.dp
        val columnSpacing = 10.dp
        val rowSpacing = 10.dp
        val rows = state.englishWords.size.coerceAtLeast(1)

        val availableH = maxHeight - outerPadding * 2
        val rowHeight = (availableH - rowSpacing * (rows - 1)) / rows

        Row(
            modifier = Modifier.fillMaxSize().padding(outerPadding),
            horizontalArrangement = Arrangement.spacedBy(columnSpacing),
            verticalAlignment = Alignment.CenterVertically
        ) {
            WordColumn(
                modifier = Modifier.weight(1f),
                rowHeight = rowHeight,
                rowSpacing = rowSpacing,
                words = state.englishWords,
                textOf = { it.english },
                matchedIds = state.matchedIds,
                selectedId = state.selectedEnglishId,
                wrongIds = state.wrongFlash?.let { setOf(it.first) } ?: emptySet(),
                onWordClick = viewModel::onEnglishClick
            )
            WordColumn(
                modifier = Modifier.weight(1f),
                rowHeight = rowHeight,
                rowSpacing = rowSpacing,
                words = state.russianWords,
                textOf = { it.russian },
                matchedIds = state.matchedIds,
                selectedId = state.selectedRussianId,
                wrongIds = state.wrongFlash?.let { setOf(it.second) } ?: emptySet(),
                onWordClick = viewModel::onRussianClick
            )
        }
    }
}

@Composable
private fun WordColumn(
    modifier: Modifier,
    rowHeight: Dp,
    rowSpacing: Dp,
    words: List<Word>,
    textOf: (Word) -> String,
    matchedIds: Set<Long>,
    selectedId: Long?,
    wrongIds: Set<Long>,
    onWordClick: (Long) -> Unit
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(rowSpacing)) {
        words.forEach { word ->
            val isMatched = word.id in matchedIds
            val isSelected = word.id == selectedId
            val isWrong = word.id in wrongIds

            val container = when {
                isMatched -> MaterialTheme.colorScheme.primaryContainer
                isWrong -> MaterialTheme.colorScheme.errorContainer
                isSelected -> MaterialTheme.colorScheme.secondaryContainer
                else -> MaterialTheme.colorScheme.surfaceVariant
            }
            val content = when {
                isMatched -> MaterialTheme.colorScheme.onPrimaryContainer
                isWrong -> MaterialTheme.colorScheme.onErrorContainer
                isSelected -> MaterialTheme.colorScheme.onSecondaryContainer
                else -> MaterialTheme.colorScheme.onSurfaceVariant
            }

            Surface(
                onClick = { onWordClick(word.id) },
                enabled = !isMatched,
                shape = RoundedCornerShape(16.dp),
                color = container,
                contentColor = content,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(rowHeight)
            ) {
                Box(
                    modifier = Modifier.fillMaxSize().padding(horizontal = 12.dp, vertical = 4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = textOf(word),
                        textAlign = TextAlign.Center,
                        maxLines = 4,
                        overflow = TextOverflow.Ellipsis,
                        autoSize = TextAutoSize.StepBased(
                            minFontSize = 10.sp,
                            maxFontSize = 22.sp,
                            stepSize = 1.sp
                        )
                    )
                }
            }
        }
    }
}

@Composable
private fun QuizBoard(
    state: StudyUiState,
    viewModel: StudyViewModel
) {
    val question = state.quizQuestions.getOrNull(state.currentQuizIndex) ?: return

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "${state.currentQuizIndex + 1} / ${state.quizQuestions.size}",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(Modifier.height(12.dp))

        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .weight(0.35f),
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.fillMaxSize().padding(16.dp)
            ) {
                Text(
                    text = question.word.english,
                    textAlign = TextAlign.Center,
                    style = LocalTextStyle.current.copy(lineHeight = 1.2.em),
                    autoSize = TextAutoSize.StepBased(
                        minFontSize = 16.sp,
                        maxFontSize = 40.sp,
                        stepSize = 2.sp
                    )
                )
            }
        }

        Spacer(Modifier.height(16.dp))

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(0.65f),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            question.options.forEach { option ->
                val isCorrect = option.id == state.quizCorrectId
                val isWrong = option.id == state.quizWrongId

                val container = when {
                    isCorrect -> MaterialTheme.colorScheme.primaryContainer
                    isWrong -> MaterialTheme.colorScheme.errorContainer
                    else -> MaterialTheme.colorScheme.surfaceVariant
                }
                val content = when {
                    isCorrect -> MaterialTheme.colorScheme.onPrimaryContainer
                    isWrong -> MaterialTheme.colorScheme.onErrorContainer
                    else -> MaterialTheme.colorScheme.onSurfaceVariant
                }

                Surface(
                    onClick = { viewModel.onQuizOptionClick(option.id) },
                    enabled = state.quizCorrectId == null && state.quizWrongId == null,
                    shape = RoundedCornerShape(12.dp),
                    color = container,
                    contentColor = content,
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.fillMaxSize().padding(horizontal = 12.dp)
                    ) {
                        Text(
                            text = option.russian,
                            style = MaterialTheme.typography.titleMedium
                                .copy(lineHeight = 1.2.em),
                            textAlign = TextAlign.Center,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun RecallBoard(
    state: StudyUiState,
    viewModel: StudyViewModel
) {
    val question = state.quizQuestions.getOrNull(state.recallIndex) ?: return

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Счетчик и таймер
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "${state.recallIndex + 1} / ${state.quizQuestions.size}",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "${state.timeLeft / 1000} сек",
                style = MaterialTheme.typography.labelLarge,
                color = if (state.timeLeft < 3000 && !state.isTranslationVisible)
                    MaterialTheme.colorScheme.error
                else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Spacer(Modifier.height(8.dp))

        LinearProgressIndicator(
            progress = { state.timeLeft / 10_000f },
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp),
            color = if (state.timeLeft < 3000 && !state.isTranslationVisible)
                MaterialTheme.colorScheme.error
            else MaterialTheme.colorScheme.primary
        )

        Spacer(Modifier.height(24.dp))

        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.fillMaxSize().padding(16.dp)
            ) {
                Text(
                    text = question.word.english,
                    textAlign = TextAlign.Center,
                    style = LocalTextStyle.current.copy(lineHeight = 1.2.em),
                    autoSize = TextAutoSize.StepBased(
                        minFontSize = 16.sp,
                        maxFontSize = 40.sp,
                        stepSize = 2.sp
                    )
                )
            }
        }

        Spacer(Modifier.height(16.dp))

        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            shape = RoundedCornerShape(16.dp),
            color = if (state.isTranslationVisible)
                MaterialTheme.colorScheme.primaryContainer
            else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.fillMaxSize().padding(16.dp)
            ) {
                if (state.isTranslationVisible) {
                    Text(
                        text = question.word.russian,
                        style = MaterialTheme.typography.headlineMedium
                            .copy(lineHeight = 1.2.em),
                        textAlign = TextAlign.Center,
                        maxLines = 3,
                        overflow = TextOverflow.Ellipsis
                    )
                } else {
                    Text(
                        text = "••••••••••",
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f)
                    )
                }
            }
        }

        Spacer(Modifier.height(24.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
        ) {
            when {
                state.isTranslationVisible -> {
                    Row(
                        modifier = Modifier.fillMaxSize(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Button(
                            onClick = { viewModel.onRecallAnswer(false) },
                            modifier = Modifier.weight(1f).fillMaxHeight(),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.error
                            )
                        ) {
                            Text("Не помню", style = MaterialTheme.typography.titleMedium)
                        }
                        Button(
                            onClick = { viewModel.onRecallAnswer(true) },
                            modifier = Modifier.weight(1f).fillMaxHeight(),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary
                            )
                        ) {
                            Text("Помню", style = MaterialTheme.typography.titleMedium)
                        }
                    }
                }
                state.isTimerExpired -> {
                    Button(
                        onClick = { viewModel.onRecallAnswer(false) },
                        modifier = Modifier.fillMaxSize()
                    ) {
                        Text("Далее", style = MaterialTheme.typography.titleMedium)
                    }
                }
                else -> {
                    Button(
                        onClick = { viewModel.onShowTranslation() },
                        modifier = Modifier.fillMaxSize()
                    ) {
                        Text("Показать", style = MaterialTheme.typography.titleMedium)
                    }
                }
            }
        }
    }
}

@Composable
private fun WritingBoard(
    state: StudyUiState,
    viewModel: StudyViewModel
) {
    val question = state.quizQuestions.getOrNull(state.writingIndex) ?: return

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "${state.writingIndex + 1} / ${state.quizQuestions.size}",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(Modifier.height(24.dp))

        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.fillMaxSize().padding(16.dp)
            ) {
                Text(
                    text = question.word.russian,
                    textAlign = TextAlign.Center,
                    style = LocalTextStyle.current.copy(lineHeight = 1.2.em),
                    autoSize = TextAutoSize.StepBased(
                        minFontSize = 16.sp,
                        maxFontSize = 40.sp,
                        stepSize = 2.sp
                    )
                )
            }
        }

        Spacer(Modifier.height(16.dp))

        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            shape = RoundedCornerShape(16.dp),
            color = if (state.isWritingError)
                MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f)
            else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.fillMaxSize().padding(16.dp)
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    OutlinedTextField(
                        value = state.writingInput,
                        onValueChange = viewModel::onWritingInputChange,
                        modifier = Modifier.fillMaxWidth(),
                        textStyle = MaterialTheme.typography
                            .headlineSmall.copy(textAlign = TextAlign.Center),
                        singleLine = true,
                        isError = state.isWritingError,
                        placeholder = {
                            Text("Введите слово",
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth()
                            )
                        },
                        keyboardOptions = KeyboardOptions(
                            capitalization = KeyboardCapitalization.None,
                            autoCorrectEnabled = false,
                            keyboardType = KeyboardType.Text,
                            imeAction = ImeAction.Done
                        ),
                        keyboardActions = KeyboardActions(
                            onDone = { viewModel.onCheckWriting() }
                        )
                    )

                    if (state.isWritingError) {
                        Spacer(Modifier.height(12.dp))
                        Text(
                            text = "Правильно: ${question.word.english}",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
        }

        Spacer(Modifier.height(24.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
        ) {
            if (state.isWritingError) {
                Row(
                    modifier = Modifier.fillMaxSize(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = { viewModel.onWritingRetry() },
                        modifier = Modifier.weight(1f).fillMaxHeight()
                    ) {
                        Text("Еще раз", style = MaterialTheme.typography.titleMedium)
                    }
                    Button(
                        onClick = { viewModel.moveToNextWritingWord() },
                        modifier = Modifier.weight(1f).fillMaxHeight()
                    ) {
                        Text("Правильно", style = MaterialTheme.typography.titleMedium)
                    }
                }
            } else {
                Button(
                    onClick = { viewModel.onCheckWriting() },
                    modifier = Modifier.fillMaxSize(),
                    enabled = state.writingInput.isNotBlank()
                ) {
                    Text("Проверить", style = MaterialTheme.typography.titleMedium)
                }
            }
        }
    }
}

@Composable
private fun DoneScreen(onBack: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Готово!", style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(24.dp))
        Button(
            onClick = onBack,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Вернуться")
        }
    }
}