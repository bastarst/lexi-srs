package com.bastarst.lexisrs.data.repository

import com.bastarst.lexisrs.data.csv.CsvParser
import com.bastarst.lexisrs.data.local.dao.WordDao
import com.bastarst.lexisrs.data.local.entity.Word
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import java.io.InputStream

class WordRepository(private val wordDao: WordDao) {

    private val intervals = listOf(0L, 1L, 3L, 7L, 21L)
    private val dayMillis = 24L * 60L * 60L * 1000L

    fun searchByDeck(deckId: Long, query: String): Flow<List<Word>> =
        if (query.isBlank()) wordDao.getAllByDeck(deckId)
        else wordDao.searchByDeck(deckId, query)

    fun deckStats(deckId: Long): Flow<DeckStats> {
        val now = System.currentTimeMillis()
        return combine(
            wordDao.countByDeck(deckId),
            wordDao.countByDeckAndStatus(deckId, Word.STATUS_LEARNED),
            wordDao.countNewByDeck(deckId),
            wordDao.countDueByDeck(deckId, now)
        ) { total, learned, new, due ->
            DeckStats(total, learned, new, due)
        }
    }

    suspend fun countByDeckOnce(deckId: Long): Int = wordDao.countByDeckOnce(deckId)

    suspend fun importFromStream(
        input: InputStream,
        deckId: Long
    ) {
        val pairs = input.use { CsvParser.parse(it) }
        val words = pairs.map { (en, ru) ->
            Word(
                english = en,
                russian = ru,
                deckId = deckId
            )
        }
        wordDao.insertAll(words)
    }

    suspend fun getStudyWords(
        deckId: Long,
        mode: StudyMode,
        limit: Int = 5
    ): List<Word> = when (mode) {
        StudyMode.LEARN ->
            wordDao.getRandomNew(deckId, limit)
        StudyMode.REVIEW ->
            wordDao.getRandomDue(deckId, System.currentTimeMillis(), limit)
    }

    suspend fun updateWord(word: Word) {
        wordDao.update(word)
    }

    suspend fun updateSrs(word: Word, isSuccess: Boolean) {
        val newBox = if (isSuccess) {
            (word.box + 1).coerceAtMost(5)
        } else {
            1
        }

        val days = intervals[newBox]
        val nextReview = System.currentTimeMillis() + days * dayMillis
        val newStatus = if (newBox == 5) Word.STATUS_LEARNED else Word.STATUS_REVIEW

        val updated = word.copy(
            box = newBox,
            nextReviewAt = nextReview,
            status = newStatus
        )

        wordDao.update(updated)
    }
}