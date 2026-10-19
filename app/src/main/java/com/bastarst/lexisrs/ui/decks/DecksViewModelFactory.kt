package com.bastarst.lexisrs.ui.decks

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.bastarst.lexisrs.data.repository.WordRepository

class DecksViewModelFactory(
    private val repository: WordRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        @Suppress("UNCHECKED_CAST")
        return DecksViewModel(repository) as T
    }
}