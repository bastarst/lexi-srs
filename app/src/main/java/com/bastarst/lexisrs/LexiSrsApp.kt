package com.bastarst.lexisrs

import android.app.Application
import com.bastarst.lexisrs.data.local.AppDatabase
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
            if (wordRepository.countAllOnce() == 0) {
                val result = wordRepository.importFromAssets(
                    assets.open("Oxford_3000_English_Russian.csv")
                )
            }
        }
    }
}