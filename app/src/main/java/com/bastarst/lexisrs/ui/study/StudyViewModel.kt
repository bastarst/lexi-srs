package com.bastarst.lexisrs.ui.study

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bastarst.lexisrs.data.local.entity.Word
import com.bastarst.lexisrs.data.repository.StudyMode
import com.bastarst.lexisrs.data.repository.WordRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class StudyViewModel(
    private val repository: WordRepository,
    private val deckId: Long,
    private val mode: StudyMode
): ViewModel() {

    private var timerJob: Job? = null
    private val _state = MutableStateFlow(StudyUiState())
    val state: StateFlow<StudyUiState> = _state.asStateFlow()

    init {
        load()
    }

    private fun load() {
        viewModelScope.launch {
            val words = repository.getStudyWords(deckId, mode)

            if (words.isEmpty()) {
                _state.value = StudyUiState(
                    isLoading = false,
                    phase = StudyPhase.DONE
                )

                return@launch
            }

            val questions = buildQuiz(words)

            if (mode == StudyMode.LEARN) {
                _state.value = StudyUiState(
                    englishWords = words.shuffled(),
                    russianWords = words.shuffled(),
                    isLoading = false,
                    phase = StudyPhase.MATCH,
                    quizQuestions = questions,
                    recallQueue = questions
                )
            } else {
                _state.value = StudyUiState(
                    isLoading = false,
                    phase = StudyPhase.RECALL,
                    quizQuestions = questions,
                    recallQueue = questions,
                    recallIndex = 0,
                    timeLeft = 10_000L
                )
                startRecallTimer()
            }
        }
    }

    fun onEnglishClick(id: Long) {
        val s = _state.value
        if (id in s.matchedIds || s.wrongFlash != null) return
        val newSel = if (s.selectedEnglishId == id) null else id
        _state.value = s.copy(selectedEnglishId = newSel)
        checkMatch()
    }

    fun onRussianClick(id: Long) {
        val s = _state.value
        if (id in s.matchedIds || s.wrongFlash != null) return
        val newSel = if (s.selectedRussianId == id) null else id
        _state.value = s.copy(selectedRussianId = newSel)
        checkMatch()
    }

    private fun checkMatch() {
        val s = _state.value
        val en = s.selectedEnglishId ?: return
        val ru = s.selectedRussianId ?: return

        if (en == ru) {
            val newMatched = s.matchedIds + en
            if (newMatched.size == s.englishWords.size) {
                _state.value = s.copy(
                    matchedIds = newMatched,
                    selectedEnglishId = null,
                    selectedRussianId = null,
                    phase = StudyPhase.QUIZ,
                    currentQuizIndex = 0
                )
            } else {
                _state.value = s.copy(
                    matchedIds = newMatched,
                    selectedEnglishId = null,
                    selectedRussianId = null
                )
            }
        } else {
            _state.value = s.copy(wrongFlash = en to ru)
            viewModelScope.launch {
                delay(400)
                _state.value = _state.value.copy(
                    wrongFlash = null,
                    selectedEnglishId = null,
                    selectedRussianId = null
                )
            }
        }
    }

    private fun buildQuiz(words: List<Word>): List<QuizQuestion> =
        words.map { w ->
            val distractors = words.filter { it.id != w.id }
            QuizQuestion(
                word = w,
                options = (distractors + w).shuffled()
            )
        }

    fun onQuizOptionClick(optionId: Long) {
        val s = _state.value
        if (s.quizCorrectId != null || s.quizWrongId != null) return

        val question = s.quizQuestions.getOrNull(s.currentQuizIndex) ?: return

        if (optionId == question.word.id) {
            _state.value = s.copy(quizCorrectId = optionId)
            viewModelScope.launch {
                delay(150)
                val next = s.currentQuizIndex + 1
                if (next >= s.quizQuestions.size) {
                    _state.value = _state.value.copy(
                        quizCorrectId = null,
                        phase = StudyPhase.RECALL,
                        recallQueue = s.quizQuestions,
                        recallIndex = 0,
                        isTranslationVisible = false,
                        isTimerExpired = false
                    )
                    startRecallTimer()
                } else {
                    _state.value = _state.value.copy(
                        quizCorrectId = null,
                        currentQuizIndex = next
                    )
                }
            }
        } else {
            _state.value = s.copy(quizWrongId = optionId)
            viewModelScope.launch {
                delay(250)
                _state.value = _state.value.copy(quizWrongId = null)
            }
        }
    }

    private fun startRecallTimer() {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            var timeLeft = 10_000L
            _state.value = _state.value.copy(timeLeft = timeLeft)

            while (timeLeft > 0) {
                delay(100)
                timeLeft -= 100
                _state.value = _state.value.copy(timeLeft = timeLeft.coerceAtLeast(0))
            }

            if (!_state.value.isTranslationVisible) {
                _state.value = _state.value.copy(
                    isTranslationVisible = true,
                    isTimerExpired = true
                )
            }
        }
    }

    fun onShowTranslation() {
        timerJob?.cancel()
        _state.value = _state.value.copy(isTranslationVisible = true)
    }

    fun onRecallAnswer(isRemembered: Boolean) {
        timerJob?.cancel()
        val s = _state.value
        val currentQuestion = s.recallQueue[s.recallIndex]

        val updatedFailedIds = if (!isRemembered) {
            s.failedWordIds + currentQuestion.word.id
        } else {
            s.failedWordIds
        }

        val updatedQueue = if (!isRemembered) {
            s.recallQueue + currentQuestion
        } else {
            s.recallQueue
        }

        val next = s.recallIndex + 1

        if (next >= updatedQueue.size) {
            _state.value = s.copy(
                phase = StudyPhase.WRITING,
                writingIndex = 0,
                writingInput = "",
                isWritingError = false,
                failedWordIds = updatedFailedIds
            )
        } else {
            _state.value = s.copy(
                recallQueue = updatedQueue,
                recallIndex = next,
                isTranslationVisible = false,
                isTimerExpired = false,
                timeLeft = 10_000L,
                failedWordIds = updatedFailedIds
            )
            startRecallTimer()
        }
    }

    fun onWritingInputChange(text: String) {
        _state.value = _state.value.copy(
            writingInput = text,
            isWritingError = false
        )
    }

    fun onCheckWriting() {
        val s = _state.value
        val question = s.quizQuestions.getOrNull(s.writingIndex) ?: return
        val isCorrect = s.writingInput.trim()
            .equals(question.word.english.trim(), ignoreCase = true)

        if (isCorrect) {
            moveToNextWritingWord()
        } else {
            _state.value = s.copy(
                isWritingError = true,
                failedWordIds = s.failedWordIds + question.word.id
            )
        }
    }

    fun onWritingRetry() {
        _state.value = _state.value.copy(
            writingInput = "",
            isWritingError = false
        )
    }

    fun moveToNextWritingWord() {
        val s = _state.value
        val next = s.writingIndex + 1

        if (next >= s.quizQuestions.size) {
            viewModelScope.launch {
                val uniqueWords = s.quizQuestions.map { it.word }.distinctBy { it.id }

                viewModelScope.launch {
                    uniqueWords.forEach { word ->
                        val isSuccess = !s.failedWordIds.contains(word.id)
                        repository.updateSrs(word, isSuccess)
                    }
                    _state.value = s.copy(phase = StudyPhase.DONE)
                }
            }
        } else {
            _state.value = s.copy(
                writingIndex = next,
                writingInput = "",
                isWritingError = false
            )
        }
    }
}