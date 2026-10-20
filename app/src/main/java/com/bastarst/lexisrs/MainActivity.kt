package com.bastarst.lexisrs

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.bastarst.lexisrs.data.repository.DeckConfig
import com.bastarst.lexisrs.data.repository.StudyMode
import com.bastarst.lexisrs.ui.decks.DecksScreen
import com.bastarst.lexisrs.ui.decks.DecksViewModel
import com.bastarst.lexisrs.ui.decks.DecksViewModelFactory
import com.bastarst.lexisrs.ui.study.StudyScreen
import com.bastarst.lexisrs.ui.study.StudyViewModel
import com.bastarst.lexisrs.ui.study.StudyViewModelFactory
import com.bastarst.lexisrs.ui.theme.LexiSRSTheme
import com.bastarst.lexisrs.ui.words.WordsScreen
import com.bastarst.lexisrs.ui.words.WordsViewModel
import com.bastarst.lexisrs.ui.words.WordsViewModelFactory

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val app = application as LexiSrsApp

        setContent {
            LexiSRSTheme {
                val navController = rememberNavController()

                NavHost(
                    navController = navController,
                    startDestination = "decks"
                ) {
                    composable("decks") {
                        val factory = DecksViewModelFactory(app.wordRepository)
                        val vm: DecksViewModel = viewModel(factory = factory)

                        DecksScreen(
                            viewModel = vm,
                            onDeckClick = { deckId ->
                                navController.navigate("words/$deckId")
                            },
                            onStartStudy = { deckId, mode ->
                                navController.navigate("study/$deckId/${mode.name.lowercase()}")
                            }
                        )
                    }

                    composable(
                        route = "words/{deckId}",
                        arguments = listOf(
                            navArgument("deckId") { type = NavType.LongType }
                        )
                    ) { backStackEntry ->
                        val deckId = backStackEntry.arguments?.getLong("deckId") ?: 0L
                        val deck = DeckConfig.byId(deckId)
                        val factory = WordsViewModelFactory(app.wordRepository, deckId)
                        val vm: WordsViewModel = viewModel(factory = factory)

                        WordsScreen(
                            viewModel = vm,
                            deckName = deck.name,
                            onBack = { navController.popBackStack() }
                        )
                    }

                    composable(
                        route = "study/{deckId}/{mode}",
                        arguments = listOf(
                            navArgument("deckId") { type = NavType.LongType },
                            navArgument("mode") { type = NavType.StringType }
                        )
                    ) { entry ->
                        val deckId = entry.arguments?.getLong("deckId") ?: 0L
                        val modeStr = entry.arguments?.getString("mode") ?: "learn"
                        val mode = StudyMode.valueOf(modeStr.uppercase())
                        val deck = DeckConfig.byId(deckId)

                        val factory = StudyViewModelFactory(app.wordRepository, deckId, mode)
                        val vm: StudyViewModel = viewModel(factory = factory)

                        StudyScreen(
                            viewModel = vm,
                            deckName = deck.name,
                            mode = mode,
                            onBack = { navController.popBackStack() }
                        )
                    }
                }
            }
        }
    }
}