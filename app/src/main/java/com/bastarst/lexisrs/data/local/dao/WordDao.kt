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
    @Query("SELECT * FROM words")
    fun getAll(): Flow<List<Word>>

    @Query("SELECT * FROM words WHERE id = :id")
    suspend fun getById(id: Long): Word?

    @Query("""
        SELECT * FROM words
        WHERE nextReviewAt <= :now
        ORDER BY nextReviewAt ASC
        LIMIT :limit
    """)
    suspend fun getDueForReview(now: Long, limit: Int): List<Word>

    @Query("""
        SELECT * FROM words
        WHERE status = :status
        ORDER BY english ASC
    """)
    fun getByStatus(status: Int): Flow<List<Word>>

    @Query("""
        SELECT * FROM words
        WHERE english LIKE '%' || :query || '%'
        OR russian LIKE '%' || :query || '%'
        ORDER BY english ASC
    """)
    fun search(query: String): Flow<List<Word>>

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
}