package com.example.android.unscramble.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.example.android.unscramble.data.allWords
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

private lateinit var currentWord: String
private var usedWords: MutableSet<String> = mutableSetOf()

// Game UI state
private val _uiState = MutableStateFlow(GameUiState())



class GameViewModel: ViewModel() {
    // Backing property to avoid state updates from other classes
    // mutable state flow is now read only
    val uiState: StateFlow<GameUiState> = _uiState.asStateFlow()
    var userGuess by mutableStateOf("")
        private set

    init {
        resetGame()
    }

    fun updateUserGuess(guessedWord: String) {
        userGuess = guessedWord
    }

    fun checkUserGuess() {
        if (userGuess.equals(currentWord, ignoreCase=true)) {

        } else {
            _uiState.update{ currentState ->
                currentState.copy(isGuessedWordWrong = true)
            }
        }
        updateUserGuess("")
    }
}

private fun pickRandomWordAndShuffle(): String {
    // Continue picking up a new random word until you get one that hasn't been used before.
    currentWord = allWords.random()
    if (usedWords.contains(currentWord)) {
        return pickRandomWordAndShuffle()
    } else {
        usedWords.add(currentWord)
        return shuffleCurrentWord(currentWord)
    }
}

private fun shuffleCurrentWord(word: String): String {
    val tempWord = word.toCharArray()
    tempWord.shuffle()
    while(String(tempWord).equals(word)) {
        tempWord.shuffle()
    }
    return String(tempWord)
}

fun resetGame() {
    usedWords.clear()
    _uiState.value = GameUiState(currentScrambledWord=pickRandomWordAndShuffle())
}