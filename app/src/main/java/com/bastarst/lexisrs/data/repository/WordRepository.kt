package com.bastarst.lexisrs.data.repository

import com.bastarst.lexisrs.data.csv.CsvParser
import com.bastarst.lexisrs.data.local.dao.WordDao
import com.bastarst.lexisrs.data.local.entity.Word
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import java.io.InputStream

class WordRepository(private val wordDao: WordDao) {

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
}