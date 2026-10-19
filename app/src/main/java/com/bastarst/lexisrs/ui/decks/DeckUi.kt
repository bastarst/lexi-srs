package com.bastarst.lexisrs.ui.decks

data class DeckUi(
    val id: Long,
    val name: String,
    val totalCount: Int,
    val learnedCount: Int,
    val newCount: Int,
    val dueCount: Int
) {
    val progressPercent: Int
        get() = if (totalCount == 0) 0 else learnedCount * 100 / totalCount
}
