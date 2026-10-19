package com.bastarst.lexisrs.ui.decks

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bastarst.lexisrs.data.repository.DeckConfig
import com.bastarst.lexisrs.data.repository.WordRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

class DecksViewModel(
    repository: WordRepository
) : ViewModel() {

    val decks: StateFlow<List<DeckUi>> =
        combine(
            DeckConfig.ALL.map { deck ->
                repository.deckStats(deck.id).map { stats ->
                    DeckUi(
                        id = deck.id,
                        name = deck.name,
                        totalCount = stats.total,
                        learnedCount = stats.learned,
                        newCount = stats.new,
                        dueCount = stats.due
                    )
                }
            }
        ) { it.toList() }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = emptyList()
            )
}