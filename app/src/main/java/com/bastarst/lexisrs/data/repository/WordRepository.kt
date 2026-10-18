package com.bastarst.lexisrs.data.repository

import android.content.Context
import com.bastarst.lexisrs.data.csv.CsvParser
import com.bastarst.lexisrs.data.local.entity.Word
import com.bastarst.lexisrs.data.local.dao.WordDao
import kotlinx.coroutines.flow.Flow
import java.io.InputStream

class WordRepository(private val wordDao: WordDao) {

    val allWords: Flow<List<Word>> = wordDao.getAll()

    suspend fun countAllOnce(): Int = wordDao.countAllOnce()

    suspend fun importFromAssets(
        input: InputStream
    ) {
        val pairs = input.use { CsvParser.parse(input) }
        val words = pairs.map { (en, ru) ->
            Word(english = en, russian = ru, deckId = 0L)
        }
        wordDao.insertAll(words)
    }

    fun search(query: String): Flow<List<Word>> =
        if (query.isBlank()) wordDao.getAll() else wordDao.search(query)
}