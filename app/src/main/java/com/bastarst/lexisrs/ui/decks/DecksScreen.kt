package com.bastarst.lexisrs.ui.decks

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DecksScreen(
    viewModel: DecksViewModel,
    onDeckClick: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val decks by viewModel.decks.collectAsStateWithLifecycle()
    var selectedDeckId by rememberSaveable { mutableStateOf<Long?>(null) }
    val selectedDeck = decks.firstOrNull { it.id == selectedDeckId}

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = { TopAppBar(title = { Text("LexiSRS")}) }
    ) { padding ->
        LazyColumn(
            modifier = modifier.fillMaxSize().padding(padding)
        ) {
            items(decks, key = { it.id }) { deck ->
                DeckRow(
                    deck = deck,
                    onClick = { onDeckClick(deck.id) },
                    onStudyClick = { selectedDeckId = deck.id }
                )
                HorizontalDivider()
            }
        }
    }

    if (selectedDeck != null) {
        StudyDialog(
            deck = selectedDeck,
            onDismiss = { selectedDeckId = null },
            onLearnClick = {
                selectedDeckId = null
            },
            onReviewClick = {
                selectedDeckId = null
            }
        )
    }
}

@Composable
private fun DeckRow(
    deck: DeckUi,
    onClick: () -> Unit,
    onStudyClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "${deck.totalCount} слов",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = deck.name,
                style = MaterialTheme.typography.titleMedium
            )
        }
        Spacer(Modifier.width(12.dp))
        Button(onClick = onStudyClick) {
            Text("Учить ${deck.progressPercent}%")
        }
    }
}

@Composable
private fun StudyDialog(
    deck: DeckUi,
    onDismiss: () -> Unit,
    onLearnClick: () -> Unit,
    onReviewClick: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(deck.name) },
        text = {
            Column {
                Text(
                    text = "К изучению: ${deck.newCount}",
                    style = MaterialTheme.typography.bodyLarge
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "К повторению: ${deck.dueCount}",
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onLearnClick,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Учить")
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onReviewClick,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Повторить")
            }
        }
    )
}