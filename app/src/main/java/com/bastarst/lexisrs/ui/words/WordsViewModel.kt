package com.bastarst.lexisrs.ui.words

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bastarst.lexisrs.data.local.entity.Word
import com.bastarst.lexisrs.data.repository.WordRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class WordsViewModel(
    private val repository: WordRepository,
    private val deckId: Long
) : ViewModel() {

    private val _query = MutableStateFlow("")
    val query: StateFlow<String> = _query.asStateFlow()
    private val _editingWord = MutableStateFlow<Word?>(null)
    val editingWord: StateFlow<Word?> = _editingWord.asStateFlow()

    val words: StateFlow<List<Word>> = _query
        .debounce(300)
        .distinctUntilChanged()
        .flatMapLatest { q -> repository.searchByDeck(deckId, q) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList()
        )

    fun onQueryChange(newQuery: String) {
        _query.value = newQuery
    }

    fun onWordClick(word: Word) {
        _editingWord.value = word
    }

    fun onDismissEditDialog() {
        _editingWord.value = null
    }

    fun onSaveWord(id: Long, newEnglish: String, newRussian: String) {
        val currentWord = _editingWord.value ?: return
        if (newEnglish.isBlank() || newRussian.isBlank()) return

        viewModelScope.launch {
            try {
                val updated = currentWord.copy(
                    english = newEnglish.trim(),
                    russian = newRussian.trim()
                )
                repository.updateWord(updated)
                _editingWord.value = null
            } catch (e: Exception) {
                _editingWord.value = null
            }
        }
    }
}