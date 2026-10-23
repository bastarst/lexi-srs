package com.bastarst.lexisrs.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.bastarst.lexisrs.data.local.entity.Word
import kotlinx.coroutines.flow.Flow

@Dao
interface WordDao {

    @Query("SELECT * FROM words WHERE id = :id")
    suspend fun getById(id: Long): Word?

    @Query("SELECT COUNT(*) FROM words")
    suspend fun countAllOnce(): Int

    @Insert
    suspend fun insert(word: Word): Long

    @Insert
    suspend fun insertAll(words: List<Word>): List<Long>

    @Update
    suspend fun update(word: Word)

    @Delete
    suspend fun delete(word: Word)

    @Query("SELECT * FROM words WHERE deckId = :deckId")
    fun getAllByDeck(deckId: Long): Flow<List<Word>>

    @Query("""
        SELECT * FROM words
        WHERE deckId = :deckId
        AND
        (english LIKE '%' || :query || '%'
        OR russian LIKE '%' || :query || '%')
    """)
    fun searchByDeck(deckId: Long, query: String): Flow<List<Word>>

    @Query("SELECT COUNT(*) FROM words WHERE deckId = :deckId")
    fun countByDeck(deckId: Long): Flow<Int>

    @Query("SELECT COUNT(*) FROM words WHERE deckId = :deckId AND status = :status")
    fun countByDeckAndStatus(deckId: Long, status: Int): Flow<Int>

    @Query("SELECT COUNT(*) FROM words WHERE deckId = :deckId")
    suspend fun countByDeckOnce(deckId: Long): Int

    @Query("""
        SELECT COUNT(*) FROM words
        WHERE deckId = :deckId
        AND status != 0
        AND nextReviewAt <= :now
    """)
    fun countDueByDeck(deckId: Long, now: Long): Flow<Int>

    @Query("SELECT COUNT(*) FROM words WHERE deckId = :deckId AND status = 0")
    fun countNewByDeck(deckId: Long): Flow<Int>

    @Query("""
        SELECT * FROM words
        WHERE deckId = :deckId AND status = 0
        ORDER BY RANDOM()
        LIMIT :limit
    """)
    suspend fun getRandomNew(deckId: Long, limit: Int): List<Word>

    @Query("""
        SELECT * FROM words
        WHERE deckId = :deckId 
            AND status != 0
            AND nextReviewAt <= :now
        ORDER BY RANDOM()
        LIMIT :limit
    """)
    suspend fun getRandomDue(deckId: Long, now: Long, limit: Int): List<Word>

    @Query("""
        SELECT COUNT(*) FROM words
        WHERE deckId = :deckId
        AND status != 0
        AND nextReviewAt <= :now
    """)
    suspend fun countDueByDeckOnce(deckId: Long, now: Long): Int

    @Query("SELECT COUNT(*) FROM words WHERE deckId = :deckId AND status = 0")
    suspend fun countNewByDeckOnce(deckId: Long): Int
}