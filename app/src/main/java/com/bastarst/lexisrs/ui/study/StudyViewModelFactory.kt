package com.bastarst.lexisrs.ui.study

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.bastarst.lexisrs.data.repository.StudyMode
import com.bastarst.lexisrs.data.repository.WordRepository

class StudyViewModelFactory(
    private val repository: WordRepository,
    private val deckId: Long,
    private val mode: StudyMode
): ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        @Suppress("UNCHECKED_CAST")
        return StudyViewModel(repository, deckId, mode) as T
    }
}