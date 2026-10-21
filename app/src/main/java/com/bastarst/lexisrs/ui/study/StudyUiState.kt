package com.bastarst.lexisrs.ui.study

import com.bastarst.lexisrs.data.local.entity.Word

enum class StudyPhase {
    MATCH,
    QUIZ,
    RECALL,
    WRITING,
    DONE
}

data class QuizQuestion(
    val word: Word,
    val options: List<Word>
)

data class StudyUiState(
    val phase: StudyPhase = StudyPhase.MATCH,
    val englishWords: List<Word> = emptyList(),
    val russianWords: List<Word> = emptyList(),
    val failedWordIds: Set<Long> = emptySet(),
    // MATCH
    val selectedEnglishId: Long? = null,
    val selectedRussianId: Long? = null,
    val matchedIds: Set<Long> = emptySet(),
    val wrongFlash: Pair<Long, Long>? = null,
    // QUIZ
    val quizQuestions: List<QuizQuestion> = emptyList(),
    val currentQuizIndex: Int = 0,
    val quizCorrectId: Long? = null,
    val quizWrongId: Long? = null,
    // RECALL
    val recallIndex: Int = 0,
    val isTranslationVisible: Boolean = false,
    val isTimerExpired: Boolean = false,
    val timeLeft: Long = 10_000L,
    // WRITING
    val writingIndex: Int = 0,
    val writingInput: String = "",
    val isWritingError: Boolean = false,

    val isLoading: Boolean = true,
    val isComplete: Boolean = false
)
