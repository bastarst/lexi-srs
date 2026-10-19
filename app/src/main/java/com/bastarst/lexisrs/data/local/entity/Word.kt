package com.bastarst.lexisrs.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "words")
data class Word (
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val deckId: Long = 0,
    val english: String,
    val russian: String,
    val status: Int = STATUS_NEW,
    val box: Int = 0,
    val nextReviewAt: Long = 0,
    val correctCount: Int = 0,
    val wrongCount: Int = 0,
    val lastAnsweredAt: Long? = null,
    val isDifficult: Boolean = false
) {
    companion object {
        const val STATUS_NEW = 0
        const val STATUS_LEARNING = 1
        const val STATUS_REVIEW = 2
        const val STATUS_LEARNED = 3
    }
}