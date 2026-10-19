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
import com.bastarst.lexisrs.ui.decks.DecksScreen
import com.bastarst.lexisrs.ui.decks.DecksViewModel
import com.bastarst.lexisrs.ui.decks.DecksViewModelFactory
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
                }
            }
        }
    }
}