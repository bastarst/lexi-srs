package com.bastarst.lexisrs

import android.app.Application
import com.bastarst.lexisrs.data.local.AppDatabase
import com.bastarst.lexisrs.data.repository.DeckConfig
import com.bastarst.lexisrs.data.repository.WordRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class LexiSrsApp : Application() {

    val database by lazy { AppDatabase.getInstance(this) }
    val wordRepository by lazy { WordRepository(database.wordDao()) }

    private val appScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onCreate() {
        super.onCreate()
        appScope.launch {
            DeckConfig.ALL.forEach { deck ->
                importIfEmpty(deck)
            }
        }
    }

    private suspend fun importIfEmpty(deck: DeckConfig) {
        if (wordRepository.countByDeckOnce(deck.id) > 0) {
            return
        }

        wordRepository.importFromStream(
            input = assets.open(deck.assetName),
            deckId = deck.id
        )
    }
}