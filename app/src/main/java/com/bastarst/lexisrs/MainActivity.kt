package com.bastarst.lexisrs

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import com.bastarst.lexisrs.ui.theme.LexiSRSTheme
import com.bastarst.lexisrs.ui.words.WordsScreen
import com.bastarst.lexisrs.ui.words.WordsViewModel
import com.bastarst.lexisrs.ui.words.WordsViewModelFactory

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val app = application as LexiSrsApp

        setContent {
            val factory = WordsViewModelFactory(app.wordRepository)
            val viewModel: WordsViewModel = viewModel(factory = factory)

            LexiSRSTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    WordsScreen(
                        viewModel = viewModel,
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}