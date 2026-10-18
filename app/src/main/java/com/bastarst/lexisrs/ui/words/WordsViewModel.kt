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

class WordsViewModel(
    private val repository: WordRepository
) : ViewModel() {

    private val _query = MutableStateFlow("")
    val query: StateFlow<String> = _query.asStateFlow()

    val words: StateFlow<List<Word>> = _query
        .debounce(300)
        .distinctUntilChanged()
        .flatMapLatest { q -> repository.search(q) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList()
        )

    fun onQueryChange(newQuery: String) {
        _query.value = newQuery
    }
}