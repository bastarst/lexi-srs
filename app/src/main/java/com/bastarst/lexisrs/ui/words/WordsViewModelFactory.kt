package com.bastarst.lexisrs.ui.words

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.bastarst.lexisrs.data.repository.WordRepository

class WordsViewModelFactory(
    private val repository: WordRepository
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        @Suppress("UNCHECKED_CAST")
        return WordsViewModel(repository) as T
    }
}